/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

/**
 *
 * @author desarrollo5
 */
public class Mensaje {

    private int idagente;
    private int campana;
    private String evento; // esto es el login

    
    
    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String event) {
        this.evento = event;
    }

    public int getIdagente() {
        return idagente;
    }

    public void setIdagente(int idagente) {
        this.idagente = idagente;
    }
    

}
