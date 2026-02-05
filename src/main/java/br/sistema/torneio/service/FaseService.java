package br.sistema.torneio.service;

import br.sistema.torneio.dao.FaseDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.Fase;

import java.util.List;

public class FaseService {

    private FaseDAO faseDAO = new FaseDAO();

    public void inserir(Fase fase) {
        if (!verificarId(fase.getIdTorneio())) {
            throw new RegraDeNegocioException("ID de torneio é obrigatório e deve ser válido.");
        }
        if (!verificarNomeFase(fase.getNomeFase())) {
            throw new RegraDeNegocioException("O nome da fase é obrigatório.");
        }
        if (!verificarColocacao(fase.getColocacaoPerdedor())) {
            throw new RegraDeNegocioException("A colocação do perdedor deve ser informada e ser maior que 1 (um).");
        }
        faseDAO.inserir(fase);
    }

    public void atualizar(Fase fase) {
        if (!verificarId(fase.getId())) {
            throw new RegraDeNegocioException("ID de fase inválido.");
        }
        Fase faseAtualizar = faseDAO.buscarPorId(fase.getId());
        if (!verificarFase(faseAtualizar)) {
            throw new RegraDeNegocioException("Fase não encontrada. Não é possível atualizar.");
        }
        if (!verificarId(fase.getIdTorneio())) {
            throw new RegraDeNegocioException("ID de torneio é obrigatório e deve ser válido.");
        }
        if (!verificarNomeFase(fase.getNomeFase())) {
            throw new RegraDeNegocioException("O nome da fase é obrigatório.");
        }
        if (!verificarColocacao(fase.getColocacaoPerdedor())) {
            throw new RegraDeNegocioException("A colocação do perdedor deve maior que 1 (um).");
        }
        faseDAO.atualizar(fase);
    }

    public void deletar(int id) {
        Fase fase = faseDAO.buscarPorId(id);
        if (!verificarFase(fase)) {
            throw new RegraDeNegocioException("Fase não encontrada.");
        }
        faseDAO.deletar(id);
    }

    public Fase buscarPorId(int id) {
        return faseDAO.buscarPorId(id);
    }

    public List<Fase> listarTodos() {
        return faseDAO.listarTodos();
    }

    public List<Fase> listarPorTorneio(int idTorneio) {
        if (!verificarId(idTorneio)) {
            throw new RegraDeNegocioException("ID de torneio inválido.");
        }
        return faseDAO.listarPorTorneio(idTorneio);
    }

    // métodos auxiliares
    private boolean verificarId(int id) {
        return id > 0;
    }

    private boolean verificarNomeFase(String nomeFase) {
        return nomeFase != null && !nomeFase.trim().isBlank();
    }

    private boolean verificarColocacao(Integer colocacao) {
        return colocacao != null && colocacao > 1;
    }

    private boolean verificarFase(Fase fase) {
        return fase != null;
    }
}
