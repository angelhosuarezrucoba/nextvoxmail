/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import static com.netvox.mail.ServiciosImpl.CoreMailServicioImpl.getListaresumen;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ResumenServicio;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service("resumenservicio")
public class ResumenServicioImpl implements ResumenServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

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

    @Override
    public void pausar(Mensaje mensaje) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Resumen usuarioresumen = coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get();
        try {
            if (usuarioresumen.getEstadoagente() == 4) {
                coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).collect(Collectors.toList()).get(0).setEstadoagente(usuarioresumen.getPendiente() == 0 ? 1 : 2);
                 System.out.println("El agente "+ usuarioresumen.getNombre() + " salio de la pausa");
            } else {
                coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).collect(Collectors.toList()).get(0).setEstadoagente(4);
                 System.out.println("El agente "+ usuarioresumen.getNombre() + " entro en pausa");
            }           
            mongoops.updateFirst(new Query(Criteria.where("agente").is(usuarioresumen.getAgente())), new Update().set("estadoagente", coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get().getEstadoagente()), Resumen.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
