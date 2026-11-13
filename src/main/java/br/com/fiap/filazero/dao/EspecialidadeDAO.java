package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.Especialidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acesso a tabela TBL_ESPECIALIDADE (somente leitura: as especialidades sao um dominio fixo).
 */
public class EspecialidadeDAO {

    /** Colunas da especialidade com o apelido "e", reaproveitadas nos JOINs dos outros DAOs. */
    static final String COLUNAS = "e.id_especialidade, e.nm_especialidade, e.nr_duracao_padrao_min";

    private final Connection conexao;

    public EspecialidadeDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /** Devolve a especialidade com o id informado ou null se nao existir. */
    public Especialidade buscarPorId(Long id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM TBL_ESPECIALIDADE e WHERE e.id_especialidade = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    static Especialidade mapear(ResultSet rs) throws SQLException {
        Especialidade especialidade = new Especialidade();
        especialidade.setId(rs.getLong("id_especialidade"));
        especialidade.setNome(rs.getString("nm_especialidade"));
        especialidade.setDuracaoPadraoMin(rs.getInt("nr_duracao_padrao_min"));
        return especialidade;
    }
}
