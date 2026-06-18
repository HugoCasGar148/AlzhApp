package com.alzhapp.modelos;

public enum Dificultad {
    BAJA(1),
    MEDIA(2),
    ALTA(3);

    private final int valor;

    Dificultad(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    public static Dificultad desdeValor(int valor) {
        for (Dificultad d : values()) {
            if (d.getValor() == valor) {
                return d;
            }
        }
        return BAJA;
    }
}