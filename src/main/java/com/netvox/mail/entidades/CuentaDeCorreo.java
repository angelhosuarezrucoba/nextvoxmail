package com.netvox.mail.entidades;

public class CuentaDeCorreo {

    private String usuario;
    private String clave;
    private String host;
    private String store;
    private int puerto;
    private int maximo_adjunto;
    private int id_cola;
    private String nombre_cola;
    private int id_campana;
    private int idconfiguracion;

    public CuentaDeCorreo(String usuario, String clave, String host, String store, int puerto, int maximo_adjunto, int id_cola, String nombre_cola, int id_campana, int idconfiguracion) {
        this.usuario = usuario;
        this.clave = clave;
        this.host = host;
        this.store = store;
        this.puerto = puerto;
        this.maximo_adjunto = maximo_adjunto;
        this.id_cola = id_cola;
        this.nombre_cola = nombre_cola;
        this.id_campana = id_campana;
        this.idconfiguracion = idconfiguracion;
    }
    
    

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public int getMaximo_adjunto() {
        return maximo_adjunto;
    }

    public void setMaximo_adjunto(int maximo_adjunto) {
        this.maximo_adjunto = maximo_adjunto;
    }

    public int getId_cola() {
        return id_cola;
    }

    public void setId_cola(int id_cola) {
        this.id_cola = id_cola;
    }

    public String getNombre_cola() {
        return nombre_cola;
    }

    public void setNombre_cola(String nombre_cola) {
        this.nombre_cola = nombre_cola;
    }

    public int getId_campana() {
        return id_campana;
    }

    public void setId_campana(int id_campana) {
        this.id_campana = id_campana;
    }

    public int getIdconfiguracion() {
        return idconfiguracion;
    }

    public void setIdconfiguracion(int idconfiguracion) {
        this.idconfiguracion = idconfiguracion;
    }

}
