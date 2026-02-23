package br.sistema.torneio.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Fase {

    private int id;
    private @NonNull int idTorneio;
    private @NonNull String nomeFase;
    private @NonNull Integer colocacaoPerdedor;

}