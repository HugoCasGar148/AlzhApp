package com.alzhapp.controladores;

import android.view.View;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.Dificultad;
import com.alzhapp.modelos.EjercicioImagenes;
import com.alzhapp.sqlite.GestorDatos;
import com.alzhapp.modelos.ItemCatalogo;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaImagenesActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Controlador principal para la vista del módulo de Ejercicio de Imágenes.
 * Gestiona el control de flujo de la sesión, la instanciación de preguntas basadas
 * en el catálogo de recursos gráficos, la validación de las interacciones del usuario
 * y la persistencia de las métricas resultantes.
 */
public class ControladorImagenes implements View.OnClickListener {

    /**
     * Estructura de datos (POJO) representativa de una iteración del ejercicio.
     * Encapsula la referencia al recurso gráfico, la etiqueta de resolución correcta
     * y el conjunto de alternativas (distractores) asociadas.
     */
    public static class PreguntaImagen {
        private String etiquetaCorrecta;
        private String drawableName;
        private List<String> opciones;

        public PreguntaImagen() {
        }

        public PreguntaImagen(String etiquetaCorrecta, String drawableName, List<String> opciones) {
            this.etiquetaCorrecta = etiquetaCorrecta;
            this.drawableName = drawableName;
            this.opciones = opciones;
        }

        public String getEtiquetaCorrecta() {
            return etiquetaCorrecta;
        }

        public void setEtiquetaCorrecta(String etiquetaCorrecta) {
            this.etiquetaCorrecta = etiquetaCorrecta;
        }

        public String getDrawableName() {
            return drawableName;
        }

        public void setDrawableName(String drawableName) {
            this.drawableName = drawableName;
        }

        public List<String> getOpciones() {
            return opciones;
        }

        public void setOpciones(List<String> opciones) {
            this.opciones = opciones;
        }
    }

    // Referencias a los componentes del patrón arquitectónico
    private final VistaImagenesActivity vista;
    private final GestorDatos gestorDatos;
    private final Configuracion configuracion;
    private final EjercicioImagenes ejercicio;

    // Variables de estado del ciclo de vida de la sesión
    private List<PreguntaImagen> preguntas;
    private final Random random;
    private int indiceActual;

    /**
     * Constructor del controlador.
     * Establece las dependencias de persistencia y configuración, e inicializa
     * el modelo de dominio en función de los parámetros de dificultad activos.
     */
    public ControladorImagenes(VistaImagenesActivity vista) {
        this.vista = vista;
        this.gestorDatos = new GestorDatos(vista);
        this.configuracion = gestorDatos.obtenerConfiguracion();

        // Extracción del valor numérico para la instanciación del modelo de negocio
        this.ejercicio = new EjercicioImagenes(configuracion.getDificultad().getValor());
        this.random = new Random();
        this.preguntas = new ArrayList<>();
    }

    /**
     * Secuencia de inicialización del ejercicio.
     * Dispara el registro temporal, recupera la colección de entidades desde la
     * capa de persistencia y transfiere la primera iteración a la interfaz.
     */
    public void iniciarEjercicio() {
        ejercicio.iniciarSesion();
        preguntas = generarPreguntas(gestorDatos.obtenerItemsPorModulo("imagenes"));
        indiceActual = 0;
        mostrarPreguntaActual();
    }

    /**
     * Intercepta y procesa los eventos generados por los componentes visuales interactivos.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Flujo de terminación anticipada de la sesión
        if (id == R.id.btnVolverImagenes) {
            vista.confirmarSalida();
        }
        // Flujo de evaluación de respuesta
        else if (view instanceof MaterialButton) {
            String respuesta = ((MaterialButton) view).getText().toString();
            responder(respuesta);
        }
    }

    /**
     * Motor lógico para la compilación dinámica de las rondas de ejercicio.
     * Implementa la selección de la imagen objetivo y la extracción de distractores
     * garantizando la unicidad de las opciones de respuesta.
     * * @param items Colección base de entidades extraídas de la base de datos.
     * @return Lista parametrizada y aleatorizada de objetos PreguntaImagen.
     */
    private List<PreguntaImagen> generarPreguntas(List<ItemCatalogo> items) {
        int totalPreguntas = obtenerTotalPreguntas();

        // Creación de una copia aislada de la colección para preservar el estado original
        List<ItemCatalogo> copia = new ArrayList<>(items);
        Collections.shuffle(copia, random);

        List<PreguntaImagen> lista = new ArrayList<>();

        for (int i = 0; i < copia.size() && lista.size() < totalPreguntas; i++) {
            ItemCatalogo correcto = copia.get(i);

            // Procesamiento de la cadena de almacenamiento estructurada
            String[] partesCorrectas = extraerPartesImagen(correcto.getRecurso());
            if (partesCorrectas == null) {
                continue; // Descarte lógico de registros no conformes
            }

            String etiquetaCorrecta = partesCorrectas[0];
            String drawableName = partesCorrectas[1];

            // Uso de una estructura tipo Set para prevenir colisiones o duplicidades en las opciones
            Set<String> opcionesSet = new HashSet<>();
            opcionesSet.add(etiquetaCorrecta);

            // Inserción iterativa de distractores desde el catálogo general
            while (opcionesSet.size() < obtenerNumeroOpciones() && opcionesSet.size() < items.size()) {
                ItemCatalogo candidato = items.get(random.nextInt(items.size()));
                String[] partesCandidato = extraerPartesImagen(candidato.getRecurso());
                if (partesCandidato != null) {
                    opcionesSet.add(partesCandidato[0]);
                }
            }

            // Transformación del Set a List para permitir la ordenación aleatoria final
            List<String> opciones = new ArrayList<>(opcionesSet);
            Collections.shuffle(opciones, random);

            lista.add(new PreguntaImagen(etiquetaCorrecta, drawableName, opciones));
        }
        return lista;
    }

