/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author desarrollo5
 */
public class Agente {
    
    private int idagente;
    private int campana;
    private int cola;
    private WebSocketSession sesion;

    public Agente(int idagente,WebSocketSession sesion) {
        this.idagente = idagente;
        this.sesion = sesion;

    }

    
    
    public int getIdagente() {
        return idagente;
    }

    public void setIdagente(int idagente) {
        this.idagente = idagente;
    }

    public int getCola() {
        return cola;
    }

    public void setCola(int cola) {
        this.cola = cola;
    }

    public WebSocketSession getSesion() {
        return sesion;
    }

    public void setSesion(WebSocketSession sesion) {
        this.sesion = sesion;
    }


    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }
}
