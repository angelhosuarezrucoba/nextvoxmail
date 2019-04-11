package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cliente")
public class Cliente {

    private String nombres;
    private String apellidos;
    private String numero;
    private String operador;
    private int edad;
    private String direccion;
    private String estado;

    public static final String EN_ATENCION = "EN_ATENCION";
    public static final String EN_ESPERA = "EN_ESPERA";
    public static final String INACTIVO = "INACTIVO";
//    public static final String EN_ATENCION = "EN_ATENCION";
//    public static final String EN_ATENCION = "EN_ATENCION";

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }



    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

}
