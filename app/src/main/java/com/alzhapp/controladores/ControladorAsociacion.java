package com.alzhapp.controladores;

import android.view.View;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.Dificultad;
import com.alzhapp.modelos.EjercicioAsociacion;
import com.alzhapp.sqlite.GestorDatos;
import com.alzhapp.modelos.ItemCatalogo;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaAsociacionActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Controlador principal para la vista de Asociación.
 * Implementa la gestión del flujo de ejecución, la generación aleatoria de preguntas
 * basada en el nivel de dificultad, la validación de respuestas y la persistencia
 * de los resultados al finalizar la sesión.
 */
public class ControladorAsociacion implements View.OnClickListener {

    /**
     * Estructura de datos (POJO) que representa una pregunta generada.
     * Encapsula el elemento base, la respuesta correcta y las opciones generadas como distractores.
     */
    public static class PreguntaAsociacion {
        private String base;
        private String correcta;
        private List<String> opciones;

        public PreguntaAsociacion() {
        }

        public PreguntaAsociacion(String base, String correcta, List<String> opciones) {
            this.base = base;
            this.correcta = correcta;
            this.opciones = opciones;
        }

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getCorrecta() {
            return correcta;
        }

        public void setCorrecta(String correcta) {
            this.correcta = correcta;
        }

        public List<String> getOpciones() {
            return opciones;
        }

        public void setOpciones(List<String> opciones) {
            this.opciones = opciones;
        }
    }

    // Dependencias de arquitectura y configuración
    private final VistaAsociacionActivity vista;
    private final GestorDatos gestorDatos;
    private final Configuracion configuracion;

    // Componente lógico para el seguimiento de métricas (puntuación, tiempo, aciertos)
    private final EjercicioAsociacion ejercicio;

    // Estado interno de la sesión en curso
    private List<PreguntaAsociacion> preguntas;
    private final Random random;
    private int indiceActual; // Puntero de navegación sobre la lista de preguntas

    /**
     * Constructor del controlador.
     * Inicializa las dependencias, instanciando el modelo de ejercicio según
     * el nivel de dificultad almacenado en la configuración global.
     */
    public ControladorAsociacion(VistaAsociacionActivity vista) {
        this.vista = vista;
        this.gestorDatos = new GestorDatos(vista);
        this.configuracion = gestorDatos.obtenerConfiguracion();

        // Se extrae el valor entero para mantener la compatibilidad con el constructor de Ejercicio
        this.ejercicio = new EjercicioAsociacion(configuracion.getDificultad().getValor());
        this.random = new Random();
        this.preguntas = new ArrayList<>();
    }

    /**
     * Punto de entrada para la ejecución del ejercicio.
     * Inicia el registro de tiempo, consulta el catálogo en la capa de datos
     * y desencadena la generación dinámica de la lista de preguntas.
     */
    public void iniciarEjercicio() {
        ejercicio.iniciarSesion();
        preguntas = generarPreguntas(gestorDatos.obtenerItemsPorModulo("asociacion"));
        indiceActual = 0;
        mostrarPreguntaActual();
    }

