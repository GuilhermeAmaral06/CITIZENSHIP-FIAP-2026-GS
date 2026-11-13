package br.com.fiap.filazero.dao;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre as conexoes JDBC com o Oracle da FIAP.
 *
 * O usuario e a senha NUNCA ficam no codigo: sao lidos das variaveis de ambiente DB_URL,
 * DB_USER e DB_PASSWORD (e assim que o Render as fornece). Na maquina local, se a variavel
 * de ambiente nao existir, o valor e lido do arquivo .env da raiz do projeto, que esta no
 * .gitignore e por isso nao vai para o GitHub.
 */
public final class ConnectionFactory {

    /** Tempo maximo, em segundos, para o banco responder ao pedido de conexao. */
    private static final int TEMPO_LIMITE_SEGUNDOS = 20;

    private static final Properties ARQUIVO_ENV = lerArquivoEnv();

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        String url = configuracao("DB_URL");
        String usuario = configuracao("DB_USER");
        String senha = configuracao("DB_PASSWORD");
        if (url == null || usuario == null || senha == null) {
            throw new SQLException("Banco de dados não configurado: defina DB_URL, DB_USER e DB_PASSWORD "
                    + "nas variáveis de ambiente ou no arquivo .env.");
        }
        DriverManager.setLoginTimeout(TEMPO_LIMITE_SEGUNDOS);
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** Indica se as tres configuracoes do banco foram informadas. */
    public static boolean estaConfigurado() {
        return configuracao("DB_URL") != null && configuracao("DB_USER") != null
                && configuracao("DB_PASSWORD") != null;
    }

    /**
     * Procura a configuracao, nesta ordem: propriedade do Java (-DDB_URL=..., usada pelos
     * testes automatizados), variavel de ambiente (Render) e arquivo .env (maquina local).
     */
    private static String configuracao(String nome) {
        String valor = System.getProperty(nome);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(nome);
        }
        if (valor == null || valor.isBlank()) {
            valor = ARQUIVO_ENV.getProperty(nome);
        }
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static Properties lerArquivoEnv() {
        Properties propriedades = new Properties();
        Path arquivo = Path.of(".env");
        if (Files.isRegularFile(arquivo)) {
            try (Reader leitor = Files.newBufferedReader(arquivo)) {
                propriedades.load(leitor);
            } catch (IOException e) {
                // Sem o arquivo a aplicacao continua e usa apenas as variaveis de ambiente
            }
        }
        return propriedades;
    }
}
