package com.netvox.mail.Api.entidadessupervisor;

/**
 *
 * @author Angelho Suarez
 */
public class ReporteGrupalPorDias {

    private String id;
    private int recibidos;
    private int encola;
    private int atendiendo;
    private int validos;
    private int invalidos;
    private int finalizados;
    private String nombre;
  

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
}
