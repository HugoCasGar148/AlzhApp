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
import com.alzhapp.modelos.Dificultad;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

/**
 * Actividad que representa la pantalla de configuración de la aplicación.
 * Actúa como la "Vista" en el patrón MVC/MVP, encargándose de renderizar la interfaz,
 * gestionar los elementos visuales y delegar los eventos del usuario al ControladorAjustes.
 */
public class VistaAjustesActivity extends AppCompatActivity {

    // Componentes de la interfaz de usuario
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

    // Variables de estado
    private String horaSeleccionada;
    private ControladorAjustes controladorAjustes;

    /**
     * Método del ciclo de vida de Android ejecutado al crear la actividad.
     * Inicializa el diseño base y configura la vista para respetar los márgenes del sistema (Edge-to-Edge).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajustes);

        // Aplica un padding automático para evitar que la UI quede oculta bajo la barra de estado o navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAjustes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de la vista
        recogerElementos();
        establecerEscuchadores();
    }

    /**
     * Vincula las variables locales con los componentes definidos en el archivo XML del layout.
     */
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

    /**
     * Instancia el controlador y asigna los listeners correspondientes a los componentes interactivos.
     * También solicita la carga inicial de los datos guardados en la base de datos.
     */
    private void establecerEscuchadores() {
        controladorAjustes = new ControladorAjustes(this);
        switchRecordatorio.setOnCheckedChangeListener(controladorAjustes);
        btnSeleccionarHora.setOnClickListener(controladorAjustes);
        btnGuardarAjustes.setOnClickListener(controladorAjustes);
        btnBorrarHistorial.setOnClickListener(controladorAjustes);
        btnVolverAjustes.setOnClickListener(controladorAjustes);

        controladorAjustes.cargarConfiguracion();
    }

    /**
     * Recibe el modelo de configuración actual y actualiza todos los componentes
     * de la pantalla (RadioButtons, Switch, TextView) para reflejar dichos datos.
     */
    public void mostrarConfiguracion(Configuracion configuracion) {
        horaSeleccionada = configuracion.getRecordatorioHora();
        seleccionarDificultad(configuracion.getDificultad());
        switchRecordatorio.setChecked(configuracion.isRecordatorioActivo());
        tvHora.setText(getString(R.string.hora_actual, horaSeleccionada));
        actualizarEstadoHora();
    }

    /**
     * Marca el RadioButton correspondiente en la interfaz basándose en el enum Dificultad proporcionado.
     */
    public void seleccionarDificultad(Dificultad dificultad) {
        if (dificultad == Dificultad.MEDIA) {
            rgDificultad.check(R.id.rbMedia);
        } else if (dificultad == Dificultad.ALTA) {
            rgDificultad.check(R.id.rbAlta);
        } else {
            rgDificultad.check(R.id.rbBaja);
        }
    }

    /**
     * Evalúa el estado del RadioGroup en la interfaz y devuelve el enum Dificultad equivalente.
     */
    public Dificultad obtenerDificultadSeleccionada() {
        int checkedId = rgDificultad.getCheckedRadioButtonId();

        if (checkedId == R.id.rbMedia) {
            return Dificultad.MEDIA;
        } else if (checkedId == R.id.rbAlta) {
            return Dificultad.ALTA;
        }
        return Dificultad.BAJA;
    }

    /**
     * Habilita o deshabilita visualmente el botón de selección de hora y ajusta
     * la opacidad (alpha) del texto en función de si el switch de recordatorio está activo.
     */
    public void actualizarEstadoHora() {
        boolean activo = switchRecordatorio.isChecked();
        btnSeleccionarHora.setEnabled(activo);
        tvHora.setAlpha(activo ? 1f : 0.6f);
    }

    /**
     * Despliega un diálogo nativo (TimePickerDialog) para permitir al usuario seleccionar una hora.
     * Actualiza la variable local y la interfaz al confirmar la selección.
     */
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

    /**
     * Muestra un diálogo de alerta (AlertDialog) para solicitar confirmación antes
     * de ejecutar el borrado del historial a través del controlador.
     */
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

    /**
     * Muestra un mensaje emergente nativo (Toast) en la pantalla.
     */
    public void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    // GETTERS
    // Permiten al controlador acceder a los componentes y valores de la vista
    // =========================================================

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