package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.HorarioAgenda;
import br.com.fiap.filazero.model.Profissional;
import br.com.fiap.filazero.model.StatusHorario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acesso a tabela TBL_HORARIO_AGENDA. As consultas trazem junto o profissional
 * (TBL_PROFISSIONAL), a UBS e a especialidade dele.
 */
public class HorarioAgendaDAO {

    /** Colunas do horario ("h") e do profissional ("pr"), reaproveitadas nos JOINs dos outros DAOs. */
    static final String COLUNAS = "h.id_horario, h.dt_horario, h.nr_duracao_min, h.st_horario, "
            + "pr.id_profissional, pr.nm_profissional, pr.nr_registro, "
            + UbsDAO.COLUNAS + ", " + EspecialidadeDAO.COLUNAS;

    /** JOINs que partem do horario ("h") ate a UBS e a especialidade do profissional. */
    static final String JUNCOES = "JOIN TBL_PROFISSIONAL pr ON pr.id_profissional = h.id_profissional "
            + "JOIN TBL_UBS u ON u.id_ubs = pr.id_ubs "
            + "JOIN TBL_ESPECIALIDADE e ON e.id_especialidade = pr.id_especialidade ";

    private final Connection conexao;

    public HorarioAgendaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /** Devolve o horario com o id informado ou null se nao existir. */
    public HorarioAgenda buscarPorId(Long id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM TBL_HORARIO_AGENDA h " + JUNCOES + "WHERE h.id_horario = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * RN01: marca o horario como OCUPADO somente se ele ainda estiver LIVRE.
     * A condicao fica no proprio UPDATE para que duas pessoas agendando ao mesmo tempo
     * nao consigam o mesmo horario: so uma delas altera a linha.
     *
     * @return true se o horario estava livre e agora esta ocupado
     */
    public boolean ocupar(Long id) throws SQLException {
        String sql = "UPDATE TBL_HORARIO_AGENDA SET st_horario = 'OCUPADO' "
                + "WHERE id_horario = ? AND st_horario = 'LIVRE'";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    /** RN03: devolve o horario para LIVRE quando a consulta e cancelada. */
    public void liberar(Long id) throws SQLException {
        String sql = "UPDATE TBL_HORARIO_AGENDA SET st_horario = 'LIVRE' WHERE id_horario = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    static HorarioAgenda mapear(ResultSet rs) throws SQLException {
        Profissional profissional = new Profissional();
        profissional.setId(rs.getLong("id_profissional"));
        profissional.setNome(rs.getString("nm_profissional"));
        profissional.setRegistro(rs.getString("nr_registro"));
        profissional.setUbs(UbsDAO.mapear(rs));
        profissional.setEspecialidade(EspecialidadeDAO.mapear(rs));

        HorarioAgenda horario = new HorarioAgenda();
        horario.setId(rs.getLong("id_horario"));
        horario.setProfissional(profissional);
        horario.setDataHora(Conversor.paraDataHora(rs.getTimestamp("dt_horario")));
        horario.setDuracaoMin(rs.getInt("nr_duracao_min"));
        horario.setStatus(StatusHorario.valueOf(rs.getString("st_horario")));
        return horario;
    }
}
