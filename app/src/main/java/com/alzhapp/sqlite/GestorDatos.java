package com.alzhapp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.ItemCatalogo;
import com.alzhapp.modelos.Sesion;

import java.util.ArrayList;
import java.util.List;

public class GestorDatos {

    private final GestorSQLite dbHelper;

    public GestorDatos(Context context) {
        this.dbHelper = new GestorSQLite(context);
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

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("dificultad", configuracionValidada.getDificultad().getValor());
            values.put("recordatorio_activo", configuracionValidada.isRecordatorioActivo() ? 1 : 0);
            values.put("recordatorio_hora", configuracionValidada.getRecordatorioHora());

            int filas = db.update(GestorSQLite.TABLA_CONFIGURACION, values, "id = 1", null);
            if (filas == 0) {
                values.put("id", 1);
                if (db.insert(GestorSQLite.TABLA_CONFIGURACION, null, values) == -1) {
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
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                GestorSQLite.TABLA_CONFIGURACION,
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

        SQLiteDatabase db = dbHelper.getWritableDatabase();
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
            id = db.insert(GestorSQLite.TABLA_SESION, null, values);
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
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        if (filtroModulo != null && !filtroModulo.trim().isEmpty() && esModuloValido(filtroModulo)) {
            selection = "modulo = ?";
            selectionArgs = new String[]{filtroModulo};
        }

        try (Cursor cursor = db.query(
                GestorSQLite.TABLA_SESION,
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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(GestorSQLite.TABLA_SESION, null, null);
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

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                GestorSQLite.TABLA_CATALOGO,
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
                && (sesion.getDificultad() >= 1 && sesion.getDificultad() <= 3)
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