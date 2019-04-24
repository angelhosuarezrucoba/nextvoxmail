/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;

import com.netvox.mail.ServiciosImpl.MailServicioImpl;
import com.netvox.mail.entidadesfront.MailFront;
import com.netvox.mail.entidadesfront.Peticion;
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
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST})
public class ApiMail {

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    @Qualifier("mailservicio")
    MailServicioImpl mailservicio;
    
    @PostMapping("/lista")
    public List<MailFront> obtenerMails(@RequestBody Peticion peticion) {
        System.out.println(peticion.getIdentificador());
        return mailservicio.obtenerMails();
    }
    
   

}
