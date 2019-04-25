/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import com.google.gson.Gson;
import com.netvox.mail.entidadesfront.Agente;
import com.netvox.mail.entidadesfront.MapaAgentes;
import com.netvox.mail.entidadesfront.MensajeFront;
import com.netvox.mail.entidadesfront.RespuestaLogin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service("websocket")
public class WebSocket extends TextWebSocketHandler {

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Gson gson = new Gson();
        MensajeFront mensajedelfront = gson.fromJson(message.getPayload(), MensajeFront.class);
        switch (mensajedelfront.getEvento()) {
            case "LOGIN":
                RespuestaLogin mensaje = new RespuestaLogin();
                mensaje.setEvento("LOGINRESPONSE");
                enviarMensajeParaUnUsuario(mensaje,mensajedelfront.getIdagente());
                System.out.println("es es el evento de login");
                break;
            case "perrochango":
                System.out.println("este es el perrochango");
                break;
            default:
                break;
        }
// enviarMensajeParaUnUsuario(mensajedelfront, obtenerAgente(session).getIdagente());
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
        List<Agente> lista = MapaAgentes.getMapa().values().stream()
                .filter((listadeagentes) -> {
                    return listadeagentes.stream().filter((agente) -> {
                        return agente.getSesion().getId().equalsIgnoreCase(session.getId());
                    }).collect(Collectors.toList()).size() > 0;
                }).collect(Collectors.toList()).get(0);

        lista.remove(lista.stream().filter((agente) -> {
            return agente.getSesion().getId().equalsIgnoreCase(session.getId());
        }).collect(Collectors.toList()).get(0));

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
                    ex.printStackTrace();
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
