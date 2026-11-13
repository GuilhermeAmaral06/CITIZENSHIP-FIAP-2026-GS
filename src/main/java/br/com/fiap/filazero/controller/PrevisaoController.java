package br.com.fiap.filazero.controller;

import br.com.fiap.filazero.bo.PrevisaoBO;
import br.com.fiap.filazero.model.PrevisaoFalta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoint das previsoes de falta, usado pelo painel da recepcao. A regra (RN07) fica no PrevisaoBO.
 */
@RestController
@RequestMapping("/previsoes-falta")
@Tag(name = "Previsões de falta", description = "Probabilidade e nível de risco de falta por consulta")
public class PrevisaoController {

    private final PrevisaoBO previsaoBO;

    public PrevisaoController(PrevisaoBO previsaoBO) {
        this.previsaoBO = previsaoBO;
    }

    @GetMapping
    @Operation(summary = "Probabilidade e nível de risco por consulta (RN07)",
            description = "Filtros opcionais: ubs (id) e data (aaaa-mm-dd). Cada item traz os lembretes já "
                    + "enviados e os que a consulta deve receber: dois se o risco é ALTO, um nos demais casos.")
    public ResponseEntity<List<PrevisaoFalta>> listar(
            @RequestParam(name = "ubs", required = false) Long idUbs,
            @RequestParam(name = "data", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(previsaoBO.listar(idUbs, data));
    }
}
