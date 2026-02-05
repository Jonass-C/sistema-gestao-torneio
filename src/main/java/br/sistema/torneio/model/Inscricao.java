package br.sistema.torneio.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Inscricao {

    private @NonNull int idTorneio; // FK de Torneio
    private @NonNull int idJogador; // FK de Jogador
    private Integer colocacaoFinal; // 9, 5, 3, 2

}