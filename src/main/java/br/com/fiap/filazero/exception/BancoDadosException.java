package br.com.fiap.filazero.exception;

/**
 * Lancada quando ocorre uma falha de acesso ao banco de dados (SQLException).
 * Vira resposta HTTP 500.
 */
public class BancoDadosException extends RuntimeException {

    public BancoDadosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
