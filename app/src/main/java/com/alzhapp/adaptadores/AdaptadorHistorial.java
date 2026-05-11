package com.alzhapp.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alzhapp.R;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaHistorialActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdaptadorHistorial extends BaseAdapter {

    private VistaHistorialActivity actividad;
    private int layoutTarjeta;
    private List<Sesion> sesiones;
    private SimpleDateFormat formatoFecha;

    public AdaptadorHistorial(VistaHistorialActivity actividad, int layoutTarjeta, List<Sesion> sesiones) {
        this.actividad = actividad;
        this.layoutTarjeta = layoutTarjeta;
        this.sesiones = new ArrayList<>();
        if (sesiones != null) {
            this.sesiones.addAll(sesiones);
        }
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @Override
    public int getCount() {
        return sesiones.size();
    }

    @Override
    public Object getItem(int position) {
        return sesiones.get(position);
    }

    @Override
    public long getItemId(int position) {
        return sesiones.get(position).getIdSesion();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ViewHolder holder;

        if (v == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(actividad);
            v = layoutInflater.inflate(layoutTarjeta, parent, false);

            holder = new ViewHolder();
            holder.tvHistorialModulo = v.findViewById(R.id.tvHistorialModulo);
            holder.tvHistorialFecha = v.findViewById(R.id.tvHistorialFecha);
            holder.tvHistorialDatos = v.findViewById(R.id.tvHistorialDatos);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        Sesion sesion = sesiones.get(position);
        holder.tvHistorialModulo.setText(capitalizarModulo(sesion.getModulo()));
        holder.tvHistorialFecha.setText(formatoFecha.format(new Date(sesion.getFechaHora())));
        holder.tvHistorialDatos.setText(
                "Puntuación: " + sesion.getPuntuacion()
                        + " · Tiempo: " + sesion.getTiempoSegundos() + " s"
                        + "\nAciertos: " + sesion.getAciertos()
                        + " · Errores: " + sesion.getErrores()
        );

        return v;
    }

    public void actualizarDatos(List<Sesion> nuevasSesiones) {
        sesiones.clear();
        if (nuevasSesiones != null) {
            sesiones.addAll(nuevasSesiones);
        }
        notifyDataSetChanged();
    }

    private String capitalizarModulo(String modulo) {
        if (modulo == null || modulo.length() == 0) {
            return "";
        }
        return modulo.substring(0, 1).toUpperCase(Locale.getDefault()) + modulo.substring(1);
    }

    private class ViewHolder {
        TextView tvHistorialModulo;
        TextView tvHistorialFecha;
        TextView tvHistorialDatos;
    }
}
