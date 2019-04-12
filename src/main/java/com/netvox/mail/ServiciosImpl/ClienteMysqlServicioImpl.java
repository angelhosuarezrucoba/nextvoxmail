/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("clientemysqlservicio")
public class ClienteMysqlServicioImpl {

    @Autowired
    @Qualifier("datasource")
    DataSource datasource;

    public void cerrarConexion(Connection conexion) {
        try {
            if (null != conexion) {
                conexion.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection obtenerConexion() {
        Connection conexion = null;
        try {
            conexion = datasource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conexion;
    }
}
