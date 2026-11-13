package br.com.fiap.filazero.config;

import br.com.fiap.filazero.dao.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Ao iniciar, avisa no log se faltou configurar o acesso ao banco. A API sobe mesmo assim
 * (o /health responde), mas os endpoints que usam o banco devolvem erro 500 ate a
 * configuracao ser feita.
 */
@Component
public class AvisoBancoNaoConfigurado implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AvisoBancoNaoConfigurado.class);

    @Override
    public void run(ApplicationArguments args) {
        if (!ConnectionFactory.estaConfigurado()) {
            LOG.warn("""

                    ============================================================================
                     BANCO DE DADOS NÃO CONFIGURADO: faltam DB_URL, DB_USER e/ou DB_PASSWORD.
                     - Na sua máquina: copie o arquivo .env.example para .env (na raiz do projeto),
                       preencha os três valores e rode a aplicação a partir da raiz.
                     - No Render: cadastre as três variáveis em Environment.
                    ============================================================================
                    """);
        }
    }
}
