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

/**
 * Adaptador personalizado para la lista del historial.
 * Se encarga de transformar los datos de los objetos 'Sesion' en vistas (tarjetas visuales)
 * para mostrarlos dentro de un ListView u otro componente similar.
 */
public class AdaptadorHistorial extends BaseAdapter {

    // Referencia a la actividad donde se va a mostrar la lista
    private final VistaHistorialActivity actividad;
    // ID del diseño XML (layout) que representa una única fila de la lista
    private final int layoutTarjeta;
    // Lista interna donde se almacenan los datos de las sesiones a mostrar
    private final List<Sesion> sesiones;
    // Formateador de fechas instanciado a nivel de clase para optimizar memoria
    // (evita crear uno nuevo por cada fila de la lista)
    private final SimpleDateFormat formatoFecha;

    /**
     * Constructor del adaptador.
     * Prepara la lista de datos y previene errores en caso de recibir valores nulos.
     */
    public AdaptadorHistorial(VistaHistorialActivity actividad, int layoutTarjeta, List<Sesion> sesiones) {
        this.actividad = actividad;
        this.layoutTarjeta = layoutTarjeta;
        this.sesiones = new ArrayList<>();

        // Programación defensiva: aseguramos que no dé un NullPointerException si la lista viene vacía
        if (sesiones != null) {
            this.sesiones.addAll(sesiones);
        }

        // Inicializamos el formateador usando la configuración regional (idioma) del teléfono
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    // Indica a la lista cuántos elementos en total hay que dibujar
    @Override
    public int getCount() {
        return sesiones.size();
    }

    // Devuelve el objeto 'Sesion' exacto que corresponde a la posición solicitada
    @Override
    public Object getItem(int position) {
        return sesiones.get(position);
    }

    // Devuelve el identificador único (ID de base de datos) del elemento en esa posición
    @Override
    public long getItemId(int position) {
        return sesiones.get(position).getIdSesion();
    }

    /**
     * El corazón del adaptador: construye y devuelve la vista de cada fila.
     * Implementa el patrón de diseño "ViewHolder" para reciclar las vistas de la memoria.
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ViewHolder holder;

        // Si la vista es nula, es una fila nueva y debemos "inflarla" (crearla desde el XML)
        if (v == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(actividad);
            v = layoutInflater.inflate(layoutTarjeta, parent, false);

            // Creamos el contenedor (holder) y buscamos los TextViews por su ID (solo esta vez)
            holder = new ViewHolder();
            holder.tvHistorialModulo = v.findViewById(R.id.tvHistorialModulo);
            holder.tvHistorialFecha = v.findViewById(R.id.tvHistorialFecha);
            holder.tvHistorialDatos = v.findViewById(R.id.tvHistorialDatos);

            // Guardamos el holder dentro de la vista usando 'setTag' para reutilizarlo luego
            v.setTag(holder);
        } else {
            // Si la vista no es nula, la estamos reciclando al hacer scroll.
            // Recuperamos el holder y nos ahorramos llamadas costosas a findViewById()
            holder = (ViewHolder) v.getTag();
        }

        // Rescatamos los datos que tocan en la fila actual
        Sesion sesion = sesiones.get(position);

        // Volcamos los datos en la interfaz gráfica
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

    /**
     * Método para refrescar la información en pantalla sin crear un adaptador nuevo.
     * Limpia la lista anterior, añade los nuevos datos y avisa a la interfaz.
     */
    public void actualizarDatos(List<Sesion> nuevasSesiones) {
        sesiones.clear();
        if (nuevasSesiones != null) {
            sesiones.addAll(nuevasSesiones);
        }
        // Dispara la recarga gráfica del ListView
        notifyDataSetChanged();
    }

    /**
     * Función auxiliar para formatear textos.
     * Se asegura de que la primera letra del nombre del módulo esté en mayúscula.
     */
    private String capitalizarModulo(String modulo) {
        if (modulo == null || modulo.length() == 0) {
            return "";
        }
        return modulo.substring(0, 1).toUpperCase(Locale.getDefault()) + modulo.substring(1);
    }

    /**
     * Clase interna para el Patrón ViewHolder.
     * Cachea (guarda) las referencias a los elementos visuales de la fila.
     */
    private class ViewHolder {
        TextView tvHistorialModulo;
        TextView tvHistorialFecha;
        TextView tvHistorialDatos;
    }
}