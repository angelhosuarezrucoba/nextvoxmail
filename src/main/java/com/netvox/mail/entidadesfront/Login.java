
package com.netvox.mail.entidadesfront;

import java.util.ArrayList;
import java.util.List;


public class Login {

    private int idagente;
    private int campana;
    private String evento; // esto es lo que nos dice que accion es la que se va a realizar
    private List<Integer> colas = new ArrayList<>();
    private String agente;
    
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

    public List<Integer> getColas() {
        return colas;
    }

    public void setColas(List<Integer> colas) {
        this.colas = colas;
    }

    public String getAgente() {
        return agente;
    }

    public void setAgente(String agente) {
        this.agente = agente;
    }

    
    

}
