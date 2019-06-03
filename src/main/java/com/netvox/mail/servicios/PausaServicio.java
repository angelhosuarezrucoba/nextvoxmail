package com.netvox.mail.servicios;

public interface PausaServicio {

    public void pausar(int agente);

    public abstract void despausar(int idagente);

}
