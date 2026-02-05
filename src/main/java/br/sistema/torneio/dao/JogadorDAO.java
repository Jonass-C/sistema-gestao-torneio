package br.sistema.torneio.dao;

import br.sistema.torneio.connection.ConnectionFactory;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Jogador;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JogadorDAO implements DAO<Jogador> {

    private Connection conexao;

    public JogadorDAO() {
        this.conexao = new ConnectionFactory().getConnection();
    }

    @Override
    public void inserir(Jogador objeto) {
        String sql = "INSERT INTO jogador(nome,nickname) VALUES (?,?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, objeto.getNome());
            stmt.setString(2, objeto.getNickname());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(JogadorDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao inserir Jogador.", e);
        }
    }

    @Override
    public void atualizar(Jogador objeto) {
        String sql = "UPDATE jogador " +
                     "SET nome = ?, nickname = ? " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, objeto.getNome());
            stmt.setString(2, objeto.getNickname());
            stmt.setInt(3, objeto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(JogadorDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao atualizar Jogador.", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM jogador WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(JogadorDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao deletar Jogador.", e);
        }
    }

    @Override
    public Jogador buscarPorId(int id) {
        String sql = "SELECT  * " +
                     "FROM jogador " +
                     "WHERE id = ?";
        Jogador retorno = null;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                retorno = new Jogador();
                retorno.setId(rs.getInt("id"));
                retorno.setNome(rs.getString("nome"));
                retorno.setNickname(rs.getString("nickname"));
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(JogadorDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao buscar Jogador por ID.", e);
        }
    }

    @Override
    public List<Jogador> listarTodos() {
        String sql = "SELECT * FROM jogador";
        List<Jogador> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
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
            Logger.getLogger(JogadorDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Jogadores.", e);
        }
    }

    // pesquisar o nickname do jogador a partir de um trecho, com o LIKE;
    public List<Jogador> buscarNomeJogador(String pesquisa) {
        String sql = "SELECT * " +
                     "FROM jogador " +
                     "WHERE nome LIKE ? OR nickname LIKE ?";
        List<Jogador> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, "%" + pesquisa + "%");
            stmt.setString(2, "%" + pesquisa + "%");
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
            Logger.getLogger(JogadorDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao buscar jogador por nome.", e);
        }
    }

}