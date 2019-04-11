/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.netvox.mail.ServiciosImpl.ConfiguracionesServicioImpl;
import com.netvox.mail.ServiciosImpl.CoreMailServicioImpl;
import com.netvox.mail.ServiciosImpl.HiloEntradaServicio;
import com.netvox.mail.entidades.Configuracion;
import com.netvox.mail.entidades.Parametros;
import com.netvox.mail.servicios.ClienteMongoServicio;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class InicioHilos {

    //esto permite ejecutar los hilos.
    @Autowired
    @Qualifier("executor")
    TaskExecutor taskExecutor;

    //este el hilo de entrada.
    @Autowired
    @Qualifier("hiloentradaservicio")
    HiloEntradaServicio hiloentradaservicio;

    @Autowired
    @Qualifier("configuracionservicio")
    ConfiguracionesServicioImpl configuracionservicio;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    ///esto ejecuta un hilo en cuanto inicia la web.
    @PostConstruct
    public void init() {

        configuracionservicio.cargarConfiguracionGlobal();
        coremailservicio.imprimir(2);
        coremailservicio.imprimir(2);
        //aqui se ejecutan los hilos de entrada y salida
        taskExecutor.execute(hiloentradaservicio);
    }

}
