/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("clientemysqlservicio")
public class ClienteMysqlServicioImpl {

    @Autowired
    @Qualifier("datasource")
    DataSource datasource;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public Connection obtenerConexion() {
        Connection conexion = null;
        try {
            conexion = datasource.getConnection();
        } catch (Exception e) {
            log.error("error en el metodo obtenerConexion , mysql", e);
        }
        return conexion;
    }
}
