/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "mensaje")
public class Mensaje {

    private String tipocomunicacion;
    private String numeroentrante;
    private String numerosaliente;
    private String texto;
    private String fecha;
    private String asesor;
    private boolean atendido;
    
   
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getAsesor() {
        return asesor;
    }

    public void setAsesor(String asesor) {
        this.asesor = asesor;
    }

    public boolean isAtendido() {
        return atendido;
    }

    public void setAtendido(boolean atendido) {
        this.atendido = atendido;
    }

    public String getTipocomunicacion() {
        return tipocomunicacion;
    }

    public void setTipocomunicacion(String tipocomunicacion) {
        this.tipocomunicacion = tipocomunicacion;
    }

    public String getNumeroentrante() {
        return numeroentrante;
    }

    public void setNumeroentrante(String numeroentrante) {
        this.numeroentrante = numeroentrante;
    }

    public String getNumerosaliente() {
        return numerosaliente;
    }

    public void setNumerosaliente(String numerosaliente) {
        this.numerosaliente = numerosaliente;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return "Mensaje{" + "tipocomunicacion=" + tipocomunicacion + ", numeroentrante=" + numeroentrante + ", numerosaliente=" + numerosaliente + ", texto=" + texto + ", fecha=" + fecha + ", asesor=" + asesor + ", atendido=" + atendido + '}';
    }

   
}
