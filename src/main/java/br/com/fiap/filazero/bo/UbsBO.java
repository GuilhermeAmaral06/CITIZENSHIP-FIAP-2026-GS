package br.com.fiap.filazero.bo;

import br.com.fiap.filazero.dao.Transacao;
import br.com.fiap.filazero.dao.UbsDAO;
import br.com.fiap.filazero.exception.ConflitoException;
import br.com.fiap.filazero.exception.DadosInvalidosException;
import br.com.fiap.filazero.exception.RecursoNaoEncontradoException;
import br.com.fiap.filazero.model.Ubs;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Regras do cadastro de UBS. As validacoes de formato ficam nas anotacoes da classe Ubs;
 * aqui ficam as que dependem de comparar campos ou de consultar o banco.
 */
@Service
public class UbsBO {

    public List<Ubs> listar() {
        return Transacao.executar(conexao -> new UbsDAO(conexao).listar());
    }

    public Ubs buscarPorId(Long id) {
        return Transacao.executar(conexao -> buscarOuFalhar(new UbsDAO(conexao), id));
    }

    public Ubs cadastrar(Ubs ubs) {
        return Transacao.executar(conexao -> {
            UbsDAO ubsDAO = new UbsDAO(conexao);
            validar(ubsDAO, ubs, null);
            ubsDAO.inserir(ubs);
            return ubs;
        });
    }

    public Ubs atualizar(Long id, Ubs ubs) {
        return Transacao.executar(conexao -> {
            UbsDAO ubsDAO = new UbsDAO(conexao);
            buscarOuFalhar(ubsDAO, id);
            validar(ubsDAO, ubs, id);
            ubs.setId(id);
            ubsDAO.atualizar(ubs);
            return ubs;
        });
    }

    public void excluir(Long id) {
        Transacao.executarSemRetorno(conexao -> {
            UbsDAO ubsDAO = new UbsDAO(conexao);
            buscarOuFalhar(ubsDAO, id);
            try {
                ubsDAO.excluir(id);
            } catch (SQLIntegrityConstraintViolationException e) {
                // As chaves estrangeiras do banco impedem apagar uma UBS que ja esta em uso
                throw new ConflitoException("A UBS possui profissionais, pacientes, senhas ou lista de espera "
                        + "vinculados e não pode ser removida.");
            }
        });
    }

    private Ubs buscarOuFalhar(UbsDAO ubsDAO, Long id) throws SQLException {
        Ubs ubs = ubsDAO.buscarPorId(id);
        if (ubs == null) {
            throw new RecursoNaoEncontradoException("UBS com id " + id + " não encontrada.");
        }
        return ubs;
    }

    /** idAtual e o id da UBS que esta sendo alterada (null no cadastro). */
    private void validar(UbsDAO ubsDAO, Ubs ubs, Long idAtual) throws SQLException {
        ubs.setNome(ubs.getNome().trim());
        ubs.setEndereco(ubs.getEndereco().trim());
        ubs.setBairro(ubs.getBairro().trim());

        // Mesma regra do CHECK ck_ubs_horario; no formato HH:MM a comparacao de texto funciona
        if (ubs.getHoraAbertura().compareTo(ubs.getHoraFechamento()) >= 0) {
            throw new DadosInvalidosException("O horário de abertura deve ser anterior ao de fechamento.");
        }
        // Mesma regra do UNIQUE uk_ubs_nome
        Ubs mesmoNome = ubsDAO.buscarPorNome(ubs.getNome());
        if (mesmoNome != null && !mesmoNome.getId().equals(idAtual)) {
            throw new ConflitoException("Já existe uma UBS cadastrada com o nome " + ubs.getNome() + ".");
        }
    }
}
