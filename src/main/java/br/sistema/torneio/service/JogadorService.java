package br.sistema.torneio.service;

import br.sistema.torneio.dao.JogadorDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Jogador;

import java.util.List;

public class JogadorService {

    private final JogadorDAO jogadorDAO = new JogadorDAO();

    public void inserir(Jogador jogador) {
        if (!verificarTexto(jogador.getNome())) {
            throw new RegraDeNegocioException("O nome do jogador é obrigatório.");
        }
        if (!verificarTexto(jogador.getNickname())) {
            throw new RegraDeNegocioException("O nickname do jogador é obrigatório.");
        }
        if (verificarNicknameRepetidos(jogador.getNickname())){
            throw new RegraDeNegocioException("Nickname já está em uso.");
        }
        jogadorDAO.inserir(jogador);
    }

    public void atualizar(String idString, Jogador jogador) {
        int id = converterEValidarId(idString);
        jogador.setId(id);

        Jogador jogadorAtualizar = buscarPorId(jogador.getId());
        if (jogadorAtualizar == null) {
            throw new RegraDeNegocioException("Jogador não encontrado. Não é possível atualizar.");
        }
        if (verificarTexto(jogador.getNome())) {
            jogadorAtualizar.setNome(jogador.getNome());
        }
        if (verificarTexto(jogador.getNickname())) {
            jogadorAtualizar.setNickname(jogador.getNickname());
        }

        jogadorDAO.atualizar(jogadorAtualizar);
    }

    public void deletar(String idString) {
        int id = converterEValidarId(idString);

        Jogador jogador = jogadorDAO.buscarPorId(id);
        if (jogador == null) {
            throw new RegraDeNegocioException("Jogador não encontrado.");
        }
        try {
            jogadorDAO.deletar(id);
        } catch (SqlRuntimeException e) {
            throw new RegraDeNegocioException("Não é possível deletar. Jogador está inscrito em torneios.");
        }
    }

    public Jogador buscarPorId(int id) {
        return jogadorDAO.buscarPorId(id);
    }

    public List<Jogador> listarTodos() {
        return jogadorDAO.listarTodos();
    }

    public List<Jogador> buscarNomeJogador(String pesquisa) {
        return jogadorDAO.buscarNomeJogador(pesquisa);
    }

    // métodos auxiliares
    private int converterEValidarId(String idString) {
        if (idString == null || idString.trim().isEmpty()) {
            throw new RegraDeNegocioException("O ID não pode estar vazio.");
        }
        try {
            int id = Integer.parseInt(idString.trim());
            if (id <= 0) {
                throw new RegraDeNegocioException("O ID deve ser um número maior que zero.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new RegraDeNegocioException("O ID deve ser numérico e não conter letras.");
        }
    }

    private boolean verificarTexto(String texto) {
        return texto != null && !texto.trim().isBlank();
    }

    private boolean verificarNicknameRepetidos(String nickname) {
        List<Jogador> existentes = jogadorDAO.buscarNomeJogador(nickname);
        for (Jogador j : existentes) {
            if (j.getNickname().equalsIgnoreCase(nickname)) {
                return true;
            }
        }
        return false;
    }

}
