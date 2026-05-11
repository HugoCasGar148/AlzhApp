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
import com.alzhapp.sqlite.GestorSQLite;
import com.alzhapp.vistas.VistaAjustesActivity;

import java.util.Calendar;

public class ControladorAjustes implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final int REQUEST_NOTIFICATIONS = 1001;
    private static final int REQUEST_RECORDATORIO = 2001;

    private VistaAjustesActivity vista;
    private Context contexto;
    private GestorSQLite gestorSQLite;

    public ControladorAjustes(VistaAjustesActivity vista) {
        this.vista = vista;
        this.contexto = vista;
        this.gestorSQLite = new GestorSQLite(vista);
    }

    public ControladorAjustes(Context contexto) {
        this.contexto = contexto;
        this.gestorSQLite = new GestorSQLite(contexto);
    }

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
            vista.finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (vista != null) {
            vista.actualizarEstadoHora();
        }
    }

    public void cargarConfiguracion() {
        Configuracion configuracion = gestorSQLite.obtenerConfiguracion();
        vista.mostrarConfiguracion(configuracion);
    }

    public void guardarAjustes() {
        if (vista == null) {
            return;
        }

        if (vista.getSwitchRecordatorio().isChecked() && !tienePermisoNotificaciones(vista)) {
            solicitarPermisoNotificaciones(vista);
            vista.mostrarMensaje(vista.getString(R.string.sin_permiso_notificaciones));
        }

        Configuracion configuracion = new Configuracion(
                vista.obtenerDificultadSeleccionada(),
                vista.getSwitchRecordatorio().isChecked(),
                vista.getHoraSeleccionada()
        );

        boolean guardado = gestorSQLite.guardarConfiguracion(configuracion);
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

    public void borrarHistorial() {
        boolean borrado = gestorSQLite.borrarHistorial();
        if (vista != null && borrado) {
            vista.mostrarMensaje(vista.getString(R.string.historial_borrado));
        }
    }

    public void reprogramarRecordatorioSegunConfiguracion(Context context) {
        Configuracion configuracion = gestorSQLite.obtenerConfiguracion();
        if (configuracion.isRecordatorioActivo()) {
            programarRecordatorio(context, configuracion.getRecordatorioHora());
        }
    }

    public static boolean tienePermisoNotificaciones(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void solicitarPermisoNotificaciones(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !tienePermisoNotificaciones(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS
            );
        }
    }

    private void programarRecordatorio(Context context, String horaTexto) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = crearPendingIntent(context);
        alarmManager.cancel(pendingIntent);
        long momentoRecordatorio = construirMomentoRecordatorio(horaTexto).getTimeInMillis();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
            }
        } catch (SecurityException exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, momentoRecordatorio, pendingIntent);
        }
    }

    private void cancelarRecordatorio(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            PendingIntent pendingIntent = crearPendingIntent(context);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private PendingIntent crearPendingIntent(Context context) {
        Intent intent = new Intent(context, RecordatorioReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_RECORDATORIO,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private Calendar construirMomentoRecordatorio(String horaTexto) {
        String horaValidada = Configuracion.normalizarHora(horaTexto);
        String[] partes = horaValidada.split(":");
        int hora = Integer.parseInt(partes[0]);
        int minuto = Integer.parseInt(partes[1]);

        Calendar ahora = Calendar.getInstance();
        Calendar recordatorio = Calendar.getInstance();
        recordatorio.set(Calendar.HOUR_OF_DAY, hora);
        recordatorio.set(Calendar.MINUTE, minuto);
        recordatorio.set(Calendar.SECOND, 0);
        recordatorio.set(Calendar.MILLISECOND, 0);

        if (!recordatorio.after(ahora)) {
            recordatorio.add(Calendar.DAY_OF_YEAR, 1);
        }
        return recordatorio;
    }
}
