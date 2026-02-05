package br.sistema.torneio.exception;

public class SqlRuntimeException extends RuntimeException {

    public SqlRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}