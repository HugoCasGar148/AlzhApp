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

public class GestorSQLite extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "alzhapp.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLA_CONFIGURACION = "CONFIGURACION";
    public static final String TABLA_SESION = "SESION";
    public static final String TABLA_CATALOGO = "CATALOGO";
    private final Context contexto;

    public GestorSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.contexto = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA_CONFIGURACION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "dificultad INTEGER NOT NULL," +
                "recordatorio_activo INTEGER NOT NULL," +
                "recordatorio_hora TEXT)"
        );

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

        db.execSQL("CREATE TABLE " + TABLA_CATALOGO + " (" +
                "id_item INTEGER PRIMARY KEY AUTOINCREMENT," +
                "modulo TEXT NOT NULL," +
                "recurso TEXT NOT NULL," +
                "grupo_pareja INTEGER)"
        );

        insertarConfiguracionInicial(db);
        insertarCatalogoInicial(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_SESION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CONFIGURACION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CATALOGO);
        onCreate(db);
    }

    private void insertarConfiguracionInicial(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("dificultad", Dificultad.BAJA.getValor());
        values.put("recordatorio_activo", 0);
        values.put("recordatorio_hora", Configuracion.HORA_PREDETERMINADA);
        db.insert(TABLA_CONFIGURACION, null, values);
    }

    private void insertarCatalogoInicial(SQLiteDatabase db) {
        leerEInsertarDesdeArchivo(db, "imagenes", "imagenes.txt");
        leerEInsertarDesdeArchivo(db, "palabras", "palabras.txt");
        leerEInsertarDesdeArchivo(db, "asociacion", "asociacion.txt");
    }

    private void leerEInsertarDesdeArchivo(SQLiteDatabase db, String modulo, String nombreArchivo) {
        try (InputStream is = contexto.getAssets().open(nombreArchivo);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (modulo.equals("asociacion")) {
                    String[] partes = linea.split("\\|");
                    if (partes.length == 2) {
                        insertarItem(db, modulo, partes[0].trim(), Integer.parseInt(partes[1].trim()));
                    }
                } else {
                    insertarItem(db, modulo, linea, null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertarItem(SQLiteDatabase db, String modulo, String recurso, Integer grupoPareja) {
        ContentValues values = new ContentValues();
        values.put("modulo", modulo);
        values.put("recurso", recurso);
        if (grupoPareja == null) {
            values.putNull("grupo_pareja");
        } else {
            values.put("grupo_pareja", grupoPareja);
        }
        db.insert(TABLA_CATALOGO, null, values);
    }
}