/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.controladores;

import com.netvox.mail.entidadesfront.Mapa;
import com.netvox.mail.entidadesfront.Prueba;
import com.netvox.mail.entidadesfront.Registrar;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MailControlador {

    // SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss");
    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/prueba") // este es el destino al que se envian los mensajes y lo redirige a controlmensajes
    public void enviar(Prueba mensaje) {
        Mapa.getMapa().get(18).forEach((sesion) -> {
            this.template.convertAndSend("/mailcore/respuestas-" + sesion, mensaje);
        });
    }

    @MessageMapping("/registrar") // este es el destino al que se envian los mensajes y lo redirige a controlmensajes
    //@SendToUser("/respuestas/mensajes") // este destino es el la cola por asi decirlo donde los usuarios estan suscrito por el websocket.    
    public void registrar(Registrar registro, @Header("simpSessionId") String sessionId) {
        if (Mapa.getMapa().containsKey(registro.getNumero())) {
            Mapa.getMapa().get(registro.getNumero()).add(sessionId);
        } else {
            List<String> lista = new ArrayList<>();
            lista.add(sessionId);
            Mapa.getMapa().put(registro.getNumero(), lista);
        }
    }

   
//    @MessageMapping("/chat") // este es el destino al que se envian los mensajes y lo redirige a controlmensajes
//    @SendTo("/controlmensajes/mensajes") // este destino es el la cola por asi decirlo donde los usuarios estan suscrito por el websocket.
//    public Mensaje enviar(Peticion peticion) {
//        Mensaje whatsapp = new Mensaje();
//        RestTemplate restTemplate = new RestTemplate();
//        String ruta = "https://panel.apiwha.com/send_message.php?apikey=IKNJDGPU0M0K1I0RSSUL&number=51960368316&text=" + peticion.getTexto();
//        restTemplate.execute(ruta, HttpMethod.GET, null, null);
////   
////        whatsapp.setEvent("SALIDA");
////        whatsapp.setFrom("923325598");
////        whatsapp.setText(peticion.getTexto());
////        whatsapp.setTo("960368316");
//
//        return whatsapp;
//    }
}
