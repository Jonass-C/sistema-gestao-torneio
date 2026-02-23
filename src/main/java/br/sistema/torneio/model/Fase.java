package br.sistema.torneio.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Fase {

    private int id;                    // incrementado automático
    private @NonNull int idTorneio;             // FK de Torneio
    private @NonNull String nomeFase;  // Oitavas, Quartas, Semi, Final
    private @NonNull Integer colocacaoPerdedor; // 9, 5, 3, 2

}