package com.alzhapp;

import com.alzhapp.modelos.Configuracion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfiguracionUnitTest {
    @Test
    public void dificultadInvalidaSeNormalizaABaja() {
        Configuracion configuracion = new Configuracion(99, false, "09:00");
        assertEquals(Configuracion.DIFICULTAD_BAJA, configuracion.getDificultad());
    }

    @Test
    public void horaInvalidaSeNormalizaAHoraPredeterminada() {
        Configuracion configuracion = new Configuracion(Configuracion.DIFICULTAD_MEDIA, true, "25:80");
        assertEquals(Configuracion.HORA_PREDETERMINADA, configuracion.getRecordatorioHora());
        assertTrue(configuracion.isRecordatorioActivo());
    }

    @Test
    public void horaValidaSeMantiene() {
        Configuracion configuracion = new Configuracion(Configuracion.DIFICULTAD_ALTA, false, "18:45");
        assertEquals("18:45", configuracion.getRecordatorioHora());
        assertFalse(configuracion.isRecordatorioActivo());
    }
}
