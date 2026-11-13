package br.com.fiap.filazero.exception;

import br.com.fiap.filazero.util.DataHora;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Camada de tratamento de excecoes: captura as excecoes lancadas em qualquer controller e
 * devolve sempre o mesmo JSON (ErroResposta), com o codigo HTTP correto:
 * 400 validacao, 404 recurso inexistente, 409 e 422 regra de negocio, 500 erro generico.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404 - o registro pedido nao existe. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                                   HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** 400 - dados invalidos detectados pelo BO. */
    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResposta> tratarDadosInvalidos(DadosInvalidosException ex,
                                                             HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** 400 - dados invalidos detectados pelas anotacoes de validacao (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(" "));
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    /** 422 - regra de negocio impede a operacao (ex.: RN01 e RN02). */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    /** 409 - a situacao atual do registro nao permite a operacao (ex.: RN04). */
    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(ConflitoException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** 500 - falha ao acessar o banco de dados (o detalhe tecnico fica apenas no log). */
    @ExceptionHandler(BancoDadosException.class)
    public ResponseEntity<ErroResposta> tratarBancoDados(BancoDadosException ex, HttpServletRequest request) {
        LOG.error("Falha de banco de dados em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /** 400 - corpo da requisicao nao e um JSON valido (ou tem data/valor em formato errado). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "JSON inválido ou mal formatado. Confira os campos e os valores permitidos.", request);
    }

    /** 400 - parametro da URL com tipo errado (ex.: /ubs/abc ou data=16/11/2026). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarParametroInvalido(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        String mensagem = "Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue();
        return responder(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    /** 404 - endereco (URL) que nao existe na API. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> tratarEnderecoInexistente(NoResourceFoundException ex,
                                                                  HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, "Endereço não encontrado nesta API.", request);
    }

    /** 405 - verbo HTTP nao aceito pelo endereco (ex.: DELETE em /ubs sem id). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        String mensagem = "O método " + ex.getMethod() + " não é permitido para este endereço.";
        return responder(HttpStatus.METHOD_NOT_ALLOWED, mensagem, request);
    }

    /** 500 - qualquer outra excecao nao prevista. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        // Excecoes do proprio Spring (ex.: parametro obrigatorio ausente) ja informam o status correto
        if (ex instanceof ErrorResponse erroSpring) {
            return responder(erroSpring.getStatusCode(), "Requisição inválida: " + ex.getMessage(), request);
        }
        LOG.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErroResposta> responder(HttpStatusCode status, String mensagem,
                                                   HttpServletRequest request) {
        HttpStatus statusHttp = HttpStatus.resolve(status.value());
        String erro = statusHttp != null ? statusHttp.getReasonPhrase() : "Erro";
        ErroResposta corpo = new ErroResposta(DataHora.agora(), status.value(), erro, mensagem,
                request.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
