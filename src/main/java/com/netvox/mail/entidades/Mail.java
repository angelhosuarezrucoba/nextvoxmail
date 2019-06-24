/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import com.netvox.mail.entidadesfront.Adjunto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mail")
public class Mail {

    private int idcorreo; //id del correo
    private String remitente;
    private String destino;
    private String asunto;//subject;  
    private String mensaje;
    private int id_cola;
    private String tipomail;//tipo de entrada o salida;
    private int campana;
    private String nombre_campana;
    private int peso_adjunto;
    private String ruta;
    private int idconfiguracion;
    private int estado;
    private String fecha_ingreso;
    private List<Adjunto> listadeadjuntos = new ArrayList<>();
    private String copia;
    private int idhilo;
    private boolean hilocerrado;
    private int tiempo_atencion;
    private String nombre_cola;
    private int usuario;
        private String fechainiciogestion;

    public Mail(int idcorreo, int estado, String tipomail, String fecha_ingreso, int idconfiguracion, int id_cola, String nombre_cola, int id_campana, String asunto, String remitente, String destino) {
        this.idcorreo = idcorreo;
        this.tipomail = tipomail;
        this.estado = estado;
        this.fecha_ingreso = fecha_ingreso;
        this.idconfiguracion = idconfiguracion;
        this.id_cola = id_cola;
        this.listadeadjuntos = new ArrayList<>();
        this.nombre_cola = nombre_cola;
        this.campana = id_campana;
        this.asunto = asunto;
        this.remitente = remitente;
        this.destino = destino;
        
    }

    public Mail() {

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

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public int getId_cola() {
        return id_cola;
    }

    public void setId_cola(int id_cola) {
        this.id_cola = id_cola;
    }

    public String getTipomail() {
        return tipomail;
    }

    public void setTipomail(String tipomail) {
        this.tipomail = tipomail;
    }

    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public String getNombre_campana() {
        return nombre_campana;
    }

    public void setNombre_campana(String nombre_campana) {
        this.nombre_campana = nombre_campana;
    }

    public int getPeso_adjunto() {
        return peso_adjunto;
    }

    public void setPeso_adjunto(int peso_adjunto) {
        this.peso_adjunto = peso_adjunto;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public int getIdconfiguracion() {
        return idconfiguracion;
    }

    public void setIdconfiguracion(int idconfiguracion) {
        this.idconfiguracion = idconfiguracion;
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

    public int getIdcorreo() {
        return idcorreo;
    }

    public void setIdcorreo(int idcorreo) {
        this.idcorreo = idcorreo;
    }

    public List<Adjunto> getListadeadjuntos() {
        return listadeadjuntos;
    }

    public void setListadeadjuntos(List<Adjunto> listadeadjuntos) {
        this.listadeadjuntos = listadeadjuntos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCopia() {
        return copia;
    }

    public void setCopia(String copia) {
        this.copia = copia;
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

    public int getTiempo_atencion() {
        return tiempo_atencion;
    }

    public void setTiempo_atencion(int tiempo_atencion) {
        this.tiempo_atencion = tiempo_atencion;
    }

    public String getNombre_cola() {
        return nombre_cola;
    }

    public void setNombre_cola(String nombre_cola) {
        this.nombre_cola = nombre_cola;
    }

    public int getUsuario() {
        return usuario;
    }

    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    public String getFechainiciogestion() {
        return fechainiciogestion;
    }

    public void setFechainiciogestion(String fechainiciogestion) {
        this.fechainiciogestion = fechainiciogestion;
    }

}
