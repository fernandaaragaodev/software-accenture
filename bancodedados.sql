--
-- PostgreSQL database dump
--

\restrict z0UKV85pOub7Vln5aigTb3mbye1kVpp5vtprVLOfXYAFTbR8iL6hddRhGZp5feF

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2026-06-25 21:43:31

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- TOC entry 5123 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 883 (class 1247 OID 18747)
-- Name: status_agente; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.status_agente AS ENUM (
    'PROCESSANDO',
    'SUCESSO',
    'FALHA'
);


ALTER TYPE public.status_agente OWNER TO postgres;

--
-- TOC entry 886 (class 1247 OID 18754)
-- Name: status_notificacao; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.status_notificacao AS ENUM (
    'FILA',
    'ENVIADA',
    'ERRO'
);


ALTER TYPE public.status_notificacao OWNER TO postgres;

--
-- TOC entry 889 (class 1247 OID 18762)
-- Name: status_reserva; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.status_reserva AS ENUM (
    'PENDENTE',
    'CONFIRMADA',
    'REJEITADA',
    'CANCELADA'
);


ALTER TYPE public.status_reserva OWNER TO postgres;

--
-- TOC entry 892 (class 1247 OID 18772)
-- Name: status_sala; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.status_sala AS ENUM (
    'ATIVA',
    'INATIVA',
    'MANUTENCAO',
    'PENDENTE_APROVACAO'
);


ALTER TYPE public.status_sala OWNER TO postgres;

--
-- TOC entry 252 (class 1255 OID 18779)
-- Name: prevent_audit_changes(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.prevent_audit_changes() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    RAISE EXCEPTION 'audit_log é somente leitura';
END;
$$;


ALTER FUNCTION public.prevent_audit_changes() OWNER TO postgres;

--
-- TOC entry 253 (class 1255 OID 18780)
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at_column() OWNER TO postgres;

--
-- TOC entry 254 (class 1255 OID 18781)
-- Name: validate_layout_sala(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.validate_layout_sala() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    layout_sala UUID;
BEGIN
    SELECT sala_id
    INTO layout_sala
    FROM layouts
    WHERE id = NEW.layout_id;

    IF layout_sala <> NEW.sala_id THEN
        RAISE EXCEPTION
        'Layout não pertence à sala informada';
    END IF;

    RETURN NEW;
END;
$$;


ALTER FUNCTION public.validate_layout_sala() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 218 (class 1259 OID 18782)
-- Name: agente_execucoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.agente_execucoes (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    tipo_agente character varying(255) NOT NULL,
    referencia_id uuid,
    status public.status_agente DEFAULT 'PROCESSANDO'::public.status_agente NOT NULL,
    payload_entrada jsonb,
    payload_saida jsonb,
    versao_modelo character varying(100),
    tempo_processamento_ms integer,
    tentativas integer DEFAULT 0,
    erro_mensagem text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.agente_execucoes OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 18792)
-- Name: audit_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_log (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    usuario_id uuid,
    acao character varying(50) NOT NULL,
    entidade character varying(100) NOT NULL,
    entidade_id uuid NOT NULL,
    dados_anteriores jsonb,
    dados_novos jsonb,
    ip_origem character varying(100),
    user_agent text,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.audit_log OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 25304)
-- Name: cargos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cargos (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.cargos OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 18799)
-- Name: equipe_gestores; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.equipe_gestores (
    equipe_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.equipe_gestores OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 18803)
-- Name: equipe_membros; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.equipe_membros (
    equipe_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.equipe_membros OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 18807)
-- Name: equipes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.equipes (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    descricao text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone
);


ALTER TABLE public.equipes OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 25320)
-- Name: especialidades; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.especialidades (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.especialidades OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 18815)
-- Name: excecoes_disponibilidade; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.excecoes_disponibilidade (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    sala_id uuid NOT NULL,
    data date NOT NULL,
    motivo character varying(255),
    created_by uuid,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.excecoes_disponibilidade OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 18820)
-- Name: horarios_disponibilidade; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.horarios_disponibilidade (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    regra_disponibilidade_id uuid NOT NULL,
    dia_semana integer NOT NULL,
    hora_abertura time without time zone NOT NULL,
    hora_fechamento time without time zone NOT NULL,
    CONSTRAINT chk_horario CHECK ((hora_abertura < hora_fechamento)),
    CONSTRAINT horarios_disponibilidade_dia_semana_check CHECK (((dia_semana >= 0) AND (dia_semana <= 6)))
);


ALTER TABLE public.horarios_disponibilidade OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 18826)
-- Name: layouts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.layouts (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    sala_id uuid NOT NULL,
    versao character varying(50),
    ativo boolean DEFAULT false,
    aprovado_por uuid,
    aprovado_em timestamp with time zone,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.layouts OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 18832)
-- Name: notificacoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notificacoes (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    usuario_id uuid NOT NULL,
    reserva_id uuid,
    tipo character varying(100),
    assunto character varying(255),
    mensagem text,
    status public.status_notificacao DEFAULT 'FILA'::public.status_notificacao NOT NULL,
    tentativas integer DEFAULT 0,
    enviado_em timestamp with time zone,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.notificacoes OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 18841)
-- Name: perfis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.perfis (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text
);


ALTER TABLE public.perfis OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 18847)
-- Name: posicao_equipamentos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.posicao_equipamentos (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    posicao_id uuid NOT NULL,
    tipo_equipamento_id uuid NOT NULL,
    quantidade integer DEFAULT 1 NOT NULL,
    observacao text,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT chk_quantidade_equipamentos CHECK ((quantidade > 0))
);


ALTER TABLE public.posicao_equipamentos OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 18856)
-- Name: posicoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.posicoes (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    sala_id uuid NOT NULL,
    layout_id uuid NOT NULL,
    identificador character varying(100) NOT NULL,
    tipo character varying(100),
    coord_x numeric,
    coord_y numeric,
    tipo_cadeira character varying(100),
    tipo_mesa character varying(100),
    status character varying(50),
    ajustado_manualmente boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone,
    pixel_y numeric(10,2),
    pixel_x numeric(10,2)
);


ALTER TABLE public.posicoes OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 18865)
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_tokens (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    usuario_id uuid NOT NULL,
    token_hash character varying(255) NOT NULL,
    expira_em timestamp with time zone NOT NULL,
    revogado_em timestamp with time zone,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.refresh_tokens OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 18870)
-- Name: regras_disponibilidade; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.regras_disponibilidade (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    sala_id uuid,
    antecedencia_minima_dias integer NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    nome character varying(255) NOT NULL
);


ALTER TABLE public.regras_disponibilidade OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 18876)
-- Name: reserva_pessoas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reserva_pessoas (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    reserva_id uuid NOT NULL,
    usuario_id uuid,
    nome_externo character varying(255),
    tipo_preferido_1 character varying(100),
    tipo_preferido_2 character varying(100),
    tipo_preferido_3 character varying(100),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT chk_pessoa_reserva CHECK (((usuario_id IS NOT NULL) OR (nome_externo IS NOT NULL)))
);


ALTER TABLE public.reserva_pessoas OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 18884)
-- Name: reserva_posicoes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reserva_posicoes (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    reserva_id uuid NOT NULL,
    reserva_pessoa_id uuid NOT NULL,
    posicao_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.reserva_posicoes OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 18889)
-- Name: reservas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reservas (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    sala_id uuid NOT NULL,
    solicitante_id uuid NOT NULL,
    data_reserva date NOT NULL,
    quantidade_pessoas integer NOT NULL,
    criterio_proximidade character varying(255),
    status public.status_reserva DEFAULT 'PENDENTE'::public.status_reserva NOT NULL,
    motivo_rejeicao text,
    motivo_cancelamento text,
    cancelado_por uuid,
    cancelado_em timestamp with time zone,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone,
    hora_inicio time without time zone NOT NULL,
    hora_fim time without time zone NOT NULL,
    CONSTRAINT chk_horario_reserva CHECK ((hora_inicio < hora_fim)),
    CONSTRAINT chk_quantidade_pessoas CHECK ((quantidade_pessoas > 0))
);


