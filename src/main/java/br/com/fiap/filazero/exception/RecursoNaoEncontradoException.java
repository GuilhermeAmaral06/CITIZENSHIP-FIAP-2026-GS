package br.com.fiap.filazero.exception;

/**
 * Lancada quando o registro procurado nao existe. Vira resposta HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
