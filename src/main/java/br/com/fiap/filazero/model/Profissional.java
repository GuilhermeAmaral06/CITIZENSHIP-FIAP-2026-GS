package br.com.fiap.filazero.model;

/**
 * Profissional de saude que atende consultas agendadas em uma UBS.
 * Representa a tabela TBL_PROFISSIONAL.
 */
public class Profissional {

    private Long id;
    private String nome;
    private String registro;
    private Ubs ubs;
    private Especialidade especialidade;

    public Profissional() {
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

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
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
}
