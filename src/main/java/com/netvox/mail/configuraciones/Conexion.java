/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class Conexion {

    @Bean
    public MongoClient mongo() {
        return new MongoClient(new MongoClientURI("mongodb://angelho:2740522ab@190.41.82.4:27017/mail"));
    }

    @Bean
    public MongoTemplate dameConexion() {
        return new MongoTemplate(mongo(), "mail");
    }

    @Bean(name = "executor")
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

}
