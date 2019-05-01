/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "resumen")
public class Resumen {

    private int campana;
    private int agente;
    private String nombre;
    //private int mailsacumulados;
    private int pendiente;
    private List<Integer> listacolas;
    private int estadoagente;

    public Resumen() {
    }

    public Resumen(int campana, int agente, String nombre, int pendiente, List<Integer> listacolas, int estadoagente) {
        this.campana = campana;
        this.agente = agente;
        this.nombre = nombre;
        this.pendiente = pendiente;
        this.listacolas = listacolas;
        this.estadoagente = estadoagente;
    }

    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public int getAgente() {
        return agente;
    }

    public void setAgente(int agente) {
        this.agente = agente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPendiente() {
        return pendiente;
    }

    public void setPendiente(int pendiente) {
        this.pendiente = pendiente;
    }

 

    public int getEstadoagente() {
        return estadoagente;
    }

    public void setEstadoagente(int estadoagente) {
        this.estadoagente = estadoagente;
    }

    public List<Integer> getListacolas() {
        return listacolas;
    }

    public void setListacolas(List<Integer> listacolas) {
        this.listacolas = listacolas;
    }
}
