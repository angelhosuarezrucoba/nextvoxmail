package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.LogConexiones;
import com.netvox.mail.entidades.Resumen;
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

@Service("logconexionesservicio")
public class LogConexionesServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    @Autowired
    @Qualifier("formatodefechas")
    FormatoDeFechas formatodefechas;

    @Autowired
    @Qualifier("resumendiarioservicio")
    ResumenDiarioServicioImpl resumendiarioservicio;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public void grabarConexion(Resumen resumen) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            LogConexiones log = mongoops.findOne(new Query(Criteria.where("agente").is(resumen.getAgente())
                    .and("fechaconexion").regex(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA))), LogConexiones.class);
            if (log == null) {
                mongoops.insert(
                        new LogConexiones(
                                resumen.getCampana(),
                                resumen.getAgente(),
                                resumen.getNombre(),
                                resumen.getPendiente(),
                                resumen.getListacolas(),
                                resumen.getEstadoagente(),
                                formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)
                        ));
                resumendiarioservicio.insertarConexion(resumen);
            } else {
                resumendiarioservicio.actualizaHoraLogueo(resumen);
            }
        } catch (Exception e) {
            log.error("error en el metodo grabarConexion", e.getCause());
        }
    }

    public void grabarDesconexion(int idagente) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            mongoops.updateFirst(new Query(Criteria.where("agente").is(idagente)
                    .and("fechaconexion").regex(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA))),
                    new Update().set("fechadesconexion", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)), LogConexiones.class);
        } catch (Exception e) {
            log.error("error en el metodo grabarDesconexion", e.getCause());
        }
    }

}
