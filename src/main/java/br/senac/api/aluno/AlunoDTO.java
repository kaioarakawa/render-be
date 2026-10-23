package br.senac.api.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class AlunoDTO {

    public record Request(
            @NotBlank(message = "nome é obrigatório")
            @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
            String nome,

            @NotBlank(message = "email é obrigatório")
            @Email(message = "email inválido")
            @Size(max = 150, message = "email deve ter no máximo 150 caracteres")
            String email,

            @NotNull(message = "cursoId é obrigatório")
            Long cursoId
    ) {
    }

    public record Response(
            Long id,
            String nome,
            String email,
            LocalDate dataMatricula,
            Long cursoId,
            String cursoNome
    ) {
        public static Response de(Aluno aluno) {
            return new Response(
                    aluno.getId(),
                    aluno.getNome(),
                    aluno.getEmail(),
                    aluno.getDataMatricula(),
                    aluno.getCurso().getId(),
                    aluno.getCurso().getNome());
        }
    }
}
