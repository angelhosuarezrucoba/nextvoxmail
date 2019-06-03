/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.google.gson.Gson;
import com.netvox.mail.ServiciosImpl.CoreMailServicioImpl;
import com.netvox.mail.ServiciosImpl.LogConexionesServicio;
import com.netvox.mail.ServiciosImpl.VerificadorDeSesionServicioImpl;
import com.netvox.mail.entidadesfront.Agente;
import com.netvox.mail.entidadesfront.MapaAgentes;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.PausaServicio;
import com.netvox.mail.servicios.ResumenServicio;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service("websocket")
public class WebSocket extends TextWebSocketHandler {

    @Autowired
    @Qualifier("resumenservicio")
    ResumenServicio resumenservicio;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    @Autowired
    @Qualifier("logconexionesservicio")
    LogConexionesServicio logconexionesservcio;

    @Autowired
    @Qualifier("verificadordesesionservicio")
    VerificadorDeSesionServicioImpl verificadordesesionservicio;

    @Autowired
    @Qualifier("pausaservicio")
    PausaServicio pausaservicio;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Mensaje mensaje = new Gson().fromJson(message.getPayload(), Mensaje.class);
        switch (mensaje.getEvento()) {
            case "LOGIN":
                if (verificadordesesionservicio.sesionvalida(mensaje.getIdentificador())) {
                    enviarMensajeParaUnUsuario(coremailservicio.obtenerRespuestaDeLogin(mensaje), mensaje.getIdagente());
                } else {
                    Mensaje mensajerror = new Mensaje();
                    mensajerror.setEvento("ERRORLOGIN");
                    enviarMensajeParaUnUsuario(mensajerror, mensaje.getIdagente());
                }
                break;
            case "LOGOUT":
                break;
            default:
                break;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        //esto debe migrar a un metodo para ser llamado luego del login y no exactamente despues de conseguir la conexion
        System.out.println("Conectando agente " + session.getAttributes().get("idagente") + " SessionID " + session.getId());
        int idagente = Integer.parseInt((String) session.getAttributes().get("idagente"));
        Agente agente = new Agente(idagente, session);
        if (MapaAgentes.getMapa().containsKey(idagente)) {
            MapaAgentes.getMapa().get(idagente).add(agente);
        } else {
            List<Agente> lista = new ArrayList<>();
            lista.add(agente);
            MapaAgentes.getMapa().put(idagente, lista);
        }

        System.out.println("Al conectarme ");
        MapaAgentes.getMapa().forEach((id_agente, listasesiones) -> {
            System.out.println("Id agente : " + idagente);
            listasesiones.forEach((sesion) -> {
                System.out.println("       sesion: " + sesion.getSesion().getId());
            });
        });
    } //coloca un id de agente a cada sesion y las agrupa para que se visualice en cada pantalla

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception { // remueve la sesion de la lista de sesiones que tiene un agente        
        //deberia validarse en caso de que nunca se haya conseguido el inicio de sesion , que es el caso de un login invalido, por lo general no sucedera.
        System.out.println("cerr lasesion " + session.getId());
        List<Agente> lista = MapaAgentes.getMapa().values().stream()
                .filter((listadeagentes) -> {
                    return listadeagentes.stream().filter((agente) -> {
                        return agente.getSesion().getId().equalsIgnoreCase(session.getId());
                    }).collect(Collectors.toList()).size() > 0;
                }).collect(Collectors.toList()).get(0);

        List<Agente> listaidagente = new ArrayList<>();
        listaidagente.add(lista.stream().filter((agente) -> {
            return agente.getSesion().getId().equalsIgnoreCase(session.getId());
        }).collect(Collectors.toList()).get(0));

        lista.remove(listaidagente.get(0));

        if (lista.isEmpty()) {
            logconexionesservcio.grabarDesconexion(listaidagente.get(0).getIdagente());
            if (resumenservicio.obtenerEstado(listaidagente.get(0).getIdagente()) == 4) {
                pausaservicio.despausar(listaidagente.get(0).getIdagente());
            }
            MapaAgentes.getMapa().remove(listaidagente.get(0).getIdagente());
            resumenservicio.borrarResumenBaseDatos(listaidagente.get(0).getIdagente());
            coremailservicio.borrarListaResumen(listaidagente.get(0).getIdagente());//en memoria
        }

    }

    public void enviarMensajeParaUnUsuario(Object mensaje, int idagente) {

        MapaAgentes.getMapa().get(idagente).forEach((sesion) -> {
            if (sesion.getSesion().isOpen()) {
                try {
                    sesion.getSesion().sendMessage(new TextMessage(new Gson().toJson(mensaje)));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

}
