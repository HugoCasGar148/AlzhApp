package com.alzhapp.vistas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alzhapp.R;
import com.alzhapp.controladores.ControladorHistorial;
import com.alzhapp.modelos.Sesion;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad que representa la pantalla del historial de sesiones.
 * Actúa como la "Vista" en el patrón MVC/MVP, encargándose de renderizar la interfaz,
 * gestionar el adaptador del menú desplegable y delegar los eventos al ControladorHistorial.
 */
public class VistaHistorialActivity extends AppCompatActivity {

    // Componentes de la interfaz de usuario
    private Spinner spinnerFiltro;
    private ListView listViewHistorial;
    private TextView tvVacio;
    private MaterialButton btnVolver;

    // Referencia al controlador asociado
    private ControladorHistorial controladorHistorial;

    /**
     * Método del ciclo de vida de Android llamado al instanciar la actividad.
     * Configura el diseño Edge-to-Edge, inicializa la vista y prepara los componentes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);

        // Aplica un padding dinámico para evitar solapamientos con las barras de estado y navegación del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootHistorial), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();
    }

    /**
     * Método del ciclo de vida ejecutado cuando la actividad vuelve a primer plano.
     * Delega en el controlador la recarga del historial para garantizar que los datos mostrados estén actualizados
     * (por ejemplo, tras volver de la pantalla de ajustes habiendo borrado los registros).
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (controladorHistorial != null) {
            controladorHistorial.cargarHistorial();
        }
    }

    /**
     * Vincula las variables locales con los elementos definidos en el archivo XML (layout).
     * Configura también el texto alternativo (EmptyView) que mostrará el ListView cuando no haya datos.
     */
    private void recogerElementos() {
        spinnerFiltro = findViewById(R.id.spinnerFiltroHistorial);
        listViewHistorial = findViewById(R.id.listViewHistorial);
        tvVacio = findViewById(R.id.tvHistorialVacio);
        btnVolver = findViewById(R.id.btnVolverHistorial);

        listViewHistorial.setEmptyView(tvVacio);
    }

    /**
     * Instancia el controlador, configura el adaptador de recursos para el menú desplegable (Spinner)
     * y asigna los listeners a los componentes interactivos de la pantalla.
     */
    private void establecerEscuchadores() {
        controladorHistorial = new ControladorHistorial(this);

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this,
                R.array.filtro_historial_array,
                android.R.layout.simple_spinner_item
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterSpinner);

        spinnerFiltro.setOnItemSelectedListener(controladorHistorial);
        listViewHistorial.setOnItemClickListener(controladorHistorial);
        btnVolver.setOnClickListener(controladorHistorial);

        controladorHistorial.prepararListado();
    }

    /**
     * Evalúa la posición seleccionada en el menú desplegable (Spinner).
     * @return La cadena de texto correspondiente al módulo seleccionado, o null si se selecciona "Todos".
     */
    public String obtenerFiltroSeleccionado() {
        int posicion = spinnerFiltro.getSelectedItemPosition();

        if (posicion == 1) {
            return "imagenes";
        } else if (posicion == 2) {
            return "palabras";
        } else if (posicion == 3) {
            return "asociacion";
        }
        return null;
    }

    /**
     * Alterna la visibilidad del componente de texto que indica que el historial está vacío.
     */
    public void mostrarHistorialVacio(boolean vacio) {
        tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
    }

    /**
     * Despliega un cuadro de diálogo nativo (AlertDialog) con el desglose
     * de las estadísticas de una sesión específica seleccionada en la lista.
     */
    public void mostrarDetalleSesion(Sesion sesion) {
        if (sesion == null) {
            return;
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String texto = "Fecha: " + formato.format(new Date(sesion.getFechaHora()))
                + "\nMódulo: " + capitalizarModulo(sesion.getModulo())
                + "\nDificultad: " + sesion.getDificultad()
                + "\nPuntuación: " + sesion.getPuntuacion()
                + "\nTiempo: " + sesion.getTiempoSegundos() + " s"
                + "\nAciertos: " + sesion.getAciertos()
                + "\nErrores: " + sesion.getErrores();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.historial);
        builder.setMessage(texto);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Método auxiliar para capitalizar la primera letra del nombre del módulo con fines de presentación.
     */
    private String capitalizarModulo(String modulo) {
        if (modulo == null || modulo.length() == 0) {
            return "";
        }
        return modulo.substring(0, 1).toUpperCase(Locale.getDefault()) + modulo.substring(1);
    }

    // =========================================================
    // GETTERS
    // Permiten al controlador el acceso de solo lectura a los componentes de la vista
    // =========================================================

    public Spinner getSpinnerFiltro() {
        return spinnerFiltro;
    }

    public ListView getListViewHistorial() {
        return listViewHistorial;
    }

    public TextView getTvVacio() {
        return tvVacio;
    }

    public MaterialButton getBtnVolver() {
        return btnVolver;
    }
}