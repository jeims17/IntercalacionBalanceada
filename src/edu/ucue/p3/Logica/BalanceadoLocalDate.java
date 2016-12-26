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
import java.time.LocalDate;
import javax.swing.JProgressBar;

/**
 *
 * @author paul
 */
public class BalanceadoLocalDate extends IntercalacionBalanceada {
    private LocalDate topeFecha;                // La fecha máxima
    private int topeAnio = 0;                   // El máximo año de la fecha

    public BalanceadoLocalDate(File f0, JProgressBar barraProgreso, int indice) {
        super(f0, barraProgreso, indice);
    }
    
    @Override
    protected int distribuir() throws IOException
    {
        int j, nt;
        LocalDate anterior = null;
        LocalDate clave;
        String[] aux;
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
            aux = flujo.getValues()[indice].split("/");
            anterior = LocalDate.of(Integer.parseInt(aux[2]),
                                    Integer.parseInt(aux[1]),
                                    Integer.parseInt(aux[0]));
            flujoSalida[j].writeRecord(flujo.getValues());
        }

        // bucle termina con la excepción fin de fichero
        while (flujo.readRecord())
        {
            aux = flujo.getValues()[indice].split("/");
            clave = LocalDate.of(Integer.parseInt(aux[2]),
                                    Integer.parseInt(aux[1]),
                                    Integer.parseInt(aux[0]));
            while (anterior.compareTo(clave) <= 0)
            {
                flujoSalida[j].writeRecord(flujo.getValues());
                anterior = clave;
                if (flujo.readRecord())
                {
                    aux = flujo.getValues()[indice].split("/");
                    clave = LocalDate.of(Integer.parseInt(aux[2]),
                                            Integer.parseInt(aux[1]),
                                            Integer.parseInt(aux[0]));
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
    
    @Override
    protected int minimo(String [][] r, boolean [] activo, int n)
    {
        int i, index;
        i = index = 0;
        LocalDate m = topeFecha;
        String[] aux;
        LocalDate actual;
        
        for ( ; i < n; i++)
        {
            aux = r[i][indice].split("/");
            actual = LocalDate.of(Integer.parseInt(aux[2]),
                                        Integer.parseInt(aux[1]),
                                        Integer.parseInt(aux[0]));
            if (activo[i] && actual.compareTo(m) < 0 )
            {
                m = actual;
                index = i;
            }
        }
        return index;
    }
    
    @Override
    protected void setTope()
    {
        CsvReader flujo = null;
        int anio;
        try {
            flujo = new CsvReader(new FileReader(f0));
            flujo.readHeaders();
            flujo.readRecord();
            topeAnio = Integer.parseInt( flujo.getValues()[indice].split("/")[2] );
            while (flujo.readRecord())
            {   
                anio = Integer.parseInt( flujo.getValues()[indice].split("/")[2] );
                if ( anio > topeAnio )
                    topeAnio = anio;
                numeroRegistros++;
            }
            //System.out.println("\n *** Fin del archivo ***\n");
            flujo.close();
            topeFecha = LocalDate.of(topeAnio+1, 1, 1);
        }
        catch (IOException eof)
        {
            System.out.println("Archivo sin registros");
        }
    }

    @Override
    protected boolean findeTramo(String[] r, String[] anterior, int indice) {
        String [] aux;
        LocalDate fAnterior;
        LocalDate fActual;
        
        aux = anterior[indice].split("/");
        fAnterior = LocalDate.of(Integer.parseInt(aux[2]),
                                Integer.parseInt(aux[1]),
                                Integer.parseInt(aux[0]));
        aux = r[indice].split("/");
        fActual = LocalDate.of(Integer.parseInt(aux[2]),
                                Integer.parseInt(aux[1]),
                                Integer.parseInt(aux[0]));
        return ( fAnterior.compareTo(fActual) > 0 );
    }  
    
}