ALTER TABLE public.reservas OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 18899)
-- Name: salas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.salas (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    descricao text,
    andar integer,
    bloco character varying(100),
    capacidade_maxima integer,
    raio_proximidade numeric,
    status public.status_sala DEFAULT 'ATIVA'::public.status_sala NOT NULL,
    imagem_path character varying(500),
    created_by uuid,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone,
    largura numeric(10,2),
    altura numeric(10,2),
    CONSTRAINT chk_capacidade_maxima CHECK ((capacidade_maxima > 0))
);


ALTER TABLE public.salas OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 18909)
-- Name: tipos_equipamento; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tipos_equipamento (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    descricao text,
    ativo boolean DEFAULT true
);


ALTER TABLE public.tipos_equipamento OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 25331)
-- Name: usuario_especialidades; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario_especialidades (
    usuario_id uuid NOT NULL,
    especialidade_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.usuario_especialidades OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 18916)
-- Name: usuario_perfis; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario_perfis (
    usuario_id uuid NOT NULL,
    perfil_id uuid NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.usuario_perfis OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 18920)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarios (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    nome character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    ativo boolean DEFAULT true,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    deleted_at timestamp with time zone,
    cargo_id uuid
);


ALTER TABLE public.usuarios OWNER TO postgres;

--
-- TOC entry 5094 (class 0 OID 18782)
-- Dependencies: 218
-- Data for Name: agente_execucoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.agente_execucoes (id, tipo_agente, referencia_id, status, payload_entrada, payload_saida, versao_modelo, tempo_processamento_ms, tentativas, erro_mensagem, created_at, updated_at) FROM stdin;
\.


