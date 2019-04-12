/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

public class Mail { //se conservo los nombres originales de las tablas

    private int id;
    private String remitente;
    private int estado;
    private String fecha_captured;
    private String fecha_assigned;
    private String fecha_answered;
    private int agente;
    private int campana;
    private String subject;
    private int spam;
    private String fecha_index;
    private int duracion;
    private int hora_index;
    private String fecha_captured_index;
    private String hora_captured_index;
    private String fecha_assigned_index;
    private int hora_assigned_index;
    private int mail_outbound;
    private String tipo;
    private int mail_inbound;
    private int tipificacion;
    private String nombre;
    private String apellido;
    private Cola cola;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getFecha_captured() {
        return fecha_captured;
    }

    public void setFecha_captured(String fecha_captured) {
        this.fecha_captured = fecha_captured;
    }

    public String getFecha_assigned() {
        return fecha_assigned;
    }

    public void setFecha_assigned(String fecha_assigned) {
        this.fecha_assigned = fecha_assigned;
    }

    public String getFecha_answered() {
        return fecha_answered;
    }

    public void setFecha_answered(String fecha_answered) {
        this.fecha_answered = fecha_answered;
    }

    public int getAgente() {
        return agente;
    }

    public void setAgente(int agente) {
        this.agente = agente;
    }

    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getSpam() {
        return spam;
    }

    public void setSpam(int spam) {
        this.spam = spam;
    }

    public String getFecha_index() {
        return fecha_index;
    }

    public void setFecha_index(String fecha_index) {
        this.fecha_index = fecha_index;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public int getHora_index() {
        return hora_index;
    }

    public void setHora_index(int hora_index) {
        this.hora_index = hora_index;
    }

    public String getFecha_captured_index() {
        return fecha_captured_index;
    }

    public void setFecha_captured_index(String fecha_captured_index) {
        this.fecha_captured_index = fecha_captured_index;
    }

    public String getHora_captured_index() {
        return hora_captured_index;
    }

    public void setHora_captured_index(String hora_captured_index) {
        this.hora_captured_index = hora_captured_index;
    }

    public String getFecha_assigned_index() {
        return fecha_assigned_index;
    }

    public void setFecha_assigned_index(String fecha_assigned_index) {
        this.fecha_assigned_index = fecha_assigned_index;
    }

    public int getHora_assigned_index() {
        return hora_assigned_index;
    }

    public void setHora_assigned_index(int hora_assigned_index) {
        this.hora_assigned_index = hora_assigned_index;
    }

    public int getMail_outbound() {
        return mail_outbound;
    }

    public void setMail_outbound(int mail_outbound) {
        this.mail_outbound = mail_outbound;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getMail_inbound() {
        return mail_inbound;
    }

    public void setMail_inbound(int mail_inbound) {
        this.mail_inbound = mail_inbound;
    }

    public int getTipificacion() {
        return tipificacion;
    }

    public void setTipificacion(int tipificacion) {
        this.tipificacion = tipificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Cola getCola() {
        return cola;
    }

    public void setCola(Cola cola) {
        this.cola = cola;
    }

   

}
