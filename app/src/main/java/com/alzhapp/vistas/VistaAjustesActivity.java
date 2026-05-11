package com.alzhapp.vistas;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alzhapp.R;
import com.alzhapp.controladores.ControladorAjustes;
import com.alzhapp.modelos.Configuracion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

public class VistaAjustesActivity extends AppCompatActivity {

    private RadioGroup rgDificultad;
    private RadioButton rbBaja;
    private RadioButton rbMedia;
    private RadioButton rbAlta;
    private MaterialSwitch switchRecordatorio;
    private TextView tvHora;
    private MaterialButton btnSeleccionarHora;
    private MaterialButton btnGuardarAjustes;
    private MaterialButton btnBorrarHistorial;
    private MaterialButton btnVolverAjustes;
    private String horaSeleccionada;
    private ControladorAjustes controladorAjustes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajustes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAjustes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();
    }

    private void recogerElementos() {
        rgDificultad = findViewById(R.id.rgDificultad);
        rbBaja = findViewById(R.id.rbBaja);
        rbMedia = findViewById(R.id.rbMedia);
        rbAlta = findViewById(R.id.rbAlta);
        switchRecordatorio = findViewById(R.id.switchRecordatorio);
        tvHora = findViewById(R.id.tvHoraSeleccionada);
        btnSeleccionarHora = findViewById(R.id.btnSeleccionarHora);
        btnGuardarAjustes = findViewById(R.id.btnGuardarAjustes);
        btnBorrarHistorial = findViewById(R.id.btnBorrarHistorial);
        btnVolverAjustes = findViewById(R.id.btnVolverAjustes);
    }

    private void establecerEscuchadores() {
        controladorAjustes = new ControladorAjustes(this);
        switchRecordatorio.setOnCheckedChangeListener(controladorAjustes);
        btnSeleccionarHora.setOnClickListener(controladorAjustes);
        btnGuardarAjustes.setOnClickListener(controladorAjustes);
        btnBorrarHistorial.setOnClickListener(controladorAjustes);
        btnVolverAjustes.setOnClickListener(controladorAjustes);
        controladorAjustes.cargarConfiguracion();
    }

    public void mostrarConfiguracion(Configuracion configuracion) {
        horaSeleccionada = configuracion.getRecordatorioHora();
        seleccionarDificultad(configuracion.getDificultad());
        switchRecordatorio.setChecked(configuracion.isRecordatorioActivo());
        tvHora.setText(getString(R.string.hora_actual, horaSeleccionada));
        actualizarEstadoHora();
    }

    public void seleccionarDificultad(int dificultad) {
        if (dificultad == Configuracion.DIFICULTAD_MEDIA) {
            rgDificultad.check(R.id.rbMedia);
        } else if (dificultad == Configuracion.DIFICULTAD_ALTA) {
            rgDificultad.check(R.id.rbAlta);
        } else {
            rgDificultad.check(R.id.rbBaja);
        }
    }

    public int obtenerDificultadSeleccionada() {
        int checkedId = rgDificultad.getCheckedRadioButtonId();

        if (checkedId == R.id.rbMedia) {
            return Configuracion.DIFICULTAD_MEDIA;
        } else if (checkedId == R.id.rbAlta) {
            return Configuracion.DIFICULTAD_ALTA;
        }
        return Configuracion.DIFICULTAD_BAJA;
    }

    public void actualizarEstadoHora() {
        boolean activo = switchRecordatorio.isChecked();
        btnSeleccionarHora.setEnabled(activo);
        tvHora.setAlpha(activo ? 1f : 0.6f);
    }

    public void abrirSelectorHora() {
        if (!switchRecordatorio.isChecked()) {
            return;
        }

        String[] partes = horaSeleccionada.split(":");
        int hora = Integer.parseInt(partes[0]);
        int minuto = Integer.parseInt(partes[1]);

        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                tvHora.setText(getString(R.string.hora_actual, horaSeleccionada));
            }
        }, hora, minuto, true);
        dialog.show();
    }

    public void mostrarDialogoBorrado() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.borrar_historial);
        builder.setMessage(R.string.confirmar_borrado);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                controladorAjustes.borrarHistorial();
            }
        });
        builder.setNegativeButton(R.string.cancelar, null);
        builder.show();
    }

    public void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    public RadioGroup getRgDificultad() {
        return rgDificultad;
    }

    public RadioButton getRbBaja() {
        return rbBaja;
    }

    public RadioButton getRbMedia() {
        return rbMedia;
    }

    public RadioButton getRbAlta() {
        return rbAlta;
    }

    public MaterialSwitch getSwitchRecordatorio() {
        return switchRecordatorio;
    }

    public TextView getTvHora() {
        return tvHora;
    }

    public MaterialButton getBtnSeleccionarHora() {
        return btnSeleccionarHora;
    }

    public MaterialButton getBtnGuardarAjustes() {
        return btnGuardarAjustes;
    }

    public MaterialButton getBtnBorrarHistorial() {
        return btnBorrarHistorial;
    }

    public MaterialButton getBtnVolverAjustes() {
        return btnVolverAjustes;
    }

    public String getHoraSeleccionada() {
        return horaSeleccionada;
    }
}
