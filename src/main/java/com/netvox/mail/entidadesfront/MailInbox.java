/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author desarrollo5
 */
public class MailInbox {

    private int id;
    private int estado;
    private String remitente;
    private String destino;
    private String asunto;
    private String cc;//copia
    private String mensaje;//comentario de tipificacion
    private String fecha_ingreso;
    private String fecha_respuesta;
    private int tiempo_cola;//tiempo que estuvo en una cola;
    private int tipificacion;
    private List<Adjunto> adjuntos = new ArrayList<>();
    private String tipo;//tipomail;
    private String descripcion_tipificacion;
    private int idhilo;
    private boolean hilocerrado;
    private int id_cola;
     
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getFecha_ingreso() {
        return fecha_ingreso;
    }

    public void setFecha_ingreso(String fecha_ingreso) {
        this.fecha_ingreso = fecha_ingreso;
    }

    public String getFecha_respuesta() {
        return fecha_respuesta;
    }

    public void setFecha_respuesta(String fecha_respuesta) {
        this.fecha_respuesta = fecha_respuesta;
    }

    public int getTipificacion() {
        return tipificacion;
    }

    public void setTipificacion(int tipificacion) {
        this.tipificacion = tipificacion;
    }

    public List<Adjunto> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<Adjunto> adjuntos) {
        this.adjuntos = adjuntos;
    }

      public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getTiempo_cola() {
        return tiempo_cola;
    }

    public void setTiempo_cola(int tiempo_cola) {
        this.tiempo_cola = tiempo_cola;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion_tipificacion() {
        return descripcion_tipificacion;
    }

    public void setDescripcion_tipificacion(String descripcion_tipificacion) {
        this.descripcion_tipificacion = descripcion_tipificacion;
    }

    public int getIdhilo() {
        return idhilo;
    }

    public void setIdhilo(int idhilo) {
        this.idhilo = idhilo;
    }

    public boolean isHilocerrado() {
        return hilocerrado;
    }

    public void setHilocerrado(boolean hilocerrado) {
        this.hilocerrado = hilocerrado;
    }

    public int getId_cola() {
        return id_cola;
    }

    public void setId_cola(int id_cola) {
        this.id_cola = id_cola;
    }
}
