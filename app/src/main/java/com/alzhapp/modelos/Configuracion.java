package com.alzhapp.modelos;

public class Configuracion {
    public static final int DIFICULTAD_BAJA = 1;
    public static final int DIFICULTAD_MEDIA = 2;
    public static final int DIFICULTAD_ALTA = 3;
    public static final String HORA_PREDETERMINADA = "09:00";

    private int dificultad;
    private boolean recordatorioActivo;
    private String recordatorioHora;

    public Configuracion() {
        this(DIFICULTAD_BAJA, false, HORA_PREDETERMINADA);
    }

    public Configuracion(int dificultad, boolean recordatorioActivo, String recordatorioHora) {
        setDificultad(dificultad);
        this.recordatorioActivo = recordatorioActivo;
        setRecordatorioHora(recordatorioHora);
    }

    public int getDificultad() {
        return dificultad;
    }

    public void setDificultad(int dificultad) {
        this.dificultad = esDificultadValida(dificultad) ? dificultad : DIFICULTAD_BAJA;
    }

    public boolean isRecordatorioActivo() {
        return recordatorioActivo;
    }

    public void setRecordatorioActivo(boolean recordatorioActivo) {
        this.recordatorioActivo = recordatorioActivo;
    }

    public String getRecordatorioHora() {
        return recordatorioHora;
    }

    public void setRecordatorioHora(String recordatorioHora) {
        this.recordatorioHora = normalizarHora(recordatorioHora);
    }

    public static boolean esDificultadValida(int dificultad) {
        return dificultad == DIFICULTAD_BAJA
                || dificultad == DIFICULTAD_MEDIA
                || dificultad == DIFICULTAD_ALTA;
    }

    public static String normalizarHora(String hora) {
        if (hora == null || !hora.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return HORA_PREDETERMINADA;
        }
        return hora;
    }
}
