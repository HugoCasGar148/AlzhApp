package com.alzhapp.modelos;

/**
 * Clase base abstracta para todos los ejercicios de la aplicación.
 * Funciona como una plantilla que centraliza la lógica común (cronómetro,
 * conteo de aciertos/errores, cálculo matemático de la puntuación),
 * evitando repetir código en cada tipo de juego.
 */
public abstract class Ejercicio {

    // Variables marcadas como 'protected' para que las clases hijas
    // (como EjercicioPalabras o EjercicioImagenes) puedan acceder a ellas si lo necesitan.
    protected long inicioMillis;
    protected long finMillis;
    protected int aciertos;
    protected int errores;

    // La dificultad es inmutable ('final') una vez iniciado el ejercicio
    protected final int dificultad;

    /**
     * Constructor base.
     * Aunque la clase es abstracta y no se puede instanciar directamente,
     * este constructor es llamado por las clases hijas usando 'super(dificultad)'.
     */
    protected Ejercicio(int dificultad) {
        this.dificultad = dificultad;
    }

    /**
     * Resetea los contadores y marca el momento exacto en el que empieza a correr el tiempo.
     */
    public void iniciarSesion() {
        inicioMillis = System.currentTimeMillis();
        finMillis = 0;
        aciertos = 0;
        errores = 0;
    }

    /**
     * Registra un intento del usuario y actualiza el contador correspondiente.
     */
    public void registrarRespuesta(boolean correcta) {
        if (correcta) {
            aciertos++;
        } else {
            errores++;
        }
    }

    /**
     * Penalización por agotar el tiempo sin responder (utilizado en el juego de Palabras).
     * Computa a todos los efectos como un error.
     */
    public void registrarSinRespuesta() {
        errores++;
    }

    /**
     * Detiene el cronómetro guardando la marca de tiempo actual.
     */
    public void finalizarSesion() {
        finMillis = System.currentTimeMillis();
    }

    /**
     * Calcula los segundos transcurridos desde que se llamó a iniciarSesion().
     * Si la sesión aún no ha finalizado (finMillis == 0), calcula el tiempo hasta el instante actual.
     * @return El tiempo en segundos (con un mínimo asegurado de 1 segundo para evitar divisiones por cero después).
     */
    public int getTiempoSegundos() {
        long fin = finMillis == 0 ? System.currentTimeMillis() : finMillis;
        return (int) Math.max(1, (fin - inicioMillis) / 1000L);
    }

    // --- GETTERS SIMPLES ---

    public int getAciertos() {
        return aciertos;
    }

    public int getErrores() {
        return errores;
    }

    public int getDificultad() {
        return dificultad;
    }

    /**
     * Algoritmo de puntuación estándar heredado por todos los juegos.
     * Premia los aciertos, penaliza los errores y multiplica el resultado por
     * el nivel de dificultad (1, 2 o 3) para recompensar a los usuarios que juegan en nivel Alto.
     * El uso de Math.max(0, ...) asegura que la puntuación nunca sea negativa.
     */
    public int calcularPuntuacion() {
        int puntuacionBase = Math.max(0, (aciertos * 100) - (errores * 25));
        return puntuacionBase * dificultad;
    }

    /**
     * MÉTODO ABSTRACTO:
     * Al no tener cuerpo, obliga (por contrato) a que cualquier clase que herede de 'Ejercicio'
     * escriba su propia versión de este método para identificarse (ej: devolver "palabras").
     */
    public abstract String getModulo();
}