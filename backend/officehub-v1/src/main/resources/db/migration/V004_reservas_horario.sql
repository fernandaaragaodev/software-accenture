-- Horário de início e fim nas reservas
ALTER TABLE reservas ADD COLUMN IF NOT EXISTS hora_inicio TIME;
ALTER TABLE reservas ADD COLUMN IF NOT EXISTS hora_fim TIME;
UPDATE reservas SET hora_inicio = '08:00', hora_fim = '18:00' WHERE hora_inicio IS NULL;
ALTER TABLE reservas ALTER COLUMN hora_inicio SET NOT NULL;
ALTER TABLE reservas ALTER COLUMN hora_fim SET NOT NULL;
ALTER TABLE reservas ADD CONSTRAINT chk_horario_reserva CHECK (hora_inicio < hora_fim);
