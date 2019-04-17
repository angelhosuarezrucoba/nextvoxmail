/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mail")
public class Mail {

    
    private int idcorreo; //id del correo
    private String remitente;
    private String destino;
    private String asunto;//subject;
    //private boolean spam;
    private Cola cola;
    //private String fecha_index;
    private int usuario;
    private String texto;
    //private int id_inbound;
    //private int id_outbound;
    private int id_cola;
    private String tipomail;//tipo;
    //private String subject_in; // asunto con que se responde
    // private String inicio_cola;
    private int campana;
    private String nombre;
    private String apellido;
    private String nombre_campana;
    private int peso_adjunto;
    private String ruta;
    private int idconfiguracion;
    private int estado;
    private String fecha_ingreso;

    public Mail(int idcorreo,int estado, String tipomail,String fecha_ingreso, int idconfiguracion, int id_cola) {
        this.idcorreo=idcorreo;
        this.tipomail=tipomail;
        this.estado = estado;
        this.fecha_ingreso = fecha_ingreso;
        this.idconfiguracion = idconfiguracion;
        this.id_cola = id_cola;
    }

    public Mail(){
        
    }
//    public String getNombre_campana() {
//        return nombre_campana;
//    }
//
//    public void setNombre_campana(String nombre_campana) {
//        this.nombre_campana = nombre_campana;
//    }
//
//    public Mail() {
//        this.id = 0;
//        this.id_inbound = 0;
//        this.id_outbound = 0;
//        this.cola = new Cola();
//        this.tipo = null;
//        this.campana = 0;
//
//    }
//
//    public Mail(int id) {
//        this.id = id;
//        this.id_inbound = 0;
//        this.id_outbound = 0;
//         this.tipo = null;
//        this.campana = 0;
//    }
//
//    public Mail(int id, int campana, String inicio_cola, String subject_in,int idconfiguracion, int idCola) {
//        this.id = id;
//        this.subject_in = subject_in;
//        this.inicio_cola = inicio_cola;
//        this.campana = campana;
//        this.idconfiguracion = idconfiguracion;
//        this.id_cola = idCola;
//        this.cola = new Cola(idCola, campana);
//    }
//
//    public Mail(int id, int campana, String inicio_cola) {
//        this.id = id;
//        this.campana = campana;
//        this.inicio_cola = inicio_cola;
//    }
//    /**
//     * @return the id
//     */
//    public int getId() {
//        return id;
//    }
//
//    /**
//     * @param id the id to set
//     */
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    /**
//     * @return the remitente
//     */
//    public String getRemitente() {
//        return remitente;
//    }
//
//    /**
//     * @param remitente the remitente to set
//     */
//    public void setRemitente(String remitente) {
//        this.remitente = remitente;
//    }
//
//    /**
//     * @return the destino
//     */
//    public String getDestino() {
//        return destino;
//    }
//
//    /**
//     * @param destino the destino to set
//     */
//    public void setDestino(String destino) {
//        this.destino = destino;
//    }
//
//    /**
//     * @return the subject
//     */
//    public String getSubject() {
//        return subject;
//    }
//
//    /**
//     * @param subject the subject to set
//     */
//    public void setSubject(String subject) {
//        this.subject = subject;
//    }
//
//    /**
//     * @return the spam
//     */
//    public boolean isSpam() {
//        return spam;
//    }
//
//    /**
//     * @param spam the spam to set
//     */
//    public void setSpam(boolean spam) {
//        this.spam = spam;
//    }
//
//    public Cola getCola() {
//        return cola;
//    }
//
//    public void setCola(Cola cola) {
//        this.cola = cola;
//    }
//
//    public String getFecha_index() {
//        return fecha_index;
//    }
//
//    public void setFecha_index(String fecha_index) {
//        this.fecha_index = fecha_index;
//    }
//
//
//    public int getUsuario() {
//        return usuario;
//    }
//
//
//    public void setUsuario(int usuario) {
//        this.usuario = usuario;
//    }
//
//  
//    public String getTexto() {
//        return texto;
//    }
//
//
//    public void setTexto(String texto) {
//        this.texto = texto;
//    }
//
//
//    public int getId_inbound() {
//        return id_inbound;
//    }
//
//  
//    public void setId_inbound(int id_inbound) {
//        this.id_inbound = id_inbound;
//    }
//
//
//    public int getId_outbound() {
//        return id_outbound;
//    }
//
// 
//    public void setId_outbound(int id_outbound) {
//        this.id_outbound = id_outbound;
//    }
//
//
//    public String getTipo() {
//        return tipo;
//    }
//
//   
//    public void setTipo(String tipo) {
//        this.tipo = tipo;
//    }
//
//
//    public String getSubject_in() {
//        return subject_in;
//    }
//
//    
//    public void setSubject_in(String subject_in) {
//        this.subject_in = subject_in;
//    }
//
// 
//    public String getInicio_cola() {
//        return inicio_cola;
//    }
//
//
//    public void setInicio_cola(String inicio_cola) {
//        this.inicio_cola = inicio_cola;
//    }
//
//    public int getCampana() {
//        return campana;
//    }
//
// 
//    public void setCampana(int campana) {
//        this.campana = campana;
//    }
//
//
//    public String getNombre() {
//        return nombre;
//    }
//
//
//    public void setNombre(String nombre) {
//        this.nombre = nombre;
//    }
//
//    public String getApellido() {
//        return apellido;
//    }
//
//
//    public void setApellido(String apellido) {
//        this.apellido = apellido;
//    }
//
//    public double getPeso_adjunto() {
//        return peso_adjunto;
//    }
//
//    public void setPeso_adjunto(double peso_adjunto) {
//        this.peso_adjunto = peso_adjunto;
//    }
//
//    public String getRuta() {
//        return ruta;
//    }
//
//    public void setRuta(String ruta) {
//        this.ruta = ruta;
//    }
//
//    public int getIdconfiguracion() {
//        return idconfiguracion;
//    }
//
//    public void setIdconfiguracion(int idconfiguracion) {
//        this.idconfiguracion = idconfiguracion;
//    }
//
//    public int getId_cola() {
//        return id_cola;
//    }
//
//    @Override
//    public String toString() {
//        return "Mail{" + "id=" + id + ", remitente=" + remitente + ", destino=" + destino + ", subject=" + subject + ", spam=" + spam + ", cola=" + cola + ", fecha_index=" + fecha_index + ", usuario=" + usuario + ", texto=" + texto + ", id_inbound=" + id_inbound + ", id_outbound=" + id_outbound + ", id_cola=" + id_cola + ", tipo=" + tipo + ", subject_in=" + subject_in + ", inicio_cola=" + inicio_cola + ", campana=" + campana + ", nombre=" + nombre + ", apellido=" + apellido + ", nombre_campana=" + nombre_campana + ", peso_adjunto=" + peso_adjunto + ", ruta=" + ruta + ", idconfiguracion=" + idconfiguracion + '}';
//    }
   
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

    public int getUsuario() {
        return usuario;
    }

    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
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

    public Cola getCola() {
        return cola;
    }

    public void setCola(Cola cola) {
        this.cola = cola;
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
}
