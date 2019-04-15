/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.configuraciones;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 *
 * @author Asus
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguracion extends AbstractWebSocketMessageBrokerConfigurer {
//implements WebSocketMessageBrokerConfigurer la guia menciona esto pero ... no tenemos que configurar todo. abstrac...es un adapter

    @Override
    public void configureMessageBroker(MessageBrokerRegistry configuracion) {
        configuracion.enableSimpleBroker("/controlmensajes"); // esto es el lugar donde se controla la cola de mensajes
        configuracion.setApplicationDestinationPrefixes("/mensajes");//prefijo al que debe enviarse desde el cliente, anexado con el controlador.
        configuracion.setUserDestinationPrefix("/asesor");
        /* este prefijo se agrega para generar un control sobre las etiquetas @messagemapping
            es decir /servidor no necesariamente sera el unico metodo que reciba mensajes, entonces
        todo se agrupa en /app/"ruta del controlador"
         */
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registro) {
        registro.addEndpoint("/websocket").setAllowedOrigins("*").withSockJS();
        /*habilita la ruta donde llegan las peticiones de sockjs si es que websocket protocol no esta habilitado*/

    }

}
