package br.com.fiap.filazero.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Titulo e descricao exibidos na pagina de testes dos endpoints (Swagger UI).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI filaZeroOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Fila Zero API")
                .version("1.0.0")
                .description("API do Fila Zero: senha digital na UBS, consultas com encaixe automático pela "
                        + "lista de espera e previsão de faltas. Global Solution FIAP 2026.2 (CitizenTech)."));
    }
}
