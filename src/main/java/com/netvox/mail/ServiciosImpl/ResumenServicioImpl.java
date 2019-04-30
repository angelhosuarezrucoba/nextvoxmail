/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.servicios.ResumenServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("resumenservicio")
public class ResumenServicioImpl implements ResumenServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    @Override
    public void borrarResumen(int idagente) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            mongoops.remove(new Query(Criteria.where("agente").is(idagente)), Resumen.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void borrarResumenTotal() {
         MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            mongoops.remove(new Query(), Resumen.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
