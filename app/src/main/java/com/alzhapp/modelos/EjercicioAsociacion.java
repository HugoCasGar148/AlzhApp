package com.alzhapp.modelos;

/**
 * Clase concreta que representa una partida del juego de "Asociación".
 * Al usar la palabra reservada 'extends', esta clase hereda automáticamente
 * todos los atributos y métodos de la clase padre 'Ejercicio'.
 */
public class EjercicioAsociacion extends Ejercicio {

    /**
     * Constructor de la clase.
     * Recibe la dificultad y se la pasa inmediatamente a la clase padre
     * mediante la instrucción 'super()', para que esta inicialice la configuración base.
     */
    public EjercicioAsociacion(int dificultad) {
        super(dificultad);
    }

    /**
     * Implementación obligatoria del método abstracto definido en la clase padre.
     * La anotación @Override le indica al compilador que estamos sobreescribiendo
     * un método heredado. Este método sirve para etiquetar correctamente
     * las sesiones en la base de datos de SQLite.
     */
    @Override
    public String getModulo() {
        return "asociacion";
    }
}