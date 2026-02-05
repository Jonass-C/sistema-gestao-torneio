package br.sistema.torneio.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Partida {

    private int id;
    private @NonNull int idFase;
    private LocalDateTime data;
    private @NonNull int idJogador1;
    private @NonNull int idJogador2;
    private Integer pontuacaoJogador1;
    private Integer pontuacaoJogador2;
    private Integer idVencedor;

}