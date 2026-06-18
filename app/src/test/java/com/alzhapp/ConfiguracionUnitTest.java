package com.alzhapp;

import com.alzhapp.modelos.Configuracion;
import com.alzhapp.modelos.Dificultad;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfiguracionUnitTest {
    @Test
    public void dificultadInvalidaSeNormalizaABaja() {
        // Yo le paso el int 99, y compruebo que el sistema lo normaliza al Enum Dificultad.BAJA
        Configuracion configuracion = new Configuracion(99, false, "09:00");
        assertEquals(Dificultad.BAJA, configuracion.getDificultad());
    }

    @Test
    public void horaInvalidaSeNormalizaAHoraPredeterminada() {
        // Yo utilizo el Enum directamente aquí
        Configuracion configuracion = new Configuracion(Dificultad.MEDIA, true, "25:80");
        assertEquals(Configuracion.HORA_PREDETERMINADA, configuracion.getRecordatorioHora());
        assertTrue(configuracion.isRecordatorioActivo());
    }

    @Test
    public void horaValidaSeMantiene() {
        // Yo utilizo el Enum directamente aquí también
        Configuracion configuracion = new Configuracion(Dificultad.ALTA, false, "18:45");
        assertEquals("18:45", configuracion.getRecordatorioHora());
        assertFalse(configuracion.isRecordatorioActivo());
    }
}