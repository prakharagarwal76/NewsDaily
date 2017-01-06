package com.example.prakharagarwal.newsdaily;

/**
 * Created by prakharagarwal on 05/01/17.
 */
public class WeatherData {


    long date ;

    String icon;
    String description ;
    double high;
    double low;


    public WeatherData(long date,String icon,
                       String description ,double low, double max){
        this.date=date;

        this.icon=icon;
        this.description=description;
        this.high=max;
        this.low=low;

    }
}
