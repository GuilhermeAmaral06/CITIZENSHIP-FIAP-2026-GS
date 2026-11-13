package br.com.fiap.filazero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API do Fila Zero.
 *
 * Camadas: controller (requisicoes HTTP) -> bo (regras de negocio RN01 a RN07)
 * -> dao (SQL no Oracle) -> model (classes das tabelas).
 */
@SpringBootApplication
public class FilaZeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilaZeroApplication.class, args);
    }
}
