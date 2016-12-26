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

/**
 *
 * @author paul
 */
public class BalanceadoInt extends IntercalacionBalanceada {
    private int topeNumero = 0;                 // El máximo número

    public BalanceadoInt(File f0, JProgressBar barraProgreso, int indice) {
        super(f0, barraProgreso, indice);
    }
    
    //distribuye tramos de flujos de entrada en flujos de salida
    @Override
    protected int distribuir() throws IOException
    {
        int anterior, j, nt;
        int clave;
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
        anterior = 0;
        if ( flujo.readRecord() ) {
            anterior = Integer.parseInt(flujo.getValues()[indice]);
            flujoSalida[j].writeRecord(flujo.getValues());
        }

        // bucle termina con la excepción fin de fichero
        while (flujo.readRecord())
        {
            clave = Integer.parseInt(flujo.getValues()[indice]);
            while (anterior <= clave)
            {
                flujoSalida[j].writeRecord(flujo.getValues());
                System.out.println("tramos " + flujo.getValues()[0]);
                anterior = clave;
                if (flujo.readRecord())
                {
                    clave = Integer.parseInt(flujo.getValues()[indice]);
                }
                else {
                    continuar = false;
                    break;
                }
            }
            nt++;                        // nuevo tramo
            j = (j < N2-1) ? j+1 : 0;	 // siguiente archivo
            if (continuar) {
                flujoSalida[j].writeRecord(flujo.getValues());
                System.out.println("t -> " + flujo.getValues()[0]);
            } else
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
        int m = topeNumero+1;
        
        for ( ; i < n; i++)
        {
            if (activo[i] && Integer.parseInt(r[i][indice]) < m)
            {
                m = Integer.parseInt(r[i][indice]);
                index = i;
            }
        }
        return index;
    }
 
    //asigna el tope de registros para ordenarlos
    @Override
    protected void setTope() 
    {
        CsvReader flujo;
        try {
            flujo = new CsvReader(new FileReader(f0));
            flujo.readHeaders();
            flujo.readRecord();
            topeNumero = Integer.parseInt(flujo.getValues()[indice]);
            while (flujo.readRecord())
            {       
                if ( Integer.parseInt(flujo.getValues()[indice]) > topeNumero )
                    topeNumero = Integer.parseInt(flujo.getValues()[indice]);
                numeroRegistros++;
            }
            //System.out.println("\n *** Fin del archivo ***\n");
            flujo.close();
        }
        catch (IOException eof)
        {
            System.out.println("Archivo sin registros");
        }
    }

    @Override
    protected boolean findeTramo(String[] r, String[] anterior, int indice) {
        
        return ( Integer.parseInt(anterior[indice]) > Integer.parseInt(r[indice]) );  // fin de tramo
    }
    

}
