package com.netvox.mail.Api.entidadessupervisor;

import java.util.ArrayList;
import java.util.List;

public class FiltroIndividual {

    private String fecha_inicio;
    private String fecha_fin;
    private List<Integer> listadecolas = new ArrayList<>();
    private int tipo;

    public String getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(String fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public String getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(String fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public List<Integer> getListadecolas() {
        return listadecolas;
    }

    public void setListadecolas(List<Integer> listadecolas) {
        this.listadecolas = listadecolas;
    }

    
    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
    
}
