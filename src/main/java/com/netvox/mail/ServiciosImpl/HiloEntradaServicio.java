/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Prueba;
import com.netvox.mail.servicios.ClienteMongoServicio;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("hiloentradaservicio")
public class HiloEntradaServicio implements Runnable {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    @Override
    public void run() {
        while (true) {
            List<Prueba> lista = new ArrayList<>();
            MongoOperations mongoops = clientemongoservicio.clienteMongo();
            lista = mongoops.find(new Query(), Prueba.class);
            lista.forEach((t) -> {
                try {
                    System.out.println(t.getNombre());
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}
