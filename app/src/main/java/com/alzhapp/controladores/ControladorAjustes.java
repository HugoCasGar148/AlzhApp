package com.alzhapp.controladores;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.sqlite.GestorDatos;
import com.alzhapp.vistas.VistaAjustesActivity;

import java.util.Calendar;

/**
 * Controlador para la vista de Ajustes.
 * Implementa la gestión de eventos de interfaz, persistencia de configuración
 * de usuario y registro de alarmas a nivel de sistema.
 */
public class ControladorAjustes implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    // Códigos de solicitud para flujos de permisos y PendingIntents
    public static final int REQUEST_NOTIFICATIONS = 1001;
    private static final int REQUEST_RECORDATORIO = 2001;

    private VistaAjustesActivity vista;
    private final Context contexto;
    private final GestorDatos gestorDatos;

    /**
     * Constructor principal para inicialización desde la interfaz gráfica.
     * @param vista Actividad de Ajustes que delega sus eventos al controlador.
     */
    public ControladorAjustes(VistaAjustesActivity vista) {
        this.vista = vista;
        this.contexto = vista;
        this.gestorDatos = new GestorDatos(vista);
    }

    /**
     * Constructor secundario para procesos en segundo plano.
     * @param contexto Contexto base de la aplicación.
     */
    public ControladorAjustes(Context contexto) {
        this.contexto = contexto;
        this.gestorDatos = new GestorDatos(contexto);
    }

    /**
     * Intercepta los eventos de clic en la interfaz y delega la ejecución
     * al método de negocio correspondiente.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnSeleccionarHora) {
            vista.abrirSelectorHora();
        } else if (id == R.id.btnGuardarAjustes) {
            guardarAjustes();
        } else if (id == R.id.btnBorrarHistorial) {
            vista.mostrarDialogoBorrado();
        } else if (id == R.id.btnVolverAjustes) {
            vista.finish(); // Finaliza la actividad actual
        }
    }

    /**
     * Gestiona los cambios de estado en componentes de tipo Switch o CheckBox.
     * Actualiza la interfaz habilitando o deshabilitando el selector de hora
     * en función del estado de activación.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (vista != null) {
            vista.actualizarEstadoHora();
        }
    }

    /**
     * Recupera la configuración persistida en la capa de datos
     * y la transfiere a la vista para la inicialización de sus componentes.
     */
    public void cargarConfiguracion() {
        Configuracion configuracion = gestorDatos.obtenerConfiguracion();
        vista.mostrarConfiguracion(configuracion);
    }

    /**
     * Procesa, valida y persiste la configuración actual.
     * Ejecuta el alta o baja de las alarmas en el sistema según las preferencias establecidas.
     */
    public void guardarAjustes() {
        if (vista == null) {
            return; // Control de nulidad por posible destrucción del contexto visual
        }

        // Validación y solicitud de permisos de notificación
        if (vista.getSwitchRecordatorio().isChecked() && !tienePermisoNotificaciones(vista)) {
            solicitarPermisoNotificaciones(vista);
            vista.mostrarMensaje(vista.getString(R.string.sin_permiso_notificaciones));
        }

        // Instanciación del modelo con los parámetros de la UI
        Configuracion configuracion = new Configuracion(
                vista.obtenerDificultadSeleccionada(),
                vista.getSwitchRecordatorio().isChecked(),
                vista.getHoraSeleccionada()
        );

        // Inserción o actualización en la base de datos
        boolean guardado = gestorDatos.guardarConfiguracion(configuracion);
        if (guardado) {
            if (configuracion.isRecordatorioActivo()) {
                programarRecordatorio(vista, configuracion.getRecordatorioHora());
                vista.mostrarMensaje(vista.getString(R.string.recordatorio_programado, configuracion.getRecordatorioHora()));
            } else {
                cancelarRecordatorio(vista);
                vista.mostrarMensaje(vista.getString(R.string.ajustes_guardados));
            }
        } else {
            vista.mostrarMensaje(vista.getString(R.string.ajustes_guardado_error));
        }
    }

    /**
     * Ejecuta el borrado integral de registros de historial en la base de datos.
     */
    public void borrarHistorial() {
        boolean borrado = gestorDatos.borrarHistorial();
        if (vista != null && borrado) {
            vista.mostrarMensaje(vista.getString(R.string.historial_borrado));
        }
    }

    /**
     * Reprograma el recordatorio activo tras el reinicio del sistema operativo.
     * Requerido debido a la limpieza del registro de AlarmManager durante el apagado del dispositivo.
     */
    public void reprogramarRecordatorioSegunConfiguracion(Context context) {
        Configuracion configuracion = gestorDatos.obtenerConfiguracion();
        if (configuracion.isRecordatorioActivo()) {
            programarRecordatorio(context, configuracion.getRecordatorioHora());
        }
    }

    /**
     * Verifica la concesión del permiso POST_NOTIFICATIONS.
     */
    public static boolean tienePermisoNotificaciones(Context context) {
        // Retorno afirmativo implícito para APIs < 33
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicita el permiso POST_NOTIFICATIONS mediante el flujo estándar del sistema (API 33+).
     */
    public static void solicitarPermisoNotificaciones(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !tienePermisoNotificaciones(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS
            );
        }
    }

    /**
     * Registra una alarma en el AlarmManager del sistema.
     * Implementa ramificaciones de compatibilidad para gestionar las restricciones
     * de ejecución en segundo plano de distintas versiones de la API.
     */
    private void programarRecordatorio(Context context, String horaTexto) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Cancelación de alarmas previas bajo el mismo Intent
        PendingIntent pendingIntent = crearPendingIntent(context);
        alarmManager.cancel(pendingIntent);

        // Cálculo del instante de ejecución
        long momentoRecordatorio = construirMomentoRecordatorio(horaTexto).getTimeInMillis();

        // Estructura de compatibilidad para escalado de alarmas exactas
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+: Bypass parcial de Doze Mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    // API 31+: Fallback por ausencia de permiso SCHEDULE_EXACT_ALARM
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
                } else {
                    // API 23-30 o API 31+ con permiso: Alarma exacta
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // API 19-22: Programación exacta estándar
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
            } else {
                // API < 19: Programación base
                alarmManager.set(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
            }
        } catch (SecurityException exception) {
            // Captura de excepción para dispositivos con restricciones OEM severas o Android 14+
            alarmManager.set(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
        }
    }

    /**
     * Elimina del registro del sistema cualquier alarma asociada y cancela el PendingIntent.
     */
    private void cancelarRecordatorio(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            PendingIntent pendingIntent = crearPendingIntent(context);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * Construye el PendingIntent destinado a la invocación del RecordatorioReceiver.
     */
    private PendingIntent crearPendingIntent(Context context) {
        Intent intent = new Intent(context, RecordatorioReceiver.class);
        // FLAG_IMMUTABLE requerido para APIs 31+. FLAG_UPDATE_CURRENT aplicado para actualización de datos.
        return PendingIntent.getBroadcast(
                context,
                REQUEST_RECORDATORIO,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * Parsea la cadena horaria y genera un objeto Calendar configurado
     * para la siguiente ocurrencia cronológica de dicha hora.
     */
    private Calendar construirMomentoRecordatorio(String horaTexto) {
        // Normalización y extracción de los componentes de tiempo
        String horaValidada = Configuracion.normalizarHora(horaTexto);
        String[] partes = horaValidada.split(":");
        int hora = Integer.parseInt(partes[0]);
        int minuto = Integer.parseInt(partes[1]);

        Calendar ahora = Calendar.getInstance();
        Calendar recordatorio = Calendar.getInstance();

        // Asignación de la hora objetivo
        recordatorio.set(Calendar.HOUR_OF_DAY, hora);
        recordatorio.set(Calendar.MINUTE, minuto);
        recordatorio.set(Calendar.SECOND, 0);
        recordatorio.set(Calendar.MILLISECOND, 0);

        // Adición de 24 horas si el instante calculado es cronológicamente anterior al actual
        if (!recordatorio.after(ahora)) {
            recordatorio.add(Calendar.DAY_OF_YEAR, 1);
        }
        return recordatorio;
    }
}