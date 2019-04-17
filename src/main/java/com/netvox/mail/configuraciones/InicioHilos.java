/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.netvox.mail.ServiciosImpl.CoreMailServicioImpl;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class InicioHilos {


    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio; //esto tiene la logica de las operaciones sobre los correos.

    ///esto ejecuta un hilo en cuanto inicia la web.
    @PostConstruct
    public void init() {
       coremailservicio.ejecutarHiloEntrada();       
      // coremailservicio.ejecutarHiloAsignacion();
    }

}
