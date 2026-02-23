package br.sistema.torneio.dao;

import br.sistema.torneio.connection.ConnectionFactory;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Partida;
import br.sistema.torneio.model.PartidaDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PartidaDAO implements DAO<Partida> {

    private Connection conexao;

    public PartidaDAO() {
        this.conexao = new ConnectionFactory().getConnection();
    }

    @Override
    public void inserir(Partida objeto) {
        String sql = "INSERT INTO partida(id_fase, data, id_jogador1, id_jogador2) VALUES (?, CURRENT_DATE, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, objeto.getIdFase());
            stmt.setInt(2, objeto.getIdJogador1());
            stmt.setInt(3, objeto.getIdJogador2());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao inserir Partida.", e);
        }
    }

    @Override
    public void atualizar(Partida objeto) {
        String sql = "UPDATE partida " +
                     "SET pontuacao_jogador1 = ?, pontuacao_jogador2 = ?, data = ? " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, objeto.getPontuacaoJogador1());
            stmt.setInt(2, objeto.getPontuacaoJogador2());
            if (objeto.getData() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(objeto.getData()));
            } else {
                stmt.setNull(3, Types.TIMESTAMP);
            }
            stmt.setInt(4, objeto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao atualizar Partida.", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM partida " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao deletar Partida.", e);
        }
    }

    @Override
    public Partida buscarPorId(int id) {
        String sql = "SELECT * " +
                     "FROM partida " +
                     "WHERE id = ?";
        Partida retorno = null;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                retorno = new Partida();
                retorno.setId(rs.getInt("id"));
                retorno.setIdFase(rs.getInt("id_fase"));
                if (rs.getTimestamp("data") != null) {
                    retorno.setData(rs.getTimestamp("data").toLocalDateTime());
                }
                retorno.setIdJogador1(rs.getInt("id_jogador1"));
                retorno.setIdJogador2(rs.getInt("id_jogador2"));
                retorno.setPontuacaoJogador1(rs.getInt("pontuacao_jogador1"));
                retorno.setPontuacaoJogador2(rs.getInt("pontuacao_jogador2"));
                retorno.setIdVencedor(rs.getInt("id_vencedor"));
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao buscar Partida por ID.", e);
        }
    }

    @Override
    public List<Partida> listarTodos() {
        String sql = "SELECT * " +
                     "FROM partida";
        List<Partida> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Partida partida = new Partida();
                partida.setId(rs.getInt("id"));
                partida.setIdFase(rs.getInt("id_fase"));
                if (rs.getTimestamp("data") != null) {
                    partida.setData(rs.getTimestamp("data").toLocalDateTime());
                }
                partida.setIdJogador1(rs.getInt("id_jogador1"));
                partida.setIdJogador2(rs.getInt("id_jogador2"));
                partida.setPontuacaoJogador1(rs.getInt("pontuacao_jogador1"));
                partida.setPontuacaoJogador2(rs.getInt("pontuacao_jogador2"));
                partida.setIdVencedor(rs.getInt("id_vencedor"));
                retorno.add(partida);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Partidas.", e);
        }
    }

    public List<PartidaDTO> listarPorTorneio(int idTorneio) {
        String sql = "SELECT p.id AS id_partida, p.data, " +
                     "j1.nickname AS nick_jogador1, j2.nickname AS nick_jogador2, " +
                     "p.pontuacao_jogador1 AS placar1, p.pontuacao_jogador2 AS placar2, " +
                     "f.nome_fase " +
                     "FROM partida p " +
                     "JOIN fase f ON p.id_fase = f.id " +
                     "JOIN jogador j1 ON p.id_jogador1 = j1.id " +
                     "JOIN jogador j2 ON p.id_jogador2 = j2.id " +
                     "WHERE f.id_torneio = ?";
        List<PartidaDTO> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PartidaDTO partidaDTO = new PartidaDTO();
                partidaDTO.setIdPartida(rs.getInt("id_partida"));
                if(rs.getTimestamp("data") != null) {
                    partidaDTO.setData(rs.getTimestamp("data").toLocalDateTime());
                }
                partidaDTO.setNickJogador1(rs.getString("nick_jogador1"));
                partidaDTO.setNickJogador2(rs.getString("nick_jogador2"));
                partidaDTO.setPlacar1(rs.getInt("placar1"));
                partidaDTO.setPlacar2(rs.getInt("placar2"));
                partidaDTO.setNomeFase(rs.getString("nome_fase"));
                retorno.add(partidaDTO);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar partidas do torneio.", e);
        }
    }

    public List<Partida> listarPorFase(int idFase) {
        String sql = "SELECT * " +
                     "FROM partida " +
                     "WHERE id_fase = ?";
        List<Partida> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idFase);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Partida partida = new Partida();
                partida.setId(rs.getInt("id"));
                partida.setIdFase(rs.getInt("id_fase"));
                if (rs.getTimestamp("data") != null) {
                    partida.setData(rs.getTimestamp("data").toLocalDateTime());
                }
                partida.setIdJogador1(rs.getInt("id_jogador1"));
                partida.setIdJogador2(rs.getInt("id_jogador2"));
                partida.setPontuacaoJogador1((Integer) rs.getObject("pontuacao_jogador1"));
                partida.setPontuacaoJogador2((Integer) rs.getObject("pontuacao_jogador2"));
                partida.setIdVencedor((Integer) rs.getObject("id_vencedor"));
                retorno.add(partida);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(PartidaDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Partidas por Fase.", e);
        }
    }

    public void deletarPorTorneio(int idTorneio) {
        String sql = "DELETE FROM partida " +
                     "WHERE id_fase IN (SELECT id " +
                                       "FROM fase " +
                                       "WHERE id_torneio = ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlRuntimeException("Erro ao deletar partidas do torneio.", e);
        }
    }

}