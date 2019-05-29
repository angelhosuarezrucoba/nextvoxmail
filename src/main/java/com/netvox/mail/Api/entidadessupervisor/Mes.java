package com.netvox.mail.Api.entidadessupervisor;

public class Mes {
    private int diascantidad;
    private String nombremes;

    public Mes(int diascantidad, String nombremes) {
        this.diascantidad = diascantidad;
        this.nombremes = nombremes;
    }

    
    
    public int getDiascantidad() {
        return diascantidad;
    }

    public void setDiascantidad(int diascantidad) {
        this.diascantidad = diascantidad;
    }

    public String getNombremes() {
        return nombremes;
    }

    public void setNombremes(String nombremes) {
        this.nombremes = nombremes;
    }
}
