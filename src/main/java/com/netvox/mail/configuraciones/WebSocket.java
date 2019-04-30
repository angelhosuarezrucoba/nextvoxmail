/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.google.gson.Gson;
import com.netvox.mail.ServiciosImpl.CoreMailServicioImpl;
import com.netvox.mail.entidadesfront.Agente;
import com.netvox.mail.entidadesfront.Login;
import com.netvox.mail.entidadesfront.MapaAgentes;
import com.netvox.mail.entidadesfront.MensajeFront;
import com.netvox.mail.entidadesfront.RespuestaLogin;
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
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MensajeFront mensajedelfront = new Gson().fromJson(message.getPayload(), MensajeFront.class);
        System.out.println(message.getPayload());
        switch (mensajedelfront.getEvento()) {
            case "LOGIN":
                Login mensajelogin = new Gson().fromJson(message.getPayload(), Login.class);
                RespuestaLogin mensaje = coremailservicio.obtenerRespuestaDeLogin(mensajelogin);                
                enviarMensajeParaUnUsuario(mensaje, mensajedelfront.getIdagente());
                break;
            case "perrochango":
                System.out.println("este es el perrochango");
                break;
            default:
                break;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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
    } //coloca un id de agente a cada sesion y las agrupa para que se visualice en cada pantalla

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
            throws Exception { // remueve la sesion de la lista de sesiones que tiene un agente
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
        
        if(lista.isEmpty()){
            MapaAgentes.getMapa().remove(listaidagente.get(0).getIdagente());
        }
    }

    public void enviarMensajeGlobal(MensajeFront mensaje) {
        MapaAgentes.getMapa().values().forEach((listasesiones) -> {
            listasesiones.forEach((Agente agente) -> {
                if (agente.getSesion().isOpen()) {
                    try {
                        agente.getSesion().sendMessage(new TextMessage(new Gson().toJson(mensaje)));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
    }

    public void enviarMensajeParaUnUsuario(Object mensaje, int idagente) {

        MapaAgentes.getMapa().get(idagente).forEach((sesion) -> {
            if (sesion.getSesion().isOpen()) {
                try {
                    sesion.getSesion().sendMessage(new TextMessage(new Gson().toJson(mensaje)));
                } catch (IOException ex) {
                    //ex.printStackTrace();
                }
            }
        });
    }

    public Agente obtenerAgente(WebSocketSession sesion) {
        List<Agente> lista = MapaAgentes.getMapa().values().stream()
                .filter((listadeagentes) -> {
                    return listadeagentes.stream().filter((agente) -> {
                        return agente.getSesion().getId().equalsIgnoreCase(sesion.getId());
                    }).collect(Collectors.toList()).size() > 0;
                }).collect(Collectors.toList()).get(0);

        return lista.stream().filter((agente) -> {
            return agente.getSesion().getId().equalsIgnoreCase(sesion.getId());
        }).collect(Collectors.toList()).get(0);
    }
}
