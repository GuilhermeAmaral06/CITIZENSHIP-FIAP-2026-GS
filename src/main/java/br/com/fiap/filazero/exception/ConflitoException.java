package br.com.fiap.filazero.exception;

/**
 * Lancada pelo BO quando a situacao atual do registro nao permite a operacao
 * (ex.: RN04 consulta ja finalizada, senha ja encerrada, nome de UBS repetido).
 * Vira resposta HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
