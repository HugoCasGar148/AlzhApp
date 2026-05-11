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

public class VistaAsociacionActivity extends AppCompatActivity {

    private ControladorAsociacion controladorAsociacion;
    private TextView tvContador;
    private TextView tvAsociacionBase;
    private MaterialButton btnOpcion1;
    private MaterialButton btnOpcion2;
    private MaterialButton btnOpcion3;
    private MaterialButton btnOpcion4;
    private MaterialButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_asociacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootAsociacion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();
        prepararSalida();
        controladorAsociacion.iniciarEjercicio();
    }

    private void recogerElementos() {
        tvContador = findViewById(R.id.tvContadorAsociacion);
        tvAsociacionBase = findViewById(R.id.tvAsociacionBase);
        btnOpcion1 = findViewById(R.id.btnAsociacionOpcion1);
        btnOpcion2 = findViewById(R.id.btnAsociacionOpcion2);
        btnOpcion3 = findViewById(R.id.btnAsociacionOpcion3);
        btnOpcion4 = findViewById(R.id.btnAsociacionOpcion4);
        btnVolver = findViewById(R.id.btnVolverAsociacion);
    }

    private void establecerEscuchadores() {
        controladorAsociacion = new ControladorAsociacion(this);
        btnOpcion1.setOnClickListener(controladorAsociacion);
        btnOpcion2.setOnClickListener(controladorAsociacion);
        btnOpcion3.setOnClickListener(controladorAsociacion);
        btnOpcion4.setOnClickListener(controladorAsociacion);
        btnVolver.setOnClickListener(controladorAsociacion);
    }

    private void prepararSalida() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSalida();
            }
        });
    }

    public void mostrarPregunta(ControladorAsociacion.PreguntaAsociacion pregunta, int preguntaActual, int totalPreguntas) {
        tvContador.setText(getString(R.string.pregunta_contador, preguntaActual, totalPreguntas));
        tvAsociacionBase.setText(getString(R.string.elige_pareja, pregunta.getBase()));
        mostrarOpciones(pregunta.getOpciones());
    }

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

    public TextView getTvAsociacionBase() {
        return tvAsociacionBase;
    }

    public MaterialButton getBtnVolver() {
        return btnVolver;
    }
}
