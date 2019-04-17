/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.netvox.mail.ServiciosImpl.HiloEntradaServicio;
import javax.servlet.ServletContextEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;

@Configuration
public class ExecutorsStop extends ContextLoaderListener {

    @Autowired
    @Qualifier("executor")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    @Qualifier("hiloentradaservicio")
    HiloEntradaServicio hiloentradaservicio;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("estoy cerrando");
        hiloentradaservicio.setActivo(false);       
        taskExecutor.shutdown();       
    }

}
