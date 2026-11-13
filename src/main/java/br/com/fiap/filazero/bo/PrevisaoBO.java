package br.com.fiap.filazero.bo;

import br.com.fiap.filazero.dao.LembreteDAO;
import br.com.fiap.filazero.dao.PrevisaoFaltaDAO;
import br.com.fiap.filazero.dao.Transacao;
import br.com.fiap.filazero.model.Lembrete;
import br.com.fiap.filazero.model.NivelRisco;
import br.com.fiap.filazero.model.PrevisaoFalta;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Regras das previsoes de falta.
 *
 * RN07: uma consulta com risco Alto recebe dois lembretes (48 h e 24 h antes);
 *       as demais recebem um (24 h antes).
 *
 * A previsao nunca serve para negar ou atrasar atendimento: ela so define os lembretes.
 */
@Service
public class PrevisaoBO {

    /** Lista as previsoes com os lembretes ja enviados e os que a RN07 pede. Os filtros sao opcionais. */
    public List<PrevisaoFalta> listar(Long idUbs, LocalDate data) {
        return Transacao.executar(conexao -> {
            List<PrevisaoFalta> previsoes = new PrevisaoFaltaDAO(conexao).listar(idUbs, data);

            List<Long> idsConsultas = previsoes.stream().map(previsao -> previsao.getConsulta().getId()).toList();
            List<Lembrete> lembretes = new LembreteDAO(conexao).listarPorConsultas(idsConsultas);

            for (PrevisaoFalta previsao : previsoes) {
                Long idConsulta = previsao.getConsulta().getId();
                previsao.setLembretesEnviados(lembretes.stream()
                        .filter(lembrete -> lembrete.getIdConsulta().equals(idConsulta))
                        .toList());
                definirLembretes(previsao);
            }
            return previsoes;
        });
    }

    /** RN07: calcula quantos lembretes a consulta deve receber e quando cada um deve ser enviado. */
    private void definirLembretes(PrevisaoFalta previsao) {
        List<LocalDateTime> datas = new ArrayList<>();
        // Consulta ja realizada, com falta registrada ou cancelada nao recebe mais lembrete
        if (!previsao.getConsulta().getStatus().isFinal()) {
            LocalDateTime dataConsulta = previsao.getConsulta().getHorario().getDataHora();
            if (previsao.getNivelRisco() == NivelRisco.ALTO) {
                datas.add(dataConsulta.minusHours(48));
            }
            datas.add(dataConsulta.minusHours(24));
        }
        previsao.setDatasDosLembretes(datas);
        previsao.setLembretesNecessarios(datas.size());
    }
}
