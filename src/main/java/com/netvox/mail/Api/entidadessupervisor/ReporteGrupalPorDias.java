package com.netvox.mail.Api.entidadessupervisor;

/**
 *
 * @author Angelho Suarez
 */
public class ReporteGrupalPorDias {

    private String _id;
    private int recibidos;
    private int encola;
    private int atendiendo;
    private int validos;
    private int invalidos;
    private int finalizados;

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

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
}