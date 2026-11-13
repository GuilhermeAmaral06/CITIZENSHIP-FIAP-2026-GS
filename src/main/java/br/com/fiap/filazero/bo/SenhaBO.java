package br.com.fiap.filazero.bo;

import br.com.fiap.filazero.dao.PacienteDAO;
import br.com.fiap.filazero.dao.SenhaDAO;
import br.com.fiap.filazero.dao.ServicoDAO;
import br.com.fiap.filazero.dao.Transacao;
import br.com.fiap.filazero.dao.UbsDAO;
import br.com.fiap.filazero.dto.EsperaServicoResponse;
import br.com.fiap.filazero.dto.SenhaRequest;
import br.com.fiap.filazero.exception.ConflitoException;
import br.com.fiap.filazero.exception.DadosInvalidosException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.model.Paciente;
import br.com.fiap.filazero.model.Senha;
import br.com.fiap.filazero.model.Servico;
import br.com.fiap.filazero.model.StatusSenha;
import br.com.fiap.filazero.model.TipoPrioridade;
import br.com.fiap.filazero.model.Ubs;
import br.com.fiap.filazero.util.DataHora;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Regras das senhas da demanda espontanea.
 *
 * RN05: espera estimada = pessoas a frente x tempo medio do servico.
 * RN06: senhas preferenciais (Lei 10.048/2000) sao chamadas intercaladas com as normais:
 *       uma preferencial a cada duas normais.
 */
@Service
public class SenhaBO {

    /** RN06: quantas senhas normais sao chamadas entre uma preferencial e a proxima. */
    private static final int NORMAIS_POR_PREFERENCIAL = 2;

    /** Retira uma nova senha e ja devolve a posicao na fila e a espera estimada. */
    public Senha retirar(SenhaRequest requisicao) {
        return Transacao.executar(conexao -> {
            Ubs ubs = new UbsDAO(conexao).buscarPorId(requisicao.idUbs());
            if (ubs == null) {
                throw new DadosInvalidosException("Não existe UBS com id " + requisicao.idUbs() + ".");
            }
            Servico servico = new ServicoDAO(conexao).buscarPorId(requisicao.idServico());
            if (servico == null) {
                throw new DadosInvalidosException("Não existe serviço com id " + requisicao.idServico() + ".");
            }
            Paciente paciente = null;
            if (requisicao.idPaciente() != null) {
                paciente = new PacienteDAO(conexao).buscarPorId(requisicao.idPaciente());
                if (paciente == null) {
                    throw new DadosInvalidosException("Não existe paciente com id " + requisicao.idPaciente() + ".");
                }
            }

            SenhaDAO senhaDAO = new SenhaDAO(conexao);
            LocalDateTime agora = DataHora.agora();
            int numero = senhaDAO.contarEmitidasNoDia(ubs.getId(), servico.getId(), agora.toLocalDate()) + 1;

            Senha senha = new Senha();
            senha.setUbs(ubs);
            senha.setServico(servico);
            senha.setPaciente(paciente);
            senha.setCodigo(servico.getPrefixoSenha() + String.format("%03d", numero));
            senha.setPrioridade(definirPrioridade(requisicao.prioridade(), paciente));
            senha.setOrigem(requisicao.origem());
            senha.setDataEmissao(agora);
            senha.setStatus(StatusSenha.AGUARDANDO);
            senhaDAO.inserir(senha);

            calcularEspera(senhaDAO, senha);
            return senha;
        });
    }

    public Senha buscarPorId(Long id) {
        return Transacao.executar(conexao -> {
            SenhaDAO senhaDAO = new SenhaDAO(conexao);
            Senha senha = buscarOuFalhar(senhaDAO, id);
            calcularEspera(senhaDAO, senha);
            return senha;
        });
    }

    /**
     * A recepcao chama a senha (CHAMADA) ou a encerra (ATENDIDA ou DESISTIU).
     * So a proxima senha da fila pode ser chamada, para que a ordem da RN06 seja respeitada.
     */
    public Senha atualizarStatus(Long id, StatusSenha novoStatus) {
        return Transacao.executar(conexao -> {
            SenhaDAO senhaDAO = new SenhaDAO(conexao);
            Senha senha = buscarOuFalhar(senhaDAO, id);
            StatusSenha statusAtual = senha.getStatus();

            if (novoStatus == StatusSenha.AGUARDANDO) {
                throw new DadosInvalidosException("O status informado deve ser CHAMADA, ATENDIDA ou DESISTIU.");
            }
            if (statusAtual.isFinal()) {
                throw new ConflitoException("A senha " + senha.getCodigo() + " já foi encerrada ("
                        + statusAtual + ") e não pode ser alterada.");
            }

            LocalDateTime dataChamada = senha.getDataChamada();
            if (novoStatus == StatusSenha.CHAMADA) {
                if (statusAtual != StatusSenha.AGUARDANDO) {
                    throw new ConflitoException("A senha " + senha.getCodigo() + " já foi chamada.");
                }
                Senha proxima = filaDoServico(senhaDAO, senha).get(0);
                if (!proxima.getId().equals(senha.getId())) {
                    throw new ConflitoException("A próxima senha a ser chamada é a " + proxima.getCodigo()
                            + " (" + proxima.getPrioridade() + "), e não a " + senha.getCodigo() + " (RN06).");
                }
                // A chamada nunca pode ficar antes da emissao (CHECK ck_senha_datas)
                LocalDateTime agora = DataHora.agora();
                dataChamada = agora.isBefore(senha.getDataEmissao()) ? senha.getDataEmissao() : agora;
            }
            if (novoStatus == StatusSenha.ATENDIDA && statusAtual != StatusSenha.CHAMADA) {
                throw new ConflitoException("A senha " + senha.getCodigo()
                        + " precisa ser chamada antes de ser marcada como ATENDIDA.");
            }

            senhaDAO.atualizarStatus(id, novoStatus, dataChamada);
            senha.setStatus(novoStatus);
            senha.setDataChamada(dataChamada);
            calcularEspera(senhaDAO, senha);
            return senha;
        });
    }

    /** Situacao da fila de cada servico da UBS neste momento (tela "Minha UBS agora"). */
    public List<EsperaServicoResponse> consultarEspera(Long idUbs) {
        return Transacao.executar(conexao -> {
            if (new UbsDAO(conexao).buscarPorId(idUbs) == null) {
                throw new RecursoNaoEncontradoException("UBS com id " + idUbs + " não encontrada.");
            }
            SenhaDAO senhaDAO = new SenhaDAO(conexao);
            List<Senha> aguardando = senhaDAO.listarAguardando(idUbs);
            List<Senha> chamadasHoje = senhaDAO.listarChamadasNoDia(idUbs, DataHora.hoje());

            List<EsperaServicoResponse> resposta = new ArrayList<>();
            for (Servico servico : new ServicoDAO(conexao).listar()) {
                List<Senha> fila = ordenarFila(doServico(aguardando, servico), doServico(chamadasHoje, servico));
                String proximaSenha = fila.isEmpty() ? null : fila.get(0).getCodigo();
                // RN05: quem chegar agora tem a fila inteira na frente
                int esperaEstimada = fila.size() * servico.getTempoMedioMin();
                resposta.add(new EsperaServicoResponse(servico, fila.size(), esperaEstimada, proximaSenha));
            }
            return resposta;
        });
    }

    private Senha buscarOuFalhar(SenhaDAO senhaDAO, Long id) throws SQLException {
        Senha senha = senhaDAO.buscarPorId(id);
        if (senha == null) {
            throw new RecursoNaoEncontradoException("Senha com id " + id + " não encontrada.");
        }
        return senha;
    }

    /** Paciente prioritario sempre recebe senha preferencial; nos outros casos vale o que foi pedido. */
    private TipoPrioridade definirPrioridade(TipoPrioridade pedida, Paciente paciente) {
        if (paciente != null && paciente.isPrioritario()) {
            return TipoPrioridade.PREFERENCIAL;
        }
        return pedida != null ? pedida : TipoPrioridade.NORMAL;
    }

