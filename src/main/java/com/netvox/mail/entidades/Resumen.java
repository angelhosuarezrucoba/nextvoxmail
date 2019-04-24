/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

/**
 *
 * @author desarrollo5
 */
public class Resumen {

    private int campana;
    private int agente;
    private String nombre;
    private int acumulado_mails;
    private int pendiente;
    private int cola;

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

    public int getAcumulado_mails() {
        return acumulado_mails;
    }

    public void setAcumulado_mails(int acumulado_mails) {
        this.acumulado_mails = acumulado_mails;
    }

    public int getPendiente() {
        return pendiente;
    }

    public void setPendiente(int pendiente) {
        this.pendiente = pendiente;
    }

    public int getCola() {
        return cola;
    }

    public void setCola(int cola) {
        this.cola = cola;
    }
}