--
-- TOC entry 5095 (class 0 OID 18792)
-- Dependencies: 219
-- Data for Name: audit_log; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.audit_log (id, usuario_id, acao, entidade, entidade_id, dados_anteriores, dados_novos, ip_origem, user_agent, created_at) FROM stdin;
ab7270fd-370d-4220-9e2d-c35ff9dc2884	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	427150e7-50dd-47bb-83cb-0bf385685810	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:44:50.123868-03
8aecc38a-2ebd-46ea-9bb5-793003468a88	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	RegraDisponibilidade	9a4910ca-1631-4cee-81f6-00e394d5ead9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:44:50.243245-03
9cbf2210-989f-4991-8ab1-efe6561f123b	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	df11c959-7cf5-4a84-ac90-0efb38e489f5	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:45:46.72302-03
e6911845-190f-458c-9115-6f710b644943	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	8796799c-1d5a-4483-b5c5-fec90036e338	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:46:29.502987-03
fa3490d3-24b3-4c99-88b6-02cdeac2e8f9	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5c68d6b0-c008-4b61-8fa5-9626a901351a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:46:46.156438-03
a7e062d5-dda0-4441-82e6-e43a215f20a5	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	373130e2-8c50-4398-afa3-5d7d8dd3a133	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:46:58.007619-03
552cb03b-109c-4962-a6f6-37a59f123d88	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	74df9c52-7f9f-4974-b557-eb6bb7064082	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:47:11.265675-03
57947ae6-f246-4c5f-81ca-e40f94684057	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	f70fd309-95dd-4ed2-975a-13245521c635	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:47:45.610907-03
3df92fad-55de-4f58-b329-c4b3e6fb2fa0	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a07fdc14-e6f5-46cf-8f49-5f9f56715374	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:47:57.546209-03
d77b9a70-4476-42b5-99a7-b40216c5baea	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	753a914a-a409-460b-8f1e-80d40fb96823	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:48:13.539372-03
ef6646af-ea24-4150-8c12-57c868071d51	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	7b449b28-ee38-4565-b92e-274da6f2c186	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:48:30.447728-03
2eeaea0a-effb-42f0-88cf-c158140d1151	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	d7a1d088-59da-481c-a375-642ea3b351a6	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:48:38.562619-03
87d3cea8-e595-4a56-a4ed-95a934b181fb	f23d4188-63e7-40ad-8b62-21650ce86cb9	ATUALIZAR	Usuario	f72fdc3b-272b-48b3-aec0-ba4138f6bbd1	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:51:43.265793-03
b8594222-3d42-499a-8470-d086e79b626e	f23d4188-63e7-40ad-8b62-21650ce86cb9	ATUALIZAR	Usuario	413b0161-5abd-408b-9da4-cf00d4d1bb1a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:51:57.827305-03
d035de8f-06b7-460d-9778-87351084e11d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Equipe	15d58ea4-fc1e-4778-be9a-cfb8e02d7e2a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 13:53:45.671396-03
acc784f7-d9c4-4870-b51a-27d3e07e005b	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 14:14:46.794281-03
8ce37d76-ee3f-459b-87ae-efd1978d37de	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 14:15:03.599889-03
5af65280-c1b7-4885-b4c4-65920b6127e9	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Reserva	1b1bf82e-8032-4f00-90a1-e3778a915560	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 14:17:36.517474-03
8abfd014-b9a3-4320-99ec-cacd81395960	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 14:17:57.621057-03
e6b51f77-1469-428b-bc45-ded1649931f2	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 14:19:46.30376-03
47b8209c-49b1-49dd-885a-07d01c5d910c	f23d4188-63e7-40ad-8b62-21650ce86cb9	REMOVER_MEMBRO	Equipe	15d58ea4-fc1e-4778-be9a-cfb8e02d7e2a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 14:20:25.460514-03
43b899c2-ddc3-43ff-bfb4-bba6b8c43c01	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-12 18:09:40.503105-03
fcba338e-fb10-4796-96d5-1672c57ac46c	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-20 18:38:31.394333-03
c0065b88-114d-4ded-93ff-6112ef6b271d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Reserva	ef964812-1f63-4b50-b09c-ecccc5d7c293	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-20 18:41:46.090986-03
14b711c6-d9e5-4f60-98ad-3de1d39aa36b	f23d4188-63e7-40ad-8b62-21650ce86cb9	CANCELAR	Reserva	c8d65aa8-80b8-4bf5-92c3-50afd62196a5	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-20 18:41:55.28395-03
c95e2696-e09d-4d85-aa7b-ef0334864348	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Reserva	068e3a3d-165d-4906-89c8-a5e171ef0492	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-20 18:46:55.043273-03
98e08eda-761b-4e84-90e6-167f86679f02	f23d4188-63e7-40ad-8b62-21650ce86cb9	CANCELAR	Reserva	068e3a3d-165d-4906-89c8-a5e171ef0492	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-20 18:46:59.377185-03
3a84a4c6-aa47-403d-899e-c4112c0febc4	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Reserva	1fad092e-9db6-4e73-8e7c-fbfd59481897	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-20 18:47:22.822432-03
538b0ef6-c479-4604-8d44-34ceef790906	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 17:49:29.2974-03
af548b20-830e-477b-b559-683efe68f1fa	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 17:51:07.809903-03
fe1b41ac-79fd-4ff3-afd4-4c1d71aa9baa	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:01:10.065458-03
1d4b948b-40db-43ed-ad10-401eb56f2dc8	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:01:12.317011-03
08275f6d-a674-4660-92af-c5cdb2fa9a25	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	9521a213-f521-4ec4-8b94-72ca05f96a19	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
7f2dffe3-b135-44b1-8f50-46875969b7e3	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	016505b6-5195-46f9-b20a-b7cf6455bacb	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
6d29f515-45a8-46e0-9edb-8a3005f2ee5d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	efaa8541-72a5-4189-b437-3e797a75c633	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
d87575ac-dfe4-4cd7-ad51-ea8a66ab4893	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	29ea3711-ef32-4d79-86d2-10cbabe43ce1	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
4b258e3b-1350-4b52-9d2e-134d0a2900ed	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	2cab0926-02b9-4fd3-bc55-8cd651ee6b34	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
9ef95b67-c22f-4dad-961b-5b3f86198869	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5c514e5f-f8eb-4db3-8d3b-606c0b112a74	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
a1be8ff6-bc6d-41ce-b1e6-cf36aa0f3b9d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	94f3d98a-b47f-491d-9dc9-5cb70c6db081	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:06:05.893159-03
088eb2de-cbbd-4036-87df-ae2ae1de9a59	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	304d46db-1f59-401d-9aee-ef6d87744d13	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:08:43.567358-03
40ee0259-26ae-4a24-a239-6149175fa520	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6fe9667a-5ae2-4626-b49f-6a39cce3aadf	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:08:43.567358-03
e7c430f4-eba9-4f02-8cfa-5076271e88cc	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e7a28dd6-f676-4e0f-a7f1-8eaaeffdc628	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:08:43.567358-03
72ad306d-c29c-42a9-a09e-5594818f55c1	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	dd792725-1029-416d-acd3-0476056d4170	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:08:43.567358-03
5ac69cc0-02f5-4a1e-86f4-130d17970e55	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	3ab2caf9-3d12-4f33-b41a-de4d707cdba9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-22 18:08:43.567358-03
e66f9d63-bc83-4a8d-a463-41f3de0f408a	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 11:45:08.441513-03
34c986e1-e462-47c0-8f90-deb9666719cd	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	b1665a8e-c245-43d5-adf4-15613daad38f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
3e932d76-6a71-4da8-ac82-f2181f28966f	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5354caa5-a086-47da-9919-b6aef20e8cd1	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
1d0f640d-6066-4eef-bb53-dacf4f474b5f	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e25156f1-28c3-4431-aab6-b4f2f598f742	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
a170998a-844d-418a-a4f3-97c0419bd558	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	c67c57c2-5720-48df-a229-bd8a70a2dc30	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
debd4d98-7e0e-4a1b-9571-c8da299e102b	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	9298483c-8616-4e9b-b607-407747e696f0	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
0bb0f52d-fca9-4c55-950c-117ae0ee4d83	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	f7870b3a-d7be-49be-b3c7-3bee9089a5b6	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
9b3b6ccc-d636-4515-8879-907bf9bb2833	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	22ecdbb3-20ad-4bb1-8196-314197f2e5d5	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
c6a9eb2f-887d-490a-9c96-764ed466a2ba	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	df66d267-d6d2-47af-b496-14c67314fde6	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
94b3f4f6-6779-4382-9348-9e97e7313687	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5368ec12-a18c-455e-82ad-af78b113f6de	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
4bd4863a-7742-483a-83a9-e72aa946cdd7	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	17b44805-3240-4581-ac45-843009ea6d89	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
c6b09fcf-1c02-45ea-a67c-d892618b6896	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	7050e047-07bb-47e4-a583-0bbda683d258	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
59684890-ed85-4e54-8388-07256810c32c	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5218513a-feab-4778-8201-d6b5af70fcaf	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
67d37f72-a4cb-4f08-8e1f-445a43e05a4b	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	48076b50-9b32-467e-bd01-562e96cd263f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
3e21c0d7-b27c-4fbf-bbe8-ab2665c108af	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e830717c-af72-4c3f-bada-e0ccee911435	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
6aa921ef-84a8-4621-9b24-8dd762b7643c	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	0a4d6745-54d8-4e92-8d98-f9869251a3d0	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
5130721c-0e5e-4b94-95a1-60d68d558b56	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	f798476f-c412-425d-a2a1-d722de0d3e37	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
79919c2f-1521-4e31-bdd0-dce1f08c819e	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	7380a59c-356f-4f0d-9e5a-9f48cf661c5d	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-23 12:14:13.951082-03
a82ce386-e3b1-4bdc-be6c-84114b63aa47	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:47:49.212763-03
d9a17873-08e8-41f1-932f-3d626bc1e794	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:51:53.167413-03
ac86bd38-e588-4c23-875d-c1306214c027	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:52:13.303509-03
cd5c6c65-8102-49a5-8ab4-8ebc25c2e22a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	bb8afc8f-bf92-4259-8749-b9c694afb751	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
46f7ea1a-4f6d-43bc-a720-267920cf7361	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5b76a917-19ff-47b4-926b-8f25c8c37138	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
24d0bce7-1152-4934-8ca0-26345a17a3f5	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	9b7027bb-c93a-4e66-9f9a-b215195105f8	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
6b385200-c034-4917-b41d-33f53ba97169	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	0e27d26e-37ab-4f97-b1d4-77904ea3b185	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
282da95d-2783-4b2b-8a8e-1bc9340604f4	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	58480ba3-2579-4c91-b5de-5a44851219a1	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
36ea601e-e790-446c-b71d-d2317fa93399	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	0ece279b-0e19-4176-8d2b-9593b965b183	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
6ac00556-c0e9-49f7-9293-e3694e6439ae	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	c60bfeae-2639-409a-bff8-d48130921266	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
13aa174d-63ce-4c65-986a-d75a972172f6	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	4b7bc66d-4d18-42ae-9728-6bb6a8e5dae1	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
94d7d1c0-2e0a-423f-b9d7-b63ca5e8f78e	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	dd1b5959-66f3-40f8-b5f4-b6549462f80c	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
ff580f5d-833b-4482-af8f-ad5009ace317	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a4f5180e-8e58-400c-9f3e-4d7c0e2c4a57	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
30f7364a-832d-4930-a3d5-f3d5941452a4	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6ea168d6-989c-4acb-8aa8-41aeb5368198	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
838b4d9c-d451-4283-8470-976e60e899a9	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	bd41bcfa-744c-4709-ac6a-71c8781bc9a4	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 11:59:56.580261-03
27dda10a-70ea-43f1-a95d-c1d578953cb1	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	bc51005a-fe81-45f6-9d3c-8eedadcc8b9f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
322dd116-dafc-467e-9157-842cc7f10778	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	637d6f89-9fc2-4fba-addd-2f96fab15b34	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
5ae21b7b-fbe2-40ac-ac79-e47c3db5c8f8	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	06f75da2-418d-4078-992b-254ca0fd2cdf	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
febee061-ceab-4bff-8135-073c0b824217	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	95e301f3-f0ab-4e92-a008-794f8f40c813	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
aa6bc767-3bbc-47a4-830a-0c2fc545b112	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	97a451b4-1014-491a-b3fc-9f655776ff08	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
71daf304-e456-42da-9766-88212465129d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	1f0e20ee-9daf-4898-bb55-e99b26b1532b	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
d71f1bf7-7672-416b-9781-46782ac2c336	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	8f297ce9-b2c6-4c28-bc22-a9ed20c993be	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
6df0de3f-e0ed-4210-85a5-6eb84d572c5f	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5bcfb140-d31b-4589-8d1c-3e1395d67574	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
d4329dde-be13-4bf8-ad86-a19c1fbc90b1	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e1745bad-b197-4585-8a7f-f11b8f56df1f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
62276693-f14c-432a-979e-da09f74f3072	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	d256d3ce-6d85-4a67-9787-fdd1c86ef7bd	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
91a43a7e-dd57-493d-90e5-27519aa2b883	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	4896e072-b426-469f-b462-ccc509b89558	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
c130ae70-27ee-417b-8389-a7a9473e94b8	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	466dd3db-013a-4610-a522-e95b25d4f426	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
75fc0197-7e06-4462-b41f-ccb179a95662	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a0e7a3ba-8c59-49d8-bfb4-b1d9bf11f4a3	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
0a8ad6e6-b99c-40a7-9642-7e86f46957c6	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	691a4f22-d3d3-4ea2-9418-56ffae56f735	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
ebda04b6-7d2c-460b-a2cb-1e9d32311d14	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	25a68ceb-9ffb-4596-bb1f-2689bb0f922a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:04:10.136731-03
b148a05f-a385-4a99-a1fc-00597ff30a84	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	RegraDisponibilidade	414844d4-de97-4bb2-8e95-83cd5cdabc50	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:05:33.212025-03
9f65c4a7-43d0-4d0c-b0b3-8c44dce2e762	f23d4188-63e7-40ad-8b62-21650ce86cb9	ATRIBUIR	RegraDisponibilidade	414844d4-de97-4bb2-8e95-83cd5cdabc50	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:05:38.886422-03
5a2922bb-1152-4ccc-baf1-eee1cace629c	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	e122690a-8207-41a7-b856-d72aba5db253	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
39a29a36-190d-4349-b550-b4838e09c23c	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	2d68c2e6-4544-4740-8be0-7fcd35f28943	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
d8f616d0-3cd2-4a14-b141-b79617eb5556	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5b5a0bb2-4f20-470d-903d-bac9010cf4af	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
e0b7cadb-852a-46fd-80ee-0266bde7193a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	83a9ca0a-d274-4be2-a790-ef34732ea931	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
9d172075-e7fd-438f-924a-99c0bf3bcda8	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	4553d13b-5ee9-4080-a0cf-45f02d22cb15	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
f3d5777b-826d-4f53-add3-3aa08a877007	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	8b8a2d23-752c-4bc6-8751-fbb82e7c7710	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
02dbc16a-080a-439e-828a-545935fc09f9	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	3a379bce-590b-4b7b-a9c8-e1675003c20c	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
407eaec6-69bf-4491-8f3d-15d90a758fce	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	daa97166-23b1-4502-8571-5287a06a7104	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
eab66009-2593-4163-bedf-54d2e9ed5216	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	30607a0c-c3b3-46e9-b2ff-60e2821c0996	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
17197e7f-cae2-421c-8e1f-f6f74b2b3618	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	eedd55c9-fc40-4edc-a931-01034830d7c5	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
82232734-5d61-4ed4-a04f-27951805e329	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e509ee4c-d802-459c-ad98-a100206aa23d	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
568d3fcc-860b-4b05-b987-e1ee82259c1c	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	c847fab2-a982-4397-bdc0-ff0919f92ea7	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
9da5616a-0188-44e4-ba39-09cae3a6488d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	799a4d96-ad3f-4ce2-99b5-dd861f526de2	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
2c4f7002-d305-4408-8ed1-e4dc69113cde	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a2dcc336-3898-4dff-99cb-6fd19316d42e	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
d47d3a84-5241-4f6e-a2f9-76a86df4e89d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	7fb02051-24dc-450a-a764-c2c983a9de10	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
f4bfb1ee-c98d-4666-89c0-d0968c91aaae	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	139e1421-802d-420c-8f1b-bce7bedaa456	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
dd0bc746-1b6c-4558-9945-b4b48e6e1a63	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	19c98376-a0c2-43e7-9d3e-44bd55015e73	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
ada84f97-7028-4ae9-8935-78f747e0c5ea	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	041feea9-5591-4c27-94e5-d31314eacaf6	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
f65f8358-1f5e-4084-b99b-90b7b0a54093	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	ca13ca92-a14b-4d56-851b-ee6b02492eb4	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
4aa2e7d4-d128-4d5b-a186-a2b62f38504a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	ba8cd60d-b00f-4c57-a494-4b38e3e14955	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
cc1c6c6d-d680-43d0-9aa7-ce80435a180f	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	5ad112d9-940f-422c-83b6-44ce6492a532	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:19:17.65729-03
2687c817-129f-42b2-a655-01e656fa697f	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	RegraDisponibilidade	7829e6bc-15a9-47c1-98f0-2776b88557d9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:20:06.40929-03
d78f2364-5b04-4e14-a692-c730d365269a	f23d4188-63e7-40ad-8b62-21650ce86cb9	ATRIBUIR	RegraDisponibilidade	7829e6bc-15a9-47c1-98f0-2776b88557d9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:20:10.169037-03
eb2e4b46-4c48-4e64-959f-c40c02a46412	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	0ac953d4-e5dc-4994-99cc-f3fabd9cddb4	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
f895d92e-bc50-44e8-8315-3e7bee033bfb	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	aa850a8d-64b4-4bd8-93db-f53f528841f7	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
9856d837-2e97-439c-af10-4fc14aca89ba	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a047118f-bf43-4f6d-bc23-1a12255dcfee	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
460a132a-be2d-488a-be61-4d96a80d8a12	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e292a590-5c71-4c90-8314-a4f1938d8492	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
5ae57fa4-de1c-433d-a058-3eec642c628f	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	932457ef-a29e-46ec-93ae-1660fd9e9701	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
f8d50ac3-b131-4132-a4ea-e6a7483168db	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a9c4f17b-1b61-4e73-ac49-9cbbd2973bd6	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
86a0e458-4512-4c7c-80aa-31957deabbab	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	bf30576a-3e6e-4431-a50c-779a378b3029	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
08860e46-817a-4f9d-abed-eb46dd4cc894	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	41420748-9990-4d8e-9f30-0d53b05fc65f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
c6d984a7-49f3-4f78-b960-fa091c32315d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	19311747-d359-4580-ade9-6fbd832ce861	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
51bc6977-45c3-4580-bc83-c949cadcc358	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	79f4a2d4-35c7-4b91-9615-68777daf9269	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
db0577f4-2bce-4ac6-99c3-00313bfc0d3a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e5f91480-f1ef-4dc0-900c-18c7ffb6b940	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
de4ae584-9c4e-4de5-9018-8aeff4474dd9	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6ac51a7f-df31-4d58-9344-3aaf62c001b9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
b7c36c1c-bb9c-4f6a-9934-0f459a040e8d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	bcee4bed-4bc3-47b0-805a-9d80493efe15	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
9c220ef2-4543-42df-9f6a-a1093095f348	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6f093417-e996-44ba-b7bb-020ae77222fe	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
2aa052b7-3a70-48e9-809d-c7f4d6934b89	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6ca561e6-ee93-4a89-9901-4f7c28c4467e	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
b7d97604-46a7-43f3-8359-aebb06a94701	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	2ed6ff1f-8b12-4418-87a0-693d0e080251	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
1fa2679a-2936-4a0a-bd71-5c84bd2e0103	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	36f16792-2a1d-4f22-9a53-3d7ae0a76d8d	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
c611977b-9a27-45b9-bb33-6eaad18b94de	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	cdbba4e4-5271-4d6b-9406-d9619dc2e95c	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
35261993-ed4d-433d-bf94-db8594c4556a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	7e79a985-55ea-4e83-b2cc-2c532205fe27	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
bb0a2486-7c48-43bd-821f-3b9a79b83a67	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	1bfbf9ed-c5b8-458c-9500-f89c84f6b746	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
4507a0e7-81b7-4d48-affe-edefbc5d9b4d	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	704a64db-ac8e-43ad-aad9-555591e69b02	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:23:42.911245-03
e44f4154-5f0b-4d7f-9685-7ee4b2fdac67	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	RegraDisponibilidade	bf31a9b3-3fb5-4619-aa91-3b41242ea942	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:25:55.432683-03
0afd82d4-0da0-45e5-8073-117f5e833a80	f23d4188-63e7-40ad-8b62-21650ce86cb9	ATRIBUIR	RegraDisponibilidade	bf31a9b3-3fb5-4619-aa91-3b41242ea942	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:25:59.09394-03
f9977415-b5b2-4236-a148-d855f443d654	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:43:07.792087-03
5b92c12d-dade-4082-9247-d034e5a42f4a	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:46:08.1138-03
9c31e40c-09e3-4c4d-a42b-59b8a6284580	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:49:49.315849-03
4e76e86d-925f-42b7-83e8-a7dc7cc702b4	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	26c68a5e-7613-47d1-8ab0-f56d561d9a36	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
b5738d56-013e-494d-8b16-081352819549	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	47cc3ae2-0bec-44fb-8049-8b3c2635b33f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
23a3073c-22dc-4d3f-b65f-e24e753676da	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	0bc60d9b-94e3-408a-ae44-370dc9e7e7af	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
b1d623a8-1e25-4db7-8e85-db71a4f89478	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	80cf48bc-a617-4d0f-884a-a6452415fcb4	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
a1256cb8-86f2-4aa2-a744-13fe0e4b7653	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	dec97d8b-660c-4404-b238-db17b417073e	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
6c56e449-aa01-4f4d-bfb6-054990e540cd	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	9c7f09bc-da45-41e7-adb1-9d4a6e3d83f0	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
9429b22f-7291-4e61-95c8-a48bf9f86c86	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6da6f7b8-20c4-4906-aa1c-eec9de8978f5	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
bcfebb03-9d2a-42e5-8222-347bcfc140e8	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	20090915-58c8-4ac5-ac29-2f84a56a2028	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
524366ab-2463-4fc7-93ef-207573f540ba	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	9155ed4d-7ae1-4208-b437-a10391989d50	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
c9c60aef-240f-4bd5-8ac9-837161695cf6	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	1cdc6057-6f73-45fd-a3ce-b4d691d28855	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
3519b513-e0c1-4e75-97a4-822304518246	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	9519336f-6cfd-4fb6-a8d3-f32455fba54f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
fb349c0d-0e0e-4ae3-b37c-b90d5d5175ab	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	c30a9468-f89b-47eb-a506-a8d363213591	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
5cc662cb-2e62-4d9a-bdae-e06fa4fa4554	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e3d68709-86d9-4338-b1d5-be8693da7706	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
c7b075ff-1e07-4476-8936-9649a31112f6	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	9cfcac43-7622-4857-b2ec-649ee5d84b9d	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
a695fb54-689b-479d-af1c-ffcd32a82367	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	79e8b43b-272a-47b6-99a4-0a97bb49e541	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
c78fb1dd-2c72-4393-b249-3e655a0eda33	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	6f3f07a2-03c0-4e9f-93f8-979b0ad98b41	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
32b016ad-3270-4826-a587-5db730f8a1c4	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	e6bdfc4c-f779-4aa1-9cf0-88777c0fafb7	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
1fa3cef6-b992-4e0e-a73e-6532ba22059a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	4b450aac-1c07-43de-bc73-285c259725a0	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
cd39bcef-b021-4bf2-a21b-d7ce0b31c585	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	1d5784f0-208a-4665-bcc9-5cb484b6989a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
ff8204bc-0a15-4bad-82fd-05cd3242c4a5	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	84d2bac9-35ad-4a82-a26a-c78ce8daa62f	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
a930bd1f-da79-4d38-94fc-3f020ea61263	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	a50c99f3-205c-4415-a8a1-f26accda2ccb	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:52:31.550364-03
99952425-72bc-41a5-a8a7-abcbae97777c	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:55:05.043781-03
a08b0199-d10f-43d0-8ca2-34473a25bf72	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:55:30.280024-03
f2999728-ee65-4cd7-a712-1bda930c161d	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 12:57:55.194544-03
3b603d4c-905c-463a-a746-2ef42a2f6cc4	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-24 13:20:54.170335-03
f8fb37e8-6270-4528-966c-f888d88beec7	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 14:21:04.596191-03
3b687784-e1d1-421e-b0bf-216f5fcdb72c	f23d4188-63e7-40ad-8b62-21650ce86cb9	REMOVER_MEMBRO	Equipe	15d58ea4-fc1e-4778-be9a-cfb8e02d7e2a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 14:40:19.829852-03
4d7754b6-0aa7-45b9-8888-5764d180218c	f23d4188-63e7-40ad-8b62-21650ce86cb9	REMOVER_MEMBRO	Equipe	15d58ea4-fc1e-4778-be9a-cfb8e02d7e2a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 14:40:24.861555-03
550ed4ba-3f3f-4d8e-9cb2-5e54eb712c22	f23d4188-63e7-40ad-8b62-21650ce86cb9	DESFAZER	Equipe	15d58ea4-fc1e-4778-be9a-cfb8e02d7e2a	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 14:40:29.012611-03
fc574bc6-9636-4d2d-b723-f1753761fd3d	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGIN	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 19:05:02.113361-03
18d50cd4-88ed-4428-bf53-90b166609f46	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Sala	89295125-90e1-44a8-916c-1832244dab09	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 19:15:36.476806-03
b041022d-4415-4b4f-9ea9-69a7185be134	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	RegraDisponibilidade	47c6c1a8-4eb2-44cf-82c7-9f46a97fb7fc	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 19:15:36.554527-03
e3e8dca9-dbb6-4719-96fd-accbe7fc2e1a	f23d4188-63e7-40ad-8b62-21650ce86cb9	CRIAR	Posicao	8df2c381-9f22-447e-a0ac-2ff7849083fc	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 19:16:03.303975-03
8f4d53bd-c4ae-45f6-ade1-ce5d49a912ef	f23d4188-63e7-40ad-8b62-21650ce86cb9	LOGOUT	Usuario	f23d4188-63e7-40ad-8b62-21650ce86cb9	\N	\N	0:0:0:0:0:0:0:1	Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36	2026-06-25 19:17:04.998519-03
\.


