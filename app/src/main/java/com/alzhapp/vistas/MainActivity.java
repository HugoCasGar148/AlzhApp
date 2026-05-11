package com.alzhapp.vistas;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alzhapp.R;
import com.alzhapp.controladores.ControladorAjustes;
import com.alzhapp.controladores.ControladorPrincipal;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private ControladorPrincipal controladorPrincipal;
    private ImageButton btnAjustes;
    private MaterialButton btnModuloImagenes;
    private MaterialButton btnModuloPalabras;
    private MaterialButton btnModuloAsociacion;
    private MaterialButton btnHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recogerElementos();
        establecerEscuchadores();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ControladorAjustes.solicitarPermisoNotificaciones(this);
        }
    }

    private void recogerElementos() {
        btnAjustes = findViewById(R.id.btnAjustes);
        btnModuloImagenes = findViewById(R.id.btnModuloImagenes);
        btnModuloPalabras = findViewById(R.id.btnModuloPalabras);
        btnModuloAsociacion = findViewById(R.id.btnModuloAsociacion);
        btnHistorial = findViewById(R.id.btnHistorial);
    }

    private void establecerEscuchadores() {
        controladorPrincipal = new ControladorPrincipal(this);
        btnAjustes.setOnClickListener(controladorPrincipal);
        btnModuloImagenes.setOnClickListener(controladorPrincipal);
        btnModuloPalabras.setOnClickListener(controladorPrincipal);
        btnModuloAsociacion.setOnClickListener(controladorPrincipal);
        btnHistorial.setOnClickListener(controladorPrincipal);
    }

    public void abrirImagenes() {
        startActivity(new Intent(this, VistaImagenesActivity.class));
    }

    public void abrirPalabras() {
        startActivity(new Intent(this, VistaPalabrasActivity.class));
    }

    public void abrirAsociacion() {
        startActivity(new Intent(this, VistaAsociacionActivity.class));
    }

    public void abrirHistorial() {
        startActivity(new Intent(this, VistaHistorialActivity.class));
    }

    public void abrirAjustes() {
        startActivity(new Intent(this, VistaAjustesActivity.class));
    }

    public ImageButton getBtnAjustes() {
        return btnAjustes;
    }

    public MaterialButton getBtnModuloImagenes() {
        return btnModuloImagenes;
    }

    public MaterialButton getBtnModuloPalabras() {
        return btnModuloPalabras;
    }

    public MaterialButton getBtnModuloAsociacion() {
        return btnModuloAsociacion;
    }

    public MaterialButton getBtnHistorial() {
        return btnHistorial;
    }
}
