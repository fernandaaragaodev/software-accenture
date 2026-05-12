package com.accenture.officehub.officehub_api.dto;

import java.util.List;

public class ReservaResponse {

    private String id;
    private String titulo;
    private String nomeEspaco;
    private String local;
    private List<String> recursos;
    private String linkCancelamento;

    public ReservaResponse() {}

    public ReservaResponse(String id, String titulo, String nomeEspaco, String local,
                           List<String> recursos, String linkCancelamento) {
        this.id = id;
        this.titulo = titulo;
        this.nomeEspaco = nomeEspaco;
        this.local = local;
        this.recursos = recursos;
        this.linkCancelamento = linkCancelamento;
    }


    public static ReservaResponse of(String id, String titulo, String nomeEspaco,
                                     String local, List<String> recursos, String linkCancelamento) {
        return new ReservaResponse(id, titulo, nomeEspaco, local, recursos, linkCancelamento);
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getNomeEspaco() { return nomeEspaco; }
    public String getLocal() { return local; }
    public List<String> getRecursos() { return recursos; }
    public String getLinkCancelamento() { return linkCancelamento; }
}