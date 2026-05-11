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

public class VistaPalabrasActivity extends AppCompatActivity {

    private ControladorPalabras controladorPalabras;
    private TextView tvContador;
    private TextView tvInstruccion;
    private TextView tvPalabra;
    private MaterialButton btnOpcion1;
    private MaterialButton btnOpcion2;
    private MaterialButton btnOpcion3;
    private MaterialButton btnOpcion4;
    private MaterialButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_palabras);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootPalabras), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();
        prepararSalida();
        controladorPalabras.iniciarEjercicio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controladorPalabras != null) {
            controladorPalabras.liberar();
        }
    }

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

    private void establecerEscuchadores() {
        controladorPalabras = new ControladorPalabras(this);
        btnOpcion1.setOnClickListener(controladorPalabras);
        btnOpcion2.setOnClickListener(controladorPalabras);
        btnOpcion3.setOnClickListener(controladorPalabras);
        btnOpcion4.setOnClickListener(controladorPalabras);
        btnVolver.setOnClickListener(controladorPalabras);
    }

    private void prepararSalida() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSalida();
            }
        });
    }

    public void mostrarFaseMemorizacion(ControladorPalabras.PreguntaPalabra pregunta, int preguntaActual, int totalPreguntas) {
        tvContador.setText(getString(R.string.pregunta_contador, preguntaActual, totalPreguntas));
        tvInstruccion.setText(R.string.memoriza);
        tvPalabra.setText(TextUtils.join("\n", pregunta.getPalabrasMostradas()));
        mostrarBotones(false, null);
    }

    public void mostrarFaseRespuesta(List<String> opciones) {
        tvInstruccion.setText(R.string.elige_palabra);
        tvPalabra.setText("");
        mostrarBotones(true, opciones);
    }

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

    public void mostrarMensaje(int recursoTexto) {
        Toast.makeText(this, recursoTexto, Toast.LENGTH_SHORT).show();
    }

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
