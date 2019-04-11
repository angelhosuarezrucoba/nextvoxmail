/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.ServiciosImpl.ListaServicioImpl;
import com.netvox.mail.entidades.Mensaje;
import com.netvox.mail.servicios.AsesorServicio;
import com.netvox.mail.servicios.OrganizadorServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.netvox.mail.servicios.ClienteServicio;

@Service("organizadorservicio")
public class OrganizadorServicioImpl implements OrganizadorServicio {

    @Autowired
    @Qualifier("listaservicio")
    ListaServicioImpl listaservicio;

    @Autowired
    @Qualifier("clienteservicio")
    ClienteServicio clienteservicio;

    @Autowired
    @Qualifier("asesorservicio")
    AsesorServicio asesorservicio;
    
    @Override
    public void OrganizarMensajes(Mensaje mensaje) {
        listaservicio.agregarMensaje(mensaje); // aqui ya el mensaje se grabo en la base de datos
        Mensaje mensajeactual = listaservicio.getListamensajes().peek();
        if (!clienteservicio.existeCliente(mensajeactual)) {
            clienteservicio.CrearCliente(mensajeactual);
        }
        //comprobar que este en una  conversacion            
        if (clienteservicio.enAtencion(mensajeactual)) {
            //this.template.convertAndSend("/controlmensajes/mensajes", mensaje);
            //aqui debo enviar el mensaje del cliente hacia la web de un usuario en especifico. verificar con quien
            // tiene la conversacion            
        }else{
            // debo verificar que haya un agente libre 
            if(asesorservicio.existenAsesoresLibres()){
                /* si lo hay debo avisarle que hay mensajes sin atender.*/
                //this.template.convertAndSend("/controlmensajes/mensajes", mensaje);
            }
            
            // cuando un acesor deja de atender un cliente , debe comprobarse si hay mensajes sin atender.
            // cuando inicia debe comprobarse si hay mensajes sin atender.
            //hasta aqui termina la parte que puede hacer la api de whatsapp
        }        
    }

}
