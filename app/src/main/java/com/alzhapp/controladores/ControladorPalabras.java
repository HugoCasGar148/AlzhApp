package com.alzhapp.controladores;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.Dificultad;
import com.alzhapp.modelos.EjercicioPalabras;
import com.alzhapp.sqlite.GestorDatos;
import com.alzhapp.modelos.ItemCatalogo;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaPalabrasActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Controlador principal para el módulo de Ejercicio de Palabras.
 * Implementa un flujo de ejecución temporizado: presenta un conjunto de términos
 * para su memorización, los oculta tras un intervalo definido, y requiere la
 * identificación del término original entre un conjunto de opciones.
 */
public class ControladorPalabras implements View.OnClickListener {

    // Constantes de temporización (milisegundos)
    private static final long TIEMPO_MEMORIZACION_MS = 4000L;
    private static final long TIEMPO_RESPUESTA_MS = 10000L;

    /**
     * Estructura de datos (POJO) que modela el estado de una iteración del ejercicio.
     */
    public static class PreguntaPalabra {
        private List<String> palabrasMostradas;
        private String palabraCorrecta;
        private List<String> opciones;

        public PreguntaPalabra() {
        }

        public PreguntaPalabra(List<String> palabrasMostradas, String palabraCorrecta, List<String> opciones) {
            this.palabrasMostradas = palabrasMostradas;
            this.palabraCorrecta = palabraCorrecta;
            this.opciones = opciones;
        }

        public List<String> getPalabrasMostradas() {
            return palabrasMostradas;
        }

        public void setPalabrasMostradas(List<String> palabrasMostradas) {
            this.palabrasMostradas = palabrasMostradas;
        }

        public String getPalabraCorrecta() {
            return palabraCorrecta;
        }

        public void setPalabraCorrecta(String palabraCorrecta) {
            this.palabraCorrecta = palabraCorrecta;
        }

        public List<String> getOpciones() {
            return opciones;
        }

        public void setOpciones(List<String> opciones) {
            this.opciones = opciones;
        }
    }

    // Dependencias de arquitectura
    private final VistaPalabrasActivity vista;
    private final GestorDatos gestorDatos;
    private final Configuracion configuracion;
    private final EjercicioPalabras ejercicio;

    // Estado interno y control de flujo
    private List<PreguntaPalabra> preguntas;
    private final Random random;
    // Componente para la calendarización de tareas en el hilo de la interfaz de usuario
    private final Handler handler;
    private int indiceActual;

    /**
     * Constructor del controlador.
     * Inicializa las dependencias, el modelo de datos y el planificador de tareas (Handler).
     */
    public ControladorPalabras(VistaPalabrasActivity vista) {
        this.vista = vista;
        this.gestorDatos = new GestorDatos(vista);
        this.configuracion = gestorDatos.obtenerConfiguracion();

        // Extracción del valor entero para la instanciación del modelo subyacente
        this.ejercicio = new EjercicioPalabras(configuracion.getDificultad().getValor());
        this.random = new Random();
        // Vinculación del Handler al MainLooper para garantizar la sincronización visual
        this.handler = new Handler(Looper.getMainLooper());
        this.preguntas = new ArrayList<>();
    }

    /**
     * Secuencia de inicialización de la sesión.
     * Consulta el catálogo en la capa de datos y desencadena la fase de memorización inicial.
     */
    public void iniciarEjercicio() {
        ejercicio.iniciarSesion();
        preguntas = generarPreguntas(gestorDatos.obtenerItemsPorModulo("palabras"));
        indiceActual = 0;
        mostrarFaseMemorizacion();
    }

    /**
     * Intercepta y procesa los eventos originados en la interfaz de usuario.
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnVolverPalabras) {
            vista.confirmarSalida();
        } else if (view instanceof MaterialButton) {
            String respuesta = ((MaterialButton) view).getText().toString();
            responder(respuesta);
        }
    }

    /**
     * Algoritmo de compilación dinámica de rondas de ejercicio.
     * Ejecuta la selección de los términos objetivo y la extracción de distractores,
     * garantizando la exclusión mutua entre los elementos mostrados y las opciones falsas.
     * @param items Colección de entidades extraídas de la base de datos.
     * @return Lista parametrizada y aleatorizada de objetos PreguntaPalabra.
     */
    private List<PreguntaPalabra> generarPreguntas(List<ItemCatalogo> items) {
        // Conversión del catálogo a una estructura simple, omitiendo registros nulos o vacíos
        List<String> todasLasPalabras = new ArrayList<>();
        for (ItemCatalogo item : items) {
            if (item.getRecurso() != null && !item.getRecurso().trim().isEmpty()) {
                todasLasPalabras.add(item.getRecurso().trim());
            }
        }

        int totalPreguntas = obtenerTotalPreguntas();
        int totalOpciones = obtenerNumeroOpciones();
        List<PreguntaPalabra> lista = new ArrayList<>();

        for (int i = 0; i < totalPreguntas && !todasLasPalabras.isEmpty(); i++) {
            // Generación de una copia aislada de la colección para cada iteración
            List<String> copia = new ArrayList<>(todasLasPalabras);
            Collections.shuffle(copia, random);

            // Fase 1: Selección de los términos objetivo para la memorización
            int numeroPalabrasMemorizacion = Math.min(3, copia.size());
            List<String> palabrasMostradas = new ArrayList<>(copia.subList(0, numeroPalabrasMemorizacion));

            // Fase 2: Selección aleatoria de la respuesta correcta entre los términos objetivo
            String correcta = palabrasMostradas.get(random.nextInt(palabrasMostradas.size()));

            // Fase 3: Extracción de distractores condicionados (ausentes en la fase de memorización)
            List<String> distractoras = new ArrayList<>();
            for (String palabra : todasLasPalabras) {
                if (!palabrasMostradas.contains(palabra)) {
                    distractoras.add(palabra);
                }
            }
            Collections.shuffle(distractoras, random);

            // Fase 4: Construcción del conjunto de opciones final (correcta + distractores)
            List<String> opciones = new ArrayList<>();
            opciones.add(correcta);
            for (String distractora : distractoras) {
                if (opciones.size() >= totalOpciones) {
                    break;
                }
                if (!opciones.contains(distractora)) {
                    opciones.add(distractora);
                }
            }

            Collections.shuffle(opciones, random);
            lista.add(new PreguntaPalabra(palabrasMostradas, correcta, opciones));
        }
        return lista;
    }

