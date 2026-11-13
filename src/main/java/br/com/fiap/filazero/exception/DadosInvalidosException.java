package br.com.fiap.filazero.exception;

/**
 * Lancada pelo BO quando os dados enviados sao invalidos (ex.: id de UBS que nao existe,
 * status nao permitido na requisicao). Vira resposta HTTP 400.
 */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
