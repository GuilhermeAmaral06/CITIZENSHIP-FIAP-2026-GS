package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.bo.SenhaBO;
import br.com.fiap.filazero.dto.SenhaRequest;
import br.com.fiap.filazero.dto.StatusSenhaRequest;
import br.com.fiap.filazero.model.Senha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints das senhas da demanda espontanea. As regras (RN05 e RN06) ficam no SenhaBO.
 */
@RestController
@RequestMapping("/senhas")
@Tag(name = "Senhas", description = "Senha digital: retirada, posição na fila e chamada pela recepção")
public class SenhaController {

    private final SenhaBO senhaBO;

    public SenhaController(SenhaBO senhaBO) {
        this.senhaBO = senhaBO;
    }

    @PostMapping
    @Operation(summary = "Retira uma senha (RN05 e RN06)",
            description = "idPaciente e prioridade são opcionais. Paciente prioritário recebe senha PREFERENCIAL.")
    public ResponseEntity<Senha> retirar(@Valid @RequestBody SenhaRequest requisicao) {
        Senha criada = senhaBO.retirar(requisicao);
        URI endereco = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(endereco).body(criada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Posição na fila e espera estimada")
    public ResponseEntity<Senha> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(senhaBO.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Recepção chama ou encerra a senha",
            description = "Status aceitos: CHAMADA (só para a próxima senha da fila), ATENDIDA ou DESISTIU.")
    public ResponseEntity<Senha> atualizarStatus(@PathVariable("id") Long id,
                                                 @Valid @RequestBody StatusSenhaRequest requisicao) {
        return ResponseEntity.ok(senhaBO.atualizarStatus(id, requisicao.status()));
    }
}
