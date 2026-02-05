package br.sistema.torneio.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Jogador {

    private int id;                   // incrementado automático
    private @NonNull String nome;     // name.fullName
    private @NonNull String nickname; // esports.player

}