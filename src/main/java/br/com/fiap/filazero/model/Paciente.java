package br.com.fiap.filazero.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Paciente identificado apenas por um codigo anonimo, sem nome, CPF ou telefone (LGPD).
 * Representa a tabela TBL_PACIENTE.
 *
 * Os dados de saude e de beneficio social existem no banco para o modelo de previsao de
 * faltas, mas nao sao devolvidos nas respostas da API (@JsonIgnore).
 */
public class Paciente {

    private Long id;
    private String codigoAnonimo;
    private Integer anoNascimento;

    /** F, M ou O. */
    private String genero;

    /** Atendimento preferencial previsto na Lei 10.048/2000. */
    private boolean prioritario;

    @JsonIgnore
    private boolean bolsaFamilia;

    @JsonIgnore
    private boolean hipertensao;

    @JsonIgnore
    private boolean diabetes;

    private boolean temCelular;
    private Long idUbsReferencia;

    public Paciente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoAnonimo() {
        return codigoAnonimo;
    }

    public void setCodigoAnonimo(String codigoAnonimo) {
        this.codigoAnonimo = codigoAnonimo;
    }

    public Integer getAnoNascimento() {
        return anoNascimento;
    }

    public void setAnoNascimento(Integer anoNascimento) {
        this.anoNascimento = anoNascimento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public boolean isPrioritario() {
        return prioritario;
    }

    public void setPrioritario(boolean prioritario) {
        this.prioritario = prioritario;
    }

    public boolean isBolsaFamilia() {
        return bolsaFamilia;
    }

    public void setBolsaFamilia(boolean bolsaFamilia) {
        this.bolsaFamilia = bolsaFamilia;
    }

    public boolean isHipertensao() {
        return hipertensao;
    }

    public void setHipertensao(boolean hipertensao) {
        this.hipertensao = hipertensao;
    }

    public boolean isDiabetes() {
        return diabetes;
    }

    public void setDiabetes(boolean diabetes) {
        this.diabetes = diabetes;
    }

    public boolean isTemCelular() {
        return temCelular;
    }

    public void setTemCelular(boolean temCelular) {
        this.temCelular = temCelular;
    }

    public Long getIdUbsReferencia() {
        return idUbsReferencia;
    }

    public void setIdUbsReferencia(Long idUbsReferencia) {
        this.idUbsReferencia = idUbsReferencia;
    }
}
