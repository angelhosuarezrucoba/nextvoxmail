/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Home
 */
public class MailAjustes {

    private String user;
    private String pass;
    private String host;
    private String store;
    private int puerto;
    private int maximo_adjunto;
    private List<Cola> colas;
    private int id;


    public MailAjustes(String user, String pass, String host, String store, int puerto, int maximo_adjunto,int id) {
        this.colas = new ArrayList<>();
        this.user = user;
        this.pass = pass;
        this.store = store;
        this.host = host;
        this.puerto = puerto;
        this.maximo_adjunto = maximo_adjunto;
        this.id = id;

    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the pass
     */
    public String getPass() {
        return pass;
    }

    /**
     * @param pass the pass to set
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the puerto
     */
    public int getPuerto() {
        return puerto;
    }

    /**
     * @param puerto the puerto to set
     */
    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    /**
     * @return the maximo_adjunto
     */
    public int getMaximo_adjunto() {
        return maximo_adjunto;
    }

    /**
     * @param maximo_adjunto the maximo_adjunto to set
     */
    public void setMaximo_adjunto(int maximo_adjunto) {
        this.maximo_adjunto = maximo_adjunto;
    }

    /**
     * @return the store
     */
    public String getStore() {
        return store;
    }

    /**
     * @param store the store to set
     */
    public void setStore(String store) {
        this.store = store;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Cola> getColas() {
        return colas;
    }


}
