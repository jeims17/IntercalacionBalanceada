/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucue.p3.Logica;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JProgressBar;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author paul
 */
public class BalanceadoString extends IntercalacionBalanceada { 
    private String topeString;                  // El máximo string
    private int topeTamString = 0;              // El máximo tamaño del string

    public BalanceadoString(File f0, JProgressBar barraProgreso, int indice) {
        super(f0, barraProgreso, indice);
    }
    
    //distribuye tramos de flujos de entrada en flujos de salida
    @Override
    protected int distribuir() throws IOException
    {
        int j, nt;
        String anterior = null;
        String clave;
        boolean continuar = true;
        CsvReader flujo = new CsvReader(new FileReader(f0));
        flujo.readHeaders();
        CsvWriter flujoSalida[] = new CsvWriter[N2];
        for (j = 0; j < N2; j++)
        {
            flujoSalida[j] = new CsvWriter(new FileWriter(f[j]), ',');
        }
        
        j = 0; 		 // indice del flujo de salida
        nt = 0;
        if ( flujo.readRecord() ) {
            anterior = flujo.getValues()[indice];
            flujoSalida[j].writeRecord(flujo.getValues());
        }

        // bucle termina con la excepción fin de fichero
        while (flujo.readRecord())
        {
            clave = flujo.getValues()[indice];
            while (anterior.compareTo(clave) <= 0)
            {
                flujoSalida[j].writeRecord(flujo.getValues());
                anterior = clave;
                if (flujo.readRecord())
                {
                    clave = flujo.getValues()[indice];
                }
                else {
                    continuar = false;
                    break;
                }
            }
            nt++;                        // nuevo tramo
            j = (j < N2-1) ? j+1 : 0;	 // siguiente archivo
            if (continuar)
                flujoSalida[j].writeRecord(flujo.getValues());
            else
                break;
            anterior = clave;
        }
        
        nt++;		 // cuenta ultimo tramo
        System.out.println("\n*** Número de tramos: " + nt + " ***");
        flujo.close();
        for (j = 0; j < N2; j++)
            flujoSalida[j].close();
        return nt;
        
    }
    
    //devuelve el índice del menor valor del array de claves
    @Override
    protected int minimo(String [][] r, boolean [] activo, int n)
    {
        int i, index;
        i = index = 0;
        String m = topeString;
        
        for ( ; i < n; i++)
        {
            if (activo[i] && r[i][indice].compareTo(m) < 0)
            {
                m = r[i][indice];
                index = i;
            }
        }
        return index;
    }
    
    //asigna el tope de registros para ordenarlos
    @Override
    protected void setTope()
    {
        CsvReader flujo = null;
        try {
            flujo = new CsvReader(new FileReader(f0));
            flujo.readHeaders();
            flujo.readRecord();
            topeTamString = flujo.getValues()[indice].length();
            while (flujo.readRecord())
            {       
                if ( flujo.getValues()[indice].length() > topeTamString )
                    topeTamString = flujo.getValues()[indice].length();
                numeroRegistros++;
            }
            //System.out.println("\n *** Fin del archivo ***\n");
            flujo.close();
            topeString = StringUtils.repeat("z", topeTamString+1);
        }
        catch (IOException eof)
        {
            System.out.println("Archivo sin registros");
        }
    }

    @Override
    protected boolean findeTramo(String[] r, String[] anterior, int indice) {
        return (anterior[indice].compareTo(r[indice]) > 0);
    }
    
}
