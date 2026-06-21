package com.alzhapp.controladores;

import android.view.View;

import com.alzhapp.R;
import com.alzhapp.vistas.MainActivity;

/**
 * Controlador para la vista principal de la aplicación.
 * Implementa el patrón de arquitectura MVC/MVP para la gestión de eventos de la interfaz
 * y la delegación del enrutamiento de navegación hacia los distintos módulos operativos.
 */
public class ControladorPrincipal implements View.OnClickListener {

    // Referencia a la capa de presentación para la invocación de métodos de navegación
    private final MainActivity vista;

    /**
     * Constructor del controlador.
     * Implementa la inyección de la dependencia visual para habilitar la comunicación entre capas.
     * @param vista Instancia de la actividad principal de la aplicación.
     */
    public ControladorPrincipal(MainActivity vista) {
        this.vista = vista;
    }

    /**
     * Intercepta los eventos de interacción originados en el menú principal.
     * Implementa la lógica de enrutamiento evaluando el identificador del componente accionado
     * para delegar la transición de estado a la vista correspondiente.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Lógica de enrutamiento condicional basada en el identificador del componente
        if (id == R.id.btnModuloImagenes) {
            vista.abrirImagenes();    // Transición al módulo de reconocimiento visual
        } else if (id == R.id.btnModuloPalabras) {
            vista.abrirPalabras();    // Transición al módulo de memoria a corto plazo
        } else if (id == R.id.btnModuloAsociacion) {
            vista.abrirAsociacion();  // Transición al módulo de lógica y relación de conceptos
        } else if (id == R.id.btnHistorial) {
            vista.abrirHistorial();   // Transición a la vista de métricas y registros históricos
        } else if (id == R.id.btnAjustes) {
            vista.abrirAjustes();     // Transición a la vista de configuración global
        }
    }
}