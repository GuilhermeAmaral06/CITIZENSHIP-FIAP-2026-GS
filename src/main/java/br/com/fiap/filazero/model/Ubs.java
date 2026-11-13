package br.com.fiap.filazero.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Unidade Basica de Saude atendida pelo Fila Zero. Representa a tabela TBL_UBS.
 * As anotacoes de validacao repetem as regras das constraints do banco.
 */
public class Ubs {

    private static final String FORMATO_HORA = "^([01][0-9]|2[0-3]):[0-5][0-9]$";

    private Long id;

    @NotBlank(message = "O nome da UBS é obrigatório.")
    @Size(max = 80, message = "O nome da UBS deve ter no máximo 80 caracteres.")
    private String nome;

    @NotBlank(message = "O endereço é obrigatório.")
    @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.")
    private String endereco;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres.")
    private String bairro;

    @NotNull(message = "A latitude é obrigatória.")
    @DecimalMin(value = "-90", message = "A latitude deve ficar entre -90 e 90.")
    @DecimalMax(value = "90", message = "A latitude deve ficar entre -90 e 90.")
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória.")
    @DecimalMin(value = "-180", message = "A longitude deve ficar entre -180 e 180.")
    @DecimalMax(value = "180", message = "A longitude deve ficar entre -180 e 180.")
    private Double longitude;

    /** Horario no formato HH:MM, como esta gravado no banco (ex.: 07:00). */
    @NotNull(message = "O horário de abertura é obrigatório.")
    @Pattern(regexp = FORMATO_HORA, message = "O horário de abertura deve estar no formato HH:MM.")
    private String horaAbertura;

    @NotNull(message = "O horário de fechamento é obrigatório.")
    @Pattern(regexp = FORMATO_HORA, message = "O horário de fechamento deve estar no formato HH:MM.")
    private String horaFechamento;

    public Ubs() {
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getHoraAbertura() {
        return horaAbertura;
    }

    public void setHoraAbertura(String horaAbertura) {
        this.horaAbertura = horaAbertura;
    }

    public String getHoraFechamento() {
        return horaFechamento;
    }

    public void setHoraFechamento(String horaFechamento) {
        this.horaFechamento = horaFechamento;
    }
}
