package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acesso a tabela TBL_PACIENTE (somente leitura: a API nao cadastra pacientes).
 */
public class PacienteDAO {

    /** Colunas do paciente com o apelido "p", reaproveitadas nos JOINs dos outros DAOs. */
    static final String COLUNAS = "p.id_paciente, p.cd_paciente_anonimo, p.nr_ano_nascimento, p.tp_genero, "
            + "p.fl_prioritario, p.fl_bolsa_familia, p.fl_hipertensao, p.fl_diabetes, p.fl_tem_celular, "
            + "p.id_ubs_referencia";

    private final Connection conexao;

    public PacienteDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /** Devolve o paciente com o id informado ou null se nao existir. */
    public Paciente buscarPorId(Long id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM TBL_PACIENTE p WHERE p.id_paciente = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    static Paciente mapear(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente();
        paciente.setId(rs.getLong("id_paciente"));
        paciente.setCodigoAnonimo(rs.getString("cd_paciente_anonimo"));
        paciente.setAnoNascimento(rs.getInt("nr_ano_nascimento"));
        paciente.setGenero(rs.getString("tp_genero"));
        paciente.setPrioritario(Conversor.paraBoolean(rs.getString("fl_prioritario")));
        paciente.setBolsaFamilia(Conversor.paraBoolean(rs.getString("fl_bolsa_familia")));
        paciente.setHipertensao(Conversor.paraBoolean(rs.getString("fl_hipertensao")));
        paciente.setDiabetes(Conversor.paraBoolean(rs.getString("fl_diabetes")));
        paciente.setTemCelular(Conversor.paraBoolean(rs.getString("fl_tem_celular")));
        paciente.setIdUbsReferencia(rs.getLong("id_ubs_referencia"));
        return paciente;
    }
}
