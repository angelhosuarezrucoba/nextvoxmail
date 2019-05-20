/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;

import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ResumenServicio;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class ApiResumen {

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    @Autowired
    @Qualifier("resumenservicio")
    ResumenServicio resumenservicio;


    @PostMapping("/pausar")
    public void pausar(@RequestBody Mensaje mensaje) {
        resumenservicio.pausar(mensaje);
    }
}
