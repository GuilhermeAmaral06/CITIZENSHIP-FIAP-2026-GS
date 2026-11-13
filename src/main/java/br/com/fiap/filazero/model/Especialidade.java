package br.com.fiap.filazero.model;

/**
 * Especialidade das consultas agendadas (Clinica geral, Pediatria...).
 * Representa a tabela TBL_ESPECIALIDADE.
 */
public class Especialidade {

    private Long id;
    private String nome;
    private Integer duracaoPadraoMin;

    public Especialidade() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getDuracaoPadraoMin() {
        return duracaoPadraoMin;
    }

    public void setDuracaoPadraoMin(Integer duracaoPadraoMin) {
        this.duracaoPadraoMin = duracaoPadraoMin;
    }
}
