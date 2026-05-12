package com.accenture.officehub.officehub_api.dto;
import java.util.List;

public class ReservaRequest {
    private String titulo;
    private String nomeEspaco;
    private String local;
    private List<String> recursos;

    public ReservaRequest() {}

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getNomeEspaco() { return nomeEspaco; }
    public void setNomeEspaco(String nomeEspaco) { this.nomeEspaco = nomeEspaco; }

    public String getLocal() {return local;}

    public void setLocal(String local) {this.local = local;}

    public List<String> getRecursos() { return recursos; }
    public void setRecursos(List<String> recursos) { this.recursos = recursos; }
}
