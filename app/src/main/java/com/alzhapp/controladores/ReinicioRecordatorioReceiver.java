package com.alzhapp.controladores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReinicioRecordatorioReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new ControladorAjustes(context).reprogramarRecordatorioSegunConfiguracion(context);
        }
    }
}
