/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucue.p3.Datos;

import java.time.LocalDate;
import java.util.Random;

/**
 *
 * @author paul
 */
public class RandomDate {
    private final LocalDate minDate;
    private final LocalDate maxDate;
    private final Random random;

    public RandomDate(LocalDate minDate, LocalDate maxDate) {
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.random = new Random();
    }

    public String nextDate() {
        int minDay = (int) minDate.toEpochDay();
        int maxDay = (int) maxDate.toEpochDay();
        long randomDay = minDay + random.nextInt(maxDay - minDay);
        return String.valueOf( LocalDate.ofEpochDay(randomDay).getDayOfMonth() ) + "/" +
                String.valueOf( LocalDate.ofEpochDay(randomDay).getMonthValue() ) + "/" +
                String.valueOf( LocalDate.ofEpochDay(randomDay).getYear() );
    }

    @Override
    public String toString() {
        return "RandomDate{" +
                "maxDate=" + maxDate +
                ", minDate=" + minDate +
                '}';
    }
}