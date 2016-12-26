/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucue.p3.Logica;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import edu.ucue.p3.Datos.RandomDate;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author paul
 */
public class Balanceado {
    private static final int N = 6;             // El número de archivos auxiliares
    private static final int N2 = N/2;          // El número de archivos de escritura o lectura
    private static final int NUM_REG = 100000;   // El número de registros a generar
    private static final int TOPE = 999999;     // El número máximo que ser tener un número en un registro
    private static final int MAX_STRING = 20;   // El tamaño máximo para generar el string
    private final javax.swing.JProgressBar barraProgreso;   
    private int numeroRegistros = 1;            // El número de registros
    private int indice = 0;                     // El campo que se desea ordenar
    private int topeNumero = 0;                 // El máximo número
    private int topeTamString = 0;              // El máximo tamaño del string
    private String topeString;                  // El máximo string
    private int topeAnio = 0;                   // El máximo año de la fecha
    private LocalDate topeFecha;                // La fecha máxima
    private int proceso = 0;                    // El contador para generar el porcentaje
    private int totalTramos;                    // El número total de tramos generado
    private File f0;                            // El archivo a ordenar
    private File []f = new File[N];             // Los archivo auxiliares

    public Balanceado(javax.swing.JProgressBar barraProgreso) throws IOException
    {
        String[] nomf = {"ar1.csv", "ar2.csv", "ar3.csv", "ar4.csv", "ar5.csv", "ar6.csv"};
        f0 = new File("archivo.csv");
        generarArchivo();
        
        boolean alreadyExists = f0.exists();

        if ( !alreadyExists ) {
            System.out.println("El fichero no existe");
        } else {
            for (int i = 0; i < N; i++)
                f[i] = new File(nomf[i]);
        }
        this.barraProgreso = barraProgreso;
    }
    
    public Balanceado(File f0, javax.swing.JProgressBar barraProgreso)
    {
        String[] nomf = {"ar1.csv", "ar2.csv", "ar3.csv", "ar4.csv", "ar5.csv", "ar6.csv"};
        for (int i = 0; i < N; i++)
            f[i] = new File(nomf[i]);
        this.barraProgreso = barraProgreso;
        //escribir(f0);
        
        //balanceado();
    }
    
