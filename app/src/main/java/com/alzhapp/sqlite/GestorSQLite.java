package com.alzhapp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.Dificultad;

/**
 * Clase auxiliar (SQLiteOpenHelper) responsable de la creación, actualización
 * y definición de la estructura de la base de datos local.
 * También gestiona la precarga automática de los datos base de la aplicación.
 */
public class GestorSQLite extends SQLiteOpenHelper {

    // Nombre del archivo físico de la base de datos y su versión actual.
    // Un incremento en DATABASE_VERSION forzará la ejecución del método onUpgrade().
    public static final String DATABASE_NAME = "alzhapp.db";
    private static final int DATABASE_VERSION = 3;

    // Nombres de las tablas definidos como constantes para evitar errores tipográficos en las consultas.
    public static final String TABLA_CONFIGURACION = "CONFIGURACION";
    public static final String TABLA_SESION = "SESION";
    public static final String TABLA_CATALOGO = "CATALOGO";

    private final Context contexto;

    /**
     * Constructor principal.
     * Inicializa el componente de base de datos pasando el contexto de la aplicación.
     */
    public GestorSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.contexto = context;
    }

    /**
     * Se ejecuta automáticamente la primera vez que la aplicación intenta acceder a la base de datos.
     * Contiene las sentencias SQL (DDL) para crear las tablas y las llamadas para poblar los datos iniciales.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creación de la tabla de preferencias del usuario
        db.execSQL("CREATE TABLE " + TABLA_CONFIGURACION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "dificultad INTEGER NOT NULL," +
                "recordatorio_activo INTEGER NOT NULL," +
                "recordatorio_hora TEXT)"
        );

        // Creación de la tabla para el historial de partidas
        db.execSQL("CREATE TABLE " + TABLA_SESION + " (" +
                "id_sesion INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fecha_hora INTEGER NOT NULL," +
                "modulo TEXT NOT NULL," +
                "dificultad INTEGER NOT NULL," +
                "puntuacion INTEGER NOT NULL," +
                "tiempo_segundos INTEGER NOT NULL," +
                "aciertos INTEGER NOT NULL," +
                "errores INTEGER NOT NULL)"
        );

        // Creación de la tabla que almacena el material didáctico (imágenes, palabras, etc.)
        db.execSQL("CREATE TABLE " + TABLA_CATALOGO + " (" +
                "id_item INTEGER PRIMARY KEY AUTOINCREMENT," +
                "modulo TEXT NOT NULL," +
                "recurso TEXT NOT NULL," +
                "grupo_pareja INTEGER)"
        );

        // Llamadas a los métodos de inicialización de datos
        insertarConfiguracionInicial(db);
        insertarCatalogoInicial(db);
    }

    /**
     * Se invoca cuando el sistema detecta que DATABASE_VERSION ha incrementado.
     * Esta implementación básica opta por una estrategia destructiva: elimina las tablas
     * existentes y las vuelve a crear desde cero con el nuevo esquema.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_SESION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CONFIGURACION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CATALOGO);
        onCreate(db);
    }

    /**
     * Inserta un registro base en la tabla de configuración para asegurar que
     * la aplicación siempre disponga de unos ajustes válidos al arrancar por primera vez.
     */
    private void insertarConfiguracionInicial(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("dificultad", Dificultad.BAJA.getValor());
        values.put("recordatorio_activo", 0); // 0 representa 'false' en SQLite
        values.put("recordatorio_hora", Configuracion.HORA_PREDETERMINADA);
        db.insert(TABLA_CONFIGURACION, null, values);
    }

    /**
     * Coordina la carga inicial del material de los ejercicios leyendo
     * los archivos de texto ubicados en la carpeta 'assets' del proyecto.
     */
    private void insertarCatalogoInicial(SQLiteDatabase db) {
        leerEInsertarDesdeArchivo(db, "imagenes", "imagenes.txt");
        leerEInsertarDesdeArchivo(db, "palabras", "palabras.txt");
        leerEInsertarDesdeArchivo(db, "asociacion", "asociacion.txt");
    }

    /**
     * Abre un archivo de texto de la carpeta 'assets', lo lee línea por línea
     * y delega la inserción de cada registro en la base de datos.
     * @param db Instancia de la base de datos en modo escritura.
     * @param modulo Nombre del ejercicio al que pertenecen los datos.
     * @param nombreArchivo Nombre del archivo de texto fuente.
     */
    private void leerEInsertarDesdeArchivo(SQLiteDatabase db, String modulo, String nombreArchivo) {
        // Se utiliza try-with-resources para garantizar el cierre automático de los flujos de lectura
        try (InputStream is = contexto.getAssets().open(nombreArchivo);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                // Lógica de separación (parseo) específica para el módulo de asociación,
                // que requiere dividir el texto usando el carácter pipe (|).
                if (modulo.equals("asociacion")) {
                    String[] partes = linea.split("\\|");
                    if (partes.length == 2) {
                        insertarItem(db, modulo, partes[0].trim(), Integer.parseInt(partes[1].trim()));
                    }
                } else {
                    // Para el resto de módulos, la línea completa es el recurso
                    insertarItem(db, modulo, linea, null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar que mapea los valores individuales a un objeto ContentValues
     * y ejecuta la sentencia INSERT sobre la tabla del catálogo.
     */
    private void insertarItem(SQLiteDatabase db, String modulo, String recurso, Integer grupoPareja) {
        ContentValues values = new ContentValues();
        values.put("modulo", modulo);
        values.put("recurso", recurso);

        // Manejo explícito de valores nulos para columnas que lo permiten
        if (grupoPareja == null) {
            values.putNull("grupo_pareja");
        } else {
            values.put("grupo_pareja", grupoPareja);
        }

        db.insert(TABLA_CATALOGO, null, values);
    }
}