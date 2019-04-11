/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.servicios;

import com.netvox.mail.entidades.Mensaje;

/**
 *
 * @author desarrollo5
 */
public interface OrganizadorServicio {
    
    
    public abstract void  OrganizarMensajes(Mensaje mensaje);
    
}
