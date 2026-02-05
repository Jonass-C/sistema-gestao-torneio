package br.sistema.torneio.service;

import br.sistema.torneio.dao.PartidaDAO;
import br.sistema.torneio.dao.TorneioDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.Jogador;
import br.sistema.torneio.model.Partida;
import br.sistema.torneio.model.PartidaDTO;
import br.sistema.torneio.model.Torneio;

import java.time.LocalDateTime;
import java.util.List;

public class PartidaService {

    PartidaDAO partidaDAO = new PartidaDAO();
    TorneioDAO torneioDAO = new TorneioDAO();

    public void inserir(Partida partida) {
        if (partida == null) {
            throw new RegraDeNegocioException("Partida não pode ser nula.");
        }
        if (partida.getIdJogador1() == partida.getIdJogador2()) {
            throw new RegraDeNegocioException("Partida não pode ocorrer entre o mesmo jogador.");
        }
        partidaDAO.inserir(partida);
    }

    public void atualizar(Partida partida) {
        if (partida == null) {
            throw new RegraDeNegocioException("Partida não pode ser nula.");
        }
        if (partida.getIdJogador1() == partida.getIdJogador2()) {
            throw new RegraDeNegocioException("Partida não pode ocorrer entre o mesmo jogador.");
        }
        verificarId(partida.getId());

        partidaDAO.atualizar(partida);
    }

    public void deletar(int id) {
        if (!verificarId(id)) {
            throw new RegraDeNegocioException("ID inválido.");
        }
        partidaDAO.deletar(id);
    }

    public Partida buscarPorId(int id) {
        if (!verificarId(id)) {
            throw new RegraDeNegocioException("ID inválido.");
        }
        return partidaDAO.buscarPorId(id);
    }

    public List<Partida> listarTodos() {
        return partidaDAO.listarTodos();
    }

    public List<Partida> listarPorData(LocalDateTime data) {
        if (data == null) {
            throw new RegraDeNegocioException("Data inválida.");
        }
        return partidaDAO.listarPorData(data);
    }

    public List<PartidaDTO> listarPorTorneio(int idTorneio) {
        validarTorneio(idTorneio);
        return partidaDAO.listarPorTorneio(idTorneio);
    }

    // métodos auxiliares
    private boolean verificarId(int id) {
        return id > 0;
    }

    private boolean buscarTorneio(int id) {
        Torneio torneio = torneioDAO.buscarPorId(id);
        return torneio != null;
    }

    private void validarTorneio(int id) {
        if (!verificarId(id)) {
            throw new RegraDeNegocioException("ID de torneio é obrigatório e deve ser válido.");
        }
        if (!buscarTorneio(id)) {
            throw new RegraDeNegocioException("Torneio não encontrado.");
        }
    }

}