--
-- TOC entry 5115 (class 0 OID 25304)
-- Dependencies: 239
-- Data for Name: cargos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cargos (id, nome, descricao, created_at) FROM stdin;
b327e7c0-d614-4697-a312-7c4e44afdb47	Analista	\N	2026-06-12 12:00:25.130863-03
2c69dfed-f13d-43e0-88af-883ba56d005c	Programador Back-End	\N	2026-06-12 12:00:25.130863-03
d04b445c-02fb-40b6-b3df-7e187f996f1e	Gerente	\N	2026-06-12 12:00:25.130863-03
a6e04409-01d6-463c-b35b-97c7e32cc87e	Web Design	\N	2026-06-12 12:00:25.130863-03
\.


--
-- TOC entry 5096 (class 0 OID 18799)
-- Dependencies: 220
-- Data for Name: equipe_gestores; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.equipe_gestores (equipe_id, usuario_id, created_at) FROM stdin;
\.


--
-- TOC entry 5097 (class 0 OID 18803)
-- Dependencies: 221
-- Data for Name: equipe_membros; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.equipe_membros (equipe_id, usuario_id, created_at) FROM stdin;
\.


--
-- TOC entry 5098 (class 0 OID 18807)
-- Dependencies: 222
-- Data for Name: equipes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.equipes (id, nome, descricao, created_at, updated_at, deleted_at) FROM stdin;
15d58ea4-fc1e-4778-be9a-cfb8e02d7e2a	Equipe 001	\N	2026-06-12 13:53:45.671396-03	2026-06-25 14:40:29.012611-03	2026-06-25 14:40:29.11248-03
\.


