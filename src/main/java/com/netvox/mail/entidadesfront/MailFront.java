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
public class MailFront {

    private int id;
    private String correo;
    private String nombre_cola;

    public MailFront(int id, String correo, String nombre_cola) {
        this.id = id;
        this.correo = correo;
        this.nombre_cola = nombre_cola;
    }

  

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombre_cola() {
        return nombre_cola;
    }

    public void setNombre_cola(String nombre_cola) {
        this.nombre_cola = nombre_cola;
    }
}
