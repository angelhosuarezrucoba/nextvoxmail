/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;

import com.netvox.mail.ServiciosImpl.CoreMailServicioImpl;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidadesfront.MailSalida;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.MailServicio;
import java.io.File;
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

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    @PostMapping("/crearcorreo")
    public MailInbox crearcorreo(@RequestBody MailSalida mail) {
        MailSalida nuevomail = null;
        if (mail.getTipificacion() == -1) {
//            generados = correosDaos.setMailScratch(agent_id, campana, cola, destino, titulo, remitente, copia);
//            idrespuesta = generados[1];          
            nuevomail = coremailservicio.generarNuevoCorreo(mail);
            System.out.println("entre a -1 ");
        } else {
//            idrespuesta = correosDaos.getNewMailId(agent_id, campana, mail, tipo, titulo, destino,
//                    copia, tipificacion, mensajeTipificacion, reenvio);
            System.out.println("entre al else");
        }

        System.out.println(coremailservicio.getCARPETA_OUT());
        String rutasalida = coremailservicio.getCARPETA_OUT() + "/" + nuevomail.getId();
        File directorio = new File(rutasalida);
        if (!directorio.exists()) {
            try {
                new File(rutasalida).mkdirs();
                //  CopyEmbedFiles(mail, idrespuesta, tipo);
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }

        MailInbox respuestamail = new MailInbox();
        respuestamail.setId(nuevomail.getId());
        return respuestamail;
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
}
