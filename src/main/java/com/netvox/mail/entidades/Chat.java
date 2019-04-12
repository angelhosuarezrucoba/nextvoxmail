/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

/**
 *
 * @author Asus
 */
public class Chat {

    private int id;
    private int campana;

    public Chat(int id, int campana) {
        this.id = id;
        this.campana = campana;

    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

}
