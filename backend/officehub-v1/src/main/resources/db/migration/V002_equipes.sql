-- Schema de equipes (conforme banco2)

CREATE TABLE IF NOT EXISTS equipes (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS equipe_gestores (
    equipe_id UUID NOT NULL REFERENCES equipes (id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (equipe_id, usuario_id)
);

CREATE TABLE IF NOT EXISTS equipe_membros (
    equipe_id UUID NOT NULL REFERENCES equipes (id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (equipe_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_equipe_gestores_usuario ON equipe_gestores (usuario_id);
CREATE INDEX IF NOT EXISTS idx_equipe_membros_usuario ON equipe_membros (usuario_id);