--
-- TOC entry 5116 (class 0 OID 25320)
-- Dependencies: 240
-- Data for Name: especialidades; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.especialidades (id, nome, descricao, created_at) FROM stdin;
b0f2a6cc-f45c-4759-9216-4b560d1841a1	Java	Desenvolvimento Java	2026-06-12 12:00:25.130863-03
a8f789ff-fc99-4bb2-b9cb-899310098add	Spring Boot	Framework Spring	2026-06-12 12:00:25.130863-03
31cd0e8d-39e3-4ba0-9902-1c19d2a067d7	PostgreSQL	Banco de dados PostgreSQL	2026-06-12 12:00:25.130863-03
088fe124-34ca-422a-9ce2-1bdb52987e50	React	Desenvolvimento Front-end	2026-06-12 12:00:25.130863-03
45fd9621-0655-4e9c-aa18-9b0d0bb0323c	DevOps	Infraestrutura e automação	2026-06-12 12:00:25.130863-03
\.


--
-- TOC entry 5099 (class 0 OID 18815)
-- Dependencies: 223
-- Data for Name: excecoes_disponibilidade; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.excecoes_disponibilidade (id, sala_id, data, motivo, created_by, created_at) FROM stdin;
\.


--
-- TOC entry 5100 (class 0 OID 18820)
-- Dependencies: 224
-- Data for Name: horarios_disponibilidade; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.horarios_disponibilidade (id, regra_disponibilidade_id, dia_semana, hora_abertura, hora_fechamento) FROM stdin;
\.


--
-- TOC entry 5101 (class 0 OID 18826)
-- Dependencies: 225
-- Data for Name: layouts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.layouts (id, sala_id, versao, ativo, aprovado_por, aprovado_em, created_at) FROM stdin;
\.


--
-- TOC entry 5102 (class 0 OID 18832)
-- Dependencies: 226
-- Data for Name: notificacoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notificacoes (id, usuario_id, reserva_id, tipo, assunto, mensagem, status, tentativas, enviado_em, created_at) FROM stdin;
\.


--
-- TOC entry 5103 (class 0 OID 18841)
-- Dependencies: 227
-- Data for Name: perfis; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.perfis (id, nome, descricao) FROM stdin;
b480da9f-ac76-4643-b74a-7f917d312788	ADMIN_SALA	Administração de salas, posições, layouts e tipos de equipamento
e5c23800-0da4-4a11-a0f1-7e0d84439970	GESTOR_RESERVAS	Gestão de reservas, relatórios e notificações
bd2078de-b389-49e3-9713-77e7c6f14d40	INTEGRADOR	Integração via API de disponibilidade e reservas
7b6956d3-b1a8-43b6-ba87-b1e05a71a99f	USUARIO_FINAL	Solicitação e cancelamento de reservas
\.


--
-- TOC entry 5104 (class 0 OID 18847)
-- Dependencies: 228
-- Data for Name: posicao_equipamentos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.posicao_equipamentos (id, posicao_id, tipo_equipamento_id, quantidade, observacao, created_at) FROM stdin;
\.


--
-- TOC entry 5105 (class 0 OID 18856)
-- Dependencies: 229
-- Data for Name: posicoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.posicoes (id, sala_id, layout_id, identificador, tipo, coord_x, coord_y, tipo_cadeira, tipo_mesa, status, ajustado_manualmente, created_at, updated_at, deleted_at, pixel_y, pixel_x) FROM stdin;
\.


--
-- TOC entry 5106 (class 0 OID 18865)
-- Dependencies: 230
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.refresh_tokens (id, usuario_id, token_hash, expira_em, revogado_em, created_at) FROM stdin;
243227d6-61e1-4d62-81e6-a7477ecd80a7	f23d4188-63e7-40ad-8b62-21650ce86cb9	4b998bc41f0e788911f3d50dae193436c58f5a5413e10b0de5ff3c0ec548834b	2026-07-02 19:05:02.415165-03	2026-06-25 19:17:05.00189-03	2026-06-25 19:05:02.113361-03
\.


--
-- TOC entry 5107 (class 0 OID 18870)
-- Dependencies: 231
-- Data for Name: regras_disponibilidade; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.regras_disponibilidade (id, sala_id, antecedencia_minima_dias, created_at, updated_at, nome) FROM stdin;
\.


--
-- TOC entry 5108 (class 0 OID 18876)
-- Dependencies: 232
-- Data for Name: reserva_pessoas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reserva_pessoas (id, reserva_id, usuario_id, nome_externo, tipo_preferido_1, tipo_preferido_2, tipo_preferido_3, created_at) FROM stdin;
\.


--
-- TOC entry 5109 (class 0 OID 18884)
-- Dependencies: 233
-- Data for Name: reserva_posicoes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reserva_posicoes (id, reserva_id, reserva_pessoa_id, posicao_id, created_at) FROM stdin;
\.


--
-- TOC entry 5110 (class 0 OID 18889)
-- Dependencies: 234
-- Data for Name: reservas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reservas (id, sala_id, solicitante_id, data_reserva, quantidade_pessoas, criterio_proximidade, status, motivo_rejeicao, motivo_cancelamento, cancelado_por, cancelado_em, created_at, updated_at, deleted_at, hora_inicio, hora_fim) FROM stdin;
\.


--
-- TOC entry 5111 (class 0 OID 18899)
-- Dependencies: 235
-- Data for Name: salas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.salas (id, nome, descricao, andar, bloco, capacidade_maxima, raio_proximidade, status, imagem_path, created_by, created_at, updated_at, deleted_at, largura, altura) FROM stdin;
\.


--
-- TOC entry 5112 (class 0 OID 18909)
-- Dependencies: 236
-- Data for Name: tipos_equipamento; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.tipos_equipamento (id, nome, descricao, ativo) FROM stdin;
d9737b41-acad-4ca7-9763-d49b533396db	Monitor	Monitor 27' para estação de trabalho	t
caf0df12-25c6-4aee-9436-4386b6411eaf	Projetor	Projetor LCD	t
bce48b69-f26c-4311-b851-844509ea8a88	Monitor menor	Monitor 21' para estação de trabalho	t
c3bbde0b-984f-432b-8f7b-fecce6f37e1c	Mesa digitalizadora	Mesa padrão estudio	t
0ffa6c1a-b3e1-4bad-a2cf-5a14db236126	CADEIRA	Cadeira detectada na estação de trabalho	t
7dd6b4ad-1a56-4565-901d-64b1df791986	NOTEBOOK	\N	t
9e2b9800-6ab7-4a9d-8b0e-ef5042614181	MESA_DIGITALIZADORA	Mesa digitalizadora detectada no ambiente	t
bca39bde-c512-4dc4-9e1a-a5ee2285d054	IMPRESSORA	Impressora detectada no ambiente	t
\.


--
-- TOC entry 5117 (class 0 OID 25331)
-- Dependencies: 241
-- Data for Name: usuario_especialidades; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario_especialidades (usuario_id, especialidade_id, created_at) FROM stdin;
\.


--
-- TOC entry 5113 (class 0 OID 18916)
-- Dependencies: 237
-- Data for Name: usuario_perfis; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario_perfis (usuario_id, perfil_id, created_at) FROM stdin;
f23d4188-63e7-40ad-8b62-21650ce86cb9	7b6956d3-b1a8-43b6-ba87-b1e05a71a99f	2026-06-04 16:16:38.332141-03
f23d4188-63e7-40ad-8b62-21650ce86cb9	b480da9f-ac76-4643-b74a-7f917d312788	2026-06-04 16:17:18.255685-03
\.


--
-- TOC entry 5114 (class 0 OID 18920)
-- Dependencies: 238
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuarios (id, nome, email, senha_hash, ativo, created_at, updated_at, deleted_at, cargo_id) FROM stdin;
f23d4188-63e7-40ad-8b62-21650ce86cb9	Administrador	admin@sgsp.com	$2a$10$Dipfxxa4oq6uOFrfqlqaIeXkpr1lxgIOaB3vou842GWDsIBMx5nmS	t	2026-06-04 16:16:38.332141-03	2026-06-04 16:16:38.332141-03	\N	\N
\.


--
-- TOC entry 4825 (class 2606 OID 18930)
-- Name: agente_execucoes agente_execucoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.agente_execucoes
    ADD CONSTRAINT agente_execucoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4828 (class 2606 OID 18932)
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- TOC entry 4898 (class 2606 OID 25314)
-- Name: cargos cargos_nome_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cargos
    ADD CONSTRAINT cargos_nome_key UNIQUE (nome);


--
-- TOC entry 4900 (class 2606 OID 25312)
-- Name: cargos cargos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cargos
    ADD CONSTRAINT cargos_pkey PRIMARY KEY (id);


--
-- TOC entry 4832 (class 2606 OID 18934)
-- Name: equipe_gestores equipe_gestores_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipe_gestores
    ADD CONSTRAINT equipe_gestores_pkey PRIMARY KEY (equipe_id, usuario_id);


--
-- TOC entry 4835 (class 2606 OID 18936)
-- Name: equipe_membros equipe_membros_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipe_membros
    ADD CONSTRAINT equipe_membros_pkey PRIMARY KEY (equipe_id, usuario_id);


--
-- TOC entry 4838 (class 2606 OID 18938)
-- Name: equipes equipes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipes
    ADD CONSTRAINT equipes_pkey PRIMARY KEY (id);


--
-- TOC entry 4902 (class 2606 OID 25330)
-- Name: especialidades especialidades_nome_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.especialidades
    ADD CONSTRAINT especialidades_nome_key UNIQUE (nome);


--
-- TOC entry 4904 (class 2606 OID 25328)
-- Name: especialidades especialidades_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.especialidades
    ADD CONSTRAINT especialidades_pkey PRIMARY KEY (id);


--
-- TOC entry 4840 (class 2606 OID 18940)
-- Name: excecoes_disponibilidade excecoes_disponibilidade_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.excecoes_disponibilidade
    ADD CONSTRAINT excecoes_disponibilidade_pkey PRIMARY KEY (id);


--
-- TOC entry 4844 (class 2606 OID 18942)
-- Name: horarios_disponibilidade horarios_disponibilidade_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.horarios_disponibilidade
    ADD CONSTRAINT horarios_disponibilidade_pkey PRIMARY KEY (id);


--
-- TOC entry 4846 (class 2606 OID 18944)
-- Name: layouts layouts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.layouts
    ADD CONSTRAINT layouts_pkey PRIMARY KEY (id);


--
-- TOC entry 4850 (class 2606 OID 18946)
-- Name: notificacoes notificacoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificacoes
    ADD CONSTRAINT notificacoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4852 (class 2606 OID 18948)
-- Name: perfis perfis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfis
    ADD CONSTRAINT perfis_pkey PRIMARY KEY (id);


--
-- TOC entry 4854 (class 2606 OID 18950)
-- Name: posicao_equipamentos posicao_equipamentos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicao_equipamentos
    ADD CONSTRAINT posicao_equipamentos_pkey PRIMARY KEY (id);


--
-- TOC entry 4860 (class 2606 OID 18952)
-- Name: posicoes posicoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicoes
    ADD CONSTRAINT posicoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4866 (class 2606 OID 18954)
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- TOC entry 4868 (class 2606 OID 18956)
-- Name: refresh_tokens refresh_tokens_token_hash_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_token_hash_key UNIQUE (token_hash);


--
-- TOC entry 4870 (class 2606 OID 18958)
-- Name: regras_disponibilidade regras_disponibilidade_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regras_disponibilidade
    ADD CONSTRAINT regras_disponibilidade_pkey PRIMARY KEY (id);


--
-- TOC entry 4872 (class 2606 OID 18960)
-- Name: regras_disponibilidade regras_disponibilidade_sala_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regras_disponibilidade
    ADD CONSTRAINT regras_disponibilidade_sala_id_key UNIQUE (sala_id);


--
-- TOC entry 4874 (class 2606 OID 18962)
-- Name: reserva_pessoas reserva_pessoas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_pessoas
    ADD CONSTRAINT reserva_pessoas_pkey PRIMARY KEY (id);


--
-- TOC entry 4877 (class 2606 OID 18964)
-- Name: reserva_posicoes reserva_posicoes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_posicoes
    ADD CONSTRAINT reserva_posicoes_pkey PRIMARY KEY (id);


--
-- TOC entry 4884 (class 2606 OID 18966)
-- Name: reservas reservas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_pkey PRIMARY KEY (id);


--
-- TOC entry 4887 (class 2606 OID 18968)
-- Name: salas salas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.salas
    ADD CONSTRAINT salas_pkey PRIMARY KEY (id);


--
-- TOC entry 4889 (class 2606 OID 18970)
-- Name: tipos_equipamento tipos_equipamento_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tipos_equipamento
    ADD CONSTRAINT tipos_equipamento_pkey PRIMARY KEY (id);


--
-- TOC entry 4842 (class 2606 OID 18972)
-- Name: excecoes_disponibilidade uq_excecao_sala_data; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.excecoes_disponibilidade
    ADD CONSTRAINT uq_excecao_sala_data UNIQUE (sala_id, data);


--
-- TOC entry 4856 (class 2606 OID 18974)
-- Name: posicao_equipamentos uq_posicao_equipamento; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicao_equipamentos
    ADD CONSTRAINT uq_posicao_equipamento UNIQUE (posicao_id, tipo_equipamento_id);


--
-- TOC entry 4862 (class 2606 OID 18976)
-- Name: posicoes uq_posicao_sala; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicoes
    ADD CONSTRAINT uq_posicao_sala UNIQUE (sala_id, identificador);


--
-- TOC entry 4879 (class 2606 OID 18978)
-- Name: reserva_posicoes uq_reserva_pessoa; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_posicoes
    ADD CONSTRAINT uq_reserva_pessoa UNIQUE (reserva_pessoa_id);


--
-- TOC entry 4906 (class 2606 OID 25336)
-- Name: usuario_especialidades usuario_especialidades_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_especialidades
    ADD CONSTRAINT usuario_especialidades_pkey PRIMARY KEY (usuario_id, especialidade_id);


--
-- TOC entry 4891 (class 2606 OID 18980)
-- Name: usuario_perfis usuario_perfis_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfis
    ADD CONSTRAINT usuario_perfis_pkey PRIMARY KEY (usuario_id, perfil_id);


--
-- TOC entry 4894 (class 2606 OID 18982)
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- TOC entry 4896 (class 2606 OID 18984)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- TOC entry 4826 (class 1259 OID 18985)
-- Name: idx_agente_exec_ref_tipo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_agente_exec_ref_tipo ON public.agente_execucoes USING btree (referencia_id, tipo_agente);


--
-- TOC entry 4829 (class 1259 OID 18986)
-- Name: idx_audit_log_entidade_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_log_entidade_id ON public.audit_log USING btree (entidade, entidade_id);


--
-- TOC entry 4830 (class 1259 OID 18987)
-- Name: idx_audit_log_usuario_data; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_audit_log_usuario_data ON public.audit_log USING btree (usuario_id, created_at);


--
-- TOC entry 4833 (class 1259 OID 18988)
-- Name: idx_equipe_gestores_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_equipe_gestores_usuario ON public.equipe_gestores USING btree (usuario_id);


--
-- TOC entry 4836 (class 1259 OID 18989)
-- Name: idx_equipe_membros_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_equipe_membros_usuario ON public.equipe_membros USING btree (usuario_id);


--
-- TOC entry 4848 (class 1259 OID 18990)
-- Name: idx_notificacoes_usuario_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notificacoes_usuario_status ON public.notificacoes USING btree (usuario_id, status);


--
-- TOC entry 4857 (class 1259 OID 18991)
-- Name: idx_posicao_layout; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_posicao_layout ON public.posicoes USING btree (layout_id);


--
-- TOC entry 4858 (class 1259 OID 18992)
-- Name: idx_posicoes_sala_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_posicoes_sala_status ON public.posicoes USING btree (sala_id, status);


--
-- TOC entry 4863 (class 1259 OID 18993)
-- Name: idx_refresh_tokens_expira; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_refresh_tokens_expira ON public.refresh_tokens USING btree (expira_em);


--
-- TOC entry 4864 (class 1259 OID 18994)
-- Name: idx_refresh_tokens_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_refresh_tokens_usuario ON public.refresh_tokens USING btree (usuario_id);


--
-- TOC entry 4880 (class 1259 OID 18995)
-- Name: idx_reserva_data; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_reserva_data ON public.reservas USING btree (data_reserva);


--
-- TOC entry 4875 (class 1259 OID 18996)
-- Name: idx_reserva_posicao; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_reserva_posicao ON public.reserva_posicoes USING btree (posicao_id);


--
-- TOC entry 4881 (class 1259 OID 18997)
-- Name: idx_reservas_sala_data_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_reservas_sala_data_status ON public.reservas USING btree (sala_id, data_reserva, status);


--
-- TOC entry 4882 (class 1259 OID 18998)
-- Name: idx_reservas_solicitante; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_reservas_solicitante ON public.reservas USING btree (solicitante_id);


--
-- TOC entry 4885 (class 1259 OID 18999)
-- Name: idx_salas_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_salas_status ON public.salas USING btree (status);


--
-- TOC entry 4892 (class 1259 OID 19000)
-- Name: idx_usuarios_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX idx_usuarios_email ON public.usuarios USING btree (email);


--
-- TOC entry 4847 (class 1259 OID 19001)
-- Name: uq_layout_ativo_por_sala; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uq_layout_ativo_por_sala ON public.layouts USING btree (sala_id) WHERE (ativo = true);


--
-- TOC entry 4939 (class 2620 OID 19002)
-- Name: agente_execucoes trg_agente_execucoes_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_agente_execucoes_updated_at BEFORE UPDATE ON public.agente_execucoes FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4940 (class 2620 OID 19003)
-- Name: audit_log trg_audit_no_delete; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_audit_no_delete BEFORE DELETE ON public.audit_log FOR EACH ROW EXECUTE FUNCTION public.prevent_audit_changes();


--
-- TOC entry 4941 (class 2620 OID 19004)
-- Name: audit_log trg_audit_no_update; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_audit_no_update BEFORE UPDATE ON public.audit_log FOR EACH ROW EXECUTE FUNCTION public.prevent_audit_changes();


