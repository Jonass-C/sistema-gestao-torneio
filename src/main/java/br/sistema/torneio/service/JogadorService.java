package br.sistema.torneio.service;

import br.sistema.torneio.dao.JogadorDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Jogador;

import java.util.List;

public class JogadorService {

    private JogadorDAO jogadorDAO = new JogadorDAO();

    public void inserir(Jogador jogador) {
        if (!verificarNome(jogador.getNome())) {
            throw new RegraDeNegocioException("O nome do jogador é obrigatório.");
        }
        if (!verificarNickname(jogador.getNickname())) {
            throw new RegraDeNegocioException("O nickname do jogador é obrigatório.");
        }
        if (verificarNicknameRepetidos(jogador.getNickname())){
            throw new RegraDeNegocioException("Nickname já está em uso.");
        }
        jogadorDAO.inserir(jogador);
    }

    public void atualizar(Jogador jogador) {
        if (!verificarId(jogador.getId())) {
            throw new RegraDeNegocioException("ID de jogador inválido.");
        }
        Jogador jogadorAtualizar = jogadorDAO.buscarPorId(jogador.getId());
        if (!verificarJogador(jogadorAtualizar)) {
            throw new RegraDeNegocioException("Jogador não encontrado. Não é possível atualizar.");
        }
        if (!verificarNome(jogador.getNome())) {
            throw new RegraDeNegocioException("O novo nome do jogador é obrigatório.");
        }
        if (!verificarNickname(jogador.getNickname())) {
            throw new RegraDeNegocioException("O novo nickname do jogador é obrigatório.");
        }
        jogadorDAO.atualizar(jogador);
    }

    public void deletar(int id) {
        Jogador jogador = jogadorDAO.buscarPorId(id);
        if (!verificarJogador(jogador)) {
            throw new RegraDeNegocioException("Jogador não encontrado.");
        }
        try {
            jogadorDAO.deletar(id);
        } catch (SqlRuntimeException e) {
            throw new RegraDeNegocioException("Não é possível deletar. Jogador está inscrito em torneios.");
        }
    }

    public Jogador buscarPorId(int id) {
        if (!verificarId(id)) {
            throw new RegraDeNegocioException("ID inválido.");
        }
        return jogadorDAO.buscarPorId(id);
    }

    public List<Jogador> listarTodos() {
        return jogadorDAO.listarTodos();
    }

    public List<Jogador> buscarNomeJogador(String pesquisa) {
        if (pesquisa == null || pesquisa.trim().isEmpty()) {
            throw new RegraDeNegocioException("Digite um termo para pesquisar.");
        }
        return jogadorDAO.buscarNomeJogador(pesquisa);
    }

    // métodos auxiliares
    private boolean verificarNome(String nome) {
        return nome != null && !nome.trim().isBlank();
    }

    private boolean verificarNickname(String nickname) {
        return nickname != null && !nickname.trim().isBlank();
    }

    private boolean verificarId(int id) {
        return id > 0;
    }

    private boolean verificarJogador(Jogador jogador) {
        return jogador != null;
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
