package com.alzhapp.controladores;

import android.view.View;

import com.alzhapp.R;
import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.EjercicioImagenes;
import com.alzhapp.sqlite.GestorSQLite;
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

public class ControladorImagenes implements View.OnClickListener {

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

    private VistaImagenesActivity vista;
    private GestorSQLite gestorSQLite;
    private Configuracion configuracion;
    private EjercicioImagenes ejercicio;
    private List<PreguntaImagen> preguntas;
    private Random random;
    private int indiceActual;

    public ControladorImagenes(VistaImagenesActivity vista) {
        this.vista = vista;
        this.gestorSQLite = new GestorSQLite(vista);
        this.configuracion = gestorSQLite.obtenerConfiguracion();
        this.ejercicio = new EjercicioImagenes(configuracion.getDificultad());
        this.random = new Random();
        this.preguntas = new ArrayList<>();
    }

    public void iniciarEjercicio() {
        ejercicio.iniciarSesion();
        preguntas = generarPreguntas(gestorSQLite.obtenerItemsPorModulo("imagenes"));
        indiceActual = 0;
        mostrarPreguntaActual();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btnVolverImagenes) {
            vista.confirmarSalida();
        } else if (view instanceof MaterialButton) {
            String respuesta = ((MaterialButton) view).getText().toString();
            responder(respuesta);
        }
    }

    private List<PreguntaImagen> generarPreguntas(List<ItemCatalogo> items) {
        int totalPreguntas = obtenerTotalPreguntas();
        List<ItemCatalogo> copia = new ArrayList<>(items);
        Collections.shuffle(copia, random);
        List<PreguntaImagen> lista = new ArrayList<>();

        for (int i = 0; i < copia.size() && lista.size() < totalPreguntas; i++) {
            ItemCatalogo correcto = copia.get(i);
            String[] partesCorrectas = extraerPartesImagen(correcto.getRecurso());
            if (partesCorrectas == null) {
                continue;
            }

            String etiquetaCorrecta = partesCorrectas[0];
            String drawableName = partesCorrectas[1];
            Set<String> opcionesSet = new HashSet<>();
            opcionesSet.add(etiquetaCorrecta);

            while (opcionesSet.size() < obtenerNumeroOpciones() && opcionesSet.size() < items.size()) {
                ItemCatalogo candidato = items.get(random.nextInt(items.size()));
                String[] partesCandidato = extraerPartesImagen(candidato.getRecurso());
                if (partesCandidato != null) {
                    opcionesSet.add(partesCandidato[0]);
                }
            }

            List<String> opciones = new ArrayList<>(opcionesSet);
            Collections.shuffle(opciones, random);
            lista.add(new PreguntaImagen(etiquetaCorrecta, drawableName, opciones));
        }
        return lista;
    }

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
        PreguntaImagen pregunta = getPreguntaActual();
        if (pregunta == null) {
            finalizarSesion();
        } else {
            vista.mostrarPregunta(pregunta, getIndiceVisible(), getTotalPreguntas());
        }
    }

    private PreguntaImagen getPreguntaActual() {
        if (indiceActual >= preguntas.size()) {
            return null;
        }
        return preguntas.get(indiceActual);
    }

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
