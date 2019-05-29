/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api.entidadessupervisor;

import java.util.List;

/**
 *
 * @author Angelho
 */
public class Contenido {

    private int campana;
    private String tipo;
    private List<Integer> colas;
    private int vista;
    private String fecha;
    private int horainicio;
    private int horafin;
    private String onlinehistorico;
    private int mes ; // el mes siempre me trae una unidad menos , va de 0 a 11
    private int ano;//año
    
    
    
    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<Integer> getColas() {
        return colas;
    }

    public void setColas(List<Integer> colas) {
        this.colas = colas;
    }

    public int getVista() {
        return vista;
    }

    public void setVista(int vista) {
        this.vista = vista;
    }

    public int getHorainicio() {
        return horainicio;
    }

    public void setHorainicio(int horainicio) {
        this.horainicio = horainicio;
    }

    public int getHorafin() {
        return horafin;
    }

    public void setHorafin(int horafin) {
        this.horafin = horafin;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getOnlinehistorico() {
        return onlinehistorico;
    }

    public void setOnlinehistorico(String onlinehistorico) {
        this.onlinehistorico = onlinehistorico;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

}
