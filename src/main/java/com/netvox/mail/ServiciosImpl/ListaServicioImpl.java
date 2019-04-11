/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mensaje;

import java.util.LinkedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

@Service("listaservicio")
public class ListaServicioImpl {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    private static LinkedList<Mensaje> listamensajes;

    public LinkedList<Mensaje> getListamensajes() {
        return listamensajes;
    }

    public void setListamensajes(LinkedList<Mensaje> aListamensajes) {
        listamensajes = aListamensajes;
    }

    public ListaServicioImpl() {
        listamensajes = new LinkedList<>();
    }

    public void agregarMensaje(Mensaje mensaje) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        mongoops.insert(mensaje);        
        listamensajes.add(mensaje);
        
    }

}
