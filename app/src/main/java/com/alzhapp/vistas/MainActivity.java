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

/**
 * Actividad principal que sirve como punto de entrada y menú general de la aplicación.
 * Actúa como la "Vista" en el patrón MVC/MVP, delegando la gestión de eventos de usuario
 * al ControladorPrincipal y encargándose exclusivamente de la navegación (Intents) y la UI.
 */
public class MainActivity extends AppCompatActivity {

    // Referencia al controlador que maneja la lógica de esta pantalla
    private ControladorPrincipal controladorPrincipal;

    // Referencias a los componentes de la interfaz de usuario
    private ImageButton btnAjustes;
    private MaterialButton btnModuloImagenes;
    private MaterialButton btnModuloPalabras;
    private MaterialButton btnModuloAsociacion;
    private MaterialButton btnHistorial;

    /**
     * Método del ciclo de vida de Android llamado al crear la actividad.
     * Inicializa el diseño, los componentes visuales y solicita los permisos base.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Habilita el diseño Edge-to-Edge para que la app se dibuje detrás de las barras del sistema
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajusta el padding de la vista principal para evitar que los elementos queden ocultos
        // bajo la barra de estado (arriba) o la barra de navegación (abajo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicialización de componentes e interacciones
        recogerElementos();
        establecerEscuchadores();

        // Solicita el permiso de notificaciones en dispositivos con Android 13 (API 33) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ControladorAjustes.solicitarPermisoNotificaciones(this);
        }
    }

    /**
     * Vincula las variables de clase con los elementos definidos en el archivo XML del layout.
     */
    private void recogerElementos() {
        btnAjustes = findViewById(R.id.btnAjustes);
        btnModuloImagenes = findViewById(R.id.btnModuloImagenes);
        btnModuloPalabras = findViewById(R.id.btnModuloPalabras);
        btnModuloAsociacion = findViewById(R.id.btnModuloAsociacion);
        btnHistorial = findViewById(R.id.btnHistorial);
    }

    /**
     * Instancia el controlador y lo asigna como el 'Listener' para los eventos de clic
     * de todos los botones de navegación.
     */
    private void establecerEscuchadores() {
        controladorPrincipal = new ControladorPrincipal(this);
        btnAjustes.setOnClickListener(controladorPrincipal);
        btnModuloImagenes.setOnClickListener(controladorPrincipal);
        btnModuloPalabras.setOnClickListener(controladorPrincipal);
        btnModuloAsociacion.setOnClickListener(controladorPrincipal);
        btnHistorial.setOnClickListener(controladorPrincipal);
    }

    // =========================================================
    // MÉTODOS DE NAVEGACIÓN (Llamados por el ControladorPrincipal)
    // Utilizan Intents para iniciar nuevas actividades
    // =========================================================

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

    // =========================================================
    // GETTERS
    // Permiten al controlador acceder a las vistas si fuera necesario
    // =========================================================

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