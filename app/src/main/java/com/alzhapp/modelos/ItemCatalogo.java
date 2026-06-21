package com.alzhapp.modelos;

/**
 * Modelo de datos (POJO / Entidad) que representa un elemento individual del catálogo.
 * Actúa como un contenedor (Data Transfer Object) para mapear las filas de la base
 * de datos SQLite hacia la memoria de la aplicación. Contiene el material didáctico
 * usado en todos los ejercicios (palabras, imágenes, parejas lógicas).
 */
public class ItemCatalogo {

    // Identificador único del elemento (Clave Primaria en la base de datos)
    private long idItem;

    // Etiqueta para filtrar a qué minijuego pertenece (ej: "palabras", "imagenes", "asociacion")
    private String modulo;

    // El contenido visual o textual del ítem (ej: "Perro" o una cadena parseable como "Gato|img_gato")
    private String recurso;

    // Identificador relacional para el juego de Asociación.
    // Se declara como objeto 'Integer' en lugar de primitivo 'int' para permitir
    // valores nulos (null), ya que los ítems del módulo "palabras" no forman parejas.
    private Integer grupoPareja;

    /**
     * Constructor vacío por defecto.
     * Práctico para ir construyendo el objeto paso a paso usando los setters,
     * algo muy habitual al recorrer un Cursor devuelto por SQLite.
     */
    public ItemCatalogo() {
    }

    /**
     * Constructor completo.
     * Permite instanciar y poblar el objeto de datos en una sola línea de código.
     */
    public ItemCatalogo(long idItem, String modulo, String recurso, Integer grupoPareja) {
        this.idItem = idItem;
        this.modulo = modulo;
        this.recurso = recurso;
        this.grupoPareja = grupoPareja;
    }

    // =========================================================
    // GETTERS Y SETTERS (Encapsulamiento estándar de POO)
    // =========================================================

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