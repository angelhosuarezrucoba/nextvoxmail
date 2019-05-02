/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;

import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidadesfront.MailSalida;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.MailServicio;
import java.text.SimpleDateFormat;
import java.util.List;
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
public class ApiMail {

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    @Autowired
    @Qualifier("mailservicio")
    MailServicio mailservicio;

    @PostMapping("/crearcorreo")
    public MailInbox crearcorreo(@RequestBody MailSalida mailsalida) {
        return mailservicio.crearCorreo(mailsalida);
    }

    @PostMapping("/listarcorreo")
    public List<MailInbox> listarCorreos(@RequestBody Mensaje mensaje) {;
        return mailservicio.listarCorreos(mensaje);
    }

    @PostMapping("/abrircorreo")
    public String abrirCorreo(@RequestBody MailInbox mailconsultainbox) {
        return mailservicio.obtenerContenidoMail(mailconsultainbox);
    }

    @PostMapping("/listarcorreoencola")
    public List<MailInbox> listarcorreoencola(@RequestBody Mensaje mensaje) {;
        return mailservicio.listarCorreosEnCola(mensaje);
    }

    @PostMapping("/asignarcorreo")
    public void asignarcorreo(@RequestBody Mensaje mensaje) {
        mailservicio.autoAsignarse(mensaje);
    }

 
}
