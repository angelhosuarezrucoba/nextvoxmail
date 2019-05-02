/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import static com.netvox.mail.ServiciosImpl.CoreMailServicioImpl.getListaresumen;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.Adjunto;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.MailSalida;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.servicios.MailServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service("mailservicio")
public class MailServicioImpl implements MailServicio {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    @Autowired
    @Qualifier("clientemysqlservicio")
    ClienteMysqlServicioImpl clientemysqlservicio;

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;
    private FormatoDeFechas formato = new FormatoDeFechas();

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
            mailinbox.setTipificacion(0);
            mailinbox.setDescripcion_tipificacion("nuevo");
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

    @Override
    public void autoAsignarse(Mensaje mensaje) {
        /* 1 disponible 2 atendiendo 4 pausa 5 timbrando*/
        Connection conexion;
        CallableStatement procedimientoalmacenado = null;
        MongoOperations mongoops;
        Resumen usuarioresumen = getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get();
        MailInbox mailinbox = mensaje.getNew_mail();
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query(
                    Criteria.where("agente").is(usuarioresumen.getAgente())),
                    new Update().set("estadoagente", 2).set("pendiente", usuarioresumen.getPendiente() + 1),
                    Resumen.class);

            getListaresumen().stream().filter(
                    (agente) -> agente.getAgente() == usuarioresumen.getAgente()).forEach((agente) -> {
                        agente.setEstadoagente(2);
                        agente.setPendiente(usuarioresumen.getPendiente() + 1);
                    });

            Mail mail = mongoops.findOne(new Query(Criteria.where("idcorreo").is(mailinbox.getId())), Mail.class);
            mongoops.updateFirst(new Query(
                    Criteria.where("idcorreo").is(mailinbox.getId())),
                    new Update()
                            .set("estado", 1)
                            .set("tiempoencola", formato.restaDeFechasEnSegundos(
                                    formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA), mail.getFecha_ingreso()))
                            .set("usuario", usuarioresumen.getAgente())
                            .set("nombre", usuarioresumen.getNombre())
                            .set("fechainiciogestion", formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA)),
                    Mail.class);
            mailinbox.setEstado(1);
            mailinbox.setRemitente(mail.getRemitente());
            mailinbox.setDestino(mail.getDestino());
            mailinbox.setAsunto(mail.getAsunto());
            mailinbox.setFecha_ingreso(mail.getFecha_ingreso());
            if (mail.getListadeadjuntos() == null) {
                mailinbox.setAdjuntos(new ArrayList<Adjunto>());
            } else {
                mailinbox.setAdjuntos(mail.getListadeadjuntos());
            }

            getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola())) // aqui le envio al resto
                    .forEach((resumen) -> {
                        mensaje.setEvento("CORREOASIGNADO");
                        websocket.enviarMensajeParaUnUsuario(mensaje, resumen.getAgente());
                    });

            mensaje.setEvento("CORREOASIGNADO");
            mensaje.setNew_mail(mailinbox);
            websocket.enviarMensajeParaUnUsuario(mensaje, usuarioresumen.getAgente());//aqui envio el mensaje a un usuario asignado

            procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
            procedimientoalmacenado.setInt(1, mail.getCampana());
            procedimientoalmacenado.setInt(2, 2);
            procedimientoalmacenado.setBoolean(3, false);
            procedimientoalmacenado.execute();
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MailInbox crearCorreo(MailSalida mailsalida) {
        MailSalida nuevomail = null;
        switch (mailsalida.getTipificacion()) {
            case -1:
                break;
            case 1 : //spam
                 break;
            case 2 : //standby
                break;
            case 7 ://informaritvo
                break;
            default:
                break;
        }

        if (mailsalida.getTipificacion() == -1) {
//            generados = correosDaos.setMailScratch(agent_id, campana, cola, destino, titulo, remitente, copia);
//            idrespuesta = generados[1];          
            nuevomail = generarNuevoCorreo(mailsalida);
            System.out.println("entre a -1 ");
        } else {
//            idrespuesta = correosDaos.getNewMailId(agent_id, campana, mail, tipo, titulo, destino,
//                    copia, tipificacion, mensajeTipificacion, reenvio);
            System.out.println("entre al else");
        }

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

    public MailSalida generarNuevoCorreo(MailSalida mailsalida) {

        Mail mail = new Mail();
        int nuevoid;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            Query query = new Query().with(new Sort(Direction.DESC, "$natural"));
            query.fields().include("idcorreo");
            Mail ultimomail = mongoops.findOne(query, Mail.class);

            if (ultimomail == null) {
                mailsalida.setId(1);
            } else {
                mailsalida.setId(ultimomail.getIdcorreo() + 1);
            }
            mongoops.insert(mailsalida);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mailsalida;
    }

    
}
