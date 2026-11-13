package br.com.fiap.filazero.model;

import java.time.LocalDateTime;

/**
 * Lembrete de consulta enviado ao paciente e a resposta que ele deu.
 * Representa a tabela TBL_LEMBRETE.
 */
public class Lembrete {

    private Long id;
    private Long idConsulta;
    private CanalLembrete canal;
    private LocalDateTime dataEnvio;
    private RespostaLembrete resposta;
    private LocalDateTime dataResposta;

    public Lembrete() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public CanalLembrete getCanal() {
        return canal;
    }

    public void setCanal(CanalLembrete canal) {
        this.canal = canal;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public RespostaLembrete getResposta() {
        return resposta;
    }

    public void setResposta(RespostaLembrete resposta) {
        this.resposta = resposta;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }
}
