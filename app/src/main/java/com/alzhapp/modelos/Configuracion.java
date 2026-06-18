package com.alzhapp.modelos;

public class Configuracion {
    public static final String HORA_PREDETERMINADA = "09:00";

    private Dificultad dificultad;
    private boolean recordatorioActivo;
    private String recordatorioHora;

    public Configuracion() {
        this(Dificultad.BAJA, false, HORA_PREDETERMINADA);
    }

    public Configuracion(Dificultad dificultad, boolean recordatorioActivo, String recordatorioHora) {
        setDificultad(dificultad);
        this.recordatorioActivo = recordatorioActivo;
        setRecordatorioHora(recordatorioHora);
    }

    public Configuracion(int valorDificultad, boolean recordatorioActivo, String recordatorioHora) {
        this(Dificultad.desdeValor(valorDificultad), recordatorioActivo, recordatorioHora);
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public void setDificultad(Dificultad dificultad) {
        this.dificultad = (dificultad != null) ? dificultad : Dificultad.BAJA;
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

    public static String normalizarHora(String hora) {
        if (hora == null || !hora.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return HORA_PREDETERMINADA;
        }
        return hora;
    }
}