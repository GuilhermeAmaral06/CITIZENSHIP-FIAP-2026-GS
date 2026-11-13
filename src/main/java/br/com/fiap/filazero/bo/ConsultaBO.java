package br.com.fiap.filazero.bo;

import br.com.fiap.filazero.dao.ConsultaDAO;
import br.com.fiap.filazero.dao.HorarioAgendaDAO;
import br.com.fiap.filazero.dao.PacienteDAO;
import br.com.fiap.filazero.dao.Transacao;
import br.com.fiap.filazero.dto.ConsultaRequest;
import br.com.fiap.filazero.exception.ConflitoException;
import br.com.fiap.filazero.exception.DadosInvalidosException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.exception.RegraNegocioException;
import br.com.fiap.filazero.model.Consulta;
import br.com.fiap.filazero.model.HorarioAgenda;
import br.com.fiap.filazero.model.Paciente;
import br.com.fiap.filazero.model.StatusConsulta;
import br.com.fiap.filazero.model.StatusHorario;
import br.com.fiap.filazero.util.DataHora;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Regras das consultas agendadas.
 *
 * RN01: so e possivel agendar num horario LIVRE; ao agendar, o horario passa a OCUPADO.
 * RN02: o mesmo paciente nao pode ter duas consultas da mesma especialidade no mesmo dia.
 * RN03: ao cancelar, o horario volta a LIVRE e o primeiro da lista de espera e encaixado.
 * RN04: uma consulta REALIZADA, FALTOU ou CANCELADA nao pode mais ser alterada.
 */
@Service
public class ConsultaBO {

    private static final DateTimeFormatter DIA_MES_ANO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ListaEsperaBO listaEsperaBO;

    public ConsultaBO(ListaEsperaBO listaEsperaBO) {
        this.listaEsperaBO = listaEsperaBO;
    }

    /** Lista as consultas. Os tres filtros sao opcionais. */
    public List<Consulta> listar(Long idUbs, LocalDate data, StatusConsulta status) {
        return Transacao.executar(conexao -> new ConsultaDAO(conexao).listar(idUbs, data, status));
    }

    public Consulta buscarPorId(Long id) {
        return Transacao.executar(conexao -> buscarOuFalhar(new ConsultaDAO(conexao), id));
    }

    /** Agenda uma consulta (RN01 e RN02). */
    public Consulta agendar(ConsultaRequest requisicao) {
        return Transacao.executar(conexao -> {
            Paciente paciente = new PacienteDAO(conexao).buscarPorId(requisicao.idPaciente());
            if (paciente == null) {
                throw new DadosInvalidosException("Não existe paciente com id " + requisicao.idPaciente() + ".");
            }
            HorarioAgendaDAO horarioDAO = new HorarioAgendaDAO(conexao);
            HorarioAgenda horario = horarioDAO.buscarPorId(requisicao.idHorario());
            if (horario == null) {
                throw new DadosInvalidosException("Não existe horário com id " + requisicao.idHorario() + ".");
            }

            // RN01
            if (horario.getStatus() != StatusHorario.LIVRE) {
                throw new RegraNegocioException("O horário " + horario.getId() + " não está livre (situação: "
                        + horario.getStatus() + "). Só é possível agendar em horário LIVRE (RN01).");
            }

            // RN02
            ConsultaDAO consultaDAO = new ConsultaDAO(conexao);
            Long idEspecialidade = horario.getProfissional().getEspecialidade().getId();
            LocalDate dia = horario.getDataHora().toLocalDate();
            if (consultaDAO.contarDoPacienteNoDia(paciente.getId(), idEspecialidade, dia) > 0) {
                throw new RegraNegocioException("O paciente " + paciente.getCodigoAnonimo()
                        + " já tem consulta de " + horario.getProfissional().getEspecialidade().getNome()
                        + " em " + dia.format(DIA_MES_ANO) + " (RN02).");
            }

            // RN01: so segue se o horario ainda estava livre no momento da gravacao
            if (!horarioDAO.ocupar(horario.getId())) {
                throw new RegraNegocioException("O horário " + horario.getId()
                        + " acabou de ser ocupado por outro agendamento (RN01).");
            }
            horario.setStatus(StatusHorario.OCUPADO);

            Consulta consulta = new Consulta();
            consulta.setPaciente(paciente);
            consulta.setHorario(horario);
            consulta.setDataAgendamento(DataHora.agora());
            consulta.setStatus(StatusConsulta.AGENDADA);
            consulta.setEncaixe(false);
            consultaDAO.inserir(consulta);
            return consulta;
        });
    }

    /** Confirma a consulta ou registra que ela foi realizada ou que o paciente faltou (RN04). */
    public Consulta atualizarStatus(Long id, StatusConsulta novoStatus) {
        return Transacao.executar(conexao -> {
            ConsultaDAO consultaDAO = new ConsultaDAO(conexao);
            Consulta consulta = buscarOuFalhar(consultaDAO, id);

            if (novoStatus == StatusConsulta.CANCELADA) {
                throw new DadosInvalidosException("Para cancelar a consulta use DELETE /consultas/" + id + ".");
            }
            if (novoStatus == StatusConsulta.AGENDADA) {
                throw new DadosInvalidosException("O status informado deve ser CONFIRMADA, REALIZADA ou FALTOU.");
            }
            // RN04
            if (consulta.getStatus().isFinal()) {
                throw new ConflitoException("A consulta " + id + " está " + consulta.getStatus()
                        + " e não pode mais ser alterada (RN04).");
            }
            if (novoStatus == consulta.getStatus()) {
                throw new ConflitoException("A consulta " + id + " já está " + consulta.getStatus() + ".");
            }

            consultaDAO.atualizarStatus(id, novoStatus);
            consulta.setStatus(novoStatus);
            return consulta;
        });
    }

    /**
     * Cancela a consulta (RN03): o horario volta a LIVRE e o primeiro da lista de espera da
     * mesma UBS e especialidade e encaixado na vaga. Tudo acontece na mesma transacao.
     */
    public void cancelar(Long id) {
        Transacao.executarSemRetorno(conexao -> {
            ConsultaDAO consultaDAO = new ConsultaDAO(conexao);
            Consulta consulta = buscarOuFalhar(consultaDAO, id);
            // RN04
            if (consulta.getStatus().isFinal()) {
                throw new ConflitoException("A consulta " + id + " está " + consulta.getStatus()
                        + " e não pode mais ser cancelada (RN04).");
            }

            consultaDAO.atualizarStatus(id, StatusConsulta.CANCELADA);
            new HorarioAgendaDAO(conexao).liberar(consulta.getHorario().getId());
            listaEsperaBO.encaixarPrimeiroDaLista(conexao, consulta);
        });
    }

    private Consulta buscarOuFalhar(ConsultaDAO consultaDAO, Long id) throws SQLException {
        Consulta consulta = consultaDAO.buscarPorId(id);
        if (consulta == null) {
            throw new RecursoNaoEncontradoException("Consulta com id " + id + " não encontrada.");
        }
        return consulta;
    }
}
