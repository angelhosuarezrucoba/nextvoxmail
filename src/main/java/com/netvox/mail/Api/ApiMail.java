/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;

import com.netvox.mail.ServiciosImpl.VerificadorDeSesionServicioImpl;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidadesfront.MailSalida;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.entidadesfront.Tipificacion;
import com.netvox.mail.servicios.MailServicio;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    @Qualifier("verificadordesesionservicio")
    VerificadorDeSesionServicioImpl verificadordesesionservicio;

    Logger log = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/crearcorreo")
    public MailInbox crearCorreo(@RequestBody MailSalida mailsalida, @RequestHeader String identificador, HttpServletResponse response) {
        MailInbox mail = new MailInbox();
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            mail = mailservicio.crearCorreo(mailsalida);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return mail;
    }

    @PostMapping("/listarcorreo")
    public List<MailInbox> listarCorreos(@RequestBody Mensaje mensaje, @RequestHeader String identificador, HttpServletResponse response) {
        List<MailInbox> lista = new ArrayList<>();
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            lista = mailservicio.listarCorreos(mensaje);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return lista;
    }

    @PostMapping("/abrircorreo")
    public MailSalida abrirCorreo(@RequestBody MailInbox mailconsultainbox, @RequestHeader String identificador,
            HttpServletResponse response) {
        MailSalida mail = null;
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            mail = mailservicio.abrirCorreo(mailconsultainbox);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return mail;
    }

    @PostMapping("/listarcorreoencola")
    public List<MailInbox> listarCorreoEnCola(@RequestBody Mensaje mensaje, @RequestHeader String identificador,
            HttpServletResponse response) {
        List<MailInbox> lista = new ArrayList<>();
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            lista = mailservicio.listarCorreosEnCola(mensaje);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return lista;
    }

    @PostMapping("/asignarcorreo")
    public Mensaje asignarCorreo(@RequestBody Mensaje mensaje, @RequestHeader String identificador,
            HttpServletResponse response) {
        Mensaje nuevomensaje = null;
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            nuevomensaje = mailservicio.autoAsignarse(mensaje);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return nuevomensaje;
    }

    @PostMapping("/adjuntararchivo")
    public String adjuntarArchivo(@RequestParam("archivo") MultipartFile archivo,
            @RequestHeader int idcorreo, @RequestHeader String identificador, HttpServletResponse response) {
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            mailservicio.adjuntarcorreo(archivo, idcorreo);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return "ok";
    }

    @PostMapping("/enviarcorreo")
    public Mensaje enviarCorreo(@RequestBody MailSalida mailsalida,
            @RequestHeader String identificador, HttpServletResponse response) {
        Mensaje mensaje = new Mensaje();
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            mensaje = mailservicio.enviarcorreo(mailsalida);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return mensaje;
    }

    @PostMapping("/tipificarcorreo")
    public Mensaje tipificarCorreo(@RequestBody MailSalida mailsalida,
            @RequestHeader String identificador, HttpServletResponse response) {

        Mensaje mensaje = new Mensaje();
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            mensaje = mailservicio.tipificarCorreo(mailsalida);
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return mensaje;
    }

    @PostMapping("/listartipificaciones")
    public List<Tipificacion> listarTipificaciones(@RequestHeader String identificador,
            HttpServletResponse response) {
        List<Tipificacion> lista = new ArrayList<>();
        if (verificadordesesionservicio.sesionvalida(identificador)) {
            response.setStatus(HttpServletResponse.SC_OK);
            lista = mailservicio.listarTipificaciones();
        } else {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return lista;
    }
}
