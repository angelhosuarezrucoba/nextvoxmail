package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.servicios.ClienteMongoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Configuration
@Service("clientemongoservicio")
public class ClienteMongoServicioImpl implements ClienteMongoServicio {

    @Autowired
    @Qualifier("dameConexion")
    MongoTemplate mongotemplate;

    @Override
    public MongoOperations clienteMongo() {
        return mongotemplate;
    }

}
