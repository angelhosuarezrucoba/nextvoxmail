/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private int id;
    private int pendientes;
    private int atendidos;
    private List<Integer> listacolas;
    private int campana;
    private String nombre;
    private List<Mail> mails;


    public Usuario(int id, int pendientes, List<Integer> listacolas, int campana) {
        this.id = id;
        this.pendientes = pendientes;
        this.listacolas = listacolas;
        this.campana = campana;
        this.mails = new ArrayList<>();

    }

    public Usuario(int id, int pendientes,List<Integer> listacolas, String nombre, int campana) {
        this.id = id;
        this.pendientes = pendientes;
        this.listacolas = listacolas;
        this.mails = new ArrayList<>();
        this.nombre = nombre;
        this.campana = campana;
    

    }

    public Usuario() {
        this.id = 0;
        this.pendientes = 0;
        this.atendidos = 0;
        this.mails = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


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
    public List<Mail> getMails() {
        return mails;
    }

    /**
     * @param mails the mails to set
     */
    public void setMails(List<Mail> mails) {
        this.mails = mails;
    }


    public int getAtendidos() {
        return atendidos;
    }

    /**
     * @param atendidos the atendidos to set
     */
    public void setAtendidos(int atendidos) {
        this.atendidos = atendidos;
    }

    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public List<Integer> getListacolas() {
        return listacolas;
    }

    public void setListacolas(List<Integer> listacolas) {
        this.listacolas = listacolas;
    }


}
