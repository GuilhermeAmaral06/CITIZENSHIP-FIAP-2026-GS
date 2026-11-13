package br.com.fiap.filazero.model;

import java.time.LocalDateTime;

/**
 * Senha digital da demanda espontanea, retirada pelo app ou pelo totem.
 * Representa a tabela TBL_SENHA.
 *
 * Os dois ultimos campos nao sao colunas: sao calculados pelo SenhaBO (RN05).
 */
public class Senha {

    private Long id;
    private Ubs ubs;
    private Servico servico;

    /** Opcional: a senha retirada no totem pode ser anonima. */
    private Paciente paciente;

    /** Codigo mostrado ao paciente (ex.: A032). */
    private String codigo;

    private TipoPrioridade prioridade;
    private OrigemSenha origem;
    private LocalDateTime dataEmissao;
    private LocalDateTime dataChamada;
    private StatusSenha status;

    /** Quantas senhas serao chamadas antes desta. Null se ela nao esta mais aguardando. */
    private Integer pessoasNaFrente;

    /** Espera estimada em minutos. Null se ela nao esta mais aguardando. */
    private Integer esperaEstimadaMin;

    public Senha() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ubs getUbs() {
        return ubs;
    }

    public void setUbs(Ubs ubs) {
        this.ubs = ubs;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public TipoPrioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(TipoPrioridade prioridade) {
        this.prioridade = prioridade;
    }

    public OrigemSenha getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemSenha origem) {
        this.origem = origem;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDateTime dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDateTime getDataChamada() {
        return dataChamada;
    }

    public void setDataChamada(LocalDateTime dataChamada) {
        this.dataChamada = dataChamada;
    }

    public StatusSenha getStatus() {
        return status;
    }

    public void setStatus(StatusSenha status) {
        this.status = status;
    }

    public Integer getPessoasNaFrente() {
        return pessoasNaFrente;
    }

    public void setPessoasNaFrente(Integer pessoasNaFrente) {
        this.pessoasNaFrente = pessoasNaFrente;
    }

    public Integer getEsperaEstimadaMin() {
        return esperaEstimadaMin;
    }

    public void setEsperaEstimadaMin(Integer esperaEstimadaMin) {
        this.esperaEstimadaMin = esperaEstimadaMin;
    }
}
