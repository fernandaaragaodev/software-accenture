import { useEffect, useState } from 'react';
import { regrasDisponibilidadeApi } from '../../api/regras-disponibilidade';
import { ApiException } from '../../api/client';
import { DIAS_SEMANA, toApiTime } from '../../utils/salas';
import type { RegraDisponibilidadeResponse } from '../../types';

export interface HorarioForm {
  diaSemana: number;
  ativo: boolean;
  horaAbertura: string;
  horaFechamento: string;
}

export interface DisponibilidadeFormData {
  modo: 'nova' | 'existente';
  regraId: string;
  nomeRegra: string;
  antecedenciaMinimaDias: string;
  limiteReservasFuturas: string;
  horarios: HorarioForm[];
}

export function horariosPadrao(): HorarioForm[] {
  return DIAS_SEMANA.map((_, index) => ({
    diaSemana: index,
    ativo: index < 5,
    horaAbertura: '08:00',
    horaFechamento: '18:00',
  }));
}

export const DEFAULT_DISPONIBILIDADE: DisponibilidadeFormData = {
  modo: 'nova',
  regraId: '',
  nomeRegra: '',
  antecedenciaMinimaDias: '1',
  limiteReservasFuturas: '30',
  horarios: horariosPadrao(),
};

interface DisponibilidadeStepFormProps {
  data: DisponibilidadeFormData;
  onChange: (data: DisponibilidadeFormData) => void;
  errors: Record<string, string>;
}

export function DisponibilidadeStepForm({ data, onChange, errors }: DisponibilidadeStepFormProps) {
  const [regrasExistentes, setRegrasExistentes] = useState<RegraDisponibilidadeResponse[]>([]);

  useEffect(() => {
    regrasDisponibilidadeApi
      .listar()
      .then((regras) => setRegrasExistentes(regras.filter((r) => !r.salaId)))
      .catch((err) => {
        if (err instanceof ApiException) console.warn(err.message);
      });
  }, []);

  function updateHorario(index: number, patch: Partial<HorarioForm>) {
    const horarios = data.horarios.map((h, i) => (i === index ? { ...h, ...patch } : h));
    onChange({ ...data, horarios });
  }

  return (
    <div className="wizard-step-content">
      <div className="form-section">
        <h3>Modo de configuração</h3>
        <div className="radio-group">
          <label className="radio-option">
            <input
              type="radio"
              name="modo-disponibilidade"
              checked={data.modo === 'nova'}
              onChange={() => onChange({ ...data, modo: 'nova' })}
            />
            Criar nova regra de disponibilidade
          </label>
          <label className="radio-option">
            <input
              type="radio"
              name="modo-disponibilidade"
              checked={data.modo === 'existente'}
              onChange={() => onChange({ ...data, modo: 'existente' })}
            />
            Selecionar regra existente
          </label>
        </div>
      </div>

      {data.modo === 'existente' ? (
        <div className="form-grid">
          <label className="full-width">
            Regra de disponibilidade
            <select
              value={data.regraId}
              onChange={(e) => onChange({ ...data, regraId: e.target.value })}
            >
              <option value="">Selecione uma regra...</option>
              {regrasExistentes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.nome} ({r.horarios.length} horário(s))
                </option>
              ))}
            </select>
            {errors.regraId && <span className="field-error">{errors.regraId}</span>}
          </label>
          {data.regraId && (
            <div className="full-width">
              {(() => {
                const regra = regrasExistentes.find((r) => r.id === data.regraId);
                if (!regra) return null;
                return (
                  <div className="info-box">
                    <p>
                      Antecedência mínima: <strong>{regra.antecedenciaMinimaDias} dia(s)</strong>
                    </p>
                    <ul className="simple-list">
                      {regra.horarios.map((h) => (
                        <li key={h.id ?? h.diaSemana}>
                          {DIAS_SEMANA[h.diaSemana]}: {h.horaAbertura.slice(0, 5)} –{' '}
                          {h.horaFechamento.slice(0, 5)}
                        </li>
                      ))}
                    </ul>
                  </div>
                );
              })()}
            </div>
          )}
        </div>
      ) : (
        <>
          <div className="form-grid">
            <label>
              Nome da regra
              <input
                value={data.nomeRegra}
                onChange={(e) => onChange({ ...data, nomeRegra: e.target.value })}
                placeholder="Ex: Horário comercial"
              />
            </label>
            <label>
              Antecedência mínima de reserva (dias) *
              <input
                type="number"
                min={0}
                value={data.antecedenciaMinimaDias}
                onChange={(e) => onChange({ ...data, antecedenciaMinimaDias: e.target.value })}
              />
              {errors.antecedenciaMinimaDias && (
                <span className="field-error">{errors.antecedenciaMinimaDias}</span>
              )}
            </label>
            <label>
              Limite de reservas futuras (dias)
              <input
                type="number"
                min={1}
                value={data.limiteReservasFuturas}
                onChange={(e) => onChange({ ...data, limiteReservasFuturas: e.target.value })}
              />
              <small className="muted">Referência para planejamento (não persistido na API atual)</small>
            </label>
          </div>

          <div className="form-section mt-md">
            <h3>Dias e horários disponíveis</h3>
            {errors.horarios && <span className="field-error block">{errors.horarios}</span>}
            <div className="horarios-grid">
              {data.horarios.map((h, index) => (
                <div key={h.diaSemana} className={`horario-row ${h.ativo ? 'ativo' : ''}`}>
                  <label className="horario-check">
                    <input
                      type="checkbox"
                      checked={h.ativo}
                      onChange={(e) => updateHorario(index, { ativo: e.target.checked })}
                    />
                    {DIAS_SEMANA[h.diaSemana]}
                  </label>
                  <input
                    type="time"
                    value={h.horaAbertura}
                    disabled={!h.ativo}
                    onChange={(e) => updateHorario(index, { horaAbertura: e.target.value })}
                  />
                  <span className="muted">até</span>
                  <input
                    type="time"
                    value={h.horaFechamento}
                    disabled={!h.ativo}
                    onChange={(e) => updateHorario(index, { horaFechamento: e.target.value })}
                  />
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}

export function validateDisponibilidade(data: DisponibilidadeFormData): Record<string, string> {
  const errors: Record<string, string> = {};
  if (data.modo === 'existente') {
    if (!data.regraId) errors.regraId = 'Selecione uma regra existente';
    return errors;
  }
  const ant = Number(data.antecedenciaMinimaDias);
  if (Number.isNaN(ant) || ant < 0) {
    errors.antecedenciaMinimaDias = 'Informe um valor válido';
  }
  const ativos = data.horarios.filter((h) => h.ativo);
  if (ativos.length === 0) {
    errors.horarios = 'Selecione ao menos um dia da semana';
  }
  return errors;
}

export function buildDisponibilidadePayload(data: DisponibilidadeFormData) {
  if (data.modo === 'existente' && data.regraId) {
    return { tipo: 'existente' as const, regraId: data.regraId };
  }
  return {
    tipo: 'nova' as const,
    nome: data.nomeRegra || undefined,
    antecedenciaMinimaDias: Number(data.antecedenciaMinimaDias),
    horarios: data.horarios
      .filter((h) => h.ativo)
      .map((h) => ({
        diaSemana: h.diaSemana,
        horaAbertura: toApiTime(h.horaAbertura),
        horaFechamento: toApiTime(h.horaFechamento),
      })),
  };
}
