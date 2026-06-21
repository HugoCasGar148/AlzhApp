package com.alzhapp.vistas;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.alzhapp.controladores.ControladorPalabras;
import com.alzhapp.modelos.Sesion;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Actividad que representa la pantalla del ejercicio de memoria de Palabras.
 * Actúa como la "Vista" en la arquitectura MVC/MVP, encargándose de actualizar la interfaz gráfica
 * en sus distintas fases (memorización y respuesta) y derivar los eventos al ControladorPalabras.
 */
public class VistaPalabrasActivity extends AppCompatActivity {

    // Referencia al controlador de la lógica de negocio
    private ControladorPalabras controladorPalabras;

    // Componentes de la interfaz de usuario
    private TextView tvContador;
    private TextView tvInstruccion;
    private TextView tvPalabra;
    private MaterialButton btnOpcion1;
    private MaterialButton btnOpcion2;
    private MaterialButton btnOpcion3;
    private MaterialButton btnOpcion4;
    private MaterialButton btnVolver;

    /**
     * Método del ciclo de vida ejecutado al crear la actividad.
     * Configura el diseño Edge-to-Edge, inicializa los componentes de la vista y arranca el ejercicio.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_palabras);

        // Ajusta el padding de la vista raíz para evitar solapamientos con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootPalabras), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();
        prepararSalida();

        // Delega en el controlador el inicio de la primera ronda
        controladorPalabras.iniciarEjercicio();
    }

    /**
     * Método del ciclo de vida invocado al destruir la actividad.
     * Obliga al controlador a cancelar cualquier temporizador (Handler) pendiente
     * para evitar excepciones o fugas de memoria (Memory Leaks).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controladorPalabras != null) {
            controladorPalabras.liberar();
        }
    }

    /**
     * Vincula las variables locales con los componentes definidos en el archivo XML (layout).
     */
    private void recogerElementos() {
        tvContador = findViewById(R.id.tvContadorPalabras);
        tvInstruccion = findViewById(R.id.tvInstruccionPalabras);
        tvPalabra = findViewById(R.id.tvPalabraObjetivo);
        btnOpcion1 = findViewById(R.id.btnPalabraOpcion1);
        btnOpcion2 = findViewById(R.id.btnPalabraOpcion2);
        btnOpcion3 = findViewById(R.id.btnPalabraOpcion3);
        btnOpcion4 = findViewById(R.id.btnPalabraOpcion4);
        btnVolver = findViewById(R.id.btnVolverPalabras);
    }

    /**
     * Instancia el controlador y lo asigna como listener para los eventos de clic en los botones.
     */
    private void establecerEscuchadores() {
        controladorPalabras = new ControladorPalabras(this);
        btnOpcion1.setOnClickListener(controladorPalabras);
        btnOpcion2.setOnClickListener(controladorPalabras);
        btnOpcion3.setOnClickListener(controladorPalabras);
        btnOpcion4.setOnClickListener(controladorPalabras);
        btnVolver.setOnClickListener(controladorPalabras);
    }

    /**
     * Intercepta el gesto o botón de retroceso nativo del dispositivo (OnBackPressedDispatcher).
     * Deriva la acción a un diálogo de confirmación en lugar de cerrar la vista directamente.
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
     * Configura la interfaz para la primera fase de la ronda (Memorización).
     * Muestra la lista de palabras a recordar y oculta los botones de respuesta.
     */
    public void mostrarFaseMemorizacion(ControladorPalabras.PreguntaPalabra pregunta, int preguntaActual, int totalPreguntas) {
        tvContador.setText(getString(R.string.pregunta_contador, preguntaActual, totalPreguntas));
        tvInstruccion.setText(R.string.memoriza);
        tvPalabra.setText(TextUtils.join("\n", pregunta.getPalabrasMostradas()));
        mostrarBotones(false, null);
    }

    /**
     * Configura la interfaz para la segunda fase de la ronda (Respuesta).
     * Oculta las palabras objetivo y hace visibles los botones con las alternativas.
     */
    public void mostrarFaseRespuesta(List<String> opciones) {
        tvInstruccion.setText(R.string.elige_palabra);
        tvPalabra.setText(""); // Limpia el texto central
        mostrarBotones(true, opciones);
    }

    /**
     * Gestiona dinámicamente la visibilidad y el texto de los botones de opciones.
     * @param visibles Define si los botones deben mostrarse en pantalla.
     * @param opciones Lista de cadenas de texto a asignar a cada botón visible.
     */
    public void mostrarBotones(boolean visibles, List<String> opciones) {
        MaterialButton[] botones = getBotonesRespuesta();
        for (int i = 0; i < botones.length; i++) {
            if (visibles && opciones != null && i < opciones.size()) {
                botones[i].setVisibility(View.VISIBLE);
                botones[i].setText(opciones.get(i));
            } else {
                botones[i].setVisibility(View.GONE);
            }
        }
    }

    /**
     * Despliega un cuadro de diálogo nativo (AlertDialog) solicitando confirmación
     * antes de cancelar y abandonar la sesión de juego.
     */
    public void confirmarSalida() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.salir_modulo);
        builder.setMessage(R.string.confirmar_salida);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancelar, null);
        builder.show();
    }

    /**
     * Muestra un diálogo modal con las estadísticas de la partida finalizada.
     * Requiere interacción explícita del usuario para cerrarse debido a setCancelable(false).
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
                finish();
            }
        });
        builder.show();
    }

    /**
     * Despliega un mensaje emergente nativo (Toast) en la pantalla.
     */
    public void mostrarMensaje(int recursoTexto) {
        Toast.makeText(this, recursoTexto, Toast.LENGTH_SHORT).show();
    }

    // =========================================================
    // GETTERS
    // Proveen acceso a los componentes visuales desde el controlador
    // =========================================================

    public MaterialButton[] getBotonesRespuesta() {
        return new MaterialButton[]{btnOpcion1, btnOpcion2, btnOpcion3, btnOpcion4};
    }

    public TextView getTvContador() {
        return tvContador;
    }

    public TextView getTvInstruccion() {
        return tvInstruccion;
    }

    public TextView getTvPalabra() {
        return tvPalabra;
    }

    public MaterialButton getBtnVolver() {
        return btnVolver;
    }
}