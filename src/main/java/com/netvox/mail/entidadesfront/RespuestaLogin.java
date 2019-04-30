/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

import java.util.List;

/**
 *
 * @author desarrollo5
 */
public class RespuestaLogin {

    private String evento;
    private int cantidad_cola_mail;////cola pendientes general
    private int acumulado_mail;//acumuladopendientes;
    private int estado_mail;// estadoagente.
    private List<ListaCorreos> listacorreos ;

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public int getCantidad_cola_mail() {
        return cantidad_cola_mail;
    }

    public void setCantidad_cola_mail(int cantidad_cola_mail) {
        this.cantidad_cola_mail = cantidad_cola_mail;
    }

    public int getEstado_mail() {
        return estado_mail;
    }

    public void setEstado_mail(int estado_mail) {
        this.estado_mail = estado_mail;
    }

    public int getAcumulado_mail() {
        return acumulado_mail;
    }

    public void setAcumulado_mail(int acumulado_mail) {
        this.acumulado_mail = acumulado_mail;
    }

    public List<ListaCorreos> getListacorreos() {
        return listacorreos;
    }

    public void setListacorreos(List<ListaCorreos> listacorreos) {
        this.listacorreos = listacorreos;
    }

   

 

}
