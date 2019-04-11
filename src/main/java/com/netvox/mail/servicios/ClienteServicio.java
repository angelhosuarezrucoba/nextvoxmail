/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.servicios;

import com.netvox.mail.entidades.Mensaje;
import com.netvox.mail.entidades.Cliente;

public interface ClienteServicio {

    public abstract Cliente obtenerCliente(Mensaje mensaje);

    public abstract Cliente CrearCliente(Mensaje mensaje);

    public abstract boolean existeCliente(Mensaje mensaje);

    public boolean enAtencion(Mensaje mensaje);
}