    /**
     * Algoritmo de extracción de subcadenas.
     * Segmenta el campo de la base de datos utilizando el separador de tubería (pipe)
     * para disociar la etiqueta semántica del identificador de recurso.
     */
    private String[] extraerPartesImagen(String recurso) {
        if (recurso == null) {
            return null;
        }

        String[] partes = recurso.split("\\|");
        if (partes.length < 2 || partes[0].trim().isEmpty() || partes[1].trim().isEmpty()) {
            return null;
        }
        return new String[]{partes[0].trim(), partes[1].trim()};
    }

    /**
     * Calcula la magnitud de la sesión en base a los parámetros de configuración en vigor.
     */
    private int obtenerTotalPreguntas() {
        if (configuracion.getDificultad() == Dificultad.MEDIA) {
            return 4;
        } else if (configuracion.getDificultad() == Dificultad.ALTA) {
            return 5;
        }
        return 3;
    }

    /**
     * Define la multiplicidad de las alternativas generadas por iteración.
     */
    private int obtenerNumeroOpciones() {
        if (configuracion.getDificultad() == Dificultad.ALTA) {
            return 4;
        }
        return 3;
    }

    /**
     * Control de estado para la transición de vista.
     * Delega la visualización del estado actual o desencadena el cierre si la colección se ha agotado.
     */
    private void mostrarPreguntaActual() {
        PreguntaImagen pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
        } else {
            vista.mostrarPregunta(pregunta, getIndiceVisible(), getTotalPreguntas());
        }
    }

    /**
     * Recupera el modelo de la iteración en función del puntero actual.
     */
    private PreguntaImagen getPreguntaActual() {
        if (indiceActual >= preguntas.size()) {
            return null;
        }
        return preguntas.get(indiceActual);
    }

    /**
     * Procesa y valida la cadena suministrada por el evento de la interfaz contra la constante correcta.
     * Registra la interacción en el modelo y fuerza la transición al estado posterior.
     */
    private void responder(String respuesta) {
        PreguntaImagen pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        boolean correcta = pregunta.getEtiquetaCorrecta().equalsIgnoreCase(respuesta);
        ejercicio.registrarRespuesta(correcta);
        indiceActual++;

        vista.mostrarMensaje(correcta ? R.string.seleccion_correcta : R.string.seleccion_incorrecta);
        mostrarPreguntaActual();
    }

    /**
     * Función utilitaria para proveer a la interfaz de un índice de presentación (1-based index).
     */
    private int getIndiceVisible() {
        return Math.min(indiceActual + 1, preguntas.size());
    }

    /**
     * Función utilitaria para devolver la cardinalidad total de la sesión.
     */
    private int getTotalPreguntas() {
        return preguntas.size();
    }

    /**
     * Finaliza la temporización de la sesión e instancia el modelo de datos de resultado.
     * Invoca la escritura asíncrona en base de datos y la transición de finalización.
     */
    private void finalizarSesion() {
        ejercicio.finalizarSesion();

        Sesion sesion = new Sesion(
                System.currentTimeMillis(),
                ejercicio.getModulo(),
                ejercicio.getDificultad(),
                ejercicio.calcularPuntuacion(),
                ejercicio.getTiempoSegundos(),
                ejercicio.getAciertos(),
                ejercicio.getErrores()
        );

        gestorDatos.insertarSesion(sesion);
        vista.mostrarResultado(sesion);
    }
}