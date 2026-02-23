package br.sistema.torneio.service;

import br.sistema.torneio.dao.InscricaoDAO;
import br.sistema.torneio.dao.JogadorDAO;
import br.sistema.torneio.dao.TorneioDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.Inscricao;
import br.sistema.torneio.model.Jogador;
import br.sistema.torneio.model.Torneio;

import java.time.LocalDate;
import java.util.List;

public class InscricaoService {

    private final InscricaoDAO inscricaoDAO =  new InscricaoDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();
    private final TorneioDAO torneioDAO =  new TorneioDAO();

    public void inserir(String idTorneioString, String idJogadorString) {
        int idTorneio = converterEValidarId(idTorneioString, "Torneio");
        int idJogador = converterEValidarId(idJogadorString, "Jogador");

        validarTorneio(idTorneio);
        validarJogador(idJogador);
        if (!torneioJaIniciado(idTorneio)) {
            throw new RegraDeNegocioException("Inscrições só podem ser alteradas antes do início do torneio.");
        }
        if (inscricaoDAO.isInscrito(idTorneio, idJogador)) {
            throw new RegraDeNegocioException("Jogador já está inscrito no torneio.");
        }
        if (torneioCheio(idTorneio)) {
            throw new RegraDeNegocioException("O torneio já atingiu o limite de inscritos.");
        }
        Inscricao inscricao = new Inscricao(idTorneio, idJogador);
        inscricaoDAO.inserir(inscricao);

        if (torneioCheio(idTorneio)) {
            new PartidaService().gerarPartidasIniciais(idTorneio);
        }
    }

    public void removerInscricao(String idTorneioString, String idJogadorString) {
        int idTorneio = converterEValidarId(idTorneioString, "Torneio");
        int idJogador = converterEValidarId(idJogadorString, "Jogador");

        validarTorneio(idTorneio);
        validarJogador(idJogador);
        if (!torneioJaIniciado(idTorneio)) {
            throw new RegraDeNegocioException("Não é possível remover inscrição após o início do torneio.");
        }
        if (!inscricaoDAO.isInscrito(idTorneio, idJogador)) {
            throw new RegraDeNegocioException("Jogador não está inscrito no torneio.");
        }

        Torneio torneio = torneioDAO.buscarPorId(idTorneio);
        if (LocalDate.now().isBefore(torneio.getDataInicio())) {
            new PartidaService().deletarPartidasDoTorneio(idTorneio);
        }

        inscricaoDAO.remover(idTorneio, idJogador);
    }

    public void atualizarColocacao(int idTorneio, int idJogador, int colocacao) {
        validarTorneio(idTorneio);
        validarJogador(idJogador);
        if (!inscricaoDAO.isInscrito(idTorneio, idJogador)) {
            throw new RegraDeNegocioException("Jogador não está inscrito no torneio.");
        }
        if (colocacao != 1 && colocacao != 2 && colocacao != 3 && colocacao != 5) {
            throw new RegraDeNegocioException("Colocação inválida.");
        }

        inscricaoDAO.atualizarColocacao(idTorneio, idJogador, colocacao);
    }

    public List<Inscricao> listarRanking(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.listarRanking(idTorneio);
    }

    public List<Jogador> listarPorTorneio(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.listarPorTorneio(idTorneio);
    }

    public int contarInscritos(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.contarInscritos(idTorneio);
    }

    public List<Object[]> listarHistoricoPorJogador(int idJogador) {
        validarJogador(idJogador);
        return inscricaoDAO.listarHistoricoPorJogador(idJogador);
    }

    // métodos auxiliares
    private int converterEValidarId(String idString, String entidade) {
        if (idString == null || idString.trim().isEmpty()) {
            throw new RegraDeNegocioException("O ID do " + entidade + " não pode estar vazio.");
        }
        try {
            int id = Integer.parseInt(idString.trim());
            if (id <= 0) {
                throw new RegraDeNegocioException("O ID do " + entidade + " deve ser maior que zero.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("O ID do " + entidade + " deve ser numérico e não conter letras.");
        }
    }

    private void validarTorneio(int id) {
        if (torneioDAO.buscarPorId(id) == null) {
            throw new RegraDeNegocioException("Torneio não encontrado.");
        }
    }

    private void validarJogador(int id) {
        if (jogadorDAO.buscarPorId(id) == null) {
            throw new RegraDeNegocioException("Jogador não encontrado.");
        }
    }

    private boolean torneioJaIniciado(int idTorneio) {
        Torneio torneio = torneioDAO.buscarPorId(idTorneio);
        if (torneio.getDataInicio() == null)
            return false;
        return LocalDate.now().isBefore(torneio.getDataInicio());
    }

    private boolean torneioCheio(int idTorneio) {
        validarTorneio(idTorneio);
        return inscricaoDAO.contarInscritos(idTorneio) == torneioDAO.MAX_INSCRITOS;
    }

}
