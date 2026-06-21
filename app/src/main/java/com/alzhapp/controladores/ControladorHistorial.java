package com.alzhapp.controladores;

import android.view.View;
import android.widget.AdapterView;

import com.alzhapp.R;
import com.alzhapp.adaptadores.AdaptadorHistorial;
import com.alzhapp.sqlite.GestorDatos;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaHistorialActivity;

import java.util.List;

/**
 * Controlador para la vista de Historial de Sesiones.
 * Implementa la gestión de eventos de la lista de resultados, el filtrado
 * por módulo mediante un menú desplegable (Spinner) y la consulta a la base de datos.
 */
public class ControladorHistorial implements View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {

    // Referencias a los componentes de la arquitectura
    private final VistaHistorialActivity vista;
    private final GestorDatos gestorDatos;
    private final AdaptadorHistorial adaptadorHistorial;

    /**
     * Constructor del controlador.
     * Inicializa la conexión con la capa de datos y crea el adaptador encargado
     * de volcar los registros en el componente visual (ListView).
     */
    public ControladorHistorial(VistaHistorialActivity vista) {
        this.vista = vista;
        this.gestorDatos = new GestorDatos(vista);
        // Carga inicial del historial completo por defecto durante la creación del adaptador
        this.adaptadorHistorial = new AdaptadorHistorial(vista, R.layout.item_historial, gestorDatos.obtenerHistorial());
    }

    /**
     * Intercepta los eventos de clic en los controles generales de la interfaz.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        // Finaliza la actividad actual y retorna a la jerarquía de navegación anterior
        if (id == R.id.btnVolverHistorial) {
            vista.finish();
        }
    }

    /**
     * Listener del componente de filtrado (Spinner).
     * Se invoca cuando se selecciona una opción diferente en el menú desplegable.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Actualiza el listado en función del nuevo criterio de filtrado
        cargarHistorial();
    }

    /**
     * Fallback de seguridad en caso de pérdida de selección activa en el componente de filtrado.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        cargarHistorial();
    }

    /**
     * Listener de los elementos contenidos en el ListView.
     * Captura el evento de selección sobre un registro específico del historial.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Recupera la entidad 'Sesion' correspondiente a la posición seleccionada
        Sesion sesion = (Sesion) adaptadorHistorial.getItem(position);
        // Delega en la vista la transición a la presentación de detalles
        vista.mostrarDetalleSesion(sesion);
    }

    /**
     * Método de inicialización requerido por la vista.
     * Vincula el componente visual (ListView) con el adaptador y ejecuta
     * la carga inicial de registros.
     */
    public void prepararListado() {
        vista.getListViewHistorial().setAdapter(adaptadorHistorial);
        cargarHistorial();
    }

    /**
     * Lógica central de consulta y actualización de la vista.
     * Recupera el estado del filtro activo y solicita los datos correspondientes
     * a la capa de persistencia.
     */
    public void cargarHistorial() {
        // Extracción del criterio seleccionado en la interfaz
        String modulo = vista.obtenerFiltroSeleccionado();
        List<Sesion> sesiones;

        // Si no existe filtro o es "Todos", se solicita el historial íntegro
        if (modulo == null) {
            sesiones = gestorDatos.obtenerHistorial();
        } else {
            // Si existe un filtro específico, se ejecuta la consulta condicionada
            sesiones = gestorDatos.obtenerHistorialPorModulo(modulo);
        }

        // Inyección de los datos actualizados en el adaptador para forzar el repintado
        adaptadorHistorial.actualizarDatos(sesiones);

        // Verificación del estado de la colección para gestionar la visibilidad del mensaje de estado vacío
        vista.mostrarHistorialVacio(sesiones.isEmpty());
    }

    /**
     * Ejecuta la eliminación total de registros de la base de datos y fuerza
     * la actualización inmediata de la interfaz.
     * @return true si la operación de borrado ha sido exitosa en la base de datos.
     */
    public boolean borrarHistorial() {
        boolean borrado = gestorDatos.borrarHistorial();
        cargarHistorial(); // Refresco del estado visual tras la transacción
        return borrado;
    }

    /**
     * Expone el adaptador para requerimientos de manipulación externa o pruebas unitarias.
     */
    public AdaptadorHistorial getAdaptadorHistorial() {
        return adaptadorHistorial;
    }
}