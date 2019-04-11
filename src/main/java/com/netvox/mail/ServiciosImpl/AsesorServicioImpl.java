/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Asesor;
import com.netvox.mail.entidades.Cliente;
import com.netvox.mail.entidades.Mensaje;
import com.netvox.mail.servicios.AsesorServicio;
import com.netvox.mail.servicios.ClienteMongoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("asesorservicio")
public class AsesorServicioImpl implements AsesorServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clienteMongoServicio;

    @Override
    public boolean enAtencion(Asesor asesor) {
        //////////// este metodo no sirve aun.
        MongoOperations mongoops = clienteMongoServicio.clienteMongo();
        Asesor asesorenconsulta = null;
        try {
            asesorenconsulta = mongoops.findOne(new Query(Criteria.where("login").is(asesor.getLogin()).and("estado").is(Asesor.LIBRE)), Asesor.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return asesorenconsulta != null;
    }

    @Override
    public boolean existenAsesoresLibres() {
        // se debe revisar todos los asesores  y seleccionar
        // el que tenga menos interacciones.
        MongoOperations mongoops = clienteMongoServicio.clienteMongo();
        int asesoreslibres = 0;
        try {
            asesoreslibres = (int) mongoops.count(new Query(Criteria.where("estado").is(Asesor.LIBRE)), Asesor.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return asesoreslibres > 0;
    }

}
