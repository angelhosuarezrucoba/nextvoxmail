/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.servicios;


import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import java.util.List;

/**
 *
 * @author desarrollo5
 */
public interface MailServicio {
    
    public abstract List<MailInbox> listarCorreos(Mensaje mensaje);

    public String obtenerContenidoMail(MailInbox mailconsultainbox);

    public List<MailInbox> listarCorreosEnCola(Mensaje mensaje);
}