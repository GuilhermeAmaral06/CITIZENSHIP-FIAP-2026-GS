package br.com.fiap.filazero.bo;

import br.com.fiap.filazero.dao.ConsultaDAO;
import br.com.fiap.filazero.dao.EspecialidadeDAO;
import br.com.fiap.filazero.dao.HorarioAgendaDAO;
import br.com.fiap.filazero.dao.ListaEsperaDAO;
import br.com.fiap.filazero.dao.PacienteDAO;
import br.com.fiap.filazero.dao.Transacao;
import br.com.fiap.filazero.dao.UbsDAO;
import br.com.fiap.filazero.dto.ListaEsperaRequest;
import br.com.fiap.filazero.exception.ConflitoException;
import br.com.fiap.filazero.exception.DadosInvalidosException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.model.Consulta;
import br.com.fiap.filazero.model.Especialidade;
import br.com.fiap.filazero.model.HorarioAgenda;
import br.com.fiap.filazero.model.ListaEspera;
import br.com.fiap.filazero.model.Paciente;
import br.com.fiap.filazero.model.StatusConsulta;
import br.com.fiap.filazero.model.StatusEspera;
import br.com.fiap.filazero.model.StatusHorario;
import br.com.fiap.filazero.model.Ubs;
import br.com.fiap.filazero.util.DataHora;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Regras da lista de espera, incluindo o encaixe automatico da RN03.
 */
@Service
public class ListaEsperaBO {

    /** Peso somado a prioridade de quem tem atendimento preferencial por lei (Lei 10.048/2000). */
    private static final int PESO_PRIORIDADE_LEGAL = 5;

    /** Coloca o paciente na lista de espera de uma especialidade em uma UBS. */
    public ListaEspera entrar(ListaEsperaRequest requisicao) {
        return Transacao.executar(conexao -> {
            Paciente paciente = new PacienteDAO(conexao).buscarPorId(requisicao.idPaciente());
            if (paciente == null) {
                throw new DadosInvalidosException("Não existe paciente com id " + requisicao.idPaciente() + ".");
            }
            Ubs ubs = new UbsDAO(conexao).buscarPorId(requisicao.idUbs());
            if (ubs == null) {
                throw new DadosInvalidosException("Não existe UBS com id " + requisicao.idUbs() + ".");
            }
            Especialidade especialidade = new EspecialidadeDAO(conexao).buscarPorId(requisicao.idEspecialidade());
            if (especialidade == null) {
                throw new DadosInvalidosException("Não existe especialidade com id "
                        + requisicao.idEspecialidade() + ".");
            }

            ListaEsperaDAO listaEsperaDAO = new ListaEsperaDAO(conexao);
            boolean jaEstaNaLista = listaEsperaDAO.listarAguardando(ubs.getId(), especialidade.getId()).stream()
                    .anyMatch(espera -> espera.getPaciente().getId().equals(paciente.getId()));
            if (jaEstaNaLista) {
                throw new ConflitoException("O paciente " + paciente.getCodigoAnonimo()
                        + " já está na lista de espera de " + especialidade.getNome() + " nesta UBS.");
            }

            ListaEspera espera = new ListaEspera();
            espera.setPaciente(paciente);
            espera.setUbs(ubs);
            espera.setEspecialidade(especialidade);
            espera.setDataEntrada(DataHora.agora());
            espera.setPrioridade(paciente.isPrioritario() ? PESO_PRIORIDADE_LEGAL : 0);
            espera.setStatus(StatusEspera.AGUARDANDO);
            listaEsperaDAO.inserir(espera);
            return espera;
        });
    }

    /** Lista quem esta aguardando, na ordem em que sera encaixado. Os filtros sao opcionais. */
    public List<ListaEspera> listar(Long idUbs, Long idEspecialidade) {
        return Transacao.executar(conexao -> new ListaEsperaDAO(conexao).listarAguardando(idUbs, idEspecialidade));
    }

    /** O paciente sai da lista de espera. O registro continua no banco, com o status DESISTIU. */
    public void sair(Long id) {
        Transacao.executarSemRetorno(conexao -> {
            ListaEsperaDAO listaEsperaDAO = new ListaEsperaDAO(conexao);
            ListaEspera espera = listaEsperaDAO.buscarPorId(id);
            if (espera == null) {
                throw new RecursoNaoEncontradoException("Registro " + id + " da lista de espera não encontrado.");
            }
            if (espera.getStatus() != StatusEspera.AGUARDANDO) {
                throw new ConflitoException("Este registro da lista de espera já foi encerrado ("
                        + espera.getStatus() + ").");
            }
            listaEsperaDAO.marcarDesistencia(id);
        });
    }

    /**
     * RN03: quando uma consulta e cancelada, a vaga vai para o primeiro da lista de espera da
     * mesma UBS e especialidade. A nova consulta e criada com fl_encaixe = 'S' e o registro da
     * lista passa a ENCAIXADO. Se ninguem puder ser encaixado, o horario continua livre.
     *
     * Recebe a conexao do ConsultaBO porque o cancelamento e o encaixe fazem parte da mesma
     * transacao: ou os dois sao gravados, ou nenhum.
     *
     * @return a consulta criada para o paciente encaixado, ou null se ninguem foi encaixado
     */
    Consulta encaixarPrimeiroDaLista(Connection conexao, Consulta cancelada) throws SQLException {
        HorarioAgenda horario = cancelada.getHorario();
        Long idUbs = horario.getProfissional().getUbs().getId();
        Long idEspecialidade = horario.getProfissional().getEspecialidade().getId();
        LocalDate dia = horario.getDataHora().toLocalDate();

        ListaEsperaDAO listaEsperaDAO = new ListaEsperaDAO(conexao);
        ConsultaDAO consultaDAO = new ConsultaDAO(conexao);

        for (ListaEspera espera : listaEsperaDAO.listarAguardando(idUbs, idEspecialidade)) {
            Paciente candidato = espera.getPaciente();
            // Quem acabou de cancelar nao recebe de volta a propria vaga
            if (candidato.getId().equals(cancelada.getPaciente().getId())) {
                continue;
            }
            // A RN02 tambem vale para o encaixe: pula quem ja tem consulta da especialidade nesse dia
            if (consultaDAO.contarDoPacienteNoDia(candidato.getId(), idEspecialidade, dia) > 0) {
                continue;
            }
            if (!new HorarioAgendaDAO(conexao).ocupar(horario.getId())) {
                return null;
            }
            horario.setStatus(StatusHorario.OCUPADO);

            Consulta encaixe = new Consulta();
            encaixe.setPaciente(candidato);
            encaixe.setHorario(horario);
            encaixe.setDataAgendamento(DataHora.agora());
            encaixe.setStatus(StatusConsulta.AGENDADA);
            encaixe.setEncaixe(true);
            consultaDAO.inserir(encaixe);

            listaEsperaDAO.marcarEncaixado(espera.getId(), encaixe.getId());
            return encaixe;
        }
        return null;
    }
}
