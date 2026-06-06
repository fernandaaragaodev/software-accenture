-- Regras de disponibilidade independentes da sala
ALTER TABLE regras_disponibilidade ADD COLUMN IF NOT EXISTS nome VARCHAR(255);
UPDATE regras_disponibilidade SET nome = 'Regra padrão' WHERE nome IS NULL;
ALTER TABLE regras_disponibilidade ALTER COLUMN nome SET NOT NULL;
ALTER TABLE regras_disponibilidade ALTER COLUMN sala_id DROP NOT NULL;
