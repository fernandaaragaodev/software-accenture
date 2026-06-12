-- Cargos, especialidades e vínculos com usuários

CREATE TABLE IF NOT EXISTS cargos (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS especialidades (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS cargo_id UUID REFERENCES cargos (id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS usuario_especialidades (
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    especialidade_id UUID NOT NULL REFERENCES especialidades (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    PRIMARY KEY (usuario_id, especialidade_id)
);

CREATE INDEX IF NOT EXISTS idx_usuarios_cargo ON usuarios (cargo_id);
CREATE INDEX IF NOT EXISTS idx_usuario_especialidades_especialidade ON usuario_especialidades (especialidade_id);

INSERT INTO cargos (nome, descricao) VALUES
    ('Analista', 'Analista de negócios'),
    ('Desenvolvedor', 'Desenvolvedor de software'),
    ('Designer', 'Designer de produto'),
    ('Gerente', 'Gerente de equipe'),
    ('Consultor', 'Consultor especialista')
ON CONFLICT (nome) DO NOTHING;

INSERT INTO especialidades (nome, descricao) VALUES
    ('Java', 'Desenvolvimento Java'),
    ('React', 'Desenvolvimento frontend React'),
    ('UX/UI', 'Design de experiência e interface'),
    ('DevOps', 'Infraestrutura e CI/CD'),
    ('Dados', 'Engenharia e análise de dados'),
    ('QA', 'Garantia de qualidade')
ON CONFLICT (nome) DO NOTHING;
