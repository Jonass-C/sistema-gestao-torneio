package br.sistema.torneio.model;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Torneio {

    private int id;                  // incrementado automático
    private @NonNull String nome;    // esports.event
    private @NonNull LocalDate data; // Quando fizermos o TorneioService.criar(), faremos: novoTorneio.setData(LocalDate.now());.

}