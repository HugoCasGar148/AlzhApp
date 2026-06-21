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

/**
 * Clase que actúa como un DAO (Data Access Object) para interactuar con la base de datos SQLite.
 * Centraliza y abstrae todas las operaciones de lectura (SELECT) y escritura (INSERT, UPDATE, DELETE)
 * relativas a la configuración del usuario, el historial de sesiones y los elementos del catálogo.
 */
public class GestorDatos {

    private final GestorSQLite dbHelper;

    /**
     * Constructor del gestor.
     * Inicializa la conexión con la clase ayudante (SQLiteOpenHelper) encargada de crear
     * y actualizar la estructura de la base de datos.
     * @param context Contexto de la aplicación necesario para operar con SQLite.
     */
    public GestorDatos(Context context) {
        this.dbHelper = new GestorSQLite(context);
    }

    /**
     * Guarda o actualiza los ajustes de configuración en la base de datos.
     * Implementa una transacción para asegurar la integridad de la operación.
     * @param configuracion Objeto con los nuevos ajustes a guardar.
     * @return true si la operación se completó con éxito, false en caso contrario.
     */
    public boolean guardarConfiguracion(Configuracion configuracion) {
        if (configuracion == null) {
            return false;
        }

        // Se instancia un nuevo objeto para forzar que los datos pasen por los
        // métodos de validación internos de la clase Configuracion antes de guardarlos.
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

            // Intenta actualizar el registro único (id = 1).
            int filas = db.update(GestorSQLite.TABLA_CONFIGURACION, values, "id = 1", null);

            // Si no se actualizó ninguna fila (es decir, la tabla está vacía), inserta el primer registro.
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

    /**
     * Recupera los ajustes guardados del usuario.
     * @return Un objeto Configuracion poblado con los datos de SQLite, o una configuración
     * por defecto si la tabla está vacía.
     */
    public Configuracion obtenerConfiguracion() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // El bloque try-with-resources asegura que el Cursor se cierre automáticamente
        // al finalizar la lectura, evitando fugas de memoria (Memory Leaks).
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

    /**
     * Inserta un nuevo registro de resultados tras finalizar un ejercicio.
     * Aplica validaciones de seguridad para evitar guardar valores inconsistentes
     * (como puntuaciones o tiempos negativos).
     * @param sesion Objeto con los datos del ejercicio completado.
     * @return El ID del registro insertado, o -1 si hubo un error o los datos son inválidos.
     */
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

    /**
     * Recupera la totalidad de los registros guardados en el historial.
     */
    public List<Sesion> obtenerHistorial() {
        return obtenerSesiones(null);
    }

    /**
     * Recupera los registros del historial filtrados por un juego específico.
     */
    public List<Sesion> obtenerHistorialPorModulo(String modulo) {
        return obtenerSesiones(modulo);
    }

    /**
     * Método base para las consultas a la tabla de sesiones.
     * Realiza el mapeo de las filas devueltas por SQLite hacia una lista de objetos de Java.
     * @param filtroModulo Nombre del módulo para filtrar (opcional), o null para obtener todos.
     * @return Lista de sesiones ordenadas por fecha de forma descendente.
     */
    public List<Sesion> obtenerSesiones(String filtroModulo) {
        List<Sesion> sesiones = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = null;
        String[] selectionArgs = null;

        // Construcción condicional de la consulta WHERE según el filtro recibido
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
                "fecha_hora DESC" // Orden descendente: los más recientes primero
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

    /**
     * Elimina todos los registros contenidos en la tabla de historial de sesiones.
     * @return true si el borrado se procesó correctamente, false en caso contrario.
     */
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

    /**
     * Consulta el catálogo buscando los recursos (imágenes, palabras, etc.) asociados a un módulo.
     * @param modulo Nombre del ejercicio que solicita los datos.
     * @return Lista de objetos ItemCatalogo correspondientes al módulo.
     */
    public List<ItemCatalogo> obtenerItemsPorModulo(String modulo) {
        List<ItemCatalogo> items = new ArrayList<>();
        if (!esModuloValido(modulo)) {
            return items; // Retorna lista vacía si el módulo no existe
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

                // Comprobación específica para manejar valores nulos en columnas relacionales
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

    /**
     * Función privada de validación que verifica la coherencia de los datos de una sesión
     * antes de permitir su inserción en la base de datos.
     */
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

    /**
     * Función privada de validación para evitar inyecciones o consultas de módulos inválidos.
     */
    private boolean esModuloValido(String modulo) {
        return "imagenes".equals(modulo)
                || "palabras".equals(modulo)
                || "asociacion".equals(modulo);
    }
}