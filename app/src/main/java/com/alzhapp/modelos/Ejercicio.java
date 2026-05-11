package com.alzhapp.modelos;

public abstract class Ejercicio {
    protected long inicioMillis;
    protected long finMillis;
    protected int aciertos;
    protected int errores;
    protected final int dificultad;

    protected Ejercicio(int dificultad) {
        this.dificultad = dificultad;
    }

    public void iniciarSesion() {
        inicioMillis = System.currentTimeMillis();
        finMillis = 0;
        aciertos = 0;
        errores = 0;
    }

    public void registrarRespuesta(boolean correcta) {
        if (correcta) {
            aciertos++;
        } else {
            errores++;
        }
    }

    public void registrarSinRespuesta() {
        errores++;
    }

    public void finalizarSesion() {
        finMillis = System.currentTimeMillis();
    }

    public int getTiempoSegundos() {
        long fin = finMillis == 0 ? System.currentTimeMillis() : finMillis;
        return (int) Math.max(1, (fin - inicioMillis) / 1000L);
    }

    public int getAciertos() {
        return aciertos;
    }

    public int getErrores() {
        return errores;
    }

    public int getDificultad() {
        return dificultad;
    }

    public int calcularPuntuacion() {
        int puntuacionBase = Math.max(0, (aciertos * 100) - (errores * 25));
        return puntuacionBase * dificultad;
    }

    public abstract String getModulo();
}
