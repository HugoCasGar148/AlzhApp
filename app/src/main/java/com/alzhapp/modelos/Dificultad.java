package com.alzhapp.modelos;

/**
 * Enumeración que define los niveles de dificultad disponibles en la aplicación.
 * El uso de un 'enum' garantiza la "seguridad de tipos" (Type Safety): asegura que
 * en todo el código solo se puedan manejar estos tres niveles, evitando errores
 * tipográficos o la asignación de valores inválidos que romperían el juego.
 */
public enum Dificultad {

    // Constantes predefinidas (Singletons).
    // Tienen un número entero asociado porque las bases de datos (SQLite)
    // no saben guardar objetos Java complejos, pero sí saben guardar números.
    BAJA(1),
    MEDIA(2),
    ALTA(3);

    // Variable interna inmutable que almacena el equivalente numérico de la dificultad
    private final int valor;

    /**
     * Constructor privado del Enum.
     * Se ejecuta internamente al declarar BAJA(1), MEDIA(2) y ALTA(3) para
     * vincular el nombre del nivel con su número en la base de datos.
     */
    Dificultad(int valor) {
        this.valor = valor;
    }

    /**
     * Devuelve el número entero asociado a la dificultad actual.
     * Muy útil al momento de hacer un "INSERT" o "UPDATE" en SQLite.
     */
    public int getValor() {
        return valor;
    }

    /**
     * Método estático de conversión inversa (Factory Method).
     * Toma un número (normalmente recién leído de la base de datos) y devuelve
     * el objeto Dificultad correspondiente.
     * * @param valor El número almacenado en la configuración (1, 2 o 3).
     * @return El nivel de dificultad equivalente.
     */
    public static Dificultad desdeValor(int valor) {
        // Recorremos todos los valores posibles (BAJA, MEDIA, ALTA)
        for (Dificultad d : values()) {
            // Si el número coincide, devolvemos ese nivel
            if (d.getValor() == valor) {
                return d;
            }
        }
        // Programación defensiva: Si la base de datos devuelve un número extraño
        // (por ejemplo, un 0 o un 9 por corrupción de datos), evitamos un "crash"
        // (NullPointerException) devolviendo el nivel BAJA por defecto.
        return BAJA;
    }
}