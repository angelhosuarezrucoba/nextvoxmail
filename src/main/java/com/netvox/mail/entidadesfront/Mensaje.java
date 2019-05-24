package com.netvox.mail.entidadesfront;

import java.util.List;

public class Mensaje {

    private String evento; // esto es lo que nos dice que accion es la que se va a realizar
    private int idagente;
    private int campana;
    private List<Integer> colas;
    private int estado_mail; // estado del agente
    private String agente;
    private int acumulado_mail;
    private int cantidad_cola_mail;
    private MailInbox new_mail;
    private List<MailFront> listacorreos;
    private int peso_maximo_adjunto;
    private int idcorreoasignado;//con esto le aviso al front que correo fue asignado para que el lo descuente.
    private String firma;
    private String identificador;
    private int tiempo_pausa;
    private int pedido_pausa;
    
    public Mensaje() {
    }

    
    public Mensaje(int idagente) { //este constructor solo lo uso para los websockets de conexion y desconexion
        this.idagente = idagente;
    }   
        
    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public int getIdagente() {
        return idagente;
    }

    public void setIdagente(int idagente) {
        this.idagente = idagente;
    }

    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public List<Integer> getColas() {
        return colas;
    }

    public void setColas(List<Integer> colas) {
        this.colas = colas;
    }

    public int getEstado_mail() {
        return estado_mail;
    }

    public void setEstado_mail(int estado_mail) {
        this.estado_mail = estado_mail;
    }

    public String getAgente() {
        return agente;
    }

    public void setAgente(String agente) {
        this.agente = agente;
    }

    public int getAcumulado_mail() {
        return acumulado_mail;
    }

    public void setAcumulado_mail(int acumulado_mail) {
        this.acumulado_mail = acumulado_mail;
    }

    public int getCantidad_cola_mail() {
        return cantidad_cola_mail;
    }

    public void setCantidad_cola_mail(int cantidad_cola_mail) {
        this.cantidad_cola_mail = cantidad_cola_mail;
    }

    public MailInbox getNew_mail() {
        return new_mail;
    }

    public void setNew_mail(MailInbox new_mail) {
        this.new_mail = new_mail;
    }

    public List<MailFront> getListacorreos() {
        return listacorreos;
    }

    public void setListacorreos(List<MailFront> listacorreos) {
        this.listacorreos = listacorreos;
    }

    public int getPeso_maximo_adjunto() {
        return peso_maximo_adjunto;
    }

    public void setPeso_maximo_adjunto(int peso_maximo_adjunto) {
        this.peso_maximo_adjunto = peso_maximo_adjunto;
    }

    public int getIdcorreoasignado() {
        return idcorreoasignado;
    }

    public void setIdcorreoasignado(int idcorreoasignado) {
        this.idcorreoasignado = idcorreoasignado;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public int getTiempo_pausa() {
        return tiempo_pausa;
    }

    public void setTiempo_pausa(int tiempo_pausa) {
        this.tiempo_pausa = tiempo_pausa;
    }

    public int getPedido_pausa() {
        return pedido_pausa;
    }

    public void setPedido_pausa(int pedido_pausa) {
        this.pedido_pausa = pedido_pausa;
    }

 
  

}
