package com.alzhapp.controladores;

import android.view.View;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.EjercicioAsociacion;
import com.alzhapp.sqlite.GestorSQLite;
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

public class ControladorAsociacion implements View.OnClickListener {

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

    private VistaAsociacionActivity vista;
    private GestorSQLite gestorSQLite;
    private Configuracion configuracion;
    private EjercicioAsociacion ejercicio;
    private List<PreguntaAsociacion> preguntas;
    private Random random;
    private int indiceActual;

    public ControladorAsociacion(VistaAsociacionActivity vista) {
        this.vista = vista;
        this.gestorSQLite = new GestorSQLite(vista);
        this.configuracion = gestorSQLite.obtenerConfiguracion();
        this.ejercicio = new EjercicioAsociacion(configuracion.getDificultad());
        this.random = new Random();
        this.preguntas = new ArrayList<>();
    }

    public void iniciarEjercicio() {
        ejercicio.iniciarSesion();
        preguntas = generarPreguntas(gestorSQLite.obtenerItemsPorModulo("asociacion"));
        indiceActual = 0;
        mostrarPreguntaActual();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnVolverAsociacion) {
            vista.confirmarSalida();
        } else if (view instanceof MaterialButton) {
            String respuesta = ((MaterialButton) view).getText().toString();
            responder(respuesta);
        }
    }

    private List<PreguntaAsociacion> generarPreguntas(List<ItemCatalogo> items) {
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

        List<Integer> idsGrupo = new ArrayList<>(grupos.keySet());
        Collections.shuffle(idsGrupo, random);

        List<String> respuestas = new ArrayList<>();
        for (Integer idGrupo : idsGrupo) {
            List<String> pareja = grupos.get(idGrupo);
            if (pareja != null && pareja.size() >= 2) {
                respuestas.add(pareja.get(1));
            }
        }

        List<PreguntaAsociacion> lista = new ArrayList<>();
        int totalPreguntas = obtenerTotalPreguntas();

        for (int i = 0; i < idsGrupo.size() && lista.size() < totalPreguntas; i++) {
            List<String> pareja = grupos.get(idsGrupo.get(i));
            if (pareja == null || pareja.size() < 2) {
                continue;
            }

            String base = pareja.get(0);
            String correcta = pareja.get(1);
            Set<String> opcionesSet = new HashSet<>();
            opcionesSet.add(correcta);

            while (opcionesSet.size() < obtenerNumeroOpciones() && opcionesSet.size() < respuestas.size()) {
                opcionesSet.add(respuestas.get(random.nextInt(respuestas.size())));
            }

            List<String> opciones = new ArrayList<>(opcionesSet);
            Collections.shuffle(opciones, random);
            lista.add(new PreguntaAsociacion(base, correcta, opciones));
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

    private void mostrarPreguntaActual() {
        PreguntaAsociacion pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
        } else {
            vista.mostrarPregunta(pregunta, getIndiceVisible(), getTotalPreguntas());
        }
    }

    private PreguntaAsociacion getPreguntaActual() {
        if (indiceActual >= preguntas.size()) {
            return null;
        }
        return preguntas.get(indiceActual);
    }

    private void responder(String respuesta) {
        PreguntaAsociacion pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
            return;
        }

        boolean correcta = pregunta.getCorrecta().equalsIgnoreCase(respuesta);
        ejercicio.registrarRespuesta(correcta);
        indiceActual++;
        vista.mostrarMensaje(correcta ? R.string.seleccion_correcta : R.string.seleccion_incorrecta);
        mostrarPreguntaActual();
    }

    private int getIndiceVisible() {
        return Math.min(indiceActual + 1, preguntas.size());
    }

    private int getTotalPreguntas() {
        return preguntas.size();
    }

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
        gestorSQLite.insertarSesion(sesion);
        vista.mostrarResultado(sesion);
    }
}
