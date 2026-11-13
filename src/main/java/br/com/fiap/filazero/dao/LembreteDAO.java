package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.CanalLembrete;
import br.com.fiap.filazero.model.Lembrete;
import br.com.fiap.filazero.model.RespostaLembrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Acesso a tabela TBL_LEMBRETE (somente leitura: o envio e feito pelo servico de SMS/app).
 */
public class LembreteDAO {

    private final Connection conexao;

    public LembreteDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /**
     * Lista os lembretes de varias consultas com um unico SELECT (id_consulta IN (...)),
     * em vez de ir ao banco uma vez para cada consulta.
     */
    public List<Lembrete> listarPorConsultas(List<Long> idsConsultas) throws SQLException {
        if (idsConsultas.isEmpty()) {
            return new ArrayList<>();
        }
        String interrogacoes = String.join(", ", Collections.nCopies(idsConsultas.size(), "?"));
        String sql = "SELECT l.id_lembrete, l.id_consulta, l.tp_canal, l.dt_envio, l.tp_resposta, l.dt_resposta "
                + "FROM TBL_LEMBRETE l WHERE l.id_consulta IN (" + interrogacoes + ") "
                + "ORDER BY l.dt_envio, l.id_lembrete";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            for (int i = 0; i < idsConsultas.size(); i++) {
                stmt.setLong(i + 1, idsConsultas.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Lembrete> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    private static Lembrete mapear(ResultSet rs) throws SQLException {
        Lembrete lembrete = new Lembrete();
        lembrete.setId(rs.getLong("id_lembrete"));
        lembrete.setIdConsulta(rs.getLong("id_consulta"));
        lembrete.setCanal(CanalLembrete.valueOf(rs.getString("tp_canal")));
        lembrete.setDataEnvio(Conversor.paraDataHora(rs.getTimestamp("dt_envio")));
        lembrete.setResposta(RespostaLembrete.valueOf(rs.getString("tp_resposta")));
        lembrete.setDataResposta(Conversor.paraDataHora(rs.getTimestamp("dt_resposta")));
        return lembrete;
    }
}
