package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.bo.ListaEsperaBO;
import br.com.fiap.filazero.dto.ListaEsperaRequest;
import br.com.fiap.filazero.model.ListaEspera;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints da lista de espera. As regras ficam no ListaEsperaBO.
 */
@RestController
@RequestMapping("/lista-espera")
@Tag(name = "Lista de espera", description = "Fila de espera por especialidade, usada no encaixe automático")
public class ListaEsperaController {

    private final ListaEsperaBO listaEsperaBO;

    public ListaEsperaController(ListaEsperaBO listaEsperaBO) {
        this.listaEsperaBO = listaEsperaBO;
    }

    @PostMapping
    @Operation(summary = "Entra na lista de espera")
    public ResponseEntity<ListaEspera> entrar(@Valid @RequestBody ListaEsperaRequest requisicao) {
        ListaEspera criada = listaEsperaBO.entrar(requisicao);
        URI endereco = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(endereco).body(criada);
    }

    @GetMapping
    @Operation(summary = "Lista a fila de espera",
            description = "Mostra quem está aguardando, na ordem do encaixe. Filtros opcionais: ubs e especialidade.")
    public ResponseEntity<List<ListaEspera>> listar(
            @RequestParam(name = "ubs", required = false) Long idUbs,
            @RequestParam(name = "especialidade", required = false) Long idEspecialidade) {
        return ResponseEntity.ok(listaEsperaBO.listar(idUbs, idEspecialidade));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Sai da lista de espera")
    public ResponseEntity<Void> sair(@PathVariable("id") Long id) {
        listaEsperaBO.sair(id);
        return ResponseEntity.noContent().build();
    }
}
