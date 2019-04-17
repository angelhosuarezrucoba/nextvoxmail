/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rutas")
public class Rutas {
    private String url_in;
    private String ruta_in;
    private String ruta_out;
    private String ruta_adjunto;
    private String ruta_tomcat;
    private String ruta_firmas;
    private String ip_ccvox;
    private String ip_server_mail;
    private int puerto_server_mail;

    public String getUrl_in() {
        return url_in;
    }

    public void setUrl_in(String url_in) {
        this.url_in = url_in;
    }
    
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

    public String getRuta_adjunto() {
        return ruta_adjunto;
    }

    public void setRuta_adjunto(String ruta_adjunto) {
        this.ruta_adjunto = ruta_adjunto;
    }

    public String getRuta_tomcat() {
        return ruta_tomcat;
    }

    public void setRuta_tomcat(String ruta_tomcat) {
        this.ruta_tomcat = ruta_tomcat;
    }

    public String getIp_ccvox() {
        return ip_ccvox;
    }

    public void setIp_ccvox(String ip_ccvox) {
        this.ip_ccvox = ip_ccvox;
    }

    public String getIp_server_mail() {
        return ip_server_mail;
    }

    public void setIp_server_mail(String ip_server_mail) {
        this.ip_server_mail = ip_server_mail;
    }

    public int getPuerto_server_mail() {
        return puerto_server_mail;
    }

    public void setPuerto_server_mail(int puerto_server_mail) {
        this.puerto_server_mail = puerto_server_mail;
    }

    public String getRuta_firmas() {
        return ruta_firmas;
    }

    public void setRuta_firmas(String ruta_firmas) {
        this.ruta_firmas = ruta_firmas;
    }


       
}

