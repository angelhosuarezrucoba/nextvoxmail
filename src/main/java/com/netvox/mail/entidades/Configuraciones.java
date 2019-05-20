package com.netvox.mail.entidades;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "configuraciones")
public class Configuraciones {

    private int peso_maximo_adjunto;
    private int idcorreo;//esto permite generar la secuencia correcta de correos previene el error de cruzar ids.

    public int getPeso_maximo_adjunto() {
        return peso_maximo_adjunto;
    }

    public void setPeso_maximo_adjunto(int peso_maximo_adjunto) {
        this.peso_maximo_adjunto = peso_maximo_adjunto;
    }

    public int getIdcorreo() {
        return idcorreo;
    }

    public void setIdcorreo(int idcorreo) {
        this.idcorreo = idcorreo;
    }

}
