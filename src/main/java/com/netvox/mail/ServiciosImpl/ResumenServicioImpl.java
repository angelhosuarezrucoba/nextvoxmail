package com.netvox.mail.ServiciosImpl;

import static com.netvox.mail.ServiciosImpl.CoreMailServicioImpl.getListaresumen;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.PausaServicio;
import com.netvox.mail.servicios.ResumenServicio;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service("resumenservicio")
public class ResumenServicioImpl implements ResumenServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    @Autowired
    @Qualifier("resumendiarioservicio")
    ResumenDiarioServicioImpl resumendiarioservicio;

    @Autowired
    @Qualifier("resumenservicio")
    ResumenServicio resumenservicio;

    @Autowired
    @Qualifier("pausaservicio")
    PausaServicio pausaservicio;

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void borrarResumenBaseDatos(int idagente) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            mongoops.remove(new Query(Criteria.where("agente").is(idagente)), Resumen.class);
        } catch (Exception e) {
            log.error("error en el metodo borrarResumenBaseDatos", e.getCause());
        }
    }

    @Override
    public void borrarResumenTotal() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            mongoops.remove(new Query(), Resumen.class);
        } catch (Exception e) {
            log.error("error en el metodo borrarResumenTotal", e.getCause());
        }
    }

    @Override
    public void pausar(Mensaje mensaje) {
        Resumen usuarioresumen = obtenerResumen(mensaje.getIdagente());
        try {
            if (usuarioresumen.getEstadoagente() == 4) {//despausar
                log.info("entre al primer if");
                modificarEstado(usuarioresumen.getAgente(), 1);
                pausaservicio.despausar(usuarioresumen.getAgente());
                resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), 1, 0);// aqui si se termina una pausa real y se contabiliza
                log.info("El agente " + usuarioresumen.getNombre() + " salio de la pausa");
            } else if (usuarioresumen.getPedido_pausa() == 1) { //despausar  
                 log.info("entre al primer else if");
                modificarPedidoPausa(usuarioresumen.getAgente(), 0);
                resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), 2, 0);// en este caso se tiene que cambiar el pedido de pausa.
                log.info("El agente " + usuarioresumen.getNombre() + " salio de la pausa");
            } else {//pausar              
                 log.info("entre al else");
                if (usuarioresumen.getEstadoagente() == 1) {//si esta en 1 se pausa y se cuenta el tiempo,  si esta en 2 se da el "pedido_pausa"
                    modificarEstado(usuarioresumen.getAgente(), 4);
                    if (mensaje.isPausasupervisor()) {
                        mensaje.setEvento("PAUSA");
                        websocket.enviarMensajeParaUnUsuario(mensaje, usuarioresumen.getAgente());
                    }
                    pausaservicio.pausar(usuarioresumen.getAgente());
                } else {
                    modificarPedidoPausa(usuarioresumen.getAgente(), 1);
                }
                resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), obtenerEstado(usuarioresumen.getAgente()), obtenerPedidoPausa(usuarioresumen.getAgente()));
                log.info("El agente " + usuarioresumen.getNombre() + " entro en pausa");
            }
        } catch (Exception e) {
            log.error("Error al pausar", e.getCause());
        }
    }

    @Override
    public List<Resumen> obtenerListaResumen() {
        return coremailservicio.getListaresumen();
    }

    @Override
    public int obtenerEstado(int idagente) {
        return coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get().getEstadoagente();
    }

    @Override
    public void modificarPedidoPausa(int idagente, int pedidopausa) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get().setPedido_pausa(pedidopausa);
        mongoops.updateFirst(new Query(Criteria.where("agente").is(idagente)), new Update().set("pedido_pausa", pedidopausa), Resumen.class);
    }

    @Override
    public int obtenerPedidoPausa(int idagente) {
        return coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get().getPedido_pausa();
    }

    @Override
    public int obtenerPendientes(int idagente) {
        return coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get().getPendiente();
    }

    @Override
    public Resumen obtenerResumen(int idagente) {
        return coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get();
    }

    @Override
    public void removerResumen(int idagente) {
        coremailservicio.getListaresumen().remove(obtenerResumen(idagente));
    }

    @Override
    public void modificarPendientes(int idagente, int opcion) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get().setPendiente(opcion);
        mongoops.updateFirst(new Query(Criteria.where("agente").is(idagente)), new Update().set("pendiente", opcion), Resumen.class);
    }

    @Override
    public void modificarEstado(int idagente, int estado) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        coremailservicio.getListaresumen().stream().filter((resumen) -> resumen.getAgente() == idagente).findFirst().get().setEstadoagente(estado);
        mongoops.updateFirst(new Query(Criteria.where("agente").is(idagente)), new Update().set("estadoagente", estado), Resumen.class);
    }

    @Override
    public boolean hayResumen(int idagente) {
        return getListaresumen().stream().anyMatch((Resumen resumen) -> {
            return resumen.getAgente() == idagente;
        });
    }

}
