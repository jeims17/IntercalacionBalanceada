/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucue.p3.Logica;

import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/**
 *
 * @author paul
 */
public class Proceso implements ChangeListener {
    private final int progreso;
    
    public Proceso(int progreso) {
        this.progreso = progreso;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JProgressBar barraProgreso = (JProgressBar) e.getSource();
        barraProgreso.setString( "Progreso: " + progreso + "%" );
        barraProgreso.setValue(progreso);
    }
    
}
