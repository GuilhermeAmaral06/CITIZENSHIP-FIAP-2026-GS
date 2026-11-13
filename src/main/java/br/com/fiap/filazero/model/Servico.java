package br.com.fiap.filazero.model;

/**
 * Servico de demanda espontanea atendido por senha (Acolhimento, Vacina, Curativo,
 * Farmacia, Coleta de exames). Representa a tabela TBL_SERVICO.
 */
public class Servico {

    private Long id;
    private String nome;

    /** Letra que inicia o codigo das senhas do servico (ex.: A de Acolhimento). */
    private String prefixoSenha;

    /** Tempo medio de um atendimento, em minutos. Base da espera estimada (RN05). */
    private Integer tempoMedioMin;

    public Servico() {
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

    public String getPrefixoSenha() {
        return prefixoSenha;
    }

    public void setPrefixoSenha(String prefixoSenha) {
        this.prefixoSenha = prefixoSenha;
    }

    public Integer getTempoMedioMin() {
        return tempoMedioMin;
    }

    public void setTempoMedioMin(Integer tempoMedioMin) {
        this.tempoMedioMin = tempoMedioMin;
    }
}
