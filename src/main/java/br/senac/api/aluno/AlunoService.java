package br.senac.api.aluno;

import br.senac.api.curso.Curso;
import br.senac.api.curso.CursoRepository;
import br.senac.api.exception.RegraDeNegocioException;
import br.senac.api.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlunoService {

    private final AlunoRepository repository;
    private final CursoRepository cursoRepository;

    public AlunoService(AlunoRepository repository, CursoRepository cursoRepository) {
        this.repository = repository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional(readOnly = true)
    public List<AlunoDTO.Response> listar(Long cursoId) {
        List<Aluno> alunos = (cursoId == null)
                ? repository.findAll()
                : repository.findByCursoId(cursoId);
        return alunos.stream().map(AlunoDTO.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public AlunoDTO.Response buscarPorId(Long id) {
        return AlunoDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public AlunoDTO.Response matricular(AlunoDTO.Request request) {
        if (repository.existsByEmail(request.email())) {
            throw new RegraDeNegocioException("Já existe aluno com o e-mail " + request.email());
        }
        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso " + request.cursoId() + " não encontrado"));

        Aluno aluno = new Aluno();
        aluno.setNome(request.nome());
        aluno.setEmail(request.email());
        aluno.setCurso(curso);
        return AlunoDTO.Response.de(repository.save(aluno));
    }

    @Transactional
    public void deletar(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private Aluno buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno " + id + " não encontrado"));
    }
}
