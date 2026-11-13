package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.dao.ConnectionFactory;
import br.com.fiap.filazero.util.DataHora;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Verificacao de que a API esta no ar. No plano gratuito do Render o servico "dorme" quando
 * fica sem uso, e chamar o /health e a forma de acorda-lo.
 */
@RestController
@Tag(name = "Health", description = "Verifica se a API está no ar")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Verifica se a API está no ar")
    public Map<String, Object> health() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("status", "UP");
        resposta.put("aplicacao", "Fila Zero API");
        resposta.put("timestamp", DataHora.agora());
        // Nao abre conexao: so informa se DB_URL, DB_USER e DB_PASSWORD foram definidos
        resposta.put("bancoConfigurado", ConnectionFactory.estaConfigurado());
        return resposta;
    }

    /** Quem abre o endereco raiz (o link do deploy) e levado para a pagina de testes dos endpoints. */
    @Hidden
    @GetMapping("/")
    public ResponseEntity<Void> inicio() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/swagger-ui.html")).build();
    }
}
