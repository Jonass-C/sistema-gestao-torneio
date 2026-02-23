package br.sistema.torneio.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Jogador {

    private int id;
    private @NonNull String nome;
    private @NonNull String nickname;

}