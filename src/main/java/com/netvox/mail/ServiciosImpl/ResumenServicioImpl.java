/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.Api.entidadessupervisor.Pausa;
import static com.netvox.mail.ServiciosImpl.CoreMailServicioImpl.getListaresumen;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ResumenServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.util.Date;
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

    @Autowired
    @Qualifier("formatodefechas")
    FormatoDeFechas formatodefechas;

    @Override
    public void borrarResumenBaseDatos(int idagente) {
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
        Pausa pausa = null;
        try {
            if (usuarioresumen.getEstadoagente() == 4) {
                coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).collect(Collectors.toList()).get(0).setEstadoagente(usuarioresumen.getPendiente() == 0 ? 1 : 2);
                pausa = mongoops.findOne(new Query(Criteria.where("idagente").is(mensaje.getIdagente()).
                        and("finpausa").is(null)), Pausa.class);
                pausa.setFinpausa(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
                pausa.setDuracion(formatodefechas.restaDeFechasEnSegundos(pausa.getFinpausa(), pausa.getIniciopausa()));
                mongoops.updateFirst(new Query(Criteria.where("idagente").is(mensaje.getIdagente()).
                        and("finpausa").is(null)),
                        new Update().set("finpausa", pausa.getFinpausa()).set("duracion", pausa.getDuracion()), Pausa.class);
                System.out.println("El agente " + usuarioresumen.getNombre() + " salio de la pausa");
            } else {
                coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).collect(Collectors.toList()).get(0).setEstadoagente(4);
                pausa = new Pausa();
                pausa.setIdagente(mensaje.getIdagente());
                pausa.setNombreagente(coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).collect(Collectors.toList()).get(0).getNombre());
                pausa.setIniciopausa(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
                mongoops.insert(pausa);
                System.out.println("El agente " + usuarioresumen.getNombre() + " entro en pausa");
            }
            mongoops.updateFirst(new Query(Criteria.where("agente").is(usuarioresumen.getAgente())), new Update().set("estadoagente", coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get().getEstadoagente()), Resumen.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