    //método de ordenación
    public void balanceado(int indice) throws IOException
    {
        int i, j, k1, progreso, t = 0;
        String [] anterior;
        String [] aux;
        LocalDate fAnterior;
        LocalDate fActual;
        int [] c = new int[N];
        int [] cd = new int[N];
        String [][] r = new String[N2][];

        Object [] flujos = new Object[N];
        CsvReader flujoEntradaActual = null;
        CsvWriter flujoSalidaActual = null;
        boolean [] actvs = new boolean[N2];

        this.indice = indice;
        numeroRegistros = 1;
        
        // distribución inicial de tramos desde archivo origen
        try {
            switch (indice) {
                case 0:
                    setTopeInt();
                    t = distribuirInt();
                    break;
                case 1:
                    setTopeString();
                    t = distribuirString();
                    break;
                case 2:

                    break;
                case 3:
                    setTopeFecha();
                    t = distribuirFecha();
                    break;
                default:
                    break;
            }
            
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
                
                switch (indice) {
                    case 0:
                        while (k1 > 0)
                        {
                            t++; // mezcla de otro tramo
                            for (i = 0; i < k1; i++) {
                                actvs[i] = true;
                            }
                            flujoSalidaActual = (CsvWriter) flujos[c[j]];

                            while (!finDeTramos(actvs, k1))
                            {
                                int n;
                                n = minimoInt(r, actvs, k1);
                                flujoEntradaActual = (CsvReader) flujos[cd[n]];
                                flujoSalidaActual.writeRecord(r[n]);
                                proceso++;
                                
                                //System.out.println("Porcentaje: " + ( (proceso*50/NumReg)/(Math.log10(NumReg)) ) +"%");
                                //System.out.println(r[n]);
                                anterior = r[n];

                                if (flujoEntradaActual.readRecord()) {
                                    r[n] = flujoEntradaActual.getValues();
                                    if ( Integer.parseInt(anterior[indice]) > Integer.parseInt(r[n][indice]) ) { // fin de tramo
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
                            barraProgreso.setString( "Progreso: " + progreso + "%" );
                            barraProgreso.setValue(progreso);
                            System.out.println(progreso);
                            j = (j < N-1) ? j+1 : N2; // siguiente flujo de salida

                        }
                        break;
                    case 1:
                        while (k1 > 0)
                        {
                            t++; // mezcla de otro tramo
                            for (i = 0; i < k1; i++) {
                                actvs[i] = true;
                            }
                            flujoSalidaActual = (CsvWriter) flujos[c[j]];

                            while (!finDeTramos(actvs, k1))
                            {
                                int n;
                                n = minimoString(r, actvs, k1);
                                flujoEntradaActual = (CsvReader) flujos[cd[n]];
                                flujoSalidaActual.writeRecord(r[n]);
                                proceso++;
                                progreso = (int) ( (proceso*50/numeroRegistros)/(Math.log10(numeroRegistros)) );
                                barraProgreso.setString( "Progreso: " + progreso + "%" );
                                barraProgreso.setValue(progreso);
                                System.out.println(progreso);
                                //System.out.println("Porcentaje: " + ( (proceso*50/NumReg)/(Math.log10(NumReg)) ) +"%");
                                //System.out.println(r[n]);
                                anterior = r[n];

                                if (flujoEntradaActual.readRecord()) {
                                    r[n] = flujoEntradaActual.getValues();
                                    if ( anterior[indice].compareTo(r[n][indice]) > 0 ) { // fin de tramo
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
                            j = (j < N-1) ? j+1 : N2; // siguiente flujo de salida

                        }
                        break;
                    case 2:

                        break;
                    case 3:
                        while (k1 > 0)
                        {
                            t++; // mezcla de otro tramo
                            for (i = 0; i < k1; i++) {
                                actvs[i] = true;
                            }
                            flujoSalidaActual = (CsvWriter) flujos[c[j]];

                            while (!finDeTramos(actvs, k1))
                            {
                                int n;
                                n = minimoFecha(r, actvs, k1);
                                flujoEntradaActual = (CsvReader) flujos[cd[n]];
                                flujoSalidaActual.writeRecord(r[n]);
                                proceso++;
                                progreso = (int) ( (proceso*50/numeroRegistros)/(Math.log10(numeroRegistros)) );
                                barraProgreso.setString( "Progreso: " + progreso + "%" );
                                barraProgreso.setValue(progreso);
                                System.out.println(progreso);
                                //System.out.println(r[n]);
                                anterior = r[n];

                                if (flujoEntradaActual.readRecord()) {
                                    r[n] = flujoEntradaActual.getValues();
                                    aux = anterior[indice].split("/");
                                    fAnterior = LocalDate.of(Integer.parseInt(aux[2]),
                                                            Integer.parseInt(aux[1]),
                                                            Integer.parseInt(aux[0]));
                                    aux = r[n][indice].split("/");
                                    fActual = LocalDate.of(Integer.parseInt(aux[2]),
                                                            Integer.parseInt(aux[1]),
                                                            Integer.parseInt(aux[0]));
                                    if ( fAnterior.compareTo(fActual) > 0 ) { // fin de tramo
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
                            j = (j < N-1) ? j+1 : N2; // siguiente flujo de salida

                        }
                        break;
                    default:
                        break;
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
    private int distribuirInt() throws IOException
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
        if ( flujo.readRecord() )
            anterior = Integer.parseInt(flujo.getValues()[indice]);

        // bucle termina con la excepción fin de fichero
        while (flujo.readRecord())
        {
            clave = Integer.parseInt(flujo.getValues()[indice]);
            while (anterior <= clave)
            {
                flujoSalida[j].writeRecord(flujo.getValues());
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
    
    private int distribuirString() throws IOException
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
        if ( flujo.readRecord() )
            anterior = flujo.getValues()[indice];

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
    
    private int distribuirFecha() throws IOException
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
            System.out.println(Integer.parseInt(aux[0]) + " " +
                                            Integer.parseInt(aux[1]) + " " +
                                            Integer.parseInt(aux[2]));
            anterior = LocalDate.of(Integer.parseInt(aux[2]),
                                    Integer.parseInt(aux[1]),
                                    Integer.parseInt(aux[0]));
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
    
    //devuelve el índice del menor valor del array de claves
    private int minimoInt(String [][] r, boolean [] activo, int n)
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
    
    private int minimoString(String [][] r, boolean [] activo, int n)
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
    
    private int minimoFecha(String [][] r, boolean [] activo, int n)
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
    
    //devuelve true si no hay tramo activo
    private boolean finDeTramos(boolean [] activo, int n)
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
    
    private void setTopeInt() 
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
    
    private void setTopeString()
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
    
    private void setTopeFecha()
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
    
    private void generarArchivo() throws IOException
    {
        CsvWriter flujoSalida = new CsvWriter(new FileWriter(f0),',');
        flujoSalida.writeRecord("campo1,campo2,campo3,campo4".split(","));
        Random randomGenerator = new Random();
        RandomDate randomDate = new RandomDate(LocalDate.of(1920, 1, 1), LocalDate.of(2016, 1, 1));
                
        for (int i=0; i<NUM_REG; i++)
        {
            flujoSalida.write(String.valueOf((int)(1+TOPE*Math.random())));
            flujoSalida.write(RandomStringUtils.random((int)(1+MAX_STRING*Math.random()),true,true));
            flujoSalida.write(String.valueOf(randomGenerator.nextBoolean()));
            flujoSalida.write(randomDate.nextDate());
            flujoSalida.endRecord();
        }
        
        flujoSalida.close();
    }
}
