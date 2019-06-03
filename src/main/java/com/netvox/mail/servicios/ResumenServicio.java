package com.netvox.mail.servicios;

import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.Mensaje;
import java.util.List;

public interface ResumenServicio {

    public abstract void borrarResumenBaseDatos(int idagente);

    public abstract void borrarResumenTotal();

    public abstract void pausar(Mensaje mensaje);

    public List<Resumen> obtenerListaResumen();

    public void modificarEstado(int idagente, int estado);

    public int obtenerEstado(int idagente);

    public void modificarPedidoPausa(int idagente, int pedidopausa);

    public int obtenerPedidoPausa(int idagente);

    public void modificarPendientes(int idagente, int opcion);//esto permise sumar o restar.

    public int obtenerPendientes(int idagente);

    public Resumen obtenerResumen(int idagente);
  
    public void RemoverResumen(int idagente);

}
