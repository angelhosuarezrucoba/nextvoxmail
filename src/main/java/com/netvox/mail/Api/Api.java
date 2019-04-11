/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;

import com.netvox.mail.entidades.Mensaje;
import com.netvox.mail.servicios.OrganizadorServicio;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis")
public class Api {

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    @Qualifier("organizadorservicio")
    private OrganizadorServicio organizadorservicio;

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @RequestMapping(value = "/mensajes", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String enviar(@RequestParam Map<String, String> data) {
        Mensaje mensaje = new Mensaje();
        Document datarecibida = Document.parse(data.get("data"));
        mensaje.setTipocomunicacion(datarecibida.getString("event"));
        mensaje.setNumeroentrante(datarecibida.getString("from"));
        mensaje.setNumerosaliente(datarecibida.getString("to"));
        mensaje.setTexto(datarecibida.getString("text"));        
        mensaje.setFecha(formato.format(new Date()));
        mensaje.setAtendido(false);
        organizadorservicio.OrganizarMensajes(mensaje);
        //this.template.convertAndSend("/controlmensajes/mensajes", mensaje);
       // this.template.convertAndSendToUser(user, destination, data);
        return "OK";
    }

}
