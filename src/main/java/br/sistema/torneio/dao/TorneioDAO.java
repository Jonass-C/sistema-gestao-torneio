package br.sistema.torneio.dao;

import br.sistema.torneio.connection.ConnectionFactory;
import br.sistema.torneio.exception.SqlRuntimeException;
import br.sistema.torneio.model.Torneio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TorneioDAO implements DAO<Torneio> {

    public final int MAX_INSCRITOS = 8;

    private Connection conexao;

    public TorneioDAO() {
        this.conexao = new ConnectionFactory().getConnection();
    }

    @Override
    public void inserir(Torneio objeto) {
        String sql = "INSERT INTO torneio(nome,data_inicio, data_termino) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, objeto.getNome());
            stmt.setDate(2, java.sql.Date.valueOf(objeto.getDataInicio()));
            if (objeto.getDataTermino() != null) {
                stmt.setDate(3, java.sql.Date.valueOf(objeto.getDataTermino()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                objeto.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao inserir Torneio.", e);
        }
    }

    @Override
    public void atualizar(Torneio objeto) {
        String sql = "UPDATE torneio " +
                     "SET nome = ?, data_inicio = ?, data_termino = ? " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, objeto.getNome());
            stmt.setObject(2, objeto.getDataInicio());
            stmt.setObject(3, objeto.getDataTermino());
            stmt.setInt(4, objeto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao atualizar Torneio.", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM torneio " +
                     "WHERE id = ?";
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
                    retorno.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    retorno.setDataTermino(rs.getDate("data_termino").toLocalDate());
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
                    objeto.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    objeto.setDataTermino(rs.getDate("data_termino").toLocalDate());
                }
                retorno.add(objeto);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar Torneios.", e);
        }
    }

    public List<Torneio> torneiosNaoIniciados() {
        String sql = "SELECT * " +
                     "FROM torneio " +
                     "WHERE data_inicio > CURRENT_DATE " +
                     "ORDER BY data_inicio ASC";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    torneio.setDataTermino(rs.getDate("data_termino").toLocalDate());
                }
                retorno.add(torneio);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar torneios não iniciados.", e);
        }
    }

    public List<Torneio> torneiosEmAndamento() {
        String sql = "SELECT t.* " +
                     "FROM torneio t " +
                     "WHERE data_inicio <= CURRENT_DATE " +
                     "AND (data_termino >= CURRENT_DATE OR data_termino IS NULL) " +
                     "AND t.id NOT IN(SELECT t2.id " +
                                     "FROM torneio t2, fase f, partida p " +
                                     "WHERE p.id_fase = f.id AND f.id_torneio = t.id " +
                                     "AND f.nome_fase = 'Final' AND p.id_vencedor IS NOT NULL) " +
                     "AND (SELECT COUNT(*) " +
                          "FROM inscricao i " +
                          "WHERE i.id_torneio = t.id" +
                          ") < 8 " +
                     "ORDER BY data_inicio ASC";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    torneio.setDataTermino(rs.getDate("data_termino").toLocalDate());
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
        String sql = "SELECT t.* " +
                     "FROM torneio t, fase f, partida p " +
                     "WHERE p.id_fase = f.id AND f.id_torneio = t.id " +
                     "AND f.nome_fase = 'Final' AND p.id_vencedor IS NOT NULL " +
                     "ORDER BY data_inicio ASC";
        List<Torneio> retorno  = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    torneio.setDataTermino(rs.getDate("data_termino").toLocalDate());
                }
                retorno.add(torneio);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar torneios não iniciados.", e);
        }
    }

    public List<Torneio> torneiosCancelados() {
        String sql = "SELECT t.*\n" +
                     "FROM torneio t\n" +
                     "WHERE t.data_inicio < CURRENT_DATE\n" +
                     "AND (SELECT COUNT(*)\n" +
                     "     FROM inscricao i\n" +
                     "     WHERE i.id_torneio = t.id\n" +
                     "     ) < 8;";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    torneio.setDataTermino(rs.getDate("data_termino").toLocalDate());
                }
                retorno.add(torneio);
            }
            return retorno;
        } catch (SQLException e) {
            Logger.getLogger(TorneioDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new SqlRuntimeException("Erro ao listar torneios não iniciados.", e);
        }
    }

    public void finalizarDataTorneio(int idTorneio) {
        String sql = "UPDATE torneio " +
                     "SET data_termino = CURRENT_DATE " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idTorneio);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao finalizar data do torneio", e);
        }
    }

    public List<Torneio> buscarNomeTorneio(String pesquisa) {
        String sql = "SELECT * " +
                     "FROM torneio " +
                     "WHERE nome LIKE ?";
        List<Torneio> retorno = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, "%" + pesquisa + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Torneio torneio = new Torneio();
                torneio.setId(rs.getInt("id"));
                torneio.setNome(rs.getString("nome"));
                if (rs.getDate("data_inicio") != null) {
                    torneio.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                }
                if (rs.getDate("data_termino") != null) {
                    torneio.setDataTermino(rs.getDate("data_termino").toLocalDate());
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