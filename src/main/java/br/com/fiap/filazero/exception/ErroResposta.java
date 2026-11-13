package br.com.fiap.filazero.exception;

import java.time.LocalDateTime;

/**
 * Corpo JSON devolvido pela API sempre que ocorre um erro:
 * {timestamp, status, erro, mensagem, caminho}.
 */
public class ErroResposta {

    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;

    public ErroResposta(LocalDateTime timestamp, int status, String erro, String mensagem, String caminho) {
        this.timestamp = timestamp;
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getCaminho() {
        return caminho;
    }
}
