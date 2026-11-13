package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.Servico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela TBL_SERVICO (somente leitura: os servicos sao um dominio fixo).
 */
public class ServicoDAO {

    /** Colunas do servico com o apelido "sv", reaproveitadas nos JOINs dos outros DAOs. */
    static final String COLUNAS = "sv.id_servico, sv.nm_servico, sv.cd_prefixo_senha, sv.nr_tempo_medio_min";

    private static final String SELECT_BASE = "SELECT " + COLUNAS + " FROM TBL_SERVICO sv ";

    private final Connection conexao;

    public ServicoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Servico> listar() throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "ORDER BY sv.id_servico");
             ResultSet rs = stmt.executeQuery()) {
            List<Servico> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        }
    }

    /** Devolve o servico com o id informado ou null se nao existir. */
    public Servico buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "WHERE sv.id_servico = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    static Servico mapear(ResultSet rs) throws SQLException {
        Servico servico = new Servico();
        servico.setId(rs.getLong("id_servico"));
        servico.setNome(rs.getString("nm_servico"));
        servico.setPrefixoSenha(rs.getString("cd_prefixo_senha"));
        servico.setTempoMedioMin(rs.getInt("nr_tempo_medio_min"));
        return servico;
    }
}
