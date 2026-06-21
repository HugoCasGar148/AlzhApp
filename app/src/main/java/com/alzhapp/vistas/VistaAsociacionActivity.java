package com.alzhapp.vistas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alzhapp.R;
import com.alzhapp.controladores.ControladorAsociacion;
import com.alzhapp.modelos.Sesion;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Actividad que representa la pantalla del ejercicio de Asociación.
 * Actúa como la "Vista" en el patrón MVC/MVP, encargándose de actualizar la interfaz gráfica
 * y de derivar las interacciones del usuario al ControladorAsociacion.
 */
public class VistaAsociacionActivity extends AppCompatActivity {

    // Referencia al controlador que gestiona la lógica de negocio
    private ControladorAsociacion controladorAsociacion;

    // Componentes visuales de la interfaz
    private TextView tvContador;
    private TextView tvAsociacionBase;
    private MaterialButton btnOpcion1;
    private MaterialButton btnOpcion2;
    private MaterialButton btnOpcion3;
    private MaterialButton btnOpcion4;
    private MaterialButton btnVolver;

    /**
     * Método del ciclo de vida de Android ejecutado al crear la actividad.
     * Configura el diseño Edge-to-Edge, inicializa la interfaz y arranca el ejercicio.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_asociacion);

        // Ajusta el padding de la vista principal para evitar solapamientos con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAsociacion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configuración inicial
        recogerElementos();
        establecerEscuchadores();
        prepararSalida();

        // Inicia la primera ronda a través del controlador
        controladorAsociacion.iniciarEjercicio();
    }

    /**
     * Vincula las variables de la clase con los elementos definidos en el archivo XML (layout).
     */
    private void recogerElementos() {
        tvContador = findViewById(R.id.tvContadorAsociacion);
        tvAsociacionBase = findViewById(R.id.tvAsociacionBase);
        btnOpcion1 = findViewById(R.id.btnAsociacionOpcion1);
        btnOpcion2 = findViewById(R.id.btnAsociacionOpcion2);
        btnOpcion3 = findViewById(R.id.btnAsociacionOpcion3);
        btnOpcion4 = findViewById(R.id.btnAsociacionOpcion4);
        btnVolver = findViewById(R.id.btnVolverAsociacion);
    }

    /**
     * Instancia el controlador y lo asigna como listener para los eventos de clic de los botones.
     */
    private void establecerEscuchadores() {
        controladorAsociacion = new ControladorAsociacion(this);
        btnOpcion1.setOnClickListener(controladorAsociacion);
        btnOpcion2.setOnClickListener(controladorAsociacion);
        btnOpcion3.setOnClickListener(controladorAsociacion);
        btnOpcion4.setOnClickListener(controladorAsociacion);
        btnVolver.setOnClickListener(controladorAsociacion);
    }

    /**
     * Intercepta el botón físico o gesto de "Atrás" del dispositivo (OnBackPressedDispatcher).
     * Delega la acción al método de confirmación en lugar de cerrar la pantalla inmediatamente.
     */
    private void prepararSalida() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSalida();
            }
        });
    }

    /**
     * Actualiza los textos de la interfaz con los datos de la ronda actual.
     * @param pregunta Objeto con la información a mostrar (palabra base y opciones).
     * @param preguntaActual Número de la ronda en curso.
     * @param totalPreguntas Cantidad total de rondas de la partida.
     */
    public void mostrarPregunta(ControladorAsociacion.PreguntaAsociacion pregunta, int preguntaActual, int totalPreguntas) {
        tvContador.setText(getString(R.string.pregunta_contador, preguntaActual, totalPreguntas));
        tvAsociacionBase.setText(getString(R.string.elige_pareja, pregunta.getBase()));
        mostrarOpciones(pregunta.getOpciones());
    }

    /**
     * Gestiona la visibilidad y el texto de los botones de respuesta.
     * Oculta los botones sobrantes (View.GONE) en niveles de dificultad bajos donde hay menos opciones.
     */
    public void mostrarOpciones(List<String> opciones) {
        MaterialButton[] botones = getBotonesRespuesta();
        for (int i = 0; i < botones.length; i++) {
            if (i < opciones.size()) {
                botones[i].setVisibility(View.VISIBLE);
                botones[i].setText(opciones.get(i));
            } else {
                botones[i].setVisibility(View.GONE);
            }
        }
    }

    /**
     * Muestra un cuadro de diálogo nativo (AlertDialog) para pedir confirmación
     * antes de cancelar y abandonar la sesión de juego.
     */
    public void confirmarSalida() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.salir_modulo);
        builder.setMessage(R.string.confirmar_salida);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Cierra la actividad y vuelve al menú
            }
        });
        builder.setNegativeButton(R.string.cancelar, null);
        builder.show();
    }

    /**
     * Despliega un diálogo con el resumen estadístico de la sesión al finalizar el juego.
     * La propiedad 'setCancelable(false)' obliga al usuario a pulsar el botón para cerrar la pantalla.
     */
    public void mostrarResultado(Sesion sesion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.resultado_titulo);
        builder.setMessage(getString(
                R.string.resultado_detalle,
                sesion.getAciertos(),
                sesion.getErrores(),
                sesion.getTiempoSegundos(),
                sesion.getPuntuacion()
        ));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Finaliza la actividad al aceptar los resultados
            }
        });
        builder.show();
    }

    /**
     * Muestra un mensaje emergente nativo (Toast) en la pantalla.
     */
    public void mostrarMensaje(int recursoTexto) {
        Toast.makeText(this, recursoTexto, Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public MaterialButton[] getBotonesRespuesta() {
        return new MaterialButton[]{btnOpcion1, btnOpcion2, btnOpcion3, btnOpcion4};
    }

    public TextView getTvContador() {
        return tvContador;
    }

    public TextView getTvAsociacionBase() {
        return tvAsociacionBase;
    }

    public MaterialButton getBtnVolver() {
        return btnVolver;
    }
}