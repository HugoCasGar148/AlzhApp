package com.alzhapp.modelos;

/**
 * Clase concreta que representa una sesión del ejercicio de "Palabras" (Memoria a corto plazo).
 * Sigue el mismo patrón de herencia que el resto de ejercicios, extendiendo de la clase base 'Ejercicio'
 * para reutilizar toda la lógica del sistema de puntuación y gestión del tiempo.
 */
public class EjercicioPalabras extends Ejercicio {

    /**
     * Constructor de la clase.
     * Recibe el multiplicador o nivel de dificultad y lo envía al constructor de la clase padre
     * mediante la llamada a 'super()', asegurando así la correcta inicialización del estado.
     */
    public EjercicioPalabras(int dificultad) {
        super(dificultad);
    }

    /**
     * Implementación del método abstracto requerido por la clase padre.
     * @return La cadena "palabras", que actúa como identificador único para guardar y
     * posteriormente filtrar los resultados estadísticos de este juego en el Historial.
     */
    @Override
    public String getModulo() {
        return "palabras";
    }
}