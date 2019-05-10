package com.netvox.mail.Api.entidadessupervisor;

/**
 *
 * @author Angelho Suarez
 */
public class ReporteGrupal {

    private String id;
    private int recibidos;
    private int encola;
    private int atendiendo;
    private int validos;
    private int invalidos;
    private int finalizados;
    private String nombre;
    private String tiempo_atencion;
    private String tiempo_promedio_atencion;
  

    public int getRecibidos() {
        return recibidos;
    }

    public void setRecibidos(int recibidos) {
        this.recibidos = recibidos;
    }

    public int getEncola() {
        return encola;
    }

    public void setEncola(int encola) {
        this.encola = encola;
    }

    public int getAtendiendo() {
        return atendiendo;
    }

    public void setAtendiendo(int atendiendo) {
        this.atendiendo = atendiendo;
    }

    public int getValidos() {
        return validos;
    }

    public void setValidos(int validos) {
        this.validos = validos;
    }

    public int getInvalidos() {
        return invalidos;
    }

    public void setInvalidos(int invalidos) {
        this.invalidos = invalidos;
    }

    public int getFinalizados() {
        return finalizados;
    }

    public void setFinalizados(int finalizados) {
        this.finalizados = finalizados;
    }

    public String getId() {
        return id;
    }

    public void setId(String Id) {
        this.id = Id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTiempo_atencion() {
        return tiempo_atencion;
    }

    public void setTiempo_atencion(String tiempo_atencion) {
        this.tiempo_atencion = tiempo_atencion;
    }

    public String getTiempo_promedio_atencion() {
        return tiempo_promedio_atencion;
    }

    public void setTiempo_promedio_atencion(String tiempo_promedio_atencion) {
        this.tiempo_promedio_atencion = tiempo_promedio_atencion;
    }

   

}
