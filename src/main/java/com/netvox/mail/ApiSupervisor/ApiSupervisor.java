/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ApiSupervisor;

import com.netvox.mail.Api.entidadessupervisor.FiltroIndividual;
import com.netvox.mail.Api.entidadessupervisor.Pausa;
import com.netvox.mail.Api.entidadessupervisor.ReporteGrupalPorDias;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidadesfront.MailSalida;
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
@RequestMapping("/apis/supervisor")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class ApiSupervisor {

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    @Autowired
    @Qualifier("mailservicio")
    MailServicio mailservicio;


    @PostMapping("/listarcorreospendientes")
    public List<MailSalida> listarCorreosPendientes(@RequestBody FiltroIndividual filtro) {
         return mailservicio.listarCorreosPendientes(filtro);
    }

    @PostMapping("/listarcorreosinvalidos")
    public List<MailSalida> listarCorreoInvalidos(@RequestBody FiltroIndividual filtro) {
        return mailservicio.listarCorreoInvalidos(filtro);
    }

    @PostMapping("/listarcorreos")
    public List<MailSalida> listarCorreos(@RequestBody FiltroIndividual filtro) {
        return mailservicio.listarCorreos(filtro);
    }

    @PostMapping("/detalletiemposenpausa")
    public List<Pausa> detalleTiemposeEnPausa(@RequestBody FiltroIndividual filtro) {
        return mailservicio.detalleTiemposeEnPausa(filtro);
    }

      @PostMapping("/detallegrupaldecorreospordias")
    public List<ReporteGrupalPorDias> detalleGrupalDeCorreosPorDias(@RequestBody FiltroIndividual filtro) {
        return mailservicio.detalleGrupalDeCorreosPorDias(filtro);
    }

//    @PostMapping("/enviarcorreo")
//    public void enviarCorreo(@RequestBody MailSalida mailsalida) {
//        mailservicio.enviarcorreo(mailsalida);
//    }
//
//    @PostMapping("/tipificarcorreo")
//    public void tipificarCorreo(@RequestBody MailSalida mailsalida) {
//        
//        mailservicio.tipificarCorreo(mailsalida);
//    }
//
//    @PostMapping("/listartipificaciones")
//    public List<Tipificacion> listarTipificaciones() {
//        return mailservicio.listarTipificaciones();
//    }
}