    /** RN05: preenche na senha quantas pessoas estao na frente e a espera estimada. */
    private void calcularEspera(SenhaDAO senhaDAO, Senha senha) throws SQLException {
        if (senha.getStatus() != StatusSenha.AGUARDANDO) {
            senha.setPessoasNaFrente(null);
            senha.setEsperaEstimadaMin(null);
            return;
        }
        List<Senha> fila = filaDoServico(senhaDAO, senha);
        int pessoasNaFrente = 0;
        while (!fila.get(pessoasNaFrente).getId().equals(senha.getId())) {
            pessoasNaFrente++;
        }
        senha.setPessoasNaFrente(pessoasNaFrente);
        senha.setEsperaEstimadaMin(pessoasNaFrente * senha.getServico().getTempoMedioMin());
    }

    /** Fila (na ordem de chamada) da UBS e do servico da senha informada. */
    private List<Senha> filaDoServico(SenhaDAO senhaDAO, Senha senha) throws SQLException {
        Long idUbs = senha.getUbs().getId();
        List<Senha> aguardando = senhaDAO.listarAguardando(idUbs);
        List<Senha> chamadasHoje = senhaDAO.listarChamadasNoDia(idUbs, DataHora.hoje());
        return ordenarFila(doServico(aguardando, senha.getServico()), doServico(chamadasHoje, senha.getServico()));
    }

    private List<Senha> doServico(List<Senha> senhas, Servico servico) {
        return senhas.stream().filter(s -> s.getServico().getId().equals(servico.getId())).toList();
    }

    /**
     * RN06: monta a ordem em que as senhas que aguardam serao chamadas.
     * Cada tipo (preferencial e normal) segue a ordem de emissao, e os dois tipos sao
     * intercalados: depois de uma preferencial vem duas normais, e entao outra preferencial.
     * Se so houver senhas de um tipo aguardando, elas sao chamadas em sequencia.
     *
     * @param aguardando   senhas aguardando, da mais antiga para a mais nova
     * @param chamadasHoje senhas do mesmo servico ja chamadas hoje
     */
    private List<Senha> ordenarFila(List<Senha> aguardando, List<Senha> chamadasHoje) {
        Deque<Senha> preferenciais = new ArrayDeque<>();
        Deque<Senha> normais = new ArrayDeque<>();
        for (Senha senha : aguardando) {
            if (senha.getPrioridade() == TipoPrioridade.PREFERENCIAL) {
                preferenciais.add(senha);
            } else {
                normais.add(senha);
            }
        }

        int normaisSeguidas = normaisDesdeUltimaPreferencial(chamadasHoje);
        List<Senha> ordem = new ArrayList<>();
        while (!preferenciais.isEmpty() || !normais.isEmpty()) {
            boolean vezDaPreferencial = !preferenciais.isEmpty()
                    && (normais.isEmpty() || normaisSeguidas >= NORMAIS_POR_PREFERENCIAL);
            if (vezDaPreferencial) {
                ordem.add(preferenciais.poll());
                normaisSeguidas = 0;
            } else {
                ordem.add(normais.poll());
                normaisSeguidas++;
            }
        }
        return ordem;
    }

    /**
     * Conta quantas senhas normais foram chamadas hoje depois da ultima preferencial.
     * Se nenhuma preferencial foi chamada hoje, a vez e dela: o atendimento preferencial comeca o dia.
     *
     * As colunas DATE guardam ate o segundo. Se uma normal e uma preferencial forem chamadas no
     * mesmo segundo, a normal e contada como posterior.
     */
    private int normaisDesdeUltimaPreferencial(List<Senha> chamadasHoje) {
        LocalDateTime ultimaPreferencial = null;
        for (Senha chamada : chamadasHoje) {
            if (chamada.getPrioridade() == TipoPrioridade.PREFERENCIAL
                    && (ultimaPreferencial == null || chamada.getDataChamada().isAfter(ultimaPreferencial))) {
                ultimaPreferencial = chamada.getDataChamada();
            }
        }
        if (ultimaPreferencial == null) {
            return NORMAIS_POR_PREFERENCIAL;
        }
        int normais = 0;
        for (Senha chamada : chamadasHoje) {
            if (chamada.getPrioridade() == TipoPrioridade.NORMAL
                    && !chamada.getDataChamada().isBefore(ultimaPreferencial)) {
                normais++;
            }
        }
        return normais;
    }
}
