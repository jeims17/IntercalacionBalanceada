/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucue.p3.Datos;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Paul
 */
public class SeleccionArchivo {
    
    JFileChooser chooser = new JFileChooser();

    public void configurarFileChooser()
    {
            chooser.setDialogTitle("Seleccione su archivo");
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(".csv","csv");

            chooser.setFileFilter(csvFilter);
    }

    public String getPathArchivo(Component parent)
    {
            configurarFileChooser();
            int returnVal = chooser.showOpenDialog(parent);
            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                File archivo = chooser.getSelectedFile();
                if (archivo != null)
                {
                    System.out.println("Archivo cargado satisfactoriamente!");
                    return archivo.toString();
                }
                else
                {
                    System.out.println("No hay un archivo seleccionado!");
                    return null;
                }
            }
            else
            {
                System.out.println("No hay un archivo seleccionado!");
                return null;
            }      
    }
    
}
