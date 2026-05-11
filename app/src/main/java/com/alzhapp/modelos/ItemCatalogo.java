package com.alzhapp.modelos;

public class ItemCatalogo {
    private long idItem;
    private String modulo;
    private String recurso;
    private Integer grupoPareja;

    public ItemCatalogo() {
    }

    public ItemCatalogo(long idItem, String modulo, String recurso, Integer grupoPareja) {
        this.idItem = idItem;
        this.modulo = modulo;
        this.recurso = recurso;
        this.grupoPareja = grupoPareja;
    }

    public long getIdItem() {
        return idItem;
    }

    public void setIdItem(long idItem) {
        this.idItem = idItem;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getRecurso() {
        return recurso;
    }

    public void setRecurso(String recurso) {
        this.recurso = recurso;
    }

    public Integer getGrupoPareja() {
        return grupoPareja;
    }

    public void setGrupoPareja(Integer grupoPareja) {
        this.grupoPareja = grupoPareja;
    }
}
