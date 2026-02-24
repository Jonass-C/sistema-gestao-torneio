package br.sistema.torneio.service;

import br.sistema.torneio.dao.TorneioDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Jogador;
import br.sistema.torneio.model.Torneio;

import java.time.LocalDate;
import java.util.List;

public class TorneioService {

    private final TorneioDAO torneioDAO = new TorneioDAO();

    public void inserir(Torneio torneio) {
        if (!verificarNome(torneio.getNome())) {
            throw new RegraDeNegocioException("O nome do torneio é obrigatório.");
        }
        if (torneio.getDataInicio() == null) {
            throw new RegraDeNegocioException("A data de início do torneio é obrigatório.");
        }
        if (torneio.getDataTermino() != null && torneio.getDataTermino().isBefore(torneio.getDataInicio())) {
            throw new RegraDeNegocioException("Data de término não pode ser anterior à data de início.");
        }
        if (torneio.getDataInicio().isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("Data de início não pode ser anterior a hoje.");
        }
        if (verificarNomeRepetidos(torneio.getNome())) {
            throw new RegraDeNegocioException("Torneios não podem ter nomes repetidos.");
        }
        torneioDAO.inserir(torneio);
        new FaseService().gerarFasesPadrao(torneio.getId());
    }

    public void atualizar(String idString, Torneio torneio) {
        int id = converterEValidarId(idString);
        torneio.setId(id);

        Torneio torneioAtualizar = buscarPorId(torneio.getId());
        if (torneioAtualizar == null) {
            throw new RegraDeNegocioException("Torneio não encontrado. Não é possível atualizar.");
        }

        boolean estaFinalizado = torneiosFinalizados().stream()
                .anyMatch(t -> t.getId() == torneio.getId());

        if (estaFinalizado) {
            throw new RegraDeNegocioException("Torneio já finalizado e possui um campeão. Nenhuma alteração é permitida.");
        }
        if (verificarNome(torneio.getNome())) {
            torneioAtualizar.setNome(torneio.getNome());
        }
        if (torneio.getDataInicio() != null) {
            torneioAtualizar.setDataInicio(torneio.getDataInicio());
        }
        if (torneio.getDataTermino() != null) {
            torneioAtualizar.setDataTermino(torneio.getDataTermino());
        }

        torneioDAO.atualizar(torneioAtualizar);
    }

    public void deletar(String idString) {
        int id = converterEValidarId(idString);

        Torneio torneio = torneioDAO.buscarPorId(id);
        if (torneio == null) {
            throw new RegraDeNegocioException("Torneio não encontrado.");
        }
        try {
            torneioDAO.deletar(id);
        } catch (SqlRuntimeException e) {
            throw new RegraDeNegocioException("Não é possível deletar. Existem inscrições ou partidas vinculadas.");
        }
    }

    public Torneio buscarPorId(int id) {
        return torneioDAO.buscarPorId(id);
    }

    public List<Torneio> listarTodos() {
        return torneioDAO.listarTodos();
    }

    public List<Torneio> torneiosNaoIniciados() {
        return torneioDAO.torneiosNaoIniciados();
    }

    public List<Torneio> torneiosFinalizados() {
        return torneioDAO.torneiosFinalizados();
    }

    public List<Torneio> torneiosEmAndamento() {
        return torneioDAO.torneiosEmAndamento();
    }

    public List<Torneio> torneiosCancelados() {
        return torneioDAO.torneiosCancelados();
    }

    public void finalizarDataTorneio(int idTorneio) {
        torneioDAO.finalizarDataTorneio(idTorneio);
    }

    public List<Torneio> buscarNomeTorneio(String pesquisa) {
        return torneioDAO.buscarNomeTorneio(pesquisa);
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

    private boolean verificarNome(String nome) {
        return nome != null && !nome.trim().isBlank();
    }

    private boolean verificarNomeRepetidos(String nomeTorneio) {
        List<Torneio> existentes = torneioDAO.buscarNomeTorneio(nomeTorneio);
        for (Torneio t : existentes) {
            if (t.getNome().equalsIgnoreCase(nomeTorneio)) {
                return true;
            }
        }
        return false;
    }

}
