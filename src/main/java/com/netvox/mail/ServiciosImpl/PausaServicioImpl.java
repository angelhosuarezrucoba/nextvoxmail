/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.Api.entidadessupervisor.Pausa;
import com.netvox.mail.servicios.PausaServicio;
import com.netvox.mail.servicios.ResumenServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service("pausaservicio")
public class PausaServicioImpl implements PausaServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    @Autowired
    @Qualifier("formatodefechas")
    FormatoDeFechas formatodefechas;

    @Autowired
    @Qualifier("resumenservicio")
    ResumenServicio resumenservicio;

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void despausar(int idagente) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Pausa pausa = null;
        try {
            pausa = mongoops.findOne(new Query(Criteria.where("idagente").is(idagente).
                    and("finpausa").is(null)), Pausa.class);
            pausa.setFinpausa(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            pausa.setDuracion(formatodefechas.restaDeFechasEnSegundos(pausa.getFinpausa(), pausa.getIniciopausa()));
            mongoops.updateFirst(new Query(Criteria.where("idagente").is(idagente).
                    and("finpausa").is(null)),
                    new Update().set("finpausa", pausa.getFinpausa()).set("duracion", pausa.getDuracion()), Pausa.class);
        } catch (Exception e) {
            log.error("Error en el despausar", e);
        }

    }

    @Override
    public void pausar(int agente) {
        Pausa pausa = new Pausa();
        pausa.setIdagente(agente);
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        pausa.setNombreagente(resumenservicio.obtenerResumen(agente).getNombre());
        pausa.setIniciopausa(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
        mongoops.insert(pausa);
    }

}
