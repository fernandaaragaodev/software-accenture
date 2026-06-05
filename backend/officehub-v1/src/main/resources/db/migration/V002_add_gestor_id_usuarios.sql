-- Vínculo gestor ↔ equipe (Gestor de Reservas / Usuário Final)
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS gestor_id UUID NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT fk_usuarios_gestor
        FOREIGN KEY (gestor_id) REFERENCES usuarios (id);

CREATE INDEX IF NOT EXISTS idx_usuarios_gestor_id ON usuarios (gestor_id);
