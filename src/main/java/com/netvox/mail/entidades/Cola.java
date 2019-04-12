/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import java.util.LinkedList;

/**
 *
 * @author desarrollo5
 */
public class Cola {

    private int id_cola;
    private String nombre_cola;
    private String subject_cola;
    private int id_campana;
    private LinkedList<Usuario> users_queue;

    public LinkedList<Usuario> getUsers_queue() {
        return users_queue;
    }

    public void setUsers_queue(Usuario users) {
        this.users_queue.add(users);
    }

    public Cola() {
    }

    public Cola(int id_cola, String nombre_cola, String subject_cola, int id_campana) {
        this.id_cola = id_cola;
        this.nombre_cola = nombre_cola;
        this.subject_cola = subject_cola;
        this.id_campana = id_campana;
        this.users_queue = new LinkedList<Usuario>();
        this.users_queue.add(null);
    }

    public Cola(int id_cola, int id_campana) {
        this.id_cola = id_cola;
        this.id_campana = id_campana;
    }

    public int getId_campana() {
        return id_campana;
    }

    public void setId_campana(int id_campana) {
        this.id_campana = id_campana;
    }

    public int getId_cola() {
        return id_cola;
    }

    public void setId_cola(int id_cola) {
        this.id_cola = id_cola;
    }

    public String getNombre_cola() {
        return nombre_cola;
    }

    public void setNombre_cola(String nombre_cola) {
        this.nombre_cola = nombre_cola;
    }

    public String getSubject_cola() {
        return subject_cola;
    }

    public void setSubject_cola(String subject_cola) {
        this.subject_cola = subject_cola;
    }

}
