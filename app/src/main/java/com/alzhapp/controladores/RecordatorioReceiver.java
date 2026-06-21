package com.alzhapp.controladores;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.alzhapp.R;
import com.alzhapp.vistas.MainActivity;

/**
 * Componente BroadcastReceiver para la ejecución de tareas en segundo plano.
 * Implementa la intercepción de eventos del AlarmManager para la emisión
 * de notificaciones programadas y la reactivación del ciclo de alarmas.
 */
public class RecordatorioReceiver extends BroadcastReceiver {

    // Identificador del canal de notificaciones (Requerido a partir de la API 26)
    private static final String CHANNEL_ID = "alzhapp_recordatorios";

    /**
     * Callback invocado por el sistema operativo al cumplirse la condición temporal del AlarmManager.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Verificación e inicialización del canal de notificaciones
        crearCanal(context);

        // 2. Control de permisos en tiempo de ejecución (API 33+).
        // Interrumpe el flujo si el permiso POST_NOTIFICATIONS no está concedido.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // 3. Configuración del Intent de destino para la interacción con la notificación
        Intent openIntent = new Intent(context, MainActivity.class);
        // Aplicación de flags para prevenir la duplicación de instancias en la pila de actividades
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Encapsulamiento en un PendingIntent para delegar los permisos de ejecución al sistema operativo
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                3001,
                openIntent,
                // FLAG_IMMUTABLE implementado por requerimientos de seguridad para prevención de mutaciones (API 31+)
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 4. Ensamblado de los parámetros visuales y de comportamiento de la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(context.getString(R.string.recordatorio_titulo))
                .setContentText(context.getString(R.string.recordatorio_texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)      // Prioridad alta para habilitar notificaciones tipo Heads-up
                .setAutoCancel(true)                                // Descarte automático tras la interacción del usuario
                .setContentIntent(pendingIntent);

        // 5. Despacho de la notificación al NotificationManager
        NotificationManagerCompat.from(context).notify(3001, builder.build());

        // 6. Lógica de reprogramación cíclica.
        // Se requiere la re-calendarización explícita debido a la naturaleza de ejecución única de las alarmas exactas.
        new ControladorAjustes(context).reprogramarRecordatorioSegunConfiguracion(context);
    }

    /**
     * Inicializa y registra el canal de notificaciones en el sistema operativo.
     * Requisito estructural para el enrutamiento y gestión de preferencias de notificaciones en API 26 y superior.
     */
    private void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }

            // Instanciación del canal con nivel de importancia alto para garantizar alertas visuales y sonoras
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.canal_recordatorios),
                    NotificationManager.IMPORTANCE_HIGH
            );

            // Asignación de la descripción expuesta en la interfaz de ajustes del sistema
            channel.setDescription(context.getString(R.string.canal_recordatorios_descripcion));

            // Registro persistente del canal
            manager.createNotificationChannel(channel);
        }
    }
}