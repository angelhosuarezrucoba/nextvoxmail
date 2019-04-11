/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Configuracion;
import com.netvox.mail.entidades.Parametros;
import com.netvox.mail.servicios.ClienteMongoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("configuracionservicio")
public class ConfiguracionesServicioImpl {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    public void cargarConfiguracionGlobal() {

        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Configuracion configuracion = mongoops.find(new Query(), Configuracion.class).get(0); //este toma el unico resultado que hay en la base
        Parametros.URL_IN = configuracion.getUrl_in();
        Parametros.CARPETA_IN = configuracion.getRuta_in();
        Parametros.CARPETA_OUT = configuracion.getRuta_out();
    }
}
