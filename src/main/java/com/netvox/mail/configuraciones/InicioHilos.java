package com.netvox.mail.configuraciones;

/**
 * @author Angelho Suarez
 * @version 1.0
 * @since JDK1.0
 */

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
        coremailservicio.cargarRutas();
        coremailservicio.cargarConfiguraciones();
        coremailservicio.ejecutarHiloEntrada();
        coremailservicio.ejecutarHiloAsignacion();
    }

}
