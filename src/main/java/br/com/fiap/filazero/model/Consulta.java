package br.com.fiap.filazero.model;

import java.time.LocalDateTime;

/**
 * Consulta agendada de um paciente em um horario da agenda.
 * Representa a tabela TBL_CONSULTA.
 */
public class Consulta {

    private Long id;
    private Paciente paciente;
    private HorarioAgenda horario;
    private LocalDateTime dataAgendamento;
    private StatusConsulta status;

    /** true quando a vaga foi reaproveitada pela lista de espera (RN03). */
    private boolean encaixe;

    public Consulta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public HorarioAgenda getHorario() {
        return horario;
    }

    public void setHorario(HorarioAgenda horario) {
        this.horario = horario;
    }

    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDateTime dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public void setStatus(StatusConsulta status) {
        this.status = status;
    }

    public boolean isEncaixe() {
        return encaixe;
    }

    public void setEncaixe(boolean encaixe) {
        this.encaixe = encaixe;
    }
}
