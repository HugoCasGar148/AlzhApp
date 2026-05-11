package com.alzhapp.modelos;

public class EjercicioAsociacion extends Ejercicio {
    public EjercicioAsociacion(int dificultad) {
        super(dificultad);
    }

    @Override
    public String getModulo() {
        return "asociacion";
    }
}
