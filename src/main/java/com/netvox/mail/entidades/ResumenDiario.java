package com.netvox.mail.entidades;

public class ResumenDiario { //esta clase es solo para mandarle la info a miguel

    private int agente;
    private String nombre;
    private int campana;
    private String hora_logueo;
    private int pendientes;
    private int estado;
    private String hora_inicio_estado;
    private String hora_inicio_pausa;
    private int tiempo_acumulado_pausa;
    private int tiempo_acumulado_logueo;
    private int atendidos;
    private int pedido_pausa;
    private String session;
    private String primer_logueo;

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

    public int getCampana() {
        return campana;
    }

    public void setCampana(int campana) {
        this.campana = campana;
    }

    public String getHora_logueo() {
        return hora_logueo;
    }

    public void setHora_logueo(String hora_logueo) {
        this.hora_logueo = hora_logueo;
    }

    public int getPendientes() {
        return pendientes;
    }

    public void setPendientes(int pendientes) {
        this.pendientes = pendientes;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getHora_inicio_estado() {
        return hora_inicio_estado;
    }

    public void setHora_inicio_estado(String hora_inicio_estado) {
        this.hora_inicio_estado = hora_inicio_estado;
    }

    public String getHora_inicio_pausa() {
        return hora_inicio_pausa;
    }

    public void setHora_inicio_pausa(String hora_inicio_pausa) {
        this.hora_inicio_pausa = hora_inicio_pausa;
    }

    public int getTiempo_acumulado_pausa() {
        return tiempo_acumulado_pausa;
    }

    public void setTiempo_acumulado_pausa(int tiempo_acumulado_pausa) {
        this.tiempo_acumulado_pausa = tiempo_acumulado_pausa;
    }

    public int getTiempo_acumulado_logueo() {
        return tiempo_acumulado_logueo;
    }

    public void setTiempo_acumulado_logueo(int tiempo_acumulado_logueo) {
        this.tiempo_acumulado_logueo = tiempo_acumulado_logueo;
    }

    public int getAtendidos() {
        return atendidos;
    }

    public void setAtendidos(int atendidos) {
        this.atendidos = atendidos;
    }

    public int getPedido_pausa() {
        return pedido_pausa;
    }

    public void setPedido_pausa(int pedido_pausa) {
        this.pedido_pausa = pedido_pausa;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getPrimer_logueo() {
        return primer_logueo;
    }

    public void setPrimer_logueo(String primer_logueo) {
        this.primer_logueo = primer_logueo;
    }
}
