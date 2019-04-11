
package com.netvox.mail.servicios;

import org.springframework.data.mongodb.core.MongoOperations;

/**
 * interface para el manejo de mongoDB
 * @author sistemas
 */
public interface ClienteMongoServicio {
    
     public abstract MongoOperations clienteMongo();
    
}
