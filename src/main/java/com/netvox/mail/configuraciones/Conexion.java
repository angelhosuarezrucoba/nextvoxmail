/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.netvox.mail.ServiciosImpl.LecturaServicioImpl;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class Conexion {

    String usuariomongo;
    String clavemongo;
    String ipmongo;
    String bdmongo;
    String puertomongo;
    String usuariomysql;
    String clavemysql;
    String ipmysql;
    String bdmysql;
    String puertomysql;

    @Autowired
    @Qualifier("lecturaservicio")
    LecturaServicioImpl lecturaservicio;

    @Bean
    public MongoClient mongo() {
        List<String> lista = lecturaservicio.obtenerConfiguraciones();
        usuariomongo = lista.get(0);
        clavemongo = lista.get(1);
        ipmongo = lista.get(2);
        bdmongo = lista.get(3);
        puertomongo = lista.get(4);
        usuariomysql = lista.get(5);
        clavemysql = lista.get(6);
        ipmysql = lista.get(7);
        bdmysql = lista.get(8);
        puertomysql = lista.get(9);
        return new MongoClient(new MongoClientURI("mongodb://" + usuariomongo + ":" + clavemongo + "@" + ipmongo + ":" + puertomongo + "/" + bdmongo));
    }

    @Bean(name = "datasource")
    public DataSource dataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUsername(usuariomysql);
        basicDataSource.setPassword(clavemysql);
        basicDataSource.setUrl("jdbc:mysql://" + ipmysql + "/" + bdmysql);
        basicDataSource.setMaxActive(200);
        basicDataSource.setMaxOpenPreparedStatements(200);
        return basicDataSource;
    }

    @Bean
    public MongoTemplate dameConexion() {
        return new MongoTemplate(mongo(), bdmongo);
    }

    @Bean(name = "executor")
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

}
