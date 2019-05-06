/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "configuraciones")
public class Configuraciones {
    private int peso_maximo_adjunto;

    public int getPeso_maximo_adjunto() {
        return peso_maximo_adjunto;
    }

    public void setPeso_maximo_adjunto(int peso_maximo_adjunto) {
        this.peso_maximo_adjunto = peso_maximo_adjunto;
    }
}
