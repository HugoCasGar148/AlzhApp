package com.alzhapp.controladores;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.EjercicioPalabras;
import com.alzhapp.sqlite.GestorSQLite;
import com.alzhapp.modelos.ItemCatalogo;
import com.alzhapp.modelos.Sesion;
import com.alzhapp.vistas.VistaPalabrasActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ControladorPalabras implements View.OnClickListener {

    private static final long TIEMPO_MEMORIZACION_MS = 4000L;
    private static final long TIEMPO_RESPUESTA_MS = 10000L;

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

    private VistaPalabrasActivity vista;
    private GestorSQLite gestorSQLite;
    private Configuracion configuracion;
    private EjercicioPalabras ejercicio;
    private List<PreguntaPalabra> preguntas;
    private Random random;
    private Handler handler;
    private int indiceActual;

    public ControladorPalabras(VistaPalabrasActivity vista) {
        this.vista = vista;
        this.gestorSQLite = new GestorSQLite(vista);
        this.configuracion = gestorSQLite.obtenerConfiguracion();
        this.ejercicio = new EjercicioPalabras(configuracion.getDificultad());
        this.random = new Random();
        this.handler = new Handler(Looper.getMainLooper());
        this.preguntas = new ArrayList<>();
    }

    public void iniciarEjercicio() {
        ejercicio.iniciarSesion();
        preguntas = generarPreguntas(gestorSQLite.obtenerItemsPorModulo("palabras"));
        indiceActual = 0;
        mostrarFaseMemorizacion();
    }

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

    private List<PreguntaPalabra> generarPreguntas(List<ItemCatalogo> items) {
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
            List<String> copia = new ArrayList<>(todasLasPalabras);
            Collections.shuffle(copia, random);

            int numeroPalabrasMemorizacion = Math.min(3, copia.size());
            List<String> palabrasMostradas = new ArrayList<>(copia.subList(0, numeroPalabrasMemorizacion));
            String correcta = palabrasMostradas.get(random.nextInt(palabrasMostradas.size()));

            List<String> distractoras = new ArrayList<>();
            for (String palabra : todasLasPalabras) {
                if (!palabrasMostradas.contains(palabra)) {
                    distractoras.add(palabra);
                }
            }
            Collections.shuffle(distractoras, random);

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

    private int obtenerTotalPreguntas() {
        if (configuracion.getDificultad() == Configuracion.DIFICULTAD_MEDIA) {
            return 4;
        } else if (configuracion.getDificultad() == Configuracion.DIFICULTAD_ALTA) {
            return 5;
        }
        return 3;
    }

    private int obtenerNumeroOpciones() {
        if (configuracion.getDificultad() == Configuracion.DIFICULTAD_ALTA) {
            return 4;
        }
        return 3;
    }

    private void mostrarFaseMemorizacion() {
        handler.removeCallbacksAndMessages(null);
        PreguntaPalabra pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        vista.mostrarFaseMemorizacion(pregunta, getIndiceVisible(), getTotalPreguntas());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarOpciones();
            }
        }, TIEMPO_MEMORIZACION_MS);
    }

    private void mostrarOpciones() {
        PreguntaPalabra pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        vista.mostrarFaseRespuesta(pregunta.getOpciones());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                agotarTiempoRespuesta();
            }
        }, TIEMPO_RESPUESTA_MS);
    }

    private void agotarTiempoRespuesta() {
        if (getPreguntaActual() == null) {
            return;
        }

        ejercicio.registrarSinRespuesta();
        indiceActual++;
        vista.mostrarMensaje(R.string.tiempo_agotado);
        mostrarFaseMemorizacion();
    }

    private PreguntaPalabra getPreguntaActual() {
        if (indiceActual >= preguntas.size()) {
            return null;
        }
        return preguntas.get(indiceActual);
    }

    private void responder(String respuesta) {
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

    private void finalizarSesion() {
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
        gestorSQLite.insertarSesion(sesion);
        vista.mostrarResultado(sesion);
    }

    public void liberar() {
        handler.removeCallbacksAndMessages(null);
    }
}
