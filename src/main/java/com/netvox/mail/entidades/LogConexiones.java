package com.netvox.mail.entidades;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "logconexiones")
public class LogConexiones {

    private int campana;
    private int agente;
    private String nombre;
    private int pendiente;
    private List<Integer> listacolas;
    private int estadoagente;
    private String fechaconexion;
    private String fechadesconexion;

    public LogConexiones(int campana, int agente, String nombre, int pendiente, List<Integer> listacolas, int estadoagente, String fechaconexion) {
        this.campana = campana;
        this.agente = agente;
        this.nombre = nombre;
        this.pendiente = pendiente;
        this.listacolas = listacolas;
        this.estadoagente = estadoagente;
        this.fechaconexion = fechaconexion;
    }

    
    
    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public int getAgente() {
        return agente;
    }

    public void setAgente(int agente) {
        this.agente = agente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPendiente() {
        return pendiente;
    }

    public void setPendiente(int pendiente) {
        this.pendiente = pendiente;
    }

    public List<Integer> getListacolas() {
        return listacolas;
    }

    public void setListacolas(List<Integer> listacolas) {
        this.listacolas = listacolas;
    }

    public int getEstadoagente() {
        return estadoagente;
    }

    public void setEstadoagente(int estadoagente) {
        this.estadoagente = estadoagente;
    }

    public String getFechaconexion() {
        return fechaconexion;
    }

    public void setFechaconexion(String fechaconexion) {
        this.fechaconexion = fechaconexion;
    }

    public String getFechadesconexion() {
        return fechadesconexion;
    }

    public void setFechadesconexion(String fechadesconexion) {
        this.fechadesconexion = fechadesconexion;
    }
    
}
