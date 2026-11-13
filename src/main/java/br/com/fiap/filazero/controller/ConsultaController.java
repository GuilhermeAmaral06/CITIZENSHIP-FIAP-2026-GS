package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.bo.ConsultaBO;
import br.com.fiap.filazero.dto.ConsultaRequest;
import br.com.fiap.filazero.dto.StatusConsultaRequest;
import br.com.fiap.filazero.model.Consulta;
import br.com.fiap.filazero.model.StatusConsulta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints das consultas agendadas. As regras (RN01 a RN04) ficam no ConsultaBO.
 */
@RestController
@RequestMapping("/consultas")
@Tag(name = "Consultas", description = "Agendamento, confirmação, registro de falta e cancelamento com encaixe")
public class ConsultaController {

    private final ConsultaBO consultaBO;

    public ConsultaController(ConsultaBO consultaBO) {
        this.consultaBO = consultaBO;
    }

    @GetMapping
    @Operation(summary = "Lista consultas com filtro",
            description = "Filtros opcionais: ubs (id), data (aaaa-mm-dd) e status.")
    public ResponseEntity<List<Consulta>> listar(
            @RequestParam(name = "ubs", required = false) Long idUbs,
            @RequestParam(name = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(name = "status", required = false) StatusConsulta status) {
        return ResponseEntity.ok(consultaBO.listar(idUbs, data, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma consulta")
    public ResponseEntity<Consulta> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(consultaBO.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Agenda uma consulta (RN01 e RN02)")
    public ResponseEntity<Consulta> agendar(@Valid @RequestBody ConsultaRequest requisicao) {
        Consulta criada = consultaBO.agendar(requisicao);
        URI endereco = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(endereco).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Confirma ou registra realizada ou falta (RN04)",
            description = "Status aceitos: CONFIRMADA, REALIZADA ou FALTOU.")
    public ResponseEntity<Consulta> atualizarStatus(@PathVariable("id") Long id,
                                                    @Valid @RequestBody StatusConsultaRequest requisicao) {
        return ResponseEntity.ok(consultaBO.atualizarStatus(id, requisicao.status()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela, libera o horário e encaixa (RN03)",
            description = "A consulta fica CANCELADA, o horário volta a LIVRE e o primeiro da lista de espera "
                    + "da mesma UBS e especialidade é encaixado automaticamente.")
    public ResponseEntity<Void> cancelar(@PathVariable("id") Long id) {
        consultaBO.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
