package com.alzhapp.modelos;

public class Sesion {
    private long idSesion;
    private long fechaHora;
    private String modulo;
    private int dificultad;
    private int puntuacion;
    private int tiempoSegundos;
    private int aciertos;
    private int errores;

    public Sesion() {
    }

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
