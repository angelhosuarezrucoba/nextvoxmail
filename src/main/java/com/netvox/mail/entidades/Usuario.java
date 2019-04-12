/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import java.util.LinkedList;

public class Usuario {

    private int id;
    private int pendientes;
    private int atendidos;
    private int cola_asign;
    private int campana_asing;
    private int acumulados;
    private String nombre;
    private LinkedList<Usuario> list_user_queue;
    private LinkedList<Mail> mails;
    private LinkedList<Chat> chats;

    public Usuario(int id, int pendientes, int cola_asign, int campana_asing) {
        this.id = id;
        this.pendientes = pendientes;
        this.cola_asign = cola_asign;
        this.campana_asing = campana_asing;
        this.list_user_queue = new LinkedList<Usuario>();
        this.mails = new LinkedList<Mail>();
        //this.list_user_queue.add(this);
    }

    public Usuario(int id, int pendientes, int cola_asign, String nombre, int campana_asing) {
        this.id = id;
        this.pendientes = pendientes;
        this.cola_asign = cola_asign;
        this.list_user_queue = new LinkedList<Usuario>();
        this.mails = new LinkedList<Mail>();
        this.nombre = nombre;
        this.campana_asing = campana_asing;
        //this.list_user_queue.add(this);
    }

    public Usuario() {
        this.id = 0;
        this.pendientes = 0;
        this.atendidos = 0;
        this.mails = new LinkedList<Mail>();
        this.chats = new LinkedList<Chat>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAcumulados() {
        return acumulados;
    }

    public void setAcumulados(int acumulados) {
        this.acumulados = acumulados;
    }

    public LinkedList<Usuario> getList_user_queue() {
        return list_user_queue;
    }

    public void setList_user_queue(Usuario users) {
        this.list_user_queue.add(users);
    }

    public int getCola_asign() {
        return cola_asign;
    }

    public void setCola_asign(int cola_asign) {
        this.cola_asign = cola_asign;
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

    /**
     * @return the pendientes
     */
    public int getPendientes() {
        return pendientes;
    }

    /**
     * @param pendientes the pendientes to set
     */
    public void setPendientes(int pendientes) {
        this.pendientes = pendientes;
    }

    /**
     * @return the mails
     */
    public LinkedList<Mail> getMails() {
        return mails;
    }

    /**
     * @param mails the mails to set
     */
    public void setMails(LinkedList<Mail> mails) {
        this.mails = mails;
    }

    /**
     * @return the chats
     */
    public LinkedList<Chat> getChats() {
        return chats;
    }

    /**
     * @param chats the chats to set
     */
    public void setChats(LinkedList<Chat> chats) {
        this.chats = chats;
    }

    /**
     * @return the atendidos
     */
    public int getAtendidos() {
        return atendidos;
    }

    /**
     * @param atendidos the atendidos to set
     */
    public void setAtendidos(int atendidos) {
        this.atendidos = atendidos;
    }

    public int getCampana_asing() {
        return campana_asing;
    }

    public void setCampana_asing(int campana_asing) {
        this.campana_asing = campana_asing;
    }

}
