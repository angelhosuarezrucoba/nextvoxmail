/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author desarrollo5
 */
@Document(collection = "mails")
public class Mail {
     private String id;
     private String remitente;
     private String asunto;
     private String estado;
     private String rango_fecha;
     private String rango_hora;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRango_fecha() {
        return rango_fecha;
    }

    public void setRango_fecha(String rango_fecha) {
        this.rango_fecha = rango_fecha;
    }

    public String getRango_hora() {
        return rango_hora;
    }

    public void setRango_hora(String rango_hora) {
        this.rango_hora = rango_hora;
    }
     
     
}
