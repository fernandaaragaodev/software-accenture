-- Dimensões físicas da sala e coordenadas em pixels das posições geradas por IA
ALTER TABLE salas ADD COLUMN IF NOT EXISTS largura NUMERIC(10, 2);
ALTER TABLE salas ADD COLUMN IF NOT EXISTS altura NUMERIC(10, 2);

ALTER TABLE posicoes ADD COLUMN IF NOT EXISTS pixel_x NUMERIC(10, 2);
ALTER TABLE posicoes ADD COLUMN IF NOT EXISTS pixel_y NUMERIC(10, 2);
