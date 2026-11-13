package br.com.fiap.filazero.model;

import java.time.LocalDateTime;

/**
 * Paciente aguardando uma vaga de consulta em uma UBS e especialidade.
 * Representa a tabela TBL_LISTA_ESPERA.
 */
public class ListaEspera {

    private Long id;
    private Paciente paciente;
    private Ubs ubs;
    private Especialidade especialidade;

    /** Consulta criada quando o paciente foi encaixado (RN03). */
    private Long idConsultaEncaixe;

    private LocalDateTime dataEntrada;

    /** Quanto maior, mais na frente o paciente fica na lista. */
    private Integer prioridade;

    private StatusEspera status;

    public ListaEspera() {
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

    public Ubs getUbs() {
        return ubs;
    }

    public void setUbs(Ubs ubs) {
        this.ubs = ubs;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

    public Long getIdConsultaEncaixe() {
        return idConsultaEncaixe;
    }

    public void setIdConsultaEncaixe(Long idConsultaEncaixe) {
        this.idConsultaEncaixe = idConsultaEncaixe;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public Integer getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }

    public StatusEspera getStatus() {
        return status;
    }

    public void setStatus(StatusEspera status) {
        this.status = status;
    }
}
