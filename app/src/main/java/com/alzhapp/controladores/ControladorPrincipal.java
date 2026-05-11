package com.alzhapp.controladores;

import android.view.View;

import com.alzhapp.R;
import com.alzhapp.vistas.MainActivity;

public class ControladorPrincipal implements View.OnClickListener {

    private MainActivity vista;

    public ControladorPrincipal(MainActivity vista) {
        this.vista = vista;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnModuloImagenes) {
            vista.abrirImagenes();
        } else if (id == R.id.btnModuloPalabras) {
            vista.abrirPalabras();
        } else if (id == R.id.btnModuloAsociacion) {
            vista.abrirAsociacion();
        } else if (id == R.id.btnHistorial) {
            vista.abrirHistorial();
        } else if (id == R.id.btnAjustes) {
            vista.abrirAjustes();
        }
    }
}
