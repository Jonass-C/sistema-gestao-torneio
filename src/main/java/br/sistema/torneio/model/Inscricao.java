package br.sistema.torneio.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Inscricao {

    private @NonNull int idTorneio;
    private @NonNull int idJogador;
    private Integer colocacaoFinal;

}