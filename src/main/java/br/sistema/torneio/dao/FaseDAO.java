package br.sistema.torneio.dao;

import br.sistema.torneio.connection.ConnectionFactory;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Fase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FaseDAO implements DAO<Fase> {

    private Connection conexao;

    public FaseDAO() {
        this.conexao = new ConnectionFactory().getConnection();
    }

    @Override
    public void inserir(Fase objeto) {
        String sql = "INSERT INTO fase(id_torneio, nome_fase, colocacao_perdedor) VALUES (?,?,?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, objeto.getIdTorneio());
            stmt.setString(2, objeto.getNomeFase());
            stmt.setInt(3, objeto.getColocacaoPerdedor());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao inserir Fase.", e);
        }
    }

    @Override
    public void atualizar(Fase objeto) {
        String sql = "UPDATE fase " +
                     "SET id_torneio = ?, nome_fase = ?, colocacao_perdedor = ? " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, objeto.getIdTorneio());
            stmt.setString(2, objeto.getNomeFase());
            stmt.setInt(3, objeto.getColocacaoPerdedor());
            stmt.setInt(4, objeto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao atualizar Fase.", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM fase " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao deletar Fase.", e);
        }
    }

    @Override
    public Fase buscarPorId(int id) {
        String sql = "SELECT * " +
                     "FROM fase " +
                     "WHERE id = ?";
        Fase retorno = null;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                retorno = new Fase();
                retorno.setId(rs.getInt("id"));
                retorno.setIdTorneio(rs.getInt("id_torneio"));
                retorno.setNomeFase(rs.getString("nome_fase"));
                retorno.setColocacaoPerdedor(rs.getInt("colocacao_perdedor"));
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao buscar Fase por ID.", e);
        }
    }

    @Override
    public List<Fase> listarTodos() {
        String sql = "SELECT * " +
                     "FROM fase";
        List<Fase> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Fase fase = new Fase();
                fase.setId(rs.getInt("id"));
                fase.setIdTorneio(rs.getInt("id_torneio"));
                fase.setNomeFase(rs.getString("nome_fase"));
                fase.setColocacaoPerdedor(rs.getInt("colocacao_perdedor"));
                retorno.add(fase);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar todas as Fases.", e);
        }
    }

    // listar as fases por torneio;
    public List<Fase> listarPorTorneio(int idTorneio) {
        String sql = "SELECT * " +
                     "FROM fase " +
                     "WHERE id_torneio = ?";
        List<Fase> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Fase fase = new Fase();
                fase.setId(rs.getInt("id"));
                fase.setIdTorneio(rs.getInt("id_torneio"));
                fase.setNomeFase(rs.getString("nome_fase"));
                fase.setColocacaoPerdedor(rs.getInt("colocacao_perdedor"));
                retorno.add(fase);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Fases por Torneio.", e);
        }
    }

    public boolean existeColocacao(int idTorneio, int colocacao) {
        String sql = "SELECT 1 FROM fase WHERE id_torneio = ? AND colocacao_perdedor = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            stmt.setInt(2, colocacao);
            return true;
        } catch (SQLException e) {
            Logger.getLogger(FaseDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new  SqlRuntimeException("Erro ao buscar Fase.", e);
        }
    }

}