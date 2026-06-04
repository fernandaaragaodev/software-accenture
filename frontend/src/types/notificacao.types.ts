export type StatusNotificacao = 'PENDENTE' | 'ENVIADA' | 'FALHA';

export interface Notificacao {
  id: string;
  usuarioId: string;
  reservaId: string | null;
  tipo: string;
  assunto: string;
  mensagem: string;
  status: StatusNotificacao;
  tentativas: number;
  enviadoEm: string | null;
  createdAt: string;
}
