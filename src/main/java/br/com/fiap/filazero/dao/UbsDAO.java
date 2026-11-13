package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.Ubs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela TBL_UBS.
 */
public class UbsDAO {

    /** Colunas da UBS com o apelido "u", reaproveitadas nos JOINs dos outros DAOs. */
    static final String COLUNAS = "u.id_ubs, u.nm_ubs, u.ds_endereco, u.nm_bairro, u.nr_latitude, "
            + "u.nr_longitude, u.hr_abertura, u.hr_fechamento";

    private static final String SELECT_BASE = "SELECT " + COLUNAS + " FROM TBL_UBS u ";

    private final Connection conexao;

    public UbsDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Ubs> listar() throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "ORDER BY u.nm_ubs");
             ResultSet rs = stmt.executeQuery()) {
            List<Ubs> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        }
    }

    /** Devolve a UBS com o id informado ou null se nao existir. */
    public Ubs buscarPorId(Long id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "WHERE u.id_ubs = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Devolve a UBS com o nome informado ou null se nao existir (o nome e UNIQUE). */
    public Ubs buscarPorNome(String nome) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(SELECT_BASE + "WHERE u.nm_ubs = ?")) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Insere a UBS e preenche nela o id gerado pela sequence SEQ_UBS. */
    public void inserir(Ubs ubs) throws SQLException {
        String sql = "INSERT INTO TBL_UBS (id_ubs, nm_ubs, ds_endereco, nm_bairro, nr_latitude, nr_longitude, "
                + "hr_abertura, hr_fechamento) VALUES (SEQ_UBS.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"ID_UBS"})) {
            preencher(stmt, ubs);
            stmt.executeUpdate();
            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    ubs.setId(chaves.getLong(1));
                }
            }
        }
    }

    /** Atualiza a UBS. Devolve false se nenhum registro tiver o id informado. */
    public boolean atualizar(Ubs ubs) throws SQLException {
        String sql = "UPDATE TBL_UBS SET nm_ubs = ?, ds_endereco = ?, nm_bairro = ?, nr_latitude = ?, "
                + "nr_longitude = ?, hr_abertura = ?, hr_fechamento = ? WHERE id_ubs = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            preencher(stmt, ubs);
            stmt.setLong(8, ubs.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    /** Exclui a UBS. Devolve false se nenhum registro tiver o id informado. */
    public boolean excluir(Long id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("DELETE FROM TBL_UBS WHERE id_ubs = ?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Preenche os 7 primeiros parametros, na mesma ordem no INSERT e no UPDATE. */
    private void preencher(PreparedStatement stmt, Ubs ubs) throws SQLException {
        stmt.setString(1, ubs.getNome());
        stmt.setString(2, ubs.getEndereco());
        stmt.setString(3, ubs.getBairro());
        stmt.setDouble(4, ubs.getLatitude());
        stmt.setDouble(5, ubs.getLongitude());
        stmt.setString(6, ubs.getHoraAbertura());
        stmt.setString(7, ubs.getHoraFechamento());
    }

    /** Converte a linha atual do ResultSet em uma Ubs. */
    static Ubs mapear(ResultSet rs) throws SQLException {
        Ubs ubs = new Ubs();
        ubs.setId(rs.getLong("id_ubs"));
        ubs.setNome(rs.getString("nm_ubs"));
        ubs.setEndereco(rs.getString("ds_endereco"));
        ubs.setBairro(rs.getString("nm_bairro"));
        ubs.setLatitude(rs.getDouble("nr_latitude"));
        ubs.setLongitude(rs.getDouble("nr_longitude"));
        ubs.setHoraAbertura(rs.getString("hr_abertura"));
        ubs.setHoraFechamento(rs.getString("hr_fechamento"));
        return ubs;
    }
}
