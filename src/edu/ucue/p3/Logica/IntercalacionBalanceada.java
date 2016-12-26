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


/**
 *
 * @author paul
 */
public abstract class IntercalacionBalanceada {
    protected final int N = 6;             // El número de archivos auxiliares
    protected final int N2 = N/2;          // El número de archivos de escritura o lectura
    protected final int indice;                   // El campo que se desea ordenar
    protected final javax.swing.JProgressBar barraProgreso;   
    protected int numeroRegistros = 1;            // El número de registros
    
    
    protected int proceso = 0;                    // El contador para generar el porcentaje
    protected int totalTramos;                    // El número total de tramos generado
    protected File f0;                            // El archivo a ordenar
    protected File []f = new File[N];             // Los archivo auxiliares
    
    public IntercalacionBalanceada(File f0, javax.swing.JProgressBar barraProgreso, int indice)
    {
        String[] nomf = {"ar1.csv", "ar2.csv", "ar3.csv", "ar4.csv", "ar5.csv", "ar6.csv"};
        for (int i = 0; i < N; i++)
            f[i] = new File(nomf[i]);
        this.f0 = f0;
        this.barraProgreso = barraProgreso;
        this.indice = indice;
    }
    
    //método de ordenación
     public void balanceado() throws IOException
    {
        int i, j, k1, progreso, t = 0;
        String [] anterior;
        int [] c = new int[N];
        int [] cd = new int[N];
        String [][] r = new String[N2][];

        Object [] flujos = new Object[N];
        CsvReader flujoEntradaActual = null;
        CsvWriter flujoSalidaActual = null;
        boolean [] actvs = new boolean[N2];

        
        numeroRegistros = 1;
        
        // distribución inicial de tramos desde archivo origen
        try {
            setTope();
            t = distribuir();
            
            for (i = 0; i < N; i++)
                c[i] = i;
            
            proceso = 0;
            // bucle hasta número de tramos == 1: archivo ordenado
            do {
                k1 = (t < N2) ? t : N2;
                for (i = 0; i < k1; i++)
                {
                    flujos[c[i]] = new CsvReader(new FileReader(f[c[i]]));
                    cd[i] = c[i];
                }
                j = N2 ; // índice de archivo de salida
                t = 0;
                for (i = j; i < N; i++)
                    flujos[c[i]] = new CsvWriter(new FileWriter(f[c[i]]), ',');
                
                // entrada de una clave de cada flujo
                for (int n = 0; n < k1; n++)
                {
                    flujoEntradaActual = (CsvReader) flujos[cd[n]];
                    flujoEntradaActual.readRecord();
                    r[n] = flujoEntradaActual.getValues();	
                }

                while (k1 > 0)
                {
                    t++; // mezcla de otro tramo
                    for (i = 0; i < k1; i++) {
                        actvs[i] = true;
                    }
                    flujoSalidaActual = (CsvWriter) flujos[c[j]];
                    //System.out.println(" Archivo: " + c[j]);

                    while (!finDeTramos(actvs, k1))
                    {
                        int n;
                        n = minimo(r, actvs, k1);
                        //System.out.println(n);
                        flujoEntradaActual = (CsvReader) flujos[cd[n]];
                        flujoSalidaActual.writeRecord(r[n]);
                        //System.out.println("primero "+r[n][0]);
                        proceso++;

                        //System.out.println("Porcentaje: " + ( (proceso*50/NumReg)/(Math.log10(NumReg)) ) +"%");
                        //System.out.println(r[n]);
                        anterior = r[n];

                        if (flujoEntradaActual.readRecord()) {
                            r[n] = flujoEntradaActual.getValues();
                            //System.out.println("later "+r[n][0]);
                            if ( findeTramo(r[n], anterior, indice) ) {
                                //System.out.println("tramo inactivo -> " + n);
                                actvs[n] = false;
                            }
                            
                        } else {
                            k1--;
                            flujoEntradaActual.close();
                            cd[n] = cd[k1];
                            r[n] = r[k1];
                            actvs[n] = actvs[k1];
                            actvs[k1] = false;// no se accede a posición k1
                        }

                    }
                    
                    progreso = (int) ( (proceso*50/numeroRegistros)/(Math.log10(numeroRegistros)) );
                    barraProgreso.addChangeListener(new Proceso(proceso));
                    //System.out.println(progreso);
                    j = (j < N-1) ? j+1 : N2; // siguiente flujo de salida

                }

                
                for (i = N2; i < N; i++)
                {
                    flujoSalidaActual = (CsvWriter) flujos[c[i]];
                    flujoSalidaActual.close();
                }
                
                //Cambio de finalidad de los flujos: entrada<->salida
                
                for (i = 0; i < N2; i++)
                {
                    int a;
                    a = c[i];
                    c[i] = c[i+N2];
                    c[i+N2] 	= a;
                }
            } while (t > 1);
            System.out.print("Archivo ordenado ...");
            //escribir(f[c[0]]);
        }	
        catch (IOException er)
        {
            er.printStackTrace();
        }

    }
   
    //distribuye tramos de flujos de entrada en flujos de salida
    protected abstract int distribuir() throws IOException;
    
    //devuelve el índice del menor valor del array de claves
    protected abstract int minimo(String [][] r, boolean [] activo, int n);
    
    //asigna el tope de registros para ordenarlos
    protected abstract void setTope();
    
    //comprueba el fin de tramo
    protected abstract boolean findeTramo(String [] r, String[] anterior, int indice);
    
    //devuelve true si no hay tramo activo
    protected boolean finDeTramos(boolean [] activo, int n)
    {
        boolean s = true;

        for(int k = 0; k < n; k++)
        {
            if (activo[k])	
                s = false;
        }
        return s;
    }
    
    //escribe las claves del archivo
    public void escribir(File f)
    {
        CsvReader flujo;
        try {
            flujo = new CsvReader(new FileReader(f));
            //flujo.readHeaders();
            while (flujo.readRecord())
            {     
                System.out.println(flujo.getRawRecord());               
            }
            System.out.println("\n *** Fin del archivo ***\n");
            flujo.close();
        }
        catch (IOException eof)
        {
            System.out.println("Archivo sin registros");
        }
    }
    
}
