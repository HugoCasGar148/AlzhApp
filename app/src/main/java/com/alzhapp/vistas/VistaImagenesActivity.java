package com.alzhapp.vistas;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.alzhapp.controladores.ControladorImagenes;
import com.alzhapp.modelos.Sesion;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class VistaImagenesActivity extends AppCompatActivity {

    private ControladorImagenes controladorImagenes;
    private TextView tvContador;
    private ImageView imageViewObjetivo;
    private MaterialButton btnOpcion1;
    private MaterialButton btnOpcion2;
    private MaterialButton btnOpcion3;
    private MaterialButton btnOpcion4;
    private MaterialButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_imagenes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootImagenes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();
        prepararSalida();
        controladorImagenes.iniciarEjercicio();
    }

    private void recogerElementos() {
        tvContador = findViewById(R.id.tvContadorImagenes);
        imageViewObjetivo = findViewById(R.id.imageViewObjetivo);
        btnOpcion1 = findViewById(R.id.btnImagenOpcion1);
        btnOpcion2 = findViewById(R.id.btnImagenOpcion2);
        btnOpcion3 = findViewById(R.id.btnImagenOpcion3);
        btnOpcion4 = findViewById(R.id.btnImagenOpcion4);
        btnVolver = findViewById(R.id.btnVolverImagenes);
    }

    private void establecerEscuchadores() {
        controladorImagenes = new ControladorImagenes(this);
        btnOpcion1.setOnClickListener(controladorImagenes);
        btnOpcion2.setOnClickListener(controladorImagenes);
        btnOpcion3.setOnClickListener(controladorImagenes);
        btnOpcion4.setOnClickListener(controladorImagenes);
        btnVolver.setOnClickListener(controladorImagenes);
    }

    private void prepararSalida() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSalida();
            }
        });
    }

    public void mostrarPregunta(ControladorImagenes.PreguntaImagen pregunta, int preguntaActual, int totalPreguntas) {
        tvContador.setText(getString(R.string.pregunta_contador, preguntaActual, totalPreguntas));

        int resId = Resources.getSystem().getIdentifier(pregunta.getDrawableName(), "drawable", "android");
        if (resId == 0) {
            imageViewObjetivo.setImageResource(android.R.drawable.ic_menu_help);
        } else {
            imageViewObjetivo.setImageResource(resId);
        }
        imageViewObjetivo.setContentDescription(getString(R.string.descripcion_imagen_objetivo));

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

    public ImageView getImageViewObjetivo() {
        return imageViewObjetivo;
    }

    public MaterialButton getBtnVolver() {
        return btnVolver;
    }
}
