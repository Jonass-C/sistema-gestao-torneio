package br.sistema.torneio.service;

import br.sistema.torneio.dao.FaseDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.Fase;

import java.util.List;

public class FaseService {

    private final FaseDAO faseDAO = new FaseDAO();

    private void inserir(Fase fase) {
        faseDAO.inserir(fase);
    }

    public Fase buscarPorId(int id) {
        if (id <= 0) {
            throw new RegraDeNegocioException("ID de fase inválido.");
        }
        return faseDAO.buscarPorId(id);
    }

    public List<Fase> listarPorTorneio(int idTorneio) {
        if (idTorneio <= 0) {
            throw new RegraDeNegocioException("ID de torneio inválido.");
        }
        return faseDAO.listarPorTorneio(idTorneio);
    }

    public void gerarFasesPadrao(int idTorneio) {

        // Quartas (8 → 4)
        inserir(new Fase(idTorneio, "Quartas de Final", 5));

        // Semifinal (4 → 2)
        inserir(new Fase(idTorneio, "Semifinal", 3));

        // Final (2 → 1)
        inserir(new Fase(idTorneio, "Final", 2));
    }

}
