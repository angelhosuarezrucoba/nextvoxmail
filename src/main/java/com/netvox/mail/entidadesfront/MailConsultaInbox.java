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
public class MailConsultaInbox {
    
    private int id_mail ;//esto es el idcorreo
    private String tipo;

    public int getId_mail() {
        return id_mail;
    }

    public void setId_mail(int id_mail) {
        this.id_mail = id_mail;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
