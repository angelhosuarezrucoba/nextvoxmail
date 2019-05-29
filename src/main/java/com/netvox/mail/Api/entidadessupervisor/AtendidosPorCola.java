/*
 * Esta clase se creo para poder enviarle informacion al modulo del supervisor . (monitor)
 */
package com.netvox.mail.Api.entidadessupervisor;

/**
 *
 * @author Angelho Suarez
 */
public class AtendidosPorCola {

    private int id_cola;
    private int cantidad;

  

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getId_cola() {
        return id_cola;
    }

    public void setId_cola(int id_cola) {
        this.id_cola = id_cola;
    }

}