    /**
     * Determina la magnitud de la sesión según los parámetros de configuración en vigor.
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
     * Define la multiplicidad de alternativas generadas por iteración.
     */
    private int obtenerNumeroOpciones() {
        if (configuracion.getDificultad() == Dificultad.ALTA) {
            return 4;
        }
        return 3;
    }

    /**
     * Transición a la fase inicial de la iteración.
     * Presenta los términos objetivo y programa la transición asíncrona a la fase de evaluación.
     */
    private void mostrarFaseMemorizacion() {
        // Cancelación de temporizadores activos para prevenir fugas de ejecución
        handler.removeCallbacksAndMessages(null);

        PreguntaPalabra pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        vista.mostrarFaseMemorizacion(pregunta, getIndiceVisible(), getTotalPreguntas());

        // Programación de la transición de estado
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarOpciones();
            }
        }, TIEMPO_MEMORIZACION_MS);
    }

    /**
     * Transición a la fase de evaluación de la iteración.
     * Oculta los términos objetivo, presenta las alternativas y programa la caducidad del tiempo de respuesta.
     */
    private void mostrarOpciones() {
        PreguntaPalabra pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        vista.mostrarFaseRespuesta(pregunta.getOpciones());

        // Activación del temporizador límite para la recepción de eventos del usuario
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                agotarTiempoRespuesta();
            }
        }, TIEMPO_RESPUESTA_MS);
    }

    /**
     * Callback ejecutado al expirar el tiempo de respuesta asignado.
     * Computa el evento como un error en el modelo y fuerza la transición a la siguiente iteración.
     */
    private void agotarTiempoRespuesta() {
        if (getPreguntaActual() == null) {
            return;
        }

        ejercicio.registrarSinRespuesta();
        indiceActual++;
        vista.mostrarMensaje(R.string.tiempo_agotado);
        mostrarFaseMemorizacion();
    }

    /**
     * Retorna el modelo de la iteración correspondiente al puntero actual.
     */
    private PreguntaPalabra getPreguntaActual() {
        if (indiceActual >= preguntas.size()) {
            return null;
        }
        return preguntas.get(indiceActual);
    }

    /**
     * Procesa la cadena provista por el evento de la interfaz.
     * Cancela la temporización en curso, evalúa la validez de la selección,
     * actualiza el modelo y ejecuta la transición a la iteración posterior.
     */
    private void responder(String respuesta) {
        // Interrupción inmediata de la temporización límite
        handler.removeCallbacksAndMessages(null);

        PreguntaPalabra pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        boolean correcta = pregunta.getPalabraCorrecta().equalsIgnoreCase(respuesta);
        ejercicio.registrarRespuesta(correcta);
        indiceActual++;

        vista.mostrarMensaje(correcta ? R.string.seleccion_correcta : R.string.seleccion_incorrecta);
        mostrarFaseMemorizacion();
    }

    private int getIndiceVisible() {
        return Math.min(indiceActual + 1, preguntas.size());
    }

    private int getTotalPreguntas() {
        return preguntas.size();
    }

    /**
     * Secuencia de terminación de la sesión.
     * Cancela operaciones asíncronas pendientes, instancia el modelo de resultados,
     * delega la persistencia al GestorDatos y transfiere el control a la vista de resultados.
     */
    private void finalizarSesion() {
        // Limpieza de la cola de mensajes del Handler
        handler.removeCallbacksAndMessages(null);

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

    /**
     * Interfaz de gestión del ciclo de vida.
     * Requerido durante la destrucción o pausa de la actividad anfitriona para
     * invalidar callbacks pendientes e impedir excepciones de tipo Memory Leak o NullReference.
     */
    public void liberar() {
        handler.removeCallbacksAndMessages(null);
    }
}