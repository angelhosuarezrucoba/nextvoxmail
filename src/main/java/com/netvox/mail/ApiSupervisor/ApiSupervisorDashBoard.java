/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ApiSupervisor;

import com.netvox.mail.Api.entidadessupervisor.Contenido;
import com.netvox.mail.Api.entidadessupervisor.Grafico;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.servicios.MailServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis/supervisor")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class ApiSupervisorDashBoard {

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    @Autowired
    @Qualifier("mailservicio")
    MailServicio mailservicio;

    @PostMapping("/dashboard")
    public Grafico graficoMultiCanal(@RequestBody Contenido contenido) {
        return mailservicio.graficoMultiCanal(contenido);
    }
}
