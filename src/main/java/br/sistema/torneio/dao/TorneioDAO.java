package br.sistema.torneio.dao;

import br.sistema.torneio.connection.ConnectionFactory;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Torneio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TorneioDAO implements DAO<Torneio> {

    public final int MAX_INSCRITOS = 16;

    private Connection conexao;

    public TorneioDAO() {
        this.conexao = new ConnectionFactory().getConnection();
    }

    @Override
    public void inserir(Torneio objeto) {
        String sql = "INSERT INTO torneio(nome,data_inicio) VALUES (?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, objeto.getNome());
            stmt.setDate(2, java.sql.Date.valueOf(objeto.getData()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao inserir Torneio.", e);
        }
    }

    @Override
    public void atualizar(Torneio objeto) {
        String sql = "UPDATE torneio " +
                     "SET nome = ?, data_inicio = ? " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, objeto.getNome());
            stmt.setObject(2, objeto.getData());
            stmt.setInt(3, objeto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao atualizar Torneio.", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM torneio WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao deletar Torneio.", e);
        }
    }

    @Override
    public Torneio buscarPorId(int id) {
        String sql = "SELECT * " +
                     "FROM torneio " +
                     "WHERE id = ?";
        Torneio retorno = null;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                retorno = new Torneio();
                retorno.setId(id);
                retorno.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    retorno.setData(rs.getDate("data_inicio").toLocalDate());
                }
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao buscar Torneio por ID.", e);
        }
    }

    @Override
    public List<Torneio> listarTodos() {
        String sql = "SELECT * " +
                     "FROM torneio";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio objeto = new Torneio();
                objeto.setId(rs.getInt("id"));
                objeto.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    objeto.setData(rs.getDate("data_inicio").toLocalDate());
                }
                retorno.add(objeto);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Torneios.", e);
        }
    }

    // listar os torneios que ainda vão começar (data > data atual;)
    public List<Torneio> torneiosNaoIniciados() {
        String sql = "SELECT * " +
                     "FROM torneio " +
                     "WHERE data_inicio >= CURRENT_DATE " +
                     "ORDER BY data_inicio ASC";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setData(rs.getDate("data_inicio").toLocalDate());
                }
                retorno.add(torneio);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar torneios não iniciados.", e);
        }
    }

    public List<Torneio> torneiosFinalizados() {
        List<Torneio> torneio = null;
        return torneio;
    }

    public List<Torneio> torneiosEmAndamento() {
        List<Torneio> torneio = null;
        return torneio;
    }

    // pesquisar torneio por nome, com o LIKE;
    public List<Torneio> buscarNomeTorneio(String pesquisa) {
        String sql = "SELECT * " +
                     "FROM torneio " +
                     "WHERE nome LIKE ?";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, pesquisa);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setData(rs.getDate("data_inicio").toLocalDate());
                }
                retorno.add(torneio);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao buscar torneio por nome.", e);
        }
    }

}