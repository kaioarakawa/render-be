CREATE TABLE alunos (
    id             BIGSERIAL PRIMARY KEY,
    nome           VARCHAR(120) NOT NULL,
    email          VARCHAR(150) NOT NULL,
    data_matricula DATE         NOT NULL DEFAULT CURRENT_DATE,
    curso_id       BIGINT       NOT NULL,
    CONSTRAINT uk_alunos_email UNIQUE (email),
    CONSTRAINT fk_alunos_curso FOREIGN KEY (curso_id) REFERENCES cursos (id)
);

CREATE INDEX idx_alunos_curso_id ON alunos (curso_id);
