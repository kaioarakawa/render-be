package br.senac.api.curso;

import br.senac.api.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository repository;

    public CursoService(CursoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CursoDTO.Response> listar(String nome) {
        List<Curso> cursos = (nome == null || nome.isBlank())
                ? repository.findAll()
                : repository.findByNomeContainingIgnoreCase(nome);
        return cursos.stream().map(CursoDTO.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public CursoDTO.Response buscarPorId(Long id) {
        return CursoDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public CursoDTO.Response criar(CursoDTO.Request request) {
        Curso curso = new Curso();
        aplicar(curso, request);
        return CursoDTO.Response.de(repository.save(curso));
    }

    @Transactional
    public CursoDTO.Response atualizar(Long id, CursoDTO.Request request) {
        Curso curso = buscarEntidade(id);
        aplicar(curso, request);
        return CursoDTO.Response.de(repository.save(curso));
    }

    @Transactional
    public void deletar(Long id) {
        Curso curso = buscarEntidade(id);
        repository.delete(curso);
    }

    Curso buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso " + id + " não encontrado"));
    }

    private void aplicar(Curso curso, CursoDTO.Request request) {
        curso.setNome(request.nome());
        curso.setDescricao(request.descricao());
        curso.setCargaHoraria(request.cargaHoraria());
        curso.setAtivo(request.ativo() == null || request.ativo());
    }
}
