package com.alzhapp.controladores;

import android.view.View;
import android.widget.AdapterView;

import com.alzhapp.R;
import com.alzhapp.adaptadores.AdaptadorHistorial;
import com.alzhapp.sqlite.GestorDatos;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaHistorialActivity;

import java.util.List;

public class ControladorHistorial implements View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {

    private final VistaHistorialActivity vista;
    private final GestorDatos gestorDatos;
    private final AdaptadorHistorial adaptadorHistorial;

    public ControladorHistorial(VistaHistorialActivity vista) {
        this.vista = vista;
        this.gestorDatos = new GestorDatos(vista);
        this.adaptadorHistorial = new AdaptadorHistorial(vista, R.layout.item_historial, gestorDatos.obtenerHistorial());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnVolverHistorial) {
            vista.finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        cargarHistorial();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        cargarHistorial();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sesion sesion = (Sesion) adaptadorHistorial.getItem(position);
        vista.mostrarDetalleSesion(sesion);
    }

    public void prepararListado() {
        vista.getListViewHistorial().setAdapter(adaptadorHistorial);
        cargarHistorial();
    }

    public void cargarHistorial() {
        String modulo = vista.obtenerFiltroSeleccionado();
        List<Sesion> sesiones;

        if (modulo == null) {
            sesiones = gestorDatos.obtenerHistorial();
        } else {
            sesiones = gestorDatos.obtenerHistorialPorModulo(modulo);
        }

        adaptadorHistorial.actualizarDatos(sesiones);
        vista.mostrarHistorialVacio(sesiones.isEmpty());
    }

    public boolean borrarHistorial() {
        boolean borrado = gestorDatos.borrarHistorial();
        cargarHistorial();
        return borrado;
    }

    public AdaptadorHistorial getAdaptadorHistorial() {
        return adaptadorHistorial;
    }
}