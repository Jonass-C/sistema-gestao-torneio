package br.sistema.torneio.service;

import br.sistema.torneio.dao.PartidaDAO;
import br.sistema.torneio.dao.TorneioDAO;
import br.sistema.torneio.exception.RegraDeNegocioException;
import br.sistema.torneio.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PartidaService {

    private final PartidaDAO partidaDAO = new PartidaDAO();
    private final TorneioDAO torneioDAO = new TorneioDAO();
    private final FaseService faseService = new FaseService();
    private final InscricaoService inscricaoService = new InscricaoService();

    private void inserir(Partida partida) {
        if (partida == null) {
            throw new RegraDeNegocioException("Partida não pode ser nula.");
        }
        if (partida.getIdJogador1() == partida.getIdJogador2()) {
            throw new RegraDeNegocioException("Partida não pode ocorrer entre o mesmo jogador.");
        }
        partidaDAO.inserir(partida);
    }

    private void atualizar(Partida partida) {
        if (partida == null) {
            throw new RegraDeNegocioException("Partida não pode ser nula.");
        }
        if (partida.getIdJogador1() == partida.getIdJogador2()) {
            throw new RegraDeNegocioException("Partida não pode ocorrer entre o mesmo jogador.");
        }

        Partida existente = buscarPorId(partida.getId());
        if (existente == null) {
            throw new RegraDeNegocioException("Partida não encontrada.");
        }
        if (existente.getIdVencedor() != null) {
            throw new RegraDeNegocioException("Essa partida já foi finalizada e não pode ser alterada.");
        }

        partidaDAO.atualizar(partida);
    }

    public Partida buscarPorId(int id) {
        if (id <= 0) {
            throw new RegraDeNegocioException("ID inválido.");
        }
        return partidaDAO.buscarPorId(id);
    }

    public List<Partida> listarTodos() {
        return partidaDAO.listarTodos();
    }

    public List<PartidaDTO> listarPorTorneio(int idTorneio) {
        validarTorneio(idTorneio);
        List<PartidaDTO> partidas = partidaDAO.listarPorTorneio(idTorneio);

        partidas.sort((p1, p2) ->
                ordemFase(p1.getNomeFase()) - ordemFase(p2.getNomeFase())
        );
        return partidas;
    }

    private int ordemFase(String nome) {
        if (nome == null) return 99;
        if (nome.equalsIgnoreCase("Quartas de Final")) return 1;
        if (nome.equalsIgnoreCase("Semifinal")) return 2;
        if (nome.equalsIgnoreCase("Final")) return 3;
        return 99;
    }

    public List<Partida> listarPorFase(int idFase) {
        return partidaDAO.listarPorFase(idFase);
    }

    public void gerarPartidasIniciais(int idTorneio) {
        List<Jogador> jogadores = inscricaoService.listarPorTorneio(idTorneio);

        if (jogadores.size() != 8) {
            throw new RegraDeNegocioException("O torneio deve ter exatamente 8 jogadores.");
        }

        List<Fase> fases = faseService.listarPorTorneio(idTorneio);

        Fase quartas = fases.stream()
                .filter(f -> f.getNomeFase().equalsIgnoreCase("Quartas de Final"))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Fase Quartas não encontrada."));

        // evitar gerar duas vezes
        if (!listarPorFase(quartas.getId()).isEmpty()) {
            throw new RegraDeNegocioException("Partidas já foram geradas.");
        }

        Collections.shuffle(jogadores);
        for (int i = 0; i < jogadores.size(); i += 2) {
            Partida partida = new Partida();
            partida.setIdFase(quartas.getId());
            partida.setIdJogador1(jogadores.get(i).getId());
            partida.setIdJogador2(jogadores.get(i + 1).getId());
            partida.setPontuacaoJogador1(0);
            partida.setPontuacaoJogador2(0);
            inserir(partida);
        }
    }

    public void deletarPartidasDoTorneio(int idTorneio) {
        partidaDAO.deletarPorTorneio(idTorneio);
    }

    public void registrarResultado(int idPartida, int p1, int p2) {
        Partida partida = buscarPorId(idPartida);
        Fase fase = faseService.buscarPorId(partida.getIdFase());
        Torneio torneio = torneioDAO.buscarPorId(fase.getIdTorneio());

        if (LocalDate.now().isBefore(torneio.getDataInicio())) {
            throw new RegraDeNegocioException("Não é possível registrar resultado antes do início do torneio.");
        }
        if (partida == null){
            throw new RegraDeNegocioException("Partida não encontrada.");
        }
        if (partida.getPontuacaoJogador1() == 2 || partida.getPontuacaoJogador2() == 2) {
            throw new RegraDeNegocioException("Essa partida já foi finalizada.");
        }
        if (p1 < 0 || p2 < 0 || p1 > 2 || p2 > 2) {
            throw new RegraDeNegocioException("Pontuação inválida.");
        }
        if (p1 == p2) {
            throw new RegraDeNegocioException("Não pode haver empate.");
        }

        partida.setPontuacaoJogador1(p1);
        partida.setPontuacaoJogador2(p2);
        if (p1 == 2 || p2 == 2) {
            int vencedor = p1 > p2
                    ? partida.getIdJogador1()
                    : partida.getIdJogador2();

            partida.setIdVencedor(vencedor);
            atualizar(partida);

            int perdedor = p1 > p2
                    ? partida.getIdJogador2()
                    : partida.getIdJogador1();

            atualizarColocacaoEliminado(partida.getIdFase(), perdedor);
            verificarAvancoDeFase(partida.getIdFase());
        } else {
            atualizar(partida);
        }
    }

    private void atualizarColocacaoEliminado(int idFase, int idJogador) {
        Fase fase = faseService.buscarPorId(idFase);

        int colocacao;
        switch (fase.getNomeFase().toLowerCase()) {
            case "quartas de final":
                colocacao = 5;
                break;
            case "semifinal":
                colocacao = 3;
                break;
            case "final":
                colocacao = 2;
                break;
            default:
                return;
        }

        inscricaoService.atualizarColocacao(fase.getIdTorneio(), idJogador, colocacao);
    }

    private void verificarAvancoDeFase(int idFase) {
        List<Partida> partidas = listarPorFase(idFase);

        boolean todasFinalizadas = partidas.stream().allMatch(p -> p.getIdVencedor() != null);

        if (!todasFinalizadas) return;

        Fase faseAtual = faseService.buscarPorId(idFase);

        if (faseAtual.getNomeFase().equalsIgnoreCase("Final")) {
            finalizarTorneio(faseAtual.getIdTorneio());
            return;
        }

        gerarProximaFase(faseAtual);
    }

    private void gerarProximaFase(Fase faseAtual) {
        List<Fase> fases = faseService.listarPorTorneio(faseAtual.getIdTorneio());

        Fase proximaFase = null;

        if (faseAtual.getNomeFase().equalsIgnoreCase("Quartas de Final")) {
            proximaFase = fases.stream()
                    .filter(f -> f.getNomeFase().equalsIgnoreCase("Semifinal"))
                    .findFirst()
                    .orElse(null);
        } else if (faseAtual.getNomeFase().equalsIgnoreCase("Semifinal")) {
            proximaFase = fases.stream()
                    .filter(f -> f.getNomeFase().equalsIgnoreCase("Final"))
                    .findFirst()
                    .orElse(null);
        }

        if (proximaFase == null) return;

        // não gerar duplicado
        if (!listarPorFase(proximaFase.getId()).isEmpty()) return;

        List<Partida> partidasFaseAtual = listarPorFase(faseAtual.getId());
        List<Integer> vencedores = new ArrayList<>();

        for (Partida partida : partidasFaseAtual) {
            vencedores.add(partida.getIdVencedor());
        }

        for (int i = 0; i < vencedores.size(); i += 2) {
            Partida nova = new Partida();
            nova.setIdFase(proximaFase.getId());
            nova.setIdJogador1(vencedores.get(i));
            nova.setIdJogador2(vencedores.get(i + 1));
            nova.setPontuacaoJogador1(0);
            nova.setPontuacaoJogador2(0);
            inserir(nova);
        }
    }

    public void finalizarTorneio(int idTorneio) {
        // 1 - Buscar fases do torneio
        List<Fase> fases = faseService.listarPorTorneio(idTorneio);

        // 2 - Encontrar fase Final
        Fase faseFinal = fases.stream()
                .filter(f -> f.getNomeFase().equalsIgnoreCase("Final"))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Final não encontrada."));

        // 3 - Buscar partidas da fase final
        List<Partida> partidasFinal = listarPorFase(faseFinal.getId());

        if (partidasFinal.isEmpty()) {
            throw new RegraDeNegocioException("Partida final não encontrada.");
        }

        Partida finalPartida = partidasFinal.get(0);

        // 4 - Verificar se já tem vencedor
        if (finalPartida.getIdVencedor() == null) {
            throw new RegraDeNegocioException("A final ainda não foi concluída.");
        }

        // 5 - Atualizar colocação
        inscricaoService.atualizarColocacao(idTorneio, finalPartida.getIdVencedor(), 1);

        // 6 - Atualizar data de término do torneio
        TorneioService torneioService = new TorneioService();
        torneioService.finalizarDataTorneio(idTorneio);
    }

    // métodos auxiliares
    private void validarTorneio(int id) {
        if (id <= 0) {
            throw new RegraDeNegocioException("ID de torneio é obrigatório e deve ser válido.");
        }
        if (torneioDAO.buscarPorId(id) == null) {
            throw new RegraDeNegocioException("Torneio não encontrado.");
        }
    }

}
