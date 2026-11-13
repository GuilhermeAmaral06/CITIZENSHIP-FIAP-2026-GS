package br.com.fiap.filazero.dao;

import br.com.fiap.filazero.model.NivelRisco;
import br.com.fiap.filazero.model.PrevisaoFalta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela TBL_PREVISAO_FALTA (somente leitura: as previsoes sao calculadas pelo
 * notebook de Data Science e carregadas no banco). Cada previsao traz junto a sua consulta.
 */
public class PrevisaoFaltaDAO {

    private static final String SELECT_BASE = "SELECT pf.id_previsao, pf.pc_prob_falta, pf.nr_nivel_risco, "
            + "pf.ds_versao_modelo, pf.dt_calculo, " + ConsultaDAO.COLUNAS + " "
            + "FROM TBL_PREVISAO_FALTA pf "
            + "JOIN TBL_CONSULTA c ON c.id_consulta = pf.id_consulta "
            + ConsultaDAO.JUNCOES;

    private final Connection conexao;

    public PrevisaoFaltaDAO(Connection conexao) {
        this.conexao = conexao;
    }

    /**
     * Lista as previsoes em ordem de horario da consulta. Os dois filtros sao opcionais
     * (null = sem filtro): UBS do profissional e dia do atendimento.
     */
    public List<PrevisaoFalta> listar(Long idUbs, LocalDate data) throws SQLException {
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
        sql.append("ORDER BY h.dt_horario, c.id_consulta");

        try (PreparedStatement stmt = conexao.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<PrevisaoFalta> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    private static PrevisaoFalta mapear(ResultSet rs) throws SQLException {
        PrevisaoFalta previsao = new PrevisaoFalta();
        previsao.setId(rs.getLong("id_previsao"));
        previsao.setConsulta(ConsultaDAO.mapear(rs));
        previsao.setProbabilidadeFalta(rs.getDouble("pc_prob_falta"));
        previsao.setNivelRisco(NivelRisco.deCodigo(rs.getInt("nr_nivel_risco")));
        previsao.setVersaoModelo(rs.getString("ds_versao_modelo"));
        previsao.setDataCalculo(Conversor.paraDataHora(rs.getTimestamp("dt_calculo")));
        return previsao;
    }
}
