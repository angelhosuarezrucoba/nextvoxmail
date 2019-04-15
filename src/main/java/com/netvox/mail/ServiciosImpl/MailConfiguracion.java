/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

/**
 *
 * @author Home
 */
public class MailConfiguracion {
    private int id;
    private String usuario;
    private String pass;
    private String host;
    private String smtp;
    private String store;
    private int puertoentrada;
    private int puertosalida;
    private String tipo;
    private int max_adjunto;
    private int idcampana;
    private int maximo_pendiente;
    private String mensaje_mail_pesado;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public int getPuertoentrada() {
        return puertoentrada;
    }

    public void setPuertoentrada(int puertoentrada) {
        this.puertoentrada = puertoentrada;
    }

    public int getPuertosalida() {
        return puertosalida;
    }

    public void setPuertosalida(int puertosalida) {
        this.puertosalida = puertosalida;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getMax_adjunto() {
        return max_adjunto;
    }

    public void setMax_adjunto(int max_adjunto) {
        this.max_adjunto = max_adjunto;
    }

    public int getIdcampana() {
        return idcampana;
    }

    public void setIdcampana(int idcampana) {
        this.idcampana = idcampana;
    }

    public int getMaximo_pendiente() {
        return maximo_pendiente;
    }

    public void setMaximo_pendiente(int maximo_pendiente) {
        this.maximo_pendiente = maximo_pendiente;
    }

    public String getMensaje_mail_pesado() {
        return mensaje_mail_pesado;
    }

    public void setMensaje_mail_pesado(String mensaje_mail_pesado) {
        this.mensaje_mail_pesado = mensaje_mail_pesado;
    }
    
    @Override
    public String toString() {
        return "EmailConfiguracionBean{" + "id=" + id + ", usuario=" + usuario + ", pass=" + pass + ", host=" + host + ", smtp=" + smtp + ", store=" + store + ", puertoentrada=" + puertoentrada + ", puertosalida=" + puertosalida + ", tipo=" + tipo + ", max_adjunto=" + max_adjunto + ", idcampana=" + idcampana + ", maximo_pendiente=" + maximo_pendiente + '}';
    }
}
