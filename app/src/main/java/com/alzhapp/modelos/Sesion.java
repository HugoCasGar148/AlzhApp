package com.alzhapp.modelos;

/**
 * Entidad que representa una sesión de ejercicio finalizada.
 * Actúa como Data Transfer Object (DTO) para la persistencia de estadísticas
 * en la base de datos (SQLite) y su posterior visualización en el historial.
 */
public class Sesion {

    private long idSesion;
    private long fechaHora;
    private String modulo;
    private int dificultad;
    private int puntuacion;
    private int tiempoSegundos;
    private int aciertos;
    private int errores;

    /**
     * Constructor vacío por defecto.
     * Requerido para la instanciación y mapeo de datos manual
     * tras la lectura de registros (ej. desde un Cursor de SQLite).
     */
    public Sesion() {
    }

    /**
     * Constructor de inserción.
     * Excluye el parámetro 'idSesion' para delegar la generación del identificador
     * único al motor de base de datos (AUTOINCREMENT) al persistir una nueva entidad.
     */
    public Sesion(long fechaHora, String modulo, int dificultad, int puntuacion, int tiempoSegundos, int aciertos, int errores) {
        this.fechaHora = fechaHora;
        this.modulo = modulo;
        this.dificultad = dificultad;
        this.puntuacion = puntuacion;
        this.tiempoSegundos = tiempoSegundos;
        this.aciertos = aciertos;
        this.errores = errores;
    }

    public long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(long idSesion) {
        this.idSesion = idSesion;
    }

    public long getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(long fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public int getDificultad() {
        return dificultad;
    }

    public void setDificultad(int dificultad) {
        this.dificultad = dificultad;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public int getTiempoSegundos() {
        return tiempoSegundos;
    }

    public void setTiempoSegundos(int tiempoSegundos) {
        this.tiempoSegundos = tiempoSegundos;
    }

    public int getAciertos() {
        return aciertos;
    }

    public void setAciertos(int aciertos) {
        this.aciertos = aciertos;
    }

    public int getErrores() {
        return errores;
    }

    public void setErrores(int errores) {
        this.errores = errores;
    }
}