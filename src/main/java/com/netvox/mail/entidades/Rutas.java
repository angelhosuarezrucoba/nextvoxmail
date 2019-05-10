/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rutas")
public class Rutas {
    private String ruta_in;
    private String ruta_out;
    private String path_entrada;
    private String path_salida;

  
    
    public String getRuta_in() {
        return ruta_in;
    }

    public void setRuta_in(String ruta_in) {
        this.ruta_in = ruta_in;
    }

    public String getRuta_out() {
        return ruta_out;
    }

    public void setRuta_out(String ruta_out) {
        this.ruta_out = ruta_out;
    }

    public String getPath_entrada() {
        return path_entrada;
    }

    public void setPath_entrada(String path_entrada) {
        this.path_entrada = path_entrada;
    }

    public String getPath_salida() {
        return path_salida;
    }

    public void setPath_salida(String path_salida) {
        this.path_salida = path_salida;
    }

}

