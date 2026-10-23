package br.senac.api.aluno;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    List<Aluno> findByCursoId(Long cursoId);

    boolean existsByEmail(String email);
}
