package com.alzhapp.modelos;

/**
 * Clase concreta que representa una sesión del ejercicio de "Imágenes" (Reconocimiento visual).
 * Al heredar ('extends') de la clase abstracta 'Ejercicio', obtiene gratuitamente
 * toda la funcionalidad de puntuación, control de tiempo y registro de aciertos/errores.
 */
public class EjercicioImagenes extends Ejercicio {

    /**
     * Constructor de la clase.
     * Toma el nivel de dificultad seleccionado y lo transfiere a la clase padre
     * mediante 'super(dificultad)' para que inicialice los valores correctamente.
     */
    public EjercicioImagenes(int dificultad) {
        super(dificultad);
    }

    /**
     * Implementación del método abstracto obligatorio.
     * @return El identificador "imagenes", utilizado por el GestorDatos para filtrar
     * y clasificar esta sesión en el historial de la base de datos (SQLite).
     */
    @Override
    public String getModulo() {
        return "imagenes";
    }
}