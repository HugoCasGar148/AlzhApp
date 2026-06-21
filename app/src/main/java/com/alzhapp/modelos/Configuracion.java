package com.alzhapp.modelos;

/**
 * Entidad de dominio representativa de las preferencias de configuración del usuario.
 * Encapsula los parámetros de dificultad global y programación de notificaciones,
 * implementando validación interna para la integridad de los datos.
 */
public class Configuracion {

    // Valor de respaldo (fallback) ante datos ausentes o malformados
    public static final String HORA_PREDETERMINADA = "09:00";

    private Dificultad dificultad;
    private boolean recordatorioActivo;
    private String recordatorioHora;

    /**
     * Constructor por defecto.
     * Instancia el objeto aplicando los valores predeterminados del sistema.
     */
    public Configuracion() {
        this(Dificultad.BAJA, false, HORA_PREDETERMINADA);
    }

    /**
     * Constructor principal.
     * Delega la inicialización de atributos a los mutadores correspondientes
     * para forzar la ejecución de las rutinas de validación de datos.
     */
    public Configuracion(Dificultad dificultad, boolean recordatorioActivo, String recordatorioHora) {
        setDificultad(dificultad);
        this.recordatorioActivo = recordatorioActivo;
        setRecordatorioHora(recordatorioHora);
    }

    /**
     * Constructor sobrecargado para integración con la capa de persistencia.
     * Facilita la instanciación mapeando el equivalente numérico de la dificultad
     * almacenado en la base de datos hacia su tipo enumerado.
     */
    public Configuracion(int valorDificultad, boolean recordatorioActivo, String recordatorioHora) {
        this(Dificultad.desdeValor(valorDificultad), recordatorioActivo, recordatorioHora);
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    /**
     * Mutador del nivel de dificultad.
     * Implementa control de nulidad asignando la dificultad mínima en caso de evaluación nula.
     */
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

    /**
     * Mutador del parámetro temporal de notificación.
     * Obliga la evaluación de la cadena de texto mediante el filtro de normalización antes de la asignación.
     */
    public void setRecordatorioHora(String recordatorioHora) {
        this.recordatorioHora = normalizarHora(recordatorioHora);
    }

    /**
     * Validador estático de formato horario mediante expresión regular.
     * Verifica la conformidad estructural con el formato estándar de 24 horas (HH:mm).
     * @param hora Cadena de texto a evaluar.
     * @return La cadena original si cumple el formato, o la constante HORA_PREDETERMINADA ante cualquier inconsistencia.
     */
    public static String normalizarHora(String hora) {
        if (hora == null || !hora.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return HORA_PREDETERMINADA;
        }
        return hora;
    }
}