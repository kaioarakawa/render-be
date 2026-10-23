INSERT INTO cursos (nome, descricao, carga_horaria, ativo) VALUES
    ('Técnico em Informática', 'Formação técnica em desenvolvimento e suporte', 1200, TRUE),
    ('Java com Spring Boot',   'APIs REST, banco de dados e deploy em nuvem',    80, TRUE),
    ('Banco de Dados SQL',     'Modelagem, consultas e administração',           60, TRUE);

INSERT INTO alunos (nome, email, curso_id)
SELECT 'Maria Silva', 'maria.silva@exemplo.com', id FROM cursos WHERE nome = 'Java com Spring Boot';

INSERT INTO alunos (nome, email, curso_id)
SELECT 'João Pereira', 'joao.pereira@exemplo.com', id FROM cursos WHERE nome = 'Java com Spring Boot';
