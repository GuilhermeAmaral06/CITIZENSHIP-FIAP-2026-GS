package br.com.fiap.filazero.model;

import java.time.LocalDateTime;

/**
 * Horario da agenda de um profissional: LIVRE, OCUPADO ou BLOQUEADO.
 * Representa a tabela TBL_HORARIO_AGENDA.
 */
public class HorarioAgenda {

    private Long id;
    private Profissional profissional;
    private LocalDateTime dataHora;
    private Integer duracaoMin;
    private StatusHorario status;

    public HorarioAgenda() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Integer getDuracaoMin() {
        return duracaoMin;
    }

    public void setDuracaoMin(Integer duracaoMin) {
        this.duracaoMin = duracaoMin;
    }

    public StatusHorario getStatus() {
        return status;
    }

    public void setStatus(StatusHorario status) {
        this.status = status;
    }
}