    /**
     * Intercepta los eventos de interacción en la interfaz de usuario.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Control de salida de la sesión en curso
        if (id == R.id.btnVolverAsociacion) {
            vista.confirmarSalida();
        }
        // Procesamiento de selección de respuesta
        else if (view instanceof MaterialButton) {
            String respuesta = ((MaterialButton) view).getText().toString();
            responder(respuesta);
        }
    }

    /**
     * Algoritmo de generación de preguntas de opción múltiple.
     * Realiza la agrupación de elementos por compatibilidad relacional y extrae
     * distractores aleatorios para completar el listado de opciones.
     * * @param items Lista de elementos extraídos de la base de datos.
     * @return Colección de objetos PreguntaAsociacion listos para su renderizado.
     */
    private List<PreguntaAsociacion> generarPreguntas(List<ItemCatalogo> items) {
        // Fase 1: Agrupación de ítems por su identificador relacional
        Map<Integer, List<String>> grupos = new HashMap<>();
        for (ItemCatalogo item : items) {
            Integer grupo = item.getGrupoPareja();
            if (grupo != null) {
                if (!grupos.containsKey(grupo)) {
                    grupos.put(grupo, new ArrayList<String>());
                }
                grupos.get(grupo).add(item.getRecurso());
            }
        }

        // Extracción y mezcla de los identificadores de grupo para aleatorizar la sesión
        List<Integer> idsGrupo = new ArrayList<>(grupos.keySet());
        Collections.shuffle(idsGrupo, random);

        // Fase 2: Creación de un conjunto global de respuestas válidas para su uso como distractores
        List<String> respuestas = new ArrayList<>();
        for (Integer idGrupo : idsGrupo) {
            List<String> pareja = grupos.get(idGrupo);
            // Validación de integridad relacional (mínimo un elemento base y una respuesta)
            if (pareja != null && pareja.size() >= 2) {
                respuestas.add(pareja.get(1));
            }
        }

        List<PreguntaAsociacion> lista = new ArrayList<>();
        int totalPreguntas = obtenerTotalPreguntas();

        // Fase 3: Construcción iterativa de preguntas hasta alcanzar el límite fijado por la dificultad
        for (int i = 0; i < idsGrupo.size() && lista.size() < totalPreguntas; i++) {
            List<String> pareja = grupos.get(idsGrupo.get(i));
            if (pareja == null || pareja.size() < 2) {
                continue; // Omisión de registros relacionales incompletos
            }

            String base = pareja.get(0);
            String correcta = pareja.get(1);

            // Utilización de Set para prevenir la duplicidad de opciones en la interfaz
            Set<String> opcionesSet = new HashSet<>();
            opcionesSet.add(correcta); // Inclusión garantizada de la respuesta correcta

            // Inserción iterativa de distractores aleatorios
            while (opcionesSet.size() < obtenerNumeroOpciones() && opcionesSet.size() < respuestas.size()) {
                opcionesSet.add(respuestas.get(random.nextInt(respuestas.size())));
            }

            // Conversión a List y mezcla para aleatorizar la posición de la respuesta correcta
            List<String> opciones = new ArrayList<>(opcionesSet);
            Collections.shuffle(opciones, random);

            lista.add(new PreguntaAsociacion(base, correcta, opciones));
        }
        return lista;
    }

    /**
     * Determina la longitud de la sesión basándose en la configuración de dificultad.
     */
    private int obtenerTotalPreguntas() {
        if (configuracion.getDificultad() == Dificultad.MEDIA) {
            return 4;
        } else if (configuracion.getDificultad() == Dificultad.ALTA) {
            return 5;
        }
        return 3; // Límite inferior predeterminado
    }

    /**
     * Determina la cantidad de alternativas mostradas por pregunta.
     */
    private int obtenerNumeroOpciones() {
        if (configuracion.getDificultad() == Dificultad.ALTA) {
            return 4;
        }
        return 3;
    }

    /**
     * Transfiere el modelo de la pregunta actual a la vista.
     * Invoca la finalización de la sesión si se ha agotado el listado de preguntas.
     */
    private void mostrarPreguntaActual() {
        PreguntaAsociacion pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
        } else {
            vista.mostrarPregunta(pregunta, getIndiceVisible(), getTotalPreguntas());
        }
    }

    /**
     * Retorna el modelo de datos correspondiente al índice de iteración actual.
     */
    private PreguntaAsociacion getPreguntaActual() {
        if (indiceActual >= preguntas.size()) {
            return null;
        }
        return preguntas.get(indiceActual);
    }

    /**
     * Ejecuta la validación de la selección del usuario.
     * Actualiza el modelo de estadísticas subyacente e incrementa el índice de la sesión.
     */
    private void responder(String respuesta) {
        PreguntaAsociacion pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        // Validación insensible a formato de capitalización
        boolean correcta = pregunta.getCorrecta().equalsIgnoreCase(respuesta);
        ejercicio.registrarRespuesta(correcta);

        indiceActual++;

        vista.mostrarMensaje(correcta ? R.string.seleccion_correcta : R.string.seleccion_incorrecta);
        mostrarPreguntaActual();
    }

    /**
     * Helper para la representación del índice en formato 1-based index en la UI.
     */
    private int getIndiceVisible() {
        return Math.min(indiceActual + 1, preguntas.size());
    }

    /**
     * Helper para la obtención del tamaño del listado de preguntas generado.
     */
    private int getTotalPreguntas() {
        return preguntas.size();
    }

    /**
     * Finaliza la ejecución del ejercicio, consolida las métricas en un objeto Sesion,
     * delega la persistencia al GestorDatos y ordena la transición a la vista de resultados.
     */
    private void finalizarSesion() {
        ejercicio.finalizarSesion(); // Detiene el registro de tiempo y calcula la puntuación

        // Construcción de la entidad para el registro en el historial
        Sesion sesion = new Sesion(
                System.currentTimeMillis(),
                ejercicio.getModulo(),
                ejercicio.getDificultad(),
                ejercicio.calcularPuntuacion(),
                ejercicio.getTiempoSegundos(),
                ejercicio.getAciertos(),
                ejercicio.getErrores()
        );

        gestorDatos.insertarSesion(sesion); // Inserción en SQLite
        vista.mostrarResultado(sesion); // Transición de estado visual
    }
}