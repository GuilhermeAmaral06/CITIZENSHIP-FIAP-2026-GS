package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.ListaEspera;
import br.com.fiap.filazero.model.StatusEspera;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela TBL_LISTA_ESPERA. As consultas trazem junto o paciente, a UBS e a especialidade.
 */
public class ListaEsperaDAO {

    private static final String SELECT_BASE = "SELECT le.id_espera, le.id_consulta_encaixe, le.dt_entrada, "
            + "le.nr_prioridade, le.st_espera, "
            + PacienteDAO.COLUNAS + ", " + UbsDAO.COLUNAS + ", " + EspecialidadeDAO.COLUNAS + " "
            + "FROM TBL_LISTA_ESPERA le "
            + "JOIN TBL_PACIENTE p ON p.id_paciente = le.id_paciente "
            + "JOIN TBL_UBS u ON u.id_ubs = le.id_ubs "
            + "JOIN TBL_ESPECIALIDADE e ON e.id_especialidade = le.id_especialidade ";

    private final Connection conexao;

    public ListaEsperaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /** Insere o registro e preenche nele o id gerado pela sequence SEQ_LISTA_ESPERA. */
    public void inserir(ListaEspera espera) throws SQLException {
        String sql = "INSERT INTO TBL_LISTA_ESPERA (id_espera, id_paciente, id_ubs, id_especialidade, "
                + "id_consulta_encaixe, dt_entrada, nr_prioridade, st_espera) "
                + "VALUES (SEQ_LISTA_ESPERA.NEXTVAL, ?, ?, ?, NULL, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"ID_ESPERA"})) {
            stmt.setLong(1, espera.getPaciente().getId());
            stmt.setLong(2, espera.getUbs().getId());
            stmt.setLong(3, espera.getEspecialidade().getId());
            stmt.setTimestamp(4, Conversor.paraTimestamp(espera.getDataEntrada()));
            stmt.setInt(5, espera.getPrioridade());
            stmt.setString(6, espera.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    espera.setId(chaves.getLong(1));
                }
            }
        }
    }

    /** Devolve o registro com o id informado ou null se nao existir. */
    public ListaEspera buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "WHERE le.id_espera = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista quem esta AGUARDANDO, na ordem em que sera encaixado: maior prioridade primeiro e,
     * no empate, quem entrou antes. Os dois filtros sao opcionais (null = sem filtro).
     */
    public List<ListaEspera> listarAguardando(Long idUbs, Long idEspecialidade) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE le.st_espera = 'AGUARDANDO' ");
        List<Long> parametros = new ArrayList<>();
        if (idUbs != null) {
            sql.append("AND le.id_ubs = ? ");
            parametros.add(idUbs);
        }
        if (idEspecialidade != null) {
            sql.append("AND le.id_especialidade = ? ");
            parametros.add(idEspecialidade);
        }
        sql.append("ORDER BY le.nr_prioridade DESC, le.dt_entrada, le.id_espera");

        try (PreparedStatement stmt = conexao.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setLong(i + 1, parametros.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<ListaEspera> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    /** RN03: registra o encaixe, guardando a consulta criada para o paciente. */
    public void marcarEncaixado(Long id, Long idConsulta) throws SQLException {
        String sql = "UPDATE TBL_LISTA_ESPERA SET st_espera = 'ENCAIXADO', id_consulta_encaixe = ? "
                + "WHERE id_espera = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, idConsulta);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }

    /** O paciente saiu da lista sem ser encaixado. */
    public void marcarDesistencia(Long id) throws SQLException {
        String sql = "UPDATE TBL_LISTA_ESPERA SET st_espera = 'DESISTIU' WHERE id_espera = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private static ListaEspera mapear(ResultSet rs) throws SQLException {
        ListaEspera espera = new ListaEspera();
        espera.setId(rs.getLong("id_espera"));
        espera.setPaciente(PacienteDAO.mapear(rs));
        espera.setUbs(UbsDAO.mapear(rs));
        espera.setEspecialidade(EspecialidadeDAO.mapear(rs));
        long idConsultaEncaixe = rs.getLong("id_consulta_encaixe");
        espera.setIdConsultaEncaixe(rs.wasNull() ? null : idConsultaEncaixe);
        espera.setDataEntrada(Conversor.paraDataHora(rs.getTimestamp("dt_entrada")));
        espera.setPrioridade(rs.getInt("nr_prioridade"));
        espera.setStatus(StatusEspera.valueOf(rs.getString("st_espera")));
        return espera;
    }
}
