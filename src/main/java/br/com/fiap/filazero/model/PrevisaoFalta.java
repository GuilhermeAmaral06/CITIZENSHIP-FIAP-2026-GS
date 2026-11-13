package br.com.fiap.filazero.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Probabilidade de falta de uma consulta, calculada pelo modelo de Data Science.
 * Representa a tabela TBL_PREVISAO_FALTA.
 *
 * Os tres ultimos campos nao sao colunas: sao preenchidos pelo PrevisaoBO (RN07).
 */
public class PrevisaoFalta {

    private Long id;
    private Consulta consulta;

    /** Valor entre 0 e 1. */
    private Double probabilidadeFalta;

    private NivelRisco nivelRisco;
    private String versaoModelo;
    private LocalDateTime dataCalculo;

    /** Lembretes ja enviados para a consulta. */
    private List<Lembrete> lembretesEnviados;

    /** Quantos lembretes a consulta deve receber (RN07). */
    private int lembretesNecessarios;

    /** Quando enviar cada lembrete (RN07). */
    private List<LocalDateTime> datasDosLembretes;

    public PrevisaoFalta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public Double getProbabilidadeFalta() {
        return probabilidadeFalta;
    }

    public void setProbabilidadeFalta(Double probabilidadeFalta) {
        this.probabilidadeFalta = probabilidadeFalta;
    }

    public NivelRisco getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(NivelRisco nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public String getVersaoModelo() {
        return versaoModelo;
    }

    public void setVersaoModelo(String versaoModelo) {
        this.versaoModelo = versaoModelo;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }

    public void setDataCalculo(LocalDateTime dataCalculo) {
        this.dataCalculo = dataCalculo;
    }

    public List<Lembrete> getLembretesEnviados() {
        return lembretesEnviados;
    }

    public void setLembretesEnviados(List<Lembrete> lembretesEnviados) {
        this.lembretesEnviados = lembretesEnviados;
    }

    public int getLembretesNecessarios() {
        return lembretesNecessarios;
    }

    public void setLembretesNecessarios(int lembretesNecessarios) {
        this.lembretesNecessarios = lembretesNecessarios;
    }

    public List<LocalDateTime> getDatasDosLembretes() {
        return datasDosLembretes;
    }

    public void setDatasDosLembretes(List<LocalDateTime> datasDosLembretes) {
        this.datasDosLembretes = datasDosLembretes;
    }
}
