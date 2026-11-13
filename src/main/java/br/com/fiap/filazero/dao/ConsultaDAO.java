package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.Consulta;
import br.com.fiap.filazero.model.StatusConsulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela TBL_CONSULTA. As consultas trazem junto o paciente e o horario
 * (com profissional, UBS e especialidade).
 */
public class ConsultaDAO {

    /** Colunas da consulta ("c"), do paciente e do horario, reaproveitadas pelo PrevisaoFaltaDAO. */
    static final String COLUNAS = "c.id_consulta, c.dt_agendamento, c.st_consulta, c.fl_encaixe, "
            + PacienteDAO.COLUNAS + ", " + HorarioAgendaDAO.COLUNAS;

    /** JOINs que partem da consulta ("c") ate o paciente, o horario, o profissional, a UBS e a especialidade. */
    static final String JUNCOES = "JOIN TBL_PACIENTE p ON p.id_paciente = c.id_paciente "
            + "JOIN TBL_HORARIO_AGENDA h ON h.id_horario = c.id_horario "
            + HorarioAgendaDAO.JUNCOES;

    private static final String SELECT_BASE = "SELECT " + COLUNAS + " FROM TBL_CONSULTA c " + JUNCOES;

    private final Connection conexao;

    public ConsultaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /**
     * Lista as consultas em ordem de horario. Os tres filtros sao opcionais (null = sem filtro):
     * UBS do profissional, dia do atendimento e status.
     */
    public List<Consulta> listar(Long idUbs, LocalDate data, StatusConsulta status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (idUbs != null) {
            sql.append("AND pr.id_ubs = ? ");
            parametros.add(idUbs);
        }
        if (data != null) {
            sql.append("AND h.dt_horario >= ? AND h.dt_horario < ? ");
            parametros.add(Conversor.inicioDoDia(data));
            parametros.add(Conversor.inicioDoDiaSeguinte(data));
        }
        if (status != null) {
            sql.append("AND c.st_consulta = ? ");
            parametros.add(status.name());
        }
        sql.append("ORDER BY h.dt_horario, c.id_consulta");

        try (PreparedStatement stmt = conexao.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Consulta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    /** Devolve a consulta com o id informado ou null se nao existir. */
    public Consulta buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "WHERE c.id_consulta = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Insere a consulta e preenche nela o id gerado pela sequence SEQ_CONSULTA. */
    public void inserir(Consulta consulta) throws SQLException {
        String sql = "INSERT INTO TBL_CONSULTA (id_consulta, id_paciente, id_horario, dt_agendamento, "
                + "st_consulta, fl_encaixe) VALUES (SEQ_CONSULTA.NEXTVAL, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"ID_CONSULTA"})) {
            stmt.setLong(1, consulta.getPaciente().getId());
            stmt.setLong(2, consulta.getHorario().getId());
            stmt.setTimestamp(3, Conversor.paraTimestamp(consulta.getDataAgendamento()));
            stmt.setString(4, consulta.getStatus().name());
            stmt.setString(5, Conversor.paraSimNao(consulta.isEncaixe()));
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    consulta.setId(chaves.getLong(1));
                }
            }
        }
    }

    public void atualizarStatus(Long id, StatusConsulta status) throws SQLException {
        String sql = "UPDATE TBL_CONSULTA SET st_consulta = ? WHERE id_consulta = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * RN02: conta as consultas nao canceladas do paciente, na especialidade e no dia informados.
     */
    public int contarDoPacienteNoDia(Long idPaciente, Long idEspecialidade, LocalDate dia) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TBL_CONSULTA c "
                + "JOIN TBL_HORARIO_AGENDA h ON h.id_horario = c.id_horario "
                + "JOIN TBL_PROFISSIONAL pr ON pr.id_profissional = h.id_profissional "
                + "WHERE c.id_paciente = ? AND pr.id_especialidade = ? AND c.st_consulta <> 'CANCELADA' "
                + "AND h.dt_horario >= ? AND h.dt_horario < ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, idPaciente);
            stmt.setLong(2, idEspecialidade);
            stmt.setTimestamp(3, Conversor.inicioDoDia(dia));
            stmt.setTimestamp(4, Conversor.inicioDoDiaSeguinte(dia));
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    static Consulta mapear(ResultSet rs) throws SQLException {
        Consulta consulta = new Consulta();
        consulta.setId(rs.getLong("id_consulta"));
        consulta.setPaciente(PacienteDAO.mapear(rs));
        consulta.setHorario(HorarioAgendaDAO.mapear(rs));
        consulta.setDataAgendamento(Conversor.paraDataHora(rs.getTimestamp("dt_agendamento")));
        consulta.setStatus(StatusConsulta.valueOf(rs.getString("st_consulta")));
        consulta.setEncaixe(Conversor.paraBoolean(rs.getString("fl_encaixe")));
        return consulta;
    }
}
