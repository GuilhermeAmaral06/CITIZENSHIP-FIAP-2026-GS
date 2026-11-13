package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.OrigemSenha;
import br.com.fiap.filazero.model.Senha;
import br.com.fiap.filazero.model.StatusSenha;
import br.com.fiap.filazero.model.TipoPrioridade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela TBL_SENHA. As consultas trazem junto a UBS, o servico e, quando a senha
 * nao e anonima, o paciente (LEFT JOIN, porque id_paciente pode ser nulo).
 */
public class SenhaDAO {

    private static final String SELECT_BASE = "SELECT s.id_senha, s.cd_senha, s.tp_prioridade, s.tp_origem, "
            + "s.dt_emissao, s.dt_chamada, s.st_senha, "
            + UbsDAO.COLUNAS + ", " + ServicoDAO.COLUNAS + ", " + PacienteDAO.COLUNAS + " "
            + "FROM TBL_SENHA s "
            + "JOIN TBL_UBS u ON u.id_ubs = s.id_ubs "
            + "JOIN TBL_SERVICO sv ON sv.id_servico = s.id_servico "
            + "LEFT JOIN TBL_PACIENTE p ON p.id_paciente = s.id_paciente ";

    private final Connection conexao;

    public SenhaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /** Insere a senha e preenche nela o id gerado pela sequence SEQ_SENHA. */
    public void inserir(Senha senha) throws SQLException {
        String sql = "INSERT INTO TBL_SENHA (id_senha, id_ubs, id_servico, id_paciente, cd_senha, tp_prioridade, "
                + "tp_origem, dt_emissao, dt_chamada, st_senha) VALUES (SEQ_SENHA.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"ID_SENHA"})) {
            stmt.setLong(1, senha.getUbs().getId());
            stmt.setLong(2, senha.getServico().getId());
            if (senha.getPaciente() == null) {
                stmt.setNull(3, Types.NUMERIC);
            } else {
                stmt.setLong(3, senha.getPaciente().getId());
            }
            stmt.setString(4, senha.getCodigo());
            stmt.setString(5, senha.getPrioridade().name());
            stmt.setString(6, senha.getOrigem().name());
            stmt.setTimestamp(7, Conversor.paraTimestamp(senha.getDataEmissao()));
            stmt.setTimestamp(8, Conversor.paraTimestamp(senha.getDataChamada()));
            stmt.setString(9, senha.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    senha.setId(chaves.getLong(1));
                }
            }
        }
    }

    /** Devolve a senha com o id informado ou null se nao existir. */
    public Senha buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "WHERE s.id_senha = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Senhas que ainda aguardam atendimento na UBS (todos os servicos), da mais antiga para a mais nova. */
    public List<Senha> listarAguardando(Long idUbs) throws SQLException {
        String sql = SELECT_BASE + "WHERE s.id_ubs = ? AND s.st_senha = 'AGUARDANDO' "
                + "ORDER BY s.dt_emissao, s.id_senha";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, idUbs);
            return executarLista(stmt);
        }
    }

    /** Senhas da UBS chamadas no dia (todos os servicos), da chamada mais recente para a mais antiga. */
    public List<Senha> listarChamadasNoDia(Long idUbs, LocalDate dia) throws SQLException {
        String sql = SELECT_BASE + "WHERE s.id_ubs = ? AND s.dt_chamada >= ? AND s.dt_chamada < ? "
                + "ORDER BY s.dt_chamada DESC, s.id_senha DESC";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, idUbs);
            stmt.setTimestamp(2, Conversor.inicioDoDia(dia));
            stmt.setTimestamp(3, Conversor.inicioDoDiaSeguinte(dia));
            return executarLista(stmt);
        }
    }

    /** Quantas senhas do servico ja foram emitidas na UBS no dia (define o numero da proxima). */
    public int contarEmitidasNoDia(Long idUbs, Long idServico, LocalDate dia) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TBL_SENHA WHERE id_ubs = ? AND id_servico = ? "
                + "AND dt_emissao >= ? AND dt_emissao < ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, idUbs);
            stmt.setLong(2, idServico);
            stmt.setTimestamp(3, Conversor.inicioDoDia(dia));
            stmt.setTimestamp(4, Conversor.inicioDoDiaSeguinte(dia));
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Grava o novo status e o momento da chamada (null enquanto a senha nao for chamada). */
    public void atualizarStatus(Long id, StatusSenha status, LocalDateTime dataChamada) throws SQLException {
        String sql = "UPDATE TBL_SENHA SET st_senha = ?, dt_chamada = ? WHERE id_senha = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, Conversor.paraTimestamp(dataChamada));
            stmt.setLong(3, id);
            stmt.executeUpdate();
        }
    }

    private List<Senha> executarLista(PreparedStatement stmt) throws SQLException {
        try (ResultSet rs = stmt.executeQuery()) {
            List<Senha> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        }
    }

    private static Senha mapear(ResultSet rs) throws SQLException {
        Senha senha = new Senha();
        senha.setId(rs.getLong("id_senha"));
        senha.setUbs(UbsDAO.mapear(rs));
        senha.setServico(ServicoDAO.mapear(rs));
        // No LEFT JOIN, as colunas do paciente vem nulas quando a senha e anonima
        if (rs.getObject("id_paciente") != null) {
            senha.setPaciente(PacienteDAO.mapear(rs));
        }
        senha.setCodigo(rs.getString("cd_senha"));
        senha.setPrioridade(TipoPrioridade.valueOf(rs.getString("tp_prioridade")));
        senha.setOrigem(OrigemSenha.valueOf(rs.getString("tp_origem")));
        senha.setDataEmissao(Conversor.paraDataHora(rs.getTimestamp("dt_emissao")));
        senha.setDataChamada(Conversor.paraDataHora(rs.getTimestamp("dt_chamada")));
        senha.setStatus(StatusSenha.valueOf(rs.getString("st_senha")));
        return senha;
    }
}
