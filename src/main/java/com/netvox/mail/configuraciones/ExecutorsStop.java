/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.netvox.mail.ServiciosImpl.HiloAsignacionServicio;
import com.netvox.mail.ServiciosImpl.HiloEntradaServicio;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.ServletContextEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoaderListener;

@Configuration
public class ExecutorsStop extends ContextLoaderListener {

    @Autowired
    @Qualifier("executor")
    ThreadPoolExecutor taskExecutor;

    @Autowired
    @Qualifier("hiloentradaservicio")
    HiloEntradaServicio hiloentradaservicio;

    @Autowired
    @Qualifier("hiloasignacionservicio")
    HiloAsignacionServicio hiloasignacionservicio;



    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        hiloentradaservicio.setActivo(false);
        hiloasignacionservicio.setActivo(false);
        taskExecutor.shutdownNow();
    }

}
