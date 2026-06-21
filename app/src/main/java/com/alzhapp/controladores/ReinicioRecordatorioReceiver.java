package com.alzhapp.controladores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receptor de difusión (BroadcastReceiver) especializado en el reinicio del sistema.
 * Su única responsabilidad es escuchar el evento global del sistema operativo que
 * indica que el teléfono ha terminado de encenderse (Boot Completed).
 */
public class ReinicioRecordatorioReceiver extends BroadcastReceiver {

    /**
     * Se ejecuta automáticamente en segundo plano cuando se recibe el "aviso" del sistema.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Comprobación de seguridad: Verificamos que el Intent recibido sea estrictamente
        // el de inicio del sistema y no otro evento aleatorio o malicioso.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            // Instanciamos el controlador pasándole el contexto en segundo plano.
            // Esto leerá la base de datos (SQLite) para comprobar si el usuario tenía
            // el recordatorio activo y, de ser así, lo volverá a registrar en el AlarmManager.
            new ControladorAjustes(context).reprogramarRecordatorioSegunConfiguracion(context);
        }
    }
}