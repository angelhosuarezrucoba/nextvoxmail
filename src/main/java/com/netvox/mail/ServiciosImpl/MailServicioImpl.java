/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidadesfront.Adjunto;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.servicios.MailServicio;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("mailservicio")
public class MailServicioImpl implements MailServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    @Override
    public List<MailInbox> listarCorreos(Mensaje mensaje) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        List<Mail> lista = mongoops.find(
                new Query(Criteria.where("usuario").is(mensaje.getIdagente())).with(new Sort(new Order(Direction.DESC, "idcorreo"))),
                Mail.class);
        List<MailInbox> listamailinbox = new ArrayList<>();
        lista.forEach((t) -> {
            MailInbox mailinbox = new MailInbox();
            mailinbox.setId(t.getIdcorreo());
            mailinbox.setEstado(t.getEstado());
            mailinbox.setRemitente(t.getRemitente());
            mailinbox.setAsunto(t.getAsunto());
            mailinbox.setFecha_ingreso(t.getFecha_ingreso());
            if (t.getListadeadjuntos() == null) {
                t.setListadeadjuntos(new ArrayList<Adjunto>());
            } else {
                mailinbox.setAdjuntos(t.getListadeadjuntos());
            }
            listamailinbox.add(mailinbox);
        });
        return listamailinbox;
    }

    @Override
    public String obtenerContenidoMail(MailInbox mailconsultainbox) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Query query = new Query(Criteria.where("idcorreo").is(mailconsultainbox.getId()));
        query.fields().include("idcorreo").include("mensaje");
        Mail mail = mongoops.findOne(query, Mail.class);
        return mail.getMensaje();
    }

    @Override
    public List<MailInbox> listarCorreosEnCola(Mensaje mensaje) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        List<Mail> listacorreosencola = mongoops.find(
                new Query(Criteria.where("id_cola").in(mensaje.getColas()).and("estado").is(0).and("usuario").is(0)).with(new Sort(new Order(Direction.DESC, "idcorreo"))),
                Mail.class);
        List<MailInbox> listamailinbox = new ArrayList<>();
        listacorreosencola.forEach((t) -> {
            MailInbox mailinbox = new MailInbox();
            mailinbox.setId(t.getIdcorreo());
            mailinbox.setEstado(t.getEstado());
            mailinbox.setRemitente(t.getRemitente());
            mailinbox.setAsunto(t.getAsunto());
            mailinbox.setFecha_ingreso(t.getFecha_ingreso());
            if (t.getListadeadjuntos() == null) {
                t.setListadeadjuntos(new ArrayList<Adjunto>());
            } else {
                mailinbox.setAdjuntos(t.getListadeadjuntos());
            }
            listamailinbox.add(mailinbox);
        });
        return listamailinbox;
    }
}
