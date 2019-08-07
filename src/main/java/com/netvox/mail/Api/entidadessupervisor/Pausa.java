/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api.entidadessupervisor;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Angelho Suarez 
 */

@Document(collection = "pausas")
public class Pausa {

    private String iniciopausa;
    private String finpausa;
    private int idagente;
    private String nombreagente;
    private int duracion;
    
    ///////variables de reporte//////
    private String fecha;
    private String horainiciopausa;
    private String horafinpausa;
    private String duracionpausa;
    
    ///////////

    public String getIniciopausa() {
        return iniciopausa;
    }

    public void setIniciopausa(String iniciopausa) {
        this.iniciopausa = iniciopausa;
    }

    public String getFinpausa() {
        return finpausa;
    }

    public void setFinpausa(String finpausa) {
        this.finpausa = finpausa;
    }

    public int getIdagente() {
        return idagente;
    }

    public void setIdagente(int idagente) {
        this.idagente = idagente;
    }

    public String getNombreagente() {
        return nombreagente;
    }

    public void setNombreagente(String nombreagente) {
        this.nombreagente = nombreagente;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHorainiciopausa() {
        return horainiciopausa;
    }

    public void setHorainiciopausa(String horainiciopausa) {
        this.horainiciopausa = horainiciopausa;
    }

    public String getHorafinpausa() {
        return horafinpausa;
    }

    public void setHorafinpausa(String horafinpausa) {
        this.horafinpausa = horafinpausa;
    }

    public String getDuracionpausa() {
        return duracionpausa;
    }

    public void setDuracionpausa(String duracionpausa) {
        this.duracionpausa = duracionpausa;
    }

    @Override
    public String toString() {
        return "Pausa{" + "iniciopausa=" + iniciopausa + ", finpausa=" + finpausa + ", idagente=" + idagente + ", nombreagente=" + nombreagente + ", duracion=" + duracion + ", fecha=" + fecha + ", horainiciopausa=" + horainiciopausa + ", horafinpausa=" + horafinpausa + ", duracionpausa=" + duracionpausa + '}';
    }
    
    
}
