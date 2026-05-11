package com.alzhapp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.ItemCatalogo;
import com.alzhapp.modelos.Sesion;

import java.util.ArrayList;
import java.util.List;

public class GestorSQLite extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "alzhapp.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLA_CONFIGURACION = "CONFIGURACION";
    public static final String TABLA_SESION = "SESION";
    public static final String TABLA_CATALOGO = "CATALOGO";

    public GestorSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        values.put("dificultad", Configuracion.DIFICULTAD_BAJA);
        values.put("recordatorio_activo", 0);
        values.put("recordatorio_hora", Configuracion.HORA_PREDETERMINADA);
        db.insert(TABLA_CONFIGURACION, null, values);
    }

    public void insertarCatalogoInicial(SQLiteDatabase db) {
        insertarItem(db, "imagenes", "Cámara|ic_menu_camera", null);
        insertarItem(db, "imagenes", "Teléfono|ic_menu_call", null);
        insertarItem(db, "imagenes", "Agenda|ic_menu_agenda", null);
        insertarItem(db, "imagenes", "Mapa|ic_menu_mapmode", null);
        insertarItem(db, "imagenes", "Galería|ic_menu_gallery", null);
        insertarItem(db, "imagenes", "Ayuda|ic_menu_help", null);
        insertarItem(db, "imagenes", "Dirección|ic_menu_directions", null);
        insertarItem(db, "imagenes", "Edición|ic_menu_edit", null);

        insertarItem(db, "palabras", "casa", null);
        insertarItem(db, "palabras", "sol", null);
        insertarItem(db, "palabras", "gato", null);
        insertarItem(db, "palabras", "mesa", null);
        insertarItem(db, "palabras", "árbol", null);
        insertarItem(db, "palabras", "libro", null);
        insertarItem(db, "palabras", "agua", null);
        insertarItem(db, "palabras", "flor", null);
        insertarItem(db, "palabras", "puerta", null);
        insertarItem(db, "palabras", "tren", null);

        insertarItem(db, "asociacion", "Perro", 1);
        insertarItem(db, "asociacion", "Ladra", 1);
        insertarItem(db, "asociacion", "Gato", 2);
        insertarItem(db, "asociacion", "Maúlla", 2);
        insertarItem(db, "asociacion", "Vaca", 3);
        insertarItem(db, "asociacion", "Muge", 3);
        insertarItem(db, "asociacion", "Sol", 4);
        insertarItem(db, "asociacion", "Día", 4);
        insertarItem(db, "asociacion", "Luna", 5);
        insertarItem(db, "asociacion", "Noche", 5);
        insertarItem(db, "asociacion", "Llave", 6);
        insertarItem(db, "asociacion", "Puerta", 6);
        insertarItem(db, "asociacion", "Pez", 7);
        insertarItem(db, "asociacion", "Agua", 7);
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

    public boolean guardarConfiguracion(Configuracion configuracion) {
        if (configuracion == null) {
            return false;
        }

        Configuracion configuracionValidada = new Configuracion(
                configuracion.getDificultad(),
                configuracion.isRecordatorioActivo(),
                configuracion.getRecordatorioHora()
        );

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("dificultad", configuracionValidada.getDificultad());
            values.put("recordatorio_activo", configuracionValidada.isRecordatorioActivo() ? 1 : 0);
            values.put("recordatorio_hora", configuracionValidada.getRecordatorioHora());

            int filas = db.update(TABLA_CONFIGURACION, values, "id = 1", null);
            if (filas == 0) {
                values.put("id", 1);
                if (db.insert(TABLA_CONFIGURACION, null, values) == -1) {
                    return false;
                }
            }
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public Configuracion obtenerConfiguracion() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLA_CONFIGURACION,
                new String[]{"dificultad", "recordatorio_activo", "recordatorio_hora"},
                "id = 1",
                null,
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return new Configuracion(
                        cursor.getInt(0),
                        cursor.getInt(1) == 1,
                        cursor.getString(2)
                );
            }
        }
        return new Configuracion();
    }

    public long insertarSesion(Sesion sesion) {
        if (!esSesionValida(sesion)) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        long id;
        try {
            ContentValues values = new ContentValues();
            values.put("fecha_hora", sesion.getFechaHora());
            values.put("modulo", sesion.getModulo());
            values.put("dificultad", sesion.getDificultad());
            values.put("puntuacion", Math.max(0, sesion.getPuntuacion()));
            values.put("tiempo_segundos", Math.max(1, sesion.getTiempoSegundos()));
            values.put("aciertos", Math.max(0, sesion.getAciertos()));
            values.put("errores", Math.max(0, sesion.getErrores()));
            id = db.insert(TABLA_SESION, null, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return id;
    }

    public List<Sesion> obtenerHistorial() {
        return obtenerSesiones(null);
    }

    public List<Sesion> obtenerHistorialPorModulo(String modulo) {
        return obtenerSesiones(modulo);
    }

    public List<Sesion> obtenerSesiones(String filtroModulo) {
        List<Sesion> sesiones = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (filtroModulo != null && !filtroModulo.trim().isEmpty() && esModuloValido(filtroModulo)) {
            selection = "modulo = ?";
            selectionArgs = new String[]{filtroModulo};
        }

        try (Cursor cursor = db.query(
                TABLA_SESION,
                new String[]{"id_sesion", "fecha_hora", "modulo", "dificultad", "puntuacion", "tiempo_segundos", "aciertos", "errores"},
                selection,
                selectionArgs,
                null,
                null,
                "fecha_hora DESC"
        )) {
            while (cursor.moveToNext()) {
                Sesion sesion = new Sesion();
                sesion.setIdSesion(cursor.getLong(0));
                sesion.setFechaHora(cursor.getLong(1));
                sesion.setModulo(cursor.getString(2));
                sesion.setDificultad(cursor.getInt(3));
                sesion.setPuntuacion(cursor.getInt(4));
                sesion.setTiempoSegundos(cursor.getInt(5));
                sesion.setAciertos(cursor.getInt(6));
                sesion.setErrores(cursor.getInt(7));
                sesiones.add(sesion);
            }
        }
        return sesiones;
    }

    public boolean borrarHistorial() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLA_SESION, null, null);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<ItemCatalogo> obtenerItemsPorModulo(String modulo) {
        List<ItemCatalogo> items = new ArrayList<>();
        if (!esModuloValido(modulo)) {
            return items;
        }

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLA_CATALOGO,
                new String[]{"id_item", "modulo", "recurso", "grupo_pareja"},
                "modulo = ?",
                new String[]{modulo},
                null,
                null,
                "id_item ASC"
        )) {
            while (cursor.moveToNext()) {
                ItemCatalogo item = new ItemCatalogo();
                item.setIdItem(cursor.getLong(0));
                item.setModulo(cursor.getString(1));
                item.setRecurso(cursor.getString(2));
                if (cursor.isNull(3)) {
                    item.setGrupoPareja(null);
                } else {
                    item.setGrupoPareja(cursor.getInt(3));
                }
                items.add(item);
            }
        }
        return items;
    }

    private boolean esSesionValida(Sesion sesion) {
        return sesion != null
                && sesion.getFechaHora() > 0
                && esModuloValido(sesion.getModulo())
                && Configuracion.esDificultadValida(sesion.getDificultad())
                && sesion.getTiempoSegundos() > 0
                && sesion.getAciertos() >= 0
                && sesion.getErrores() >= 0
                && (sesion.getAciertos() + sesion.getErrores()) > 0;
    }

    private boolean esModuloValido(String modulo) {
        return "imagenes".equals(modulo)
                || "palabras".equals(modulo)
                || "asociacion".equals(modulo);
    }
}
