package com.accenture.officehub.officehub_api.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String titulo;
    private String nomeEspaco;
    private String local;

    @ElementCollection
    private List<String> recursos;

    private String linkCancelamento;

    public Reserva() {}

    public Reserva(String id, String titulo, String nomeEspaco, String local,
                   List<String> recursos, String linkCancelamento) {
        this.id = id;
        this.titulo = titulo;
        this.nomeEspaco = nomeEspaco;
        this.local = local;
        this.recursos = recursos;
        this.linkCancelamento = linkCancelamento;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getNomeEspaco() { return nomeEspaco; }
    public void setNomeEspaco(String nomeEspaco) { this.nomeEspaco = nomeEspaco; }

    public String getLocal() {return local;}

    public void setLocal(String local) {this.local = local;}

    public List<String> getRecursos() { return recursos; }
    public void setRecursos(List<String> recursos) { this.recursos = recursos; }

    public String getLinkCancelamento() { return linkCancelamento; }
    public void setLinkCancelamento(String linkCancelamento) { this.linkCancelamento = linkCancelamento; }
}