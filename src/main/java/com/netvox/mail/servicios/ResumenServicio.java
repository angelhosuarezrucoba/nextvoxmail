package com.netvox.mail.servicios;

import com.netvox.mail.entidadesfront.Mensaje;

public interface ResumenServicio {

    public abstract void borrarResumenBaseDatos(int idagente);

    public abstract void borrarResumenTotal();

    public abstract void pausar(Mensaje mensaje);

}
