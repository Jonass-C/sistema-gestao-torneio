package br.sistema.torneio.service;

import br.sistema.torneio.dao.FaseDAO;
import br.sistema.torneio.dao.InscricaoDAO;
import br.sistema.torneio.dao.JogadorDAO;
import br.sistema.torneio.dao.TorneioDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.Inscricao;
import br.sistema.torneio.model.Jogador;
import br.sistema.torneio.model.Torneio;

import java.util.List;

public class InscricaoService {

    private InscricaoDAO inscricaoDAO =  new InscricaoDAO();
    private JogadorDAO jogadorDAO = new JogadorDAO();
    private TorneioDAO torneioDAO =  new TorneioDAO();
    private FaseDAO faseDAO = new FaseDAO();

    public void inserir(Inscricao inscricao) {
        validarTorneio(inscricao.getIdTorneio());
        validarJogador(inscricao.getIdJogador());
        if (inscricaoDAO.isInscrito(inscricao.getIdTorneio(), inscricao.getIdJogador())) {
            throw new RegraDeNegocioException("Jogador já está inscrito no torneio.");
        }
        inscricaoDAO.inserir(inscricao);
    }

    // TENHO QUE FAZER O SEGUINTE: PERMITIR QUE O USUÁRIO DIGITE A COLOCAÇÃO E EU ALOQUE ELA DENTRO DOS LIMITES
    // DIGITA 10 = 9-16, DIGITA 7 = 5-8, E ASSIM POR DIANTE
    // DEVO TAMBÉM CRIAR A COLOCAÇÃOVENCEDOR, POIS AINDA NÃO TENHO, PARA INFORMAR QUEM GANHOU O CAMPEONATO
    public void atualizarColocacao(int idTorneio, int idJogador, int colocacao) {
        validarTorneio(idTorneio);
        validarJogador(idJogador);
        if (!inscricaoDAO.isInscrito(idTorneio, idJogador)) {
            throw new RegraDeNegocioException("Jogador não está inscrito no torneio.");
        }
        if (!verificarColocacao(idTorneio, colocacao)) {
            throw new RegraDeNegocioException("Colocação não permitida para este torneio.");
        }

        inscricaoDAO.atualizarColocacao(idTorneio, idJogador, colocacao);
    }

    public boolean isInscrito(int idTorneio, int idJogador) {
        validarTorneio(idTorneio);
        validarJogador(idJogador);
        return inscricaoDAO.isInscrito(idTorneio, idJogador);
    }

    public List<Inscricao> listarRanking(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.listarRanking(idTorneio);
    }

    public List<Jogador> listarPorTorneio(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.listarPorTorneio(idTorneio);
    }

    public void removerInscricao(int idTorneio, int idJogador) {
        validarTorneio(idTorneio);
        validarJogador(idJogador);
        if (!inscricaoDAO.isInscrito(idTorneio, idJogador)) {
            throw new RegraDeNegocioException("Jogador não está inscrito no torneio.");
        }
        inscricaoDAO.remover(idTorneio, idJogador);
    }

    public int contarInscritos(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.contarInscritos(idTorneio);
    }

    public boolean torneioCheio(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.contarInscritos(idTorneio) == torneioDAO.MAX_INSCRITOS;
    }

    // métodos auxiliares
    private boolean verificarId(int id) {
        return id > 0;
    }

    private boolean buscarTorneio(int id) {
        Torneio torneio = torneioDAO.buscarPorId(id);
        return torneio != null;
    }

    private boolean buscarJogador(int id) {
        Jogador jogador = jogadorDAO.buscarPorId(id);
        return jogador != null;
    }

    private void validarTorneio(int id) {
        if (!verificarId(id)) {
            throw new RegraDeNegocioException("ID de torneio é obrigatório e deve ser válido.");
        }
        if (!buscarTorneio(id)) {
            throw new RegraDeNegocioException("Torneio não encontrado.");
        }
    }

    private void validarJogador(int id) {
        if (!verificarId(id)) {
            throw new RegraDeNegocioException("ID de jogador é obrigatório e deve ser válido.");
        }
        if (!buscarJogador(id)) {
            throw new RegraDeNegocioException("Jogador não encontrado.");
        }
    }

    private boolean verificarColocacao(int idTorneio, int colocacao) {
        return faseDAO.existeColocacao(idTorneio, colocacao);
    }

}
