package br.sistema.torneio.model;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Torneio {

    private int id;
    private @NonNull String nome;
    private @NonNull LocalDate dataInicio;
    private LocalDate dataTermino;

}