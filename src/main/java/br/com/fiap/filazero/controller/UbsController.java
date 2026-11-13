package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.bo.SenhaBO;
import br.com.fiap.filazero.bo.UbsBO;
import br.com.fiap.filazero.dto.EsperaServicoResponse;
import br.com.fiap.filazero.model.Ubs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints das UBS. O controller so recebe a requisicao HTTP e devolve a resposta:
 * as regras ficam nos BOs.
 */
@RestController
@RequestMapping("/ubs")
@Tag(name = "UBS", description = "Unidades básicas de saúde e a espera de cada serviço")
public class UbsController {

    private final UbsBO ubsBO;
    private final SenhaBO senhaBO;

    public UbsController(UbsBO ubsBO, SenhaBO senhaBO) {
        this.ubsBO = ubsBO;
        this.senhaBO = senhaBO;
    }

    @GetMapping
    @Operation(summary = "Lista as UBS")
    public ResponseEntity<List<Ubs>> listar() {
        return ResponseEntity.ok(ubsBO.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma UBS")
    public ResponseEntity<Ubs> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ubsBO.buscarPorId(id));
    }

    @GetMapping("/{id}/espera")
    @Operation(summary = "Espera estimada por serviço (RN05)",
            description = "Para cada serviço da UBS: quantas senhas aguardam, a espera estimada de quem "
                    + "retirar uma senha agora e a próxima senha a ser chamada.")
    public ResponseEntity<List<EsperaServicoResponse>> consultarEspera(@PathVariable("id") Long id) {
        return ResponseEntity.ok(senhaBO.consultarEspera(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma UBS")
    public ResponseEntity<Ubs> cadastrar(@Valid @RequestBody Ubs ubs) {
        Ubs criada = ubsBO.cadastrar(ubs);
        URI endereco = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(endereco).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma UBS")
    public ResponseEntity<Ubs> atualizar(@PathVariable("id") Long id, @Valid @RequestBody Ubs ubs) {
        return ResponseEntity.ok(ubsBO.atualizar(id, ubs));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma UBS")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        ubsBO.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
