package br.sistema.torneio.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
public class PartidaDTO {

    private int idPartida;
    private LocalDateTime data;
    private String nickJogador1;
    private String nickJogador2;
    private Integer placar1;
    private Integer placar2;
    private String nomeFase;

}