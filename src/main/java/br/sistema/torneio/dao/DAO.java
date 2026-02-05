package br.sistema.torneio.dao;

import java.util.List;

public interface DAO<T> {

    void inserir(T objeto);
    void atualizar(T objeto);
    void deletar(int id);
    T buscarPorId(int id);
    List<T> listarTodos();

}
