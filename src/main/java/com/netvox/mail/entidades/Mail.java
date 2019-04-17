/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

public class Mail {

  private int id;
    private String remitente;
    private String destino;
    private String subject;
    private boolean spam;
    private Cola cola;
    private String fecha_index;
    private int usuario;
    private String texto;
    private int id_inbound;
    private int id_outbound;
    private int id_cola;
    private String tipo;
    private String subject_in;
    private String inicio_cola;
    private int campana;
    private String nombre;
    private String apellido;
    private String nombre_campana;
    private double peso_adjunto;
    private String ruta;
    private int idconfiguracion;
    
    public String getNombre_campana() {
        return nombre_campana;
    }

    public void setNombre_campana(String nombre_campana) {
        this.nombre_campana = nombre_campana;
    }
    
    

    public Mail() {
        this.id = 0;
        this.id_inbound = 0;
        this.id_outbound = 0;
        //this.queue = new CampanaBean();
        this.cola = new Cola();
        this.tipo = null;
        this.campana = 0;

    }

    public Mail(int id) {
        this.id = id;
        this.id_inbound = 0;
        this.id_outbound = 0;
        //this.queue = new CampanaBean();
        this.tipo = null;
        this.campana = 0;
    }

    public Mail(int id, int campana, String inicio_cola, String subject_in,int idconfiguracion, int idCola) {
        this.id = id;
        this.subject_in = subject_in;
        this.inicio_cola = inicio_cola;
        this.campana = campana;
        this.idconfiguracion = idconfiguracion;
        this.id_cola = idCola;
        this.cola = new Cola(idCola, campana);
    }

    public Mail(int id, int campana, String inicio_cola) {
        this.id = id;
        this.campana = campana;
        this.inicio_cola = inicio_cola;
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
     * @return the remitente
     */
    public String getRemitente() {
        return remitente;
    }

    /**
     * @param remitente the remitente to set
     */
    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    /**
     * @return the destino
     */
    public String getDestino() {
        return destino;
    }

    /**
     * @param destino the destino to set
     */
    public void setDestino(String destino) {
        this.destino = destino;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the spam
     */
    public boolean isSpam() {
        return spam;
    }

    /**
     * @param spam the spam to set
     */
    public void setSpam(boolean spam) {
        this.spam = spam;
    }

    public Cola getCola() {
        return cola;
    }

    public void setCola(Cola cola) {
        this.cola = cola;
    }

    /**
     * @return the queue
     
    public CampanaBean getQueue() {
        return queue;
    }
*/
    /**
     * @param queue the queue to set
     
    public void setQueue(CampanaBean queue) {
        this.queue = queue;
    }
*/
    
    
    /**
     * @return the fecha_index
     */
    public String getFecha_index() {
        return fecha_index;
    }

    /**
     * @param fecha_index the fecha_index to set
     */
    public void setFecha_index(String fecha_index) {
        this.fecha_index = fecha_index;
    }

    /**
     * @return the usuario
     */
    public int getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(int usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the texto
     */
    public String getTexto() {
        return texto;
    }

    /**
     * @param texto the texto to set
     */
    public void setTexto(String texto) {
        this.texto = texto;
    }

    /**
     * @return the id_inbound
     */
    public int getId_inbound() {
        return id_inbound;
    }

    /**
     * @param id_inbound the id_inbound to set
     */
    public void setId_inbound(int id_inbound) {
        this.id_inbound = id_inbound;
    }

    /**
     * @return the id_outbound
     */
    public int getId_outbound() {
        return id_outbound;
    }

    /**
     * @param id_outbound the id_outbound to set
     */
    public void setId_outbound(int id_outbound) {
        this.id_outbound = id_outbound;
    }

    /**
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the subject_in
     */
    public String getSubject_in() {
        return subject_in;
    }

    /**
     * @param subject_in the subject_in to set
     */
    public void setSubject_in(String subject_in) {
        this.subject_in = subject_in;
    }

    /**
     * @return the inicio_cola
     */
    public String getInicio_cola() {
        return inicio_cola;
    }

    /**
     * @param inicio_cola the inicio_cola to set
     */
    public void setInicio_cola(String inicio_cola) {
        this.inicio_cola = inicio_cola;
    }

    /**
     * @return the campana
     */
    public int getCampana() {
        return campana;
    }

    /**
     * @param campana the campana to set
     */
    public void setCampana(int campana) {
        this.campana = campana;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * @param apellido the apellido to set
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public double getPeso_adjunto() {
        return peso_adjunto;
    }

    public void setPeso_adjunto(double peso_adjunto) {
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

    public int getId_cola() {
        return id_cola;
    }

    @Override
    public String toString() {
        return "Mail{" + "id=" + id + ", remitente=" + remitente + ", destino=" + destino + ", subject=" + subject + ", spam=" + spam + ", cola=" + cola + ", fecha_index=" + fecha_index + ", usuario=" + usuario + ", texto=" + texto + ", id_inbound=" + id_inbound + ", id_outbound=" + id_outbound + ", id_cola=" + id_cola + ", tipo=" + tipo + ", subject_in=" + subject_in + ", inicio_cola=" + inicio_cola + ", campana=" + campana + ", nombre=" + nombre + ", apellido=" + apellido + ", nombre_campana=" + nombre_campana + ", peso_adjunto=" + peso_adjunto + ", ruta=" + ruta + ", idconfiguracion=" + idconfiguracion + '}';
    }
    
   
}
