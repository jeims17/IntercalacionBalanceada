/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucue.p3.Logica;

import com.csvreader.CsvWriter;
import edu.ucue.p3.Datos.RandomDate;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author paul
 */
public class GeneradorArchivo {
    private static final int NUM_REG = 10;  // El número de registros a generar
    private static final int TOPE = 20;     // El número máximo que ser tener un número en un registro
    private static final int MAX_STRING = 20;   // El tamaño máximo para generar el string
    private static File f0;                            // El archivo a ordenar
    private static GeneradorArchivo instancia = null;
    
    public static GeneradorArchivo getInstancia() {
        if (instancia == null)
            instancia = new GeneradorArchivo();
        return instancia;
    }
    
    public File generarArchivo() throws IOException
    {
        f0 = new File("archivo.csv");
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
        return f0;
    }

    public File getF0() {
        return f0;
    }
    
}
