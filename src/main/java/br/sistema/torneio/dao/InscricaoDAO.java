package br.sistema.torneio.dao;

import br.sistema.torneio.connection.ConnectionFactory;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Inscricao;
import br.sistema.torneio.model.Jogador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InscricaoDAO {

    private Connection conexao;

    public InscricaoDAO() {
        this.conexao = new ConnectionFactory().getConnection();
    }

    public void inserir(Inscricao inscricao) {
        String sql = "INSERT INTO inscricao(id_torneio, id_jogador, colocacao_final) VALUES(?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1,inscricao.getIdTorneio());
            stmt.setInt(2, inscricao.getIdJogador());
            if (inscricao.getColocacaoFinal() != null) {
                stmt.setInt(3, inscricao.getColocacaoFinal());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao inserir Inscrição.", e);
        }
    }

    public void remover(int idTorneio, int idJogador) {
        String sql = "DELETE FROM inscricao " +
                     "WHERE id_torneio = ? " +
                     "AND id_jogador = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            stmt.setInt(2, idJogador);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao remover Inscricao.", e);
        }
    }

    public void atualizarColocacao(int idTorneio, int idJogador, Integer colocacao) {
        String sql = "UPDATE inscricao " +
                     "SET colocacao_final = ? " +
                     "WHERE id_torneio = ? " +
                     "AND id_jogador = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, colocacao);
            stmt.setInt(2, idTorneio);
            stmt.setInt(3, idJogador);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao atualizar colocação.", e);
        }
    }

    public boolean isInscrito(int idTorneio, int idJogador) {
        String sql = "SELECT count(*) " +
                     "FROM inscricao " +
                     "WHERE id_torneio = ? " +
                     "AND id_jogador = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            stmt.setInt(2, idJogador);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao verificar inscrição.", e);
        }
    }

    public List<Inscricao> listarRanking(int idTorneio) {
        String sql = "SELECT i.id_jogador, i.colocacao_final, j.nickname " +
                     "FROM inscricao i " +
                     "JOIN jogador j ON i.id_jogador = j.id " +
                     "WHERE i.id_torneio = ? AND i.colocacao_final IS NOT NULL " +
                     "ORDER BY i.colocacao_final ASC";
        List<Inscricao> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Inscricao inscricao = new Inscricao();
                inscricao.setColocacaoFinal(rs.getInt("colocacao_final"));
                inscricao.setIdJogador(rs.getInt("id_jogador"));
                retorno.add(inscricao);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Ranking.", e);
        }
    }

    public List<Jogador> listarPorTorneio(int idTorneio) {
        String sql = "SELECT * " +
                     "FROM jogador j " +
                     "JOIN inscricao i ON j.id = i.id_jogador " +
                     "WHERE i.id_torneio = ?";
        List<Jogador> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Jogador jogador = new Jogador();
                jogador.setId(rs.getInt("id"));
                jogador.setNome(rs.getString("nome"));
                jogador.setNickname(rs.getString("nickname"));
                retorno.add(jogador);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar jogadores do torneio.", e);
        }
    }

    public int contarInscritos(int idTorneio) {
        String sql = "SELECT COUNT(*) " +
                     "FROM inscricao " +
                     "WHERE id_torneio = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao contar número de inscritos.", e);
        }
    }

    public List<Object[]> listarHistoricoPorJogador(int idJogador) {
        String sql = "SELECT t.id, t.nome, i.colocacao_final " +
                     "FROM inscricao i " +
                     "JOIN torneio t ON i.id_torneio = t.id " +
                     "WHERE i.id_jogador = ?";

        List<Object[]> historico = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idJogador);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idTorneio = rs.getInt("id");
                String nomeTorneio = rs.getString("nome");
                String colocacaoFormatada = "Não finalizado";
                if (rs.getObject("colocacao_final") != null) {
                    int col = rs.getInt("colocacao_final");
                    switch (col) {
                        case 1: colocacaoFormatada = "1st"; break;
                        case 2: colocacaoFormatada = "2nd"; break;
                        case 3: colocacaoFormatada = "3-4th"; break;
                        case 5: colocacaoFormatada = "5-8th"; break;
                        case 9: colocacaoFormatada = "9-16th"; break; // Caso tenha Oitavas
                        default: colocacaoFormatada = col + "º Lugar"; // Fallback de segurança
                    }
                }
                historico.add(new Object[]{idTorneio, nomeTorneio, colocacaoFormatada});
            }
            return historico;
        } catch (SQLException e) {
            Logger.getLogger(InscricaoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar histórico do jogador.", e);
        }
    }

}