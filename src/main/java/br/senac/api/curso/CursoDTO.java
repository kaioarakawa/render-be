package br.senac.api.curso;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class CursoDTO {

    public record Request(
            @NotBlank(message = "nome é obrigatório")
            @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
            String nome,

            @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
            String descricao,

            @NotNull(message = "cargaHoraria é obrigatória")
            @Min(value = 1, message = "cargaHoraria deve ser maior que zero")
            @Max(value = 2000, message = "cargaHoraria deve ser no máximo 2000")
            Integer cargaHoraria,

            Boolean ativo
    ) {
    }

    public record Response(
            Long id,
            String nome,
            String descricao,
            Integer cargaHoraria,
            boolean ativo,
            OffsetDateTime criadoEm
    ) {
        public static Response de(Curso curso) {
            return new Response(
                    curso.getId(),
                    curso.getNome(),
                    curso.getDescricao(),
                    curso.getCargaHoraria(),
                    curso.isAtivo(),
                    curso.getCriadoEm());
        }
    }
}
