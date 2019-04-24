/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidadesfront.MailFront;
import com.netvox.mail.servicios.ClienteMongoServicio;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("mailservicio")
public class MailServicioImpl {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    public List<MailFront> obtenerMails() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        List<Mail> lista = mongoops.find(new Query(), Mail.class);
        List<MailFront> listafront = new ArrayList<>();
        lista.forEach((t) -> {
            MailFront mailfront = new MailFront();
            mailfront.setId(t.getIdcorreo());
            mailfront.setEstado(t.getEstado());
            mailfront.setRemitente(t.getRemitente());
            mailfront.setAsunto(t.getAsunto());
            mailfront.setFecha_ingreso(t.getFecha_ingreso());
            mailfront.setAdjuntos(t.getListadeadjuntos());
            listafront.add(mailfront);
        });
        return listafront;
    }
}