--
-- TOC entry 4942 (class 2620 OID 19005)
-- Name: equipes trg_equipes_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_equipes_updated_at BEFORE UPDATE ON public.equipes FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4943 (class 2620 OID 19006)
-- Name: posicoes trg_posicoes_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_posicoes_updated_at BEFORE UPDATE ON public.posicoes FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4945 (class 2620 OID 19007)
-- Name: regras_disponibilidade trg_regras_disponibilidade_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_regras_disponibilidade_updated_at BEFORE UPDATE ON public.regras_disponibilidade FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4946 (class 2620 OID 19008)
-- Name: reservas trg_reservas_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_reservas_updated_at BEFORE UPDATE ON public.reservas FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4947 (class 2620 OID 19009)
-- Name: salas trg_salas_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_salas_updated_at BEFORE UPDATE ON public.salas FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4948 (class 2620 OID 19010)
-- Name: usuarios trg_usuarios_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_usuarios_updated_at BEFORE UPDATE ON public.usuarios FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- TOC entry 4944 (class 2620 OID 19011)
-- Name: posicoes trg_validate_layout_sala; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_validate_layout_sala BEFORE INSERT OR UPDATE ON public.posicoes FOR EACH ROW EXECUTE FUNCTION public.validate_layout_sala();


--
-- TOC entry 4907 (class 2606 OID 19012)
-- Name: audit_log audit_log_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- TOC entry 4908 (class 2606 OID 19017)
-- Name: equipe_gestores equipe_gestores_equipe_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipe_gestores
    ADD CONSTRAINT equipe_gestores_equipe_id_fkey FOREIGN KEY (equipe_id) REFERENCES public.equipes(id) ON DELETE CASCADE;


--
-- TOC entry 4909 (class 2606 OID 19022)
-- Name: equipe_gestores equipe_gestores_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipe_gestores
    ADD CONSTRAINT equipe_gestores_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4910 (class 2606 OID 19027)
-- Name: equipe_membros equipe_membros_equipe_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipe_membros
    ADD CONSTRAINT equipe_membros_equipe_id_fkey FOREIGN KEY (equipe_id) REFERENCES public.equipes(id) ON DELETE CASCADE;


--
-- TOC entry 4911 (class 2606 OID 19032)
-- Name: equipe_membros equipe_membros_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.equipe_membros
    ADD CONSTRAINT equipe_membros_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4912 (class 2606 OID 19037)
-- Name: excecoes_disponibilidade excecoes_disponibilidade_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.excecoes_disponibilidade
    ADD CONSTRAINT excecoes_disponibilidade_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.usuarios(id);


--
-- TOC entry 4913 (class 2606 OID 19042)
-- Name: excecoes_disponibilidade excecoes_disponibilidade_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.excecoes_disponibilidade
    ADD CONSTRAINT excecoes_disponibilidade_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4937 (class 2606 OID 25342)
-- Name: usuario_especialidades fk_ue_especialidade; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_especialidades
    ADD CONSTRAINT fk_ue_especialidade FOREIGN KEY (especialidade_id) REFERENCES public.especialidades(id) ON DELETE CASCADE;


--
-- TOC entry 4938 (class 2606 OID 25337)
-- Name: usuario_especialidades fk_ue_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_especialidades
    ADD CONSTRAINT fk_ue_usuario FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- TOC entry 4936 (class 2606 OID 25315)
-- Name: usuarios fk_usuario_cargo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT fk_usuario_cargo FOREIGN KEY (cargo_id) REFERENCES public.cargos(id) ON DELETE SET NULL;


--
-- TOC entry 4914 (class 2606 OID 19047)
-- Name: horarios_disponibilidade horarios_disponibilidade_regra_disponibilidade_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.horarios_disponibilidade
    ADD CONSTRAINT horarios_disponibilidade_regra_disponibilidade_id_fkey FOREIGN KEY (regra_disponibilidade_id) REFERENCES public.regras_disponibilidade(id);


--
-- TOC entry 4915 (class 2606 OID 19052)
-- Name: layouts layouts_aprovado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.layouts
    ADD CONSTRAINT layouts_aprovado_por_fkey FOREIGN KEY (aprovado_por) REFERENCES public.usuarios(id);


--
-- TOC entry 4916 (class 2606 OID 19057)
-- Name: layouts layouts_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.layouts
    ADD CONSTRAINT layouts_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4917 (class 2606 OID 19062)
-- Name: notificacoes notificacoes_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificacoes
    ADD CONSTRAINT notificacoes_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id);


--
-- TOC entry 4918 (class 2606 OID 19067)
-- Name: notificacoes notificacoes_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notificacoes
    ADD CONSTRAINT notificacoes_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- TOC entry 4919 (class 2606 OID 19072)
-- Name: posicao_equipamentos posicao_equipamentos_posicao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicao_equipamentos
    ADD CONSTRAINT posicao_equipamentos_posicao_id_fkey FOREIGN KEY (posicao_id) REFERENCES public.posicoes(id);


--
-- TOC entry 4920 (class 2606 OID 19077)
-- Name: posicao_equipamentos posicao_equipamentos_tipo_equipamento_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicao_equipamentos
    ADD CONSTRAINT posicao_equipamentos_tipo_equipamento_id_fkey FOREIGN KEY (tipo_equipamento_id) REFERENCES public.tipos_equipamento(id);


--
-- TOC entry 4921 (class 2606 OID 19082)
-- Name: posicoes posicoes_layout_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicoes
    ADD CONSTRAINT posicoes_layout_id_fkey FOREIGN KEY (layout_id) REFERENCES public.layouts(id);


--
-- TOC entry 4922 (class 2606 OID 19087)
-- Name: posicoes posicoes_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.posicoes
    ADD CONSTRAINT posicoes_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4923 (class 2606 OID 19092)
-- Name: refresh_tokens refresh_tokens_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- TOC entry 4924 (class 2606 OID 19097)
-- Name: regras_disponibilidade regras_disponibilidade_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.regras_disponibilidade
    ADD CONSTRAINT regras_disponibilidade_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4925 (class 2606 OID 19102)
-- Name: reserva_pessoas reserva_pessoas_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_pessoas
    ADD CONSTRAINT reserva_pessoas_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id);


--
-- TOC entry 4926 (class 2606 OID 19107)
-- Name: reserva_pessoas reserva_pessoas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_pessoas
    ADD CONSTRAINT reserva_pessoas_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- TOC entry 4927 (class 2606 OID 19112)
-- Name: reserva_posicoes reserva_posicoes_posicao_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_posicoes
    ADD CONSTRAINT reserva_posicoes_posicao_id_fkey FOREIGN KEY (posicao_id) REFERENCES public.posicoes(id);


--
-- TOC entry 4928 (class 2606 OID 19117)
-- Name: reserva_posicoes reserva_posicoes_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_posicoes
    ADD CONSTRAINT reserva_posicoes_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id);


--
-- TOC entry 4929 (class 2606 OID 19122)
-- Name: reserva_posicoes reserva_posicoes_reserva_pessoa_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reserva_posicoes
    ADD CONSTRAINT reserva_posicoes_reserva_pessoa_id_fkey FOREIGN KEY (reserva_pessoa_id) REFERENCES public.reserva_pessoas(id);


--
-- TOC entry 4930 (class 2606 OID 19127)
-- Name: reservas reservas_cancelado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_cancelado_por_fkey FOREIGN KEY (cancelado_por) REFERENCES public.usuarios(id);


--
-- TOC entry 4931 (class 2606 OID 19132)
-- Name: reservas reservas_sala_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_sala_id_fkey FOREIGN KEY (sala_id) REFERENCES public.salas(id);


--
-- TOC entry 4932 (class 2606 OID 19137)
-- Name: reservas reservas_solicitante_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_solicitante_id_fkey FOREIGN KEY (solicitante_id) REFERENCES public.usuarios(id);


--
-- TOC entry 4933 (class 2606 OID 19142)
-- Name: salas salas_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.salas
    ADD CONSTRAINT salas_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.usuarios(id);


--
-- TOC entry 4934 (class 2606 OID 19147)
-- Name: usuario_perfis usuario_perfis_perfil_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfis
    ADD CONSTRAINT usuario_perfis_perfil_id_fkey FOREIGN KEY (perfil_id) REFERENCES public.perfis(id);


--
-- TOC entry 4935 (class 2606 OID 19152)
-- Name: usuario_perfis usuario_perfis_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfis
    ADD CONSTRAINT usuario_perfis_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


-- Completed on 2026-06-25 21:43:31

--
-- PostgreSQL database dump complete
--

\unrestrict z0UKV85pOub7Vln5aigTb3mbye1kVpp5vtprVLOfXYAFTbR8iL6hddRhGZp5feF

