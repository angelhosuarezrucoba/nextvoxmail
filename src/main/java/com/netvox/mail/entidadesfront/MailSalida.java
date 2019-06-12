/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "mail")
public class MailSalida {



    @Field(value = "idcorreo")
    private int id;//idcorreo

    @Field(value = "tipomail")
    private String tipo;

    @Field(value = "asunto")
    private String titulo;

    @Field(value = "remitente")
    private String remitente;

    @Field(value = "destino")
    private String destino;

    @Field(value = "copia")
    private String copia;

    @Field(value = "id_cola")
    private int cola;

    @Field(value = "mensaje")
    private String mensaje;

    @Field(value = "tipificacion")
    private int tipificacion;

    @Field(value = "usuario")
    private int id_agente;

    @Field(value = "reenvio")
    private int reenvio;

    @Field(value = "campana")
    private int id_campana;

    @Field(value = "nombre_campana")
    private String nombre_campana;

    @Field(value = "estado")
    private int estado;

    @Field(value = "fecha_ingreso")
    private String fecha_ingreso;

    @Field(value = "fechainiciogestion")
    private String fechainiciogestion;

    @Field(value = "fechafingestion")
    private String fechafingestion;

    @Field(value = "descripcion_tipificacion")
    private String descripcion_tipificacion = "nuevo";//si no consigue un valor en base le pondra este por defecto

    @Field(value = "listadeadjuntos")
    private List<Adjunto> listadeadjuntos = new ArrayList<>();

    @Field(value = "listadeembebidos")
    private List<String> listadeembebidos = new ArrayList<>();

    @Field(value = "comentario")
    private String comentario;

    @Field(value = "idhilo")
    private int idhilo;//

    @Field(value = "hilocerrado")
    private boolean hilocerrado;

    @Field(value = "tiempoencola")
    private int tiempoencola;

    @Field(value = "tiempo_atencion")
    private int tiempo_atencion;//

    //////////variables para el reporte
    private String hora;
    private String estadoatencion;
    private String validacion;
    private String tiempo_cola;
    private String tiempoatencion;

    ///////////////////////////////////
    public int getTiempoencola() {
        return tiempoencola;
    }

    public void setTiempoencola(int tiempoencola) {
        this.tiempoencola = tiempoencola;
    }

    public String getEstadoatencion() {
        return estadoatencion;
    }

    public void setEstadoatencion(String estadoatencion) {
        this.estadoatencion = estadoatencion;
    }

    public String getValidacion() {
        return validacion;
    }

    public void setValidacion(String validacion) {
        this.validacion = validacion;
    }

    public String getTiempo_cola() {
        return tiempo_cola;
    }

    public void setTiempo_cola(String tiempo_cola) {
        this.tiempo_cola = tiempo_cola;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getCopia() {
        return copia;
    }

    public void setCopia(String copia) {
        this.copia = copia;
    }

    public int getCola() {
        return cola;
    }

    public void setCola(int cola) {
        this.cola = cola;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getTipificacion() {
        return tipificacion;
    }

    public void setTipificacion(int tipificacion) {
        this.tipificacion = tipificacion;
    }

    public int getId_agente() {
        return id_agente;
    }

    public void setId_agente(int id_agente) {
        this.id_agente = id_agente;
    }

    public int getReenvio() {
        return reenvio;
    }

    public void setReenvio(int reenvio) {
        this.reenvio = reenvio;
    }

    public int getId_campana() {
        return id_campana;
    }

    public void setId_campana(int id_campana) {
        this.id_campana = id_campana;
    }

    public String getNombre_campana() {
        return nombre_campana;
    }

    public void setNombre_campana(String nombre_campana) {
        this.nombre_campana = nombre_campana;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getFecha_ingreso() {
        return fecha_ingreso;
    }

    public void setFecha_ingreso(String fecha_ingreso) {
        this.fecha_ingreso = fecha_ingreso;
    }

    public String getDescripcion_tipificacion() {
        return descripcion_tipificacion;
    }

    public void setDescripcion_tipificacion(String descripcion_tipificacion) {
        this.descripcion_tipificacion = descripcion_tipificacion;
    }

    public List<Adjunto> getListadeadjuntos() {
        return listadeadjuntos;
    }

    public void setListadeadjuntos(List<Adjunto> listadeadjuntos) {
        this.listadeadjuntos = listadeadjuntos;
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

    public String getFechainiciogestion() {
        return fechainiciogestion;
    }

    public void setFechainiciogestion(String fechainiciogestion) {
        this.fechainiciogestion = fechainiciogestion;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getTiempoatencion() {
        return tiempoatencion;
    }

    public void setTiempoatencion(String tiempoatencion) {
        this.tiempoatencion = tiempoatencion;
    }


    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public List<String> getListadeembebidos() {
        return listadeembebidos;
    }

    public void setListadeembebidos(List<String> listadeembebidos) {
        this.listadeembebidos = listadeembebidos;
    }

    public int getTiempo_atencion() {
        return tiempo_atencion;
    }

    public void setTiempo_atencion(int tiempo_atencion) {
        this.tiempo_atencion = tiempo_atencion;
    }

    public String getFechafingestion() {
        return fechafingestion;
    }

    public void setFechafingestion(String fechafingestion) {
        this.fechafingestion = fechafingestion;
    }
}
