package br.com.fiap.filazero.exception;

/**
 * Lancada pelo BO quando os dados sao validos, mas uma regra de negocio impede a
 * operacao (ex.: RN01 horario nao esta livre, RN02 paciente ja tem consulta no dia).
 * Vira resposta HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
