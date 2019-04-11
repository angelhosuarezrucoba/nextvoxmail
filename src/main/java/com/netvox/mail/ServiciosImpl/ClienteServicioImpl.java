/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mensaje;
import com.netvox.mail.entidades.Cliente;
import com.netvox.mail.servicios.ClienteMongoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.netvox.mail.servicios.ClienteServicio;

/**
 *
 * @author desarrollo5
 */
@Service("clienteservicio")
public class ClienteServicioImpl implements ClienteServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clienteMongoServicio;
    
    
    

    @Override
    public Cliente obtenerCliente(Mensaje mensaje) {
        MongoOperations mongoops = clienteMongoServicio.clienteMongo();
        Cliente usuario = null;
        try {
            usuario = mongoops.findOne(new Query(Criteria.where("numero").is(mensaje.getNumeroentrante())), Cliente.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (usuario == null) {
            usuario = new Cliente();
            usuario.setNumero(mensaje.getNumeroentrante());
            usuario.setEstado(Cliente.EN_ESPERA);
            mongoops.insert(usuario);
        }
        return usuario;
    }

    @Override
    public Cliente CrearCliente(Mensaje mensaje) {
        MongoOperations mongoops = clienteMongoServicio.clienteMongo();
        Cliente usuario = new Cliente();
        usuario.setNumero(mensaje.getNumeroentrante());
        usuario.setEstado(Cliente.EN_ESPERA);
        mongoops.insert(usuario);
        return usuario;
    }

    @Override
    public boolean existeCliente(Mensaje mensaje) {
        MongoOperations mongoops = clienteMongoServicio.clienteMongo();
        Cliente cliente = null;
        try {
            cliente = mongoops.findOne(new Query(Criteria.where("numero").is(mensaje.getNumeroentrante())), Cliente.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cliente != null;
    }

    @Override
    public boolean enAtencion(Mensaje mensaje) {
        MongoOperations mongoops = clienteMongoServicio.clienteMongo();
        Cliente cliente = null;
        try {
            cliente = mongoops.findOne(new Query(Criteria.where("numero").is(mensaje.getNumeroentrante()).and("estado").is(Cliente.EN_ATENCION)), Cliente.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cliente != null;
    }

}
