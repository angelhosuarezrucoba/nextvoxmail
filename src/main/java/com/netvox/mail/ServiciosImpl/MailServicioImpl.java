/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.MailConfiguracion;
import com.netvox.mail.Api.entidadessupervisor.AtendidosPorCola;
import com.netvox.mail.Api.entidadessupervisor.Contenido;
import com.netvox.mail.Api.entidadessupervisor.FiltroIndividual;
import com.netvox.mail.Api.entidadessupervisor.Grafico;
import com.netvox.mail.Api.entidadessupervisor.Mes;
import com.netvox.mail.Api.entidadessupervisor.Pausa;
import com.netvox.mail.Api.entidadessupervisor.ReporteGrafico;
import com.netvox.mail.Api.entidadessupervisor.ReporteGrupal;
import static com.netvox.mail.ServiciosImpl.CoreMailServicioImpl.getListaresumen;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.Adjunto;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.MailSalida;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.entidadesfront.Tipificacion;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.servicios.MailServicio;
import com.netvox.mail.servicios.PausaServicio;
import com.netvox.mail.servicios.ResumenServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Cond;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    @Qualifier("formatodefechas")
    FormatoDeFechas formatodefechas;

    @Autowired
    @Qualifier("resumendiarioservicio")
    ResumenDiarioServicioImpl resumendiarioservicio;

    @Autowired
    @Qualifier("resumenservicio")
    ResumenServicio resumenservicio;

    @Autowired
    @Qualifier("pausaservicio")
    PausaServicio pausaservicio;

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<MailInbox> listarCorreos(Mensaje mensaje) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        List<MailSalida> lista = mongoops.find(
                new Query(Criteria.where("usuario").is(mensaje.getIdagente())).with(new Sort(new Order(Direction.DESC, "idcorreo"))),
                MailSalida.class);
        List<MailInbox> listamailinbox = new ArrayList<>();
        lista.forEach((t) -> {
            MailInbox mailinbox = new MailInbox();
            mailinbox.setId(t.getId());
            mailinbox.setEstado(t.getEstado());
            mailinbox.setRemitente(t.getRemitente());
            mailinbox.setAsunto(t.getTitulo());
            mailinbox.setFecha_ingreso(formatodefechas.cambiarFormatoFechas(t.getFecha_ingreso(),
                    formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));//la mando asi para el front y su comodidad
            mailinbox.setNombre_cola(t.getNombre_cola());
            mailinbox.setTipificacion(t.getTipificacion());
            mailinbox.setDescripcion_tipificacion(t.getDescripcion_tipificacion());
            mailinbox.setAdjuntos(t.getListadeadjuntos());
            mailinbox.setIdhilo(t.getIdhilo());
            mailinbox.setDestino(t.getDestino());
            mailinbox.setId_cola(t.getCola());
            mailinbox.setListacopia(t.getListacopia());
            listamailinbox.add(mailinbox);
        });
        return listamailinbox;
    }

    @Override
    public MailSalida abrirCorreo(MailInbox mailconsultainbox) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Query query = new Query(Criteria.where("idcorreo").is(mailconsultainbox.getId()));
        query.fields().include("idcorreo").include("mensaje").include("listadeembebidos");
        MailSalida mail = mongoops.findOne(query, MailSalida.class);
        String mensajefinal = mail.getMensaje();
        if (mailconsultainbox.getTipo().equals("salida")) {//con esto transformo los base64 de salida
            if (!mail.getListadeembebidos().isEmpty()) {
                for (String embebido : mail.getListadeembebidos()) {
                    mail.setMensaje(mail.getMensaje().replace("cid:" + embebido, coremailservicio.getPath_salida() + "/" + mailconsultainbox.getId() + "/" + embebido));
                }
            }
        }
        return mail;
    }

    @Override
    public List<MailInbox> listarCorreosEnCola(Mensaje mensaje) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        List<MailSalida> listacorreosencola = mongoops.find(
                new Query(Criteria.where("id_cola").in(mensaje.getColas()).and("estado").is(0).and("usuario").is(0)).with(new Sort(new Order(Direction.DESC, "idcorreo"))),
                MailSalida.class);
        List<MailInbox> listamailinbox = new ArrayList<>();
        listacorreosencola.forEach((t) -> {
            MailInbox mailinbox = new MailInbox();
            mailinbox.setId(t.getId());
            mailinbox.setEstado(t.getEstado());
            mailinbox.setRemitente(t.getRemitente());
            mailinbox.setAsunto(t.getTitulo());
            mailinbox.setFecha_ingreso(formatodefechas.cambiarFormatoFechas(t.getFecha_ingreso(),
                    formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));//la mando asi para el front y su comodidad
            mailinbox.setTipificacion(t.getTipificacion());
            mailinbox.setDescripcion_tipificacion(t.getDescripcion_tipificacion());
            mailinbox.setAdjuntos(t.getListadeadjuntos());
            mailinbox.setIdhilo(t.getIdhilo());
            mailinbox.setId_cola(t.getCola());
            mailinbox.setNombre_cola(t.getNombre_cola());
            mailinbox.setListacopia(t.getListacopia());
            listamailinbox.add(mailinbox);
            System.out.println(listamailinbox.toString());
        });

        return listamailinbox;
    }

    @Override
    public String autoAsignarse(Mensaje mensaje) {//deberia enviar un 500 si es que no se puede, y garantizar el query con estado 1
        MongoOperations mongoops;
        Resumen usuarioresumen = getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get();
        FindAndModifyOptions opciones = new FindAndModifyOptions();
        opciones.returnNew(true);
        Mail nuevomail = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            if (usuarioresumen.getEstadoagente() == 1 || usuarioresumen.getEstadoagente() == 4) {
                if (usuarioresumen.getEstadoagente() == 4) {
                    pausaservicio.despausar(usuarioresumen.getAgente());
                }
                resumenservicio.modificarEstado(usuarioresumen.getAgente(), 2);
                resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), 2, 0);
            }
            resumenservicio.modificarPendientes(usuarioresumen.getAgente(), usuarioresumen.getPendiente() + 1);
            resumendiarioservicio.actualizarPendientes(1, usuarioresumen.getAgente());
            Mail mail = mongoops.findOne(new Query(Criteria.where("idcorreo").is(mensaje.getIdcorreoasignado())), Mail.class);  //obtengo esto para tener la fecha de ingreso
            nuevomail = mongoops.findAndModify(new Query(Criteria.where("idcorreo").is(mail.getIdcorreo())), new Update()
                    .set("estado", 1)
                    .set("tiempoencola", formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), mail.getFecha_ingreso()))
                    .set("usuario", usuarioresumen.getAgente())
                    .set("nombre", usuarioresumen.getNombre())
                    .set("fechainiciogestion", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)), opciones, Mail.class);
            getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola())) // aqui le envio al resto
                    .forEach((resumen) -> {
                        if (usuarioresumen.getAgente() != resumen.getAgente()) {
                            mensaje.setEvento("CORREOASIGNADO");
                            websocket.enviarMensajeParaUnUsuario(mensaje, resumen.getAgente());
                        }
                    });
        } catch (ParseException ex) {
            log.error("error en el metodo autoAsignarse", ex);
        }
        return formatodefechas.cambiarFormatoFechas(nuevomail.getFechainiciogestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH);
    }

    @Override
    public MailInbox crearCorreo(MailSalida mailsalida) {
        MailSalida nuevomail = null;
        MailInbox respuestamail;
        File directorio;
        switch (mailsalida.getTipificacion()) {
            case -1://correo nuevo
                nuevomail = generarNuevoCorreo(mailsalida);
                break;
            default:
                nuevomail = generarCorreo(mailsalida);
                break;
        }
        directorio = new File(coremailservicio.getRUTA_OUT() + "/" + nuevomail.getId());
        directorio.mkdir();
        respuestamail = new MailInbox();
        respuestamail.setId(nuevomail.getId());
        return respuestamail;
    }

    public MailSalida generarNuevoCorreo(MailSalida mailsalida) {
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            mailsalida.setFecha_ingreso(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setFechainiciogestion(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setFechafingestion(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setEstado(2);//correo enviado
            mailsalida.setTipo("salida");
            mailsalida.setListacopia(mailsalida.getListacopia());
            mailsalida.setId(coremailservicio.generadorId());
            mailsalida.setIdhilo(mailsalida.getId());
            mongoops.insert(mailsalida);

        } catch (Exception e) {
            log.error("error en el metodo generarNuevoCorreo", e);
        }
        return mailsalida;
    }

    @Override
    public void adjuntarcorreo(MultipartFile archivo, int idcorreo) {
        FileOutputStream archivosalidastream = null;
        File directorio;
        File nuevoarchivo;
        BufferedOutputStream buferdesalida;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            directorio = new File(coremailservicio.getRUTA_OUT() + "/" + idcorreo + "/adjuntos");
            directorio.mkdir();
            nuevoarchivo = new File(directorio.getAbsolutePath(), archivo.getOriginalFilename());
            archivosalidastream = new FileOutputStream(nuevoarchivo);
            buferdesalida = new BufferedOutputStream(archivosalidastream);
            buferdesalida.write(archivo.getBytes());
            buferdesalida.close();
            archivosalidastream.close();
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(idcorreo)),
                    new Update().push("listadeadjuntos", new Adjunto(archivo.getOriginalFilename(), (int) archivo.getSize(), nuevoarchivo.getAbsolutePath())), Mail.class);
        } catch (FileNotFoundException ex) {
            log.error("error en el metodo adjuntarcorreo", ex);
        } catch (IOException ex) {
            log.error("error en el metodo adjuntarcorreo", ex);
        }
    }

    @Override
    public Mensaje enviarcorreo(MailSalida mailsalida) {
        Resumen usuarioresumen;
        Mensaje mensaje = new Mensaje();
        MongoOperations mongoops;
        MailSalida nuevomail = null;
        FileOutputStream archivosalidastream = null;
        File archivo;
        BufferedOutputStream buferdesalida;
        FindAndModifyOptions opciones = new FindAndModifyOptions();
        opciones.returnNew(true);
        try {
            mongoops = clientemongoservicio.clienteMongo();
            nuevomail = mongoops.findAndModify(new Query(Criteria.where("idcorreo").is(mailsalida.getId())),
                    new Update().set("mensaje", reemplazarImagenesEnBase64(mailsalida)), opciones, MailSalida.class);
            archivo = new File(coremailservicio.getRUTA_OUT() + "/" + nuevomail.getId() + "/" + nuevomail.getId() + ".txt");
            MailSalida clasetransformadora = mongoops.find(new Query(Criteria.where("idhilo").is(nuevomail.getIdhilo())).with(new Sort(Direction.ASC, "idcorreo")), MailSalida.class).get(0);;
            MailInbox nuevomailsalida = new MailInbox();
            nuevomailsalida.setFechainiciogestion(formatodefechas.cambiarFormatoFechas(clasetransformadora.getFechainiciogestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));
            nuevomailsalida.setFechafingestion(formatodefechas.cambiarFormatoFechas(clasetransformadora.getFechafingestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));
            archivosalidastream = new FileOutputStream(archivo);
            buferdesalida = new BufferedOutputStream(archivosalidastream);
            buferdesalida.write(nuevomail.getMensaje().getBytes());
            buferdesalida.close();
            archivosalidastream.close();
            usuarioresumen = resumenservicio.obtenerResumen(nuevomail.getId_agente());
            mensaje.setEstado_mail(resumenservicio.obtenerEstado(usuarioresumen.getAgente()));
            mensaje.setAcumulado_mail(resumenservicio.obtenerPendientes(usuarioresumen.getAgente()));
            mensaje.setPedido_pausa(resumenservicio.obtenerPedidoPausa(usuarioresumen.getAgente()));
            mensaje.setNew_mail(nuevomailsalida);
        } catch (Exception ex) {
            log.error("error en el metodo enviarcorreo", ex);
        }
        prepararMensaje(nuevomail);
        return mensaje;
    }

    public MimeMultipart agregarCuerpoDelMensaje(Object mensaje) {
        MimeMultipart multipart = new MimeMultipart("related");// mensaje en general.          
        BodyPart bodypartmensaje = new MimeBodyPart();
        try {
            bodypartmensaje.setContent(mensaje, "text/html; charset=\"UTF-8\"");
            multipart.addBodyPart(bodypartmensaje);
        } catch (MessagingException e) {
            log.error("error en el metodo agregarCuerpoDelMensaje", e);
        }
        return multipart;
    }

    public MimeMultipart agregarEmbebidosAlMensaje(MimeMultipart multipart, int idcorreo) {
        String rutaembebidos = coremailservicio.getRUTA_OUT() + "/" + idcorreo;  //embebidos
        File directorioembebidos = new File(rutaembebidos);
        for (File archivo : directorioembebidos.listFiles()) {
            if (!archivo.isDirectory() && !archivo.getName().equalsIgnoreCase(idcorreo + ".txt")) {
                try {
                    MimeBodyPart mimebodypartembebido = new MimeBodyPart();
                    mimebodypartembebido.setHeader("Content-ID", "<" + archivo.getName() + ">");
                    mimebodypartembebido.setDisposition(MimeBodyPart.INLINE);
                    mimebodypartembebido.attachFile(archivo);
                    multipart.addBodyPart(mimebodypartembebido);
                } catch (IOException | MessagingException e) {
                    log.error("error en el metodo agregarEmbebidosAlMensaje", e);
                }
            }
        }
        return multipart;
    }

    public MimeMultipart agregarAdjuntosAlMensaje(MimeMultipart multipart, int idcorreo) {
        String ruta = coremailservicio.getRUTA_OUT() + "/" + idcorreo + "/adjuntos";
        File adjuntos = new File(ruta);
        if (adjuntos.listFiles() != null) {
            for (File archivo : adjuntos.listFiles()) {
                try {
                    MimeBodyPart mimebodypart = new MimeBodyPart();
                    DataSource filedatasource = new FileDataSource(archivo);
                    mimebodypart.setDataHandler(new DataHandler(filedatasource));
                    mimebodypart.setFileName(archivo.getName());
                    multipart.addBodyPart(mimebodypart);
                } catch (Exception e) {
                    log.error("error en el metodo agregarAdjuntosAlMensaje", e);
                }
            }
        }
        return multipart;
    }

    public MailConfiguracion obtenerConfiguracionMail(String remitente) {
        MailConfiguracion mailconfiguracion = new MailConfiguracion();
        try {
            Connection conexion = clientemysqlservicio.obtenerConexion();
            ResultSet resultset = conexion.createStatement().executeQuery("select * from email_configuracion where usuario='" + remitente + "'");
            while (resultset.next()) {
                mailconfiguracion.setSmtp(resultset.getString("smtp"));
                mailconfiguracion.setUsuario(resultset.getString("usuario"));
                mailconfiguracion.setPass(resultset.getString("pass"));
                mailconfiguracion.setPuertosalida(resultset.getInt("puerto_salida"));
            }
            resultset.close();
            conexion.close();
        } catch (Exception e) {
            log.error("error en el metodo obtenerConfiguracionMail", e);
        }
        return mailconfiguracion;
    }

    public MimeMessage agregarDestinatariosEnCopia(MimeMessage mimemensaje, String copia) {
        if (!copia.equals("")) {
            String listadedestinosencopia[] = copia.split(";");
            for (String destinoencopia : listadedestinosencopia) {
                try {
                    mimemensaje.addRecipient(Message.RecipientType.CC, new InternetAddress(destinoencopia));
                } catch (MessagingException ex) {
                    log.error("error en el metodo agregarDestinatariosEnCopia", ex);
                }
            }
        }
        return mimemensaje;
    }

    public void prepararMensaje(MailSalida mailsalida) {
        MimeMultipart multipart = agregarCuerpoDelMensaje(mailsalida.getMensaje());//cuerpo mensaje
        multipart = agregarEmbebidosAlMensaje(multipart, mailsalida.getId());//archivos embebidos
        multipart = agregarAdjuntosAlMensaje(multipart, mailsalida.getId());//archivos adjuntos
        MailConfiguracion mailconfiguracion = obtenerConfiguracionMail(mailsalida.getRemitente());
        try {
            Session sesion = obtenerSesion(mailconfiguracion);
            MimeMessage mimemensaje = new MimeMessage(sesion);
            mimemensaje.setSubject(mailsalida.getTitulo());
            mimemensaje.setContent(multipart);
            mimemensaje.setFrom(new InternetAddress(mailsalida.getRemitente()));
            mimemensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(mailsalida.getDestino()));
            mimemensaje = agregarDestinatariosEnCopia(mimemensaje, mailsalida.getCopia());
            Transport.send(mimemensaje);
        } catch (MessagingException ex) {
            log.error("error en el metodo prepararMensaje", ex);
        }
    }

    public String reemplazarImagenesEnBase64(MailSalida mailsalida) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        String mensajefinal = mailsalida.getMensaje();
        if (mensajefinal.contains("<img")) {
            int contador = 0;
            int index = mensajefinal.indexOf("<img");
            while (index >= 0) {
                String aux = mensajefinal.substring(index, mensajefinal.length());
                int indexAux = aux.indexOf(">");
                String imgtag = mensajefinal.substring(index, (index + indexAux + 1));
                int indexRow = imgtag.indexOf("src=");
                while (indexRow >= 0) {
                    String srcString = imgtag.substring(indexRow, imgtag.length());
                    int indexComilla = srcString.indexOf("\"");
                    srcString = srcString.substring(indexComilla + 1, srcString.length());
                    int indexComilla2 = srcString.indexOf("\"");
                    String imgValue = srcString.substring(0, indexComilla2);
                    if (imgValue.contains("data:")) {
                        String nombre_archivo = "embed_OUT_IMG" + contador;
                        contador++;
                        nombre_archivo = guardarEmbebido(nombre_archivo, imgValue, mailsalida.getId());
                        mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mailsalida.getId())), //esto guarda una lista solo para facilitar reemplazarlos
                                new Update().push("listadeembebidos", nombre_archivo), MailSalida.class);
                        mensajefinal = mensajefinal.replace(imgValue, "cid:" + nombre_archivo);
                    }
                    indexRow = imgtag.indexOf("src=", indexRow + 1);
                }
                index = mensajefinal.indexOf("<img", index + 1);
            }
        }
        return mensajefinal;
    }

    public String guardarEmbebido(String name, String data, int idcorreo) {
        String nombre_final = name;
        String[] b64 = data.split(",");
        String[] aux = b64[0].split("/");
        String extension = aux[1].split(";")[0];
        byte[] dataBytes = DatatypeConverter.parseBase64Binary(b64[1]);
        String path = coremailservicio.getRUTA_OUT() + "/" + idcorreo + "/" + name + "." + extension;
        nombre_final = nombre_final + "." + extension;
        File file = new File(path);
        try {
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            outputStream.write(dataBytes);

        } catch (IOException e) {
            log.error("error en el metodo guardarEmbebido", e);
        }
        return nombre_final;
    }

    public Session obtenerSesion(MailConfiguracion mailconfiguracion) {

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailconfiguracion.getSmtp());
        props.setProperty("mail.user", mailconfiguracion.getUsuario());
        props.setProperty("mail.smtp.port", mailconfiguracion.getPuertosalida() + "");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.password", mailconfiguracion.getPass());
        props.setProperty("mail.smtp.auth", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailconfiguracion.getUsuario(), mailconfiguracion.getPass());
            }
        });
    }

    @Override
    public Mensaje tipificarCorreo(MailSalida mailsalida) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Mensaje mensaje = new Mensaje();
        Resumen usuarioresumen;
        FindAndModifyOptions opciones = new FindAndModifyOptions();
        opciones.returnNew(true);
        try {
            Query querymailaresponder = new Query(Criteria.where("idcorreo").is(mailsalida.getId()));
            querymailaresponder.fields().include("idcorreo").include("tipificacion").include("usuario").include("campana").include("id_cola");
            MailSalida mailaresponder = mongoops.findOne(querymailaresponder, MailSalida.class);
            usuarioresumen = resumenservicio.obtenerResumen(mailaresponder.getId_agente());
            if (mailaresponder.getTipificacion() == 0) {//esto es para evitar que reduzca los pendientes de correos ya tipificados
                resumenservicio.modificarPendientes(mailaresponder.getId_agente(), usuarioresumen.getPendiente() - 1);
                resumendiarioservicio.actualizarPendientes(-1, mailaresponder.getId_agente());
                resumendiarioservicio.actualizarAtendidosPorCola(mailaresponder.getId_agente(), mailaresponder.getId_campana(), mailaresponder.getCola());
                if (usuarioresumen.getPendiente() == 0) {
                    if (usuarioresumen.getPedido_pausa() == 1) {//el estado es 2 si o si
                        resumenservicio.modificarEstado(usuarioresumen.getAgente(), 4);
                        resumenservicio.modificarPedidoPausa(usuarioresumen.getAgente(), 0);
                        pausaservicio.pausar(usuarioresumen.getAgente());
                    } else {
                        if (usuarioresumen.getEstadoagente() == 2) {
                            resumenservicio.modificarEstado(usuarioresumen.getAgente(), 1);
                        }
                    }
                    resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), resumenservicio.obtenerEstado(usuarioresumen.getAgente()), 0);
                }
            }
            MailSalida mail = mongoops.findOne(new Query(Criteria.where("idcorreo").is(mailsalida.getId())), MailSalida.class);
            MailSalida nuevomailsalida = mongoops.findAndModify(new Query(Criteria.where("idcorreo").is(mailsalida.getId())),
                    new Update().set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).set("tipificacion", mailsalida.getTipificacion())
                            .set("tiempo_atencion", formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion()))
                            .set("fechafingestion", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA))
                            .set("hilocerrado", true), opciones,
                    MailSalida.class);//estoy usando esta clase solo por lo conveniente que es ya que tiene id y tipificacion
            MailInbox nuevomail = new MailInbox(); //esto lo hago para darle las fechas al front;
            nuevomail.setFechainiciogestion(formatodefechas.cambiarFormatoFechas(mail.getFechainiciogestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));
            nuevomail.setFechafingestion(formatodefechas.cambiarFormatoFechas(nuevomailsalida.getFechafingestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));
            mensaje.setNew_mail(nuevomail);
            mensaje.setEstado_mail(resumenservicio.obtenerEstado(usuarioresumen.getAgente()));
            mensaje.setAcumulado_mail(resumenservicio.obtenerPendientes(usuarioresumen.getAgente()));
            mensaje.setPedido_pausa(resumenservicio.obtenerPedidoPausa(usuarioresumen.getAgente()));
        } catch (ParseException ex) {
            log.error("error en el metodo tipificarCorreo", ex);
        }
        return mensaje;
    }

    @Override
    public List<Tipificacion> listarTipificaciones() {
        Connection conexion = clientemysqlservicio.obtenerConexion();
        ResultSet resultset;
        List<Tipificacion> listatipificaciones = new ArrayList<>();
        try {
            resultset = conexion.createStatement().executeQuery("select id,nombre from tipificacion_mail where activo=1 and  id not in(1,2,7)");
            while (resultset.next()) {
                listatipificaciones.add(new Tipificacion(resultset.getInt("id"), resultset.getString("nombre")));
            }
            resultset.close();
            conexion.close();
        } catch (SQLException e) {
            log.error("error en el metodo listarTipificaciones", e);
        }
        return listatipificaciones;
    }

    private MailSalida generarCorreo(MailSalida mailsalida) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            Query querymailaresponder = new Query(Criteria.where("idcorreo").is(mailsalida.getId()));
            querymailaresponder.fields().include("idcorreo").include("tipificacion").include("usuario").include("campana").include("id_cola");
            MailSalida mailaresponder = mongoops.findOne(querymailaresponder, MailSalida.class);
            Resumen usuarioresumen = resumenservicio.obtenerResumen(mailaresponder.getId_agente());
            if (mailaresponder.getTipificacion() == 0) {//esto es para evitar que reduzca los pendientes de correos ya tipificados
                resumenservicio.modificarPendientes(mailaresponder.getId_agente(), usuarioresumen.getPendiente() - 1);
                resumendiarioservicio.actualizarPendientes(-1, mailaresponder.getId_agente());
                resumendiarioservicio.actualizarAtendidosPorCola(mailaresponder.getId_agente(), mailaresponder.getId_campana(), mailaresponder.getCola());
                if (usuarioresumen.getPendiente() == 0) {
                    if (usuarioresumen.getPedido_pausa() == 1) {//el estado es 2 si o si
                        resumenservicio.modificarEstado(usuarioresumen.getAgente(), 4);
                        resumenservicio.modificarPedidoPausa(usuarioresumen.getAgente(), 0);
                        pausaservicio.pausar(usuarioresumen.getAgente());
                    } else {
                        if (usuarioresumen.getEstadoagente() == 2) {
                            resumenservicio.modificarEstado(usuarioresumen.getAgente(), 1);
                        }
                    }
                    resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), resumenservicio.obtenerEstado(usuarioresumen.getAgente()), 0);
                }
            }
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mailsalida.getId())), new Update().set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).set("tipificacion", mailsalida.getTipificacion()), MailSalida.class);
            mailsalida.setId(coremailservicio.generadorId());
            mailsalida.setFecha_ingreso(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setFechainiciogestion(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setTipo("salida");
            mailsalida.setEstado(2);//correo enviado            
            mongoops.insert(mailsalida);

            if (mailsalida.getReenvio() != 1) {//esto es para evitar el reenvio
                mongoops.updateMulti(new Query(Criteria.where("idhilo").is(mailsalida.getIdhilo())),
                        new Update().set("hilocerrado", true).
                                set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).
                                set("tipificacion", mailsalida.getTipificacion()),
                        MailSalida.class);
                Query queryhilo = new Query(Criteria.where("idhilo").is(mailsalida.getIdhilo()));
                queryhilo.fields().include("idcorreo").include("fechainiciogestion");
                List<MailSalida> listahilo = mongoops.find(queryhilo, MailSalida.class);
                MailSalida minimo = listahilo.stream().min((mail_1, mail_2) -> Integer.compare(mail_1.getId(), mail_2.getId())).get();
                MailSalida maximo = listahilo.stream().max((mail_1, mail_2) -> Integer.compare(mail_1.getId(), mail_2.getId())).get();
                mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(minimo.getId())),
                        new Update().set("tiempo_atencion", formatodefechas.restaDeFechasEnSegundos(maximo.getFechainiciogestion(), minimo.getFechainiciogestion()))
                                .set("fechafingestion", maximo.getFechainiciogestion()),
                        MailSalida.class);
            }

        } catch (ParseException e) {
            log.error("error al generar el correo", e);
        }
        return mailsalida;
    }

    @Override
    public List<MailSalida> listarCorreosPendientes(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<MailSalida> lista = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            lista = mongoops.find(
                    new Query(new Criteria().andOperator(
                            Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                            Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin()))
                            .and("estado").is(1).and("usuario").in(filtro.getListadeagentes())
                            .and("id_cola").in(filtro.getListadecolas())),
                    MailSalida.class
            );
            for (MailSalida mail : lista) {
                mail.setHora(formatodefechas.cambiarFormatoFechas(mail.getFechainiciogestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_HORA));
                mail.setEstadoatencion(mail.getEstado() == 1 ? "ATENDIENDO" : "FINALIZADO");
                mail.setValidacion(mail.getTipificacion() == 1 ? "INVALIDO" : "VALIDO");
                mail.setTiempo_cola(mail.getTiempoencola() == 0 ? "00:00:00" : formatodefechas.convertirSegundosAFecha(mail.getTiempoencola()));
                if (mail.getTipo().equals("salida")) {
                    mail.setTiempoatencion("00:00:00");
                } else {
                    List<MailSalida> sublista = lista.stream().filter((correo) -> correo.getIdhilo() == mail.getIdhilo()).collect(Collectors.toList());
                    if (sublista.size() > 0) {
                        MailSalida mailmasreciente = sublista.stream().max((mail1, mail2) -> mail1.getFechainiciogestion().compareTo(mail2.getFechainiciogestion())).get();
                        if (mailmasreciente.getTipo().equals("entrada")) {
                            mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                    formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(),
                                            formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion())));
                        } else {
                            mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                    formatodefechas.restaDeFechasEnSegundos(mailmasreciente.getFechainiciogestion(),
                                            mail.getFechainiciogestion())));
                        }
                    } else {
                        mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(),
                                        formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion())));
                    }
                }
            }

        } catch (ParseException e) {
            log.error("error en el metodo listarCorreosPendientes", e);
        }
        return lista;
    }

    @Override
    public List<MailSalida> listarCorreoInvalidos(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<MailSalida> lista = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            lista
                    = mongoops.find(
                            new Query(new Criteria().andOperator(
                                    Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                                    Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin())).
                                    and("estado").is(1).and("usuario").in(filtro.getListadeagentes())
                                    .and("id_cola").in(filtro.getListadecolas()).and("tipificacion").is(1)),//1 es spam
                            MailSalida.class
                    );
            for (MailSalida mail : lista) {
                mail.setHora(
                        formatodefechas.cambiarFormatoFechas(mail.getFechainiciogestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_HORA));
                mail.setEstadoatencion(mail.getEstado() == 1 ? "ATENDIENDO" : "FINALIZADO");
                mail.setValidacion(mail.getTipificacion() == 1 ? "INVALIDO" : "VALIDO");
                mail.setTiempo_cola(mail.getTiempoencola() == 0 ? "00:00:00" : formatodefechas.convertirSegundosAFecha(mail.getTiempoencola()));
                if (mail.getTipo().equals("salida")) {
                    mail.setTiempoatencion("00:00:00");
                } else {
                    List<MailSalida> sublista = lista.stream().filter((correo) -> correo.getIdhilo() == mail.getIdhilo()).collect(Collectors.toList());
                    if (sublista.size() > 0) {
                        MailSalida mailmasreciente = sublista.stream().max((mail1, mail2) -> mail1.getFechainiciogestion().compareTo(mail2.getFechainiciogestion())).get();
                        if (mailmasreciente.getTipo().equals("entrada")) {
                            mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                    formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(),
                                            formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion())));
                        } else {
                            mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                    formatodefechas.restaDeFechasEnSegundos(mailmasreciente.getFechainiciogestion(),
                                            mail.getFechainiciogestion())));
                        }
                    } else {
                        mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(),
                                        formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion())));
                    }
                }
            }
        } catch (ParseException e) {
            log.error("error en el metodo listarCorreoInvalidos", e);
        }
        return lista;
    }

    @Override
    public List<MailSalida> listarCorreos(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<MailSalida> lista = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            lista
                    = mongoops.find(new Query(new Criteria().andOperator(
                            Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                            Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin())).
                            and("usuario").in(filtro.getListadeagentes())
                            .and("id_cola").in(filtro.getListadecolas()).and("estado").ne(0)),
                            MailSalida.class
                    );

            for (MailSalida mail : lista) {
                mail.setHora(
                        formatodefechas.cambiarFormatoFechas(mail.getFechainiciogestion(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_HORA));
                mail.setEstadoatencion(mail.getEstado() == 1 ? "ATENDIENDO" : "FINALIZADO");
                mail.setValidacion(mail.getTipificacion() == 1 ? "INVALIDO" : "VALIDO");
                mail.setTiempo_cola(mail.getTiempoencola() == 0 ? "00:00:00" : formatodefechas.convertirSegundosAFecha(mail.getTiempoencola()));
                if (mail.getTipo().equals("salida")) {
                    mail.setTiempoatencion("00:00:00");
                } else {
                    List<MailSalida> sublista = lista.stream().filter((correo) -> correo.getIdhilo() == mail.getIdhilo()).collect(Collectors.toList());
                    if (sublista.size() > 0) {
                        MailSalida mailmasreciente = sublista.stream().max((mail1, mail2) -> mail1.getFechainiciogestion().compareTo(mail2.getFechainiciogestion())).get();
                        if (mailmasreciente.getTipo().equals("entrada")) {
                            mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                    formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(),
                                            formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion())));
                        } else {
                            mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                    formatodefechas.restaDeFechasEnSegundos(mailmasreciente.getFechainiciogestion(),
                                            mail.getFechainiciogestion())));
                        }
                    } else {
                        mail.setTiempoatencion(formatodefechas.convertirSegundosAFecha(
                                formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(),
                                        formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion())));
                    }
                }
            }
        } catch (ParseException e) {
            log.error("error en el metodo listarCorreos", e);
        }
        return lista;
    }

    @Override
    public List<Pausa> detalleTiemposeEnPausa(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<Pausa> lista = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            lista
                    = mongoops.find(
                            new Query(new Criteria().andOperator(
                                    Criteria.where("iniciopausa").gte(filtro.getFecha_inicio()),
                                    Criteria.where("iniciopausa").lte(filtro.getFecha_fin())).
                                    and("idagente").in(filtro.getListadeagentes())).with(new Sort(Direction.ASC, "iniciopausa")),
                            Pausa.class
                    );
            for (Pausa pausa : lista) {
                pausa.setFecha(formatodefechas.cambiarFormatoFechas(pausa.getIniciopausa(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA));
                pausa.setHorainiciopausa(formatodefechas.cambiarFormatoFechas(pausa.getIniciopausa(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_HORA));
                pausa.setHorafinpausa(formatodefechas.cambiarFormatoFechas(pausa.getFinpausa(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_HORA));
                pausa.setDuracionpausa(formatodefechas.convertirSegundosAFechaNormal(pausa.getDuracion()));
            }
        } catch (Exception e) {
            log.error("error en el metodo detalleTiemposeEnPausa", e);
        }
        return lista;
    }

    @Override
    public List<ReporteGrupal> detalleGrupalDeCorreosPorDias(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<ReporteGrupal> lista = null;

        Cond condicionrecibidos = ConditionalOperators.when(new Criteria("tipomail").is("entrada")).then(1).otherwise(0);
        Cond condicionencola = ConditionalOperators.when(new Criteria("estado").is(0)).then(1).otherwise(0);
        Cond condicionatendiendo = ConditionalOperators.when(new Criteria("estado").is(1)).then(1).otherwise(0);
        Cond condicionvalidos = ConditionalOperators.when(new Criteria().andOperator(new Criteria("hilocerrado").is(true),
                new Criteria("tipificacion").ne(1))).then(1).otherwise(0);
        Cond condicioninvalidos = ConditionalOperators.when(new Criteria().andOperator(new Criteria("hilocerrado").is(true),
                new Criteria("tipificacion").is(1))).then(1).otherwise(0);
        Cond condicionfinalizados = ConditionalOperators.when(new Criteria("hilocerrado").is(true)).then(1).otherwise(0);
        try {
            mongoops = clientemongoservicio.clienteMongo();
            Aggregation agregacion = Aggregation.newAggregation(
                    Aggregation.match(
                            new Criteria().andOperator(
                                    Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                                    Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin())).
                                    and("id_cola").in(filtro.getListadecolas()).and("tipomail").is("entrada")),
                    Aggregation.project().andExpression("fecha_ingreso").substring(0, 10).as("fecha_ingreso").
                            andExpression("tipomail").as("tipomail").
                            andExpression("estado").as("estado").
                            andExpression("hilocerrado").as("hilocerrado").
                            andExpression("tipificacion").as("tipificacion"),
                    Aggregation.group("fecha_ingreso").
                            sum(condicionrecibidos).as("recibidos").
                            sum(condicionencola).as("encola").
                            sum(condicionatendiendo).as("atendiendo").
                            sum(condicionvalidos).as("validos").
                            sum(condicioninvalidos).as("invalidos").
                            sum(condicionfinalizados).as("finalizados")
            );
            AggregationResults<ReporteGrupal> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrupal.class
            );
            lista = resultado.getMappedResults();

        } catch (Exception e) {
            log.error("error en el metodo detalleGrupalDeCorreosPorDias", e);
        }
        return lista;
    }

    @Override
    public List<ReporteGrupal> detalleGrupalDeCorreosPorHoras(FiltroIndividual filtro) {

        MongoOperations mongoops;
        List<ReporteGrupal> lista = null;

        Cond condicionrecibidos = ConditionalOperators.when(new Criteria("tipomail").is("entrada")).then(1).otherwise(0);
        Cond condicionencola = ConditionalOperators.when(new Criteria("estado").is(0)).then(1).otherwise(0);
        Cond condicionatendiendo = ConditionalOperators.when(new Criteria("estado").is(1)).then(1).otherwise(0);
        Cond condicionvalidos = ConditionalOperators.when(new Criteria().andOperator(new Criteria("hilocerrado").is(true),
                new Criteria("tipificacion").ne(1))).then(1).otherwise(0);
        Cond condicioninvalidos = ConditionalOperators.when(new Criteria().andOperator(new Criteria("hilocerrado").is(true),
                new Criteria("tipificacion").is(1))).then(1).otherwise(0);
        Cond condicionfinalizados = ConditionalOperators.when(new Criteria("hilocerrado").is(true)).then(1).otherwise(0);
        try {
            mongoops = clientemongoservicio.clienteMongo();
            Aggregation agregacion = Aggregation.newAggregation(
                    Aggregation.match(
                            new Criteria().andOperator(
                                    Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                                    Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin())).
                                    and("id_cola").in(filtro.getListadecolas()).and("tipomail").is("entrada")),
                    Aggregation.project().andExpression("fecha_ingreso").substring(11, 2).as("fecha_ingreso").
                            andExpression("tipomail").as("tipomail").
                            andExpression("estado").as("estado").
                            andExpression("hilocerrado").as("hilocerrado").
                            andExpression("tipificacion").as("tipificacion"),
                    Aggregation.group("fecha_ingreso").
                            sum(condicionrecibidos).as("recibidos").
                            sum(condicionencola).as("encola").
                            sum(condicionatendiendo).as("atendiendo").
                            sum(condicionvalidos).as("validos").
                            sum(condicioninvalidos).as("invalidos").
                            sum(condicionfinalizados).as("finalizados")
            );
            AggregationResults<ReporteGrupal> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrupal.class);
            lista = resultado.getMappedResults();

        } catch (Exception e) {
            log.error("error en el metodo detalleGrupalDeCorreosPorHoras", e);
        }
        return lista;
    }

    @Override
    public List<ReporteGrupal> detalleGrupalDeCorreosPorAgente(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<ReporteGrupal> lista = null;

        Cond condicionvalidos = ConditionalOperators.when(new Criteria().andOperator(new Criteria("hilocerrado").is(true),
                new Criteria("tipificacion").ne(1))).then(1).otherwise(0);
        Cond condicioninvalidos = ConditionalOperators.when(new Criteria().andOperator(new Criteria("hilocerrado").is(true),
                new Criteria("tipificacion").is(1))).then(1).otherwise(0);
        Cond condicionfinalizados = ConditionalOperators.when(new Criteria("hilocerrado").is(true)).then(1).otherwise(0);
        try {
            mongoops = clientemongoservicio.clienteMongo();
            Aggregation agregacion = Aggregation.newAggregation(
                    Aggregation.match(
                            new Criteria().andOperator(
                                    Criteria.where("fechainiciogestion").gte(filtro.getFecha_inicio()),
                                    Criteria.where("fechainiciogestion").lte(filtro.getFecha_fin())).
                                    and("id_cola").in(filtro.getListadecolas()).and("tipomail").is("entrada")),
                    Aggregation.project().andExpression("fechainiciogestion").substring(11, 2).as("fechainiciogestion").
                            andExpression("nombre").as("nombre").
                            andExpression("tipomail").as("tipomail").
                            andExpression("estado").as("estado").
                            andExpression("hilocerrado").as("hilocerrado").
                            andExpression("tipificacion").as("tipificacion").
                            andExpression("tiempo_atencion").as("tiempo_atencion"),
                    Aggregation.match(
                            new Criteria().andOperator(
                                    Criteria.where("fechainiciogestion").gte(filtro.getFecha_inicio().substring(11, 13)),
                                    Criteria.where("fechainiciogestion").lte(filtro.getFecha_fin().substring(11, 13)))),
                    Aggregation.group("fechainiciogestion", "nombre").
                            sum(condicionvalidos).as("validos").
                            sum(condicioninvalidos).as("invalidos").
                            sum(condicionfinalizados).as("finalizados").
                            sum("tiempo_atencion").as("tiempo_atencion").
                            avg("tiempo_atencion").as("tiempo_promedio_atencion"),
                    Aggregation.project().andExpression("_id.fechainiciogestion").as("_id").
                            andExpression("_id.nombre").as("nombre").
                            andExpression("nombre").as("nombre").
                            andExpression("validos").as("validos").
                            andExpression("invalidos").as("invalidos").
                            andExpression("finalizados").as("finalizados").
                            andExpression("tiempo_atencion").as("tiempo_atencion_int").
                            andExpression("tiempo_promedio_atencion").trunc().as("tiempo_promedio_atencion_double")
            );
            AggregationResults<ReporteGrupal> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrupal.class
            );
            lista = resultado.getMappedResults();
            lista.stream().forEach((resultados) -> {
                try {
                    resultados.setTiempo_atencion(formatodefechas.convertirSegundosAFechaNormal(resultados.getTiempo_atencion_int()));
                    resultados.setTiempo_promedio_atencion(formatodefechas.convertirSegundosAFechaNormal((int) resultados.getTiempo_promedio_atencion_double()));
                } catch (ParseException ex) {
                    log.error("error en el metodo detalleGrupalDeCorreosPorAgente", ex);
                }
            });
        } catch (Exception e) {
            log.error("error en el metodo listarMailsEnCola", e);
        }
        return lista;
    }

    @Override
    public List<MailSalida> detalleGrupalDeCorreosPorCola(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<MailSalida> lista = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            Aggregation agregacion = Aggregation.newAggregation(
                    Aggregation.match(
                            new Criteria().andOperator(
                                    Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                                    Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin())).
                                    and("id_cola").in(filtro.getListadecolas()).and("tipomail").is("entrada").and("estado").is(0)),
                    Aggregation.project().andExpression("fecha_ingreso").substring(11, 2).as("fecha"). //aqui lo hago solo para poder luego mostrar la fecha original                       
                            andExpression("idcorreo").as("idcorreo").
                            andExpression("fecha_ingreso").as("fecha_ingreso").
                            andExpression("remitente").as("remitente").
                            andExpression("asunto").as("asunto"),
                    Aggregation.match(
                            new Criteria().andOperator(
                                    Criteria.where("fecha").gte(filtro.getFecha_inicio().substring(11, 13)),
                                    Criteria.where("fecha").lte(filtro.getFecha_fin().substring(11, 13))))
            );
            AggregationResults<MailSalida> resultado = mongoops.aggregate(agregacion, "mail", MailSalida.class
            );
            lista = resultado.getMappedResults();

        } catch (Exception e) {
            log.error("error en el metodo detalleGrupalDeCorreosPorCola", e);
        }
        return lista;
    }

    @Override
    public Grafico graficoMultiCanal(Contenido contenido) {
        MongoOperations mongoops;
        List<ReporteGrafico> lista = null;
        Grafico grafico = new Grafico();
        Aggregation agregacion = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            int hora_actual = formatodefechas.obtenerHora(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            if (contenido.getOnlinehistorico().equals("online")) {
                if (contenido.getVista() == 1) {
                    //hroas
                    for (int i = 0; i <= hora_actual; i++) {
                        grafico.getValores_entrantes().add(0);
                    }
                    agregacion = Aggregation.newAggregation(
                            Aggregation.match(Criteria.where("id_cola").in(contenido.getColas()).and("fecha_ingreso").regex(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA)).and("tipomail").is("entrada")),
                            Aggregation.project().andExpression("fecha_ingreso").substring(0, 10).as("fecha").andExpression("fecha_ingreso").substring(11, 2).as("hora"),
                            Aggregation.group("fecha", "hora").count().as("cantidad"),
                            Aggregation.project().andExpression("_id.fecha").as("fecha").and(ConvertOperators.valueOf("_id.hora").convertToInt()).as("hora").andExpression("cantidad").as("cantidad")
                    );
                    AggregationResults<ReporteGrafico> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrafico.class);
                    lista = resultado.getMappedResults();
                    lista.stream().forEach((mail) -> {
                        grafico.getValores_entrantes().set(mail.getHora(), mail.getCantidad());
                    });
                } else {
                    //minutos
                    for (int hora = 0; hora <= hora_actual; hora++) {
                        for (int minuto = 1; minuto <= 4; minuto++) {
                            grafico.getValores_entrantes().add(0);
                        }
                    }
                    agregacion = Aggregation.newAggregation(
                            Aggregation.match(Criteria.where("id_cola").in(contenido.getColas()).and("fecha_ingreso").regex(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA)).and("tipomail").is("entrada")),
                            Aggregation.project().andExpression("fecha_ingreso").substring(0, 10).as("fecha").
                                    andExpression("fecha_ingreso").substring(11, 2).as("hora").
                                    andExpression("fecha_ingreso").substring(14, 2).as("minuto"),
                            Aggregation.group("fecha", "hora", "minuto").
                                    count().as("cantidad"),
                            Aggregation.project().andExpression("_id.fecha").as("fecha").
                                    and(ConvertOperators.valueOf("_id.hora").convertToInt()).as("hora").
                                    and(ConvertOperators.valueOf("_id.minuto").convertToInt()).as("minuto").
                                    andExpression("cantidad").as("cantidad")
                    );
                    AggregationResults<ReporteGrafico> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrafico.class);
                    lista = resultado.getMappedResults();
                    lista.stream().forEach(mail -> {
                        if (45 < mail.getMinuto()) {
                            mail.setRango_minuto(4);
                        } else if (30 < mail.getMinuto() && mail.getMinuto() < 46) {
                            mail.setRango_minuto(3);
                        } else if (15 < mail.getMinuto() && mail.getMinuto() < 31) {
                            mail.setRango_minuto(2);
                        } else {
                            mail.setRango_minuto(1);
                        }
                        grafico.getValores_entrantes().set(mail.getHora() * 4 + mail.getRango_minuto() - 1, mail.getCantidad());

                    });
                    grafico.setValores_entrantes(grafico.getValores_entrantes().stream().skip(28).collect(Collectors.toList()));
                }
            } else {
                for (int i = 0; i < 24; i++) {
                    grafico.getValores_entrantes().add(0);
                }
                agregacion = Aggregation.newAggregation(
                        Aggregation.match(Criteria.where("id_cola").in(contenido.getColas()).and("fecha_ingreso").regex(contenido.getFecha()).and("tipomail").is("entrada")),
                        Aggregation.project().andExpression("fecha_ingreso").substring(0, 10).as("fecha").andExpression("fecha_ingreso").substring(11, 2).as("hora"),
                        Aggregation.project().andExpression("fecha").as("fecha").and(ConvertOperators.valueOf("hora").convertToInt()).as("hora"),
                        Aggregation.match(new Criteria().andOperator(
                                Criteria.where("hora").gte(contenido.getHorainicio()),
                                Criteria.where("hora").lte(contenido.getHorafin()))),
                        Aggregation.group("fecha", "hora").count().as("cantidad"),
                        Aggregation.project().andExpression("_id.fecha").as("fecha").andExpression("_id.hora").as("hora").andExpression("cantidad").as("cantidad")
                );

                AggregationResults<ReporteGrafico> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrafico.class);
                lista = resultado.getMappedResults();
                lista.stream().forEach((mail) -> {
                    grafico.getValores_entrantes().set(mail.getHora(), mail.getCantidad());
                });
                grafico.setValores_entrantes(grafico.getValores_entrantes().stream().limit(contenido.getHorafin()).skip(contenido.getHorainicio()).collect(Collectors.toList()));
            }

        } catch (Exception e) {
            log.error("error en el metodo graficoMultiCanal", e);
        }

        return grafico;
    }

    @Override
    public Grafico reporteMensual(Contenido contenido) {
        MongoOperations mongoops;
        List<ReporteGrafico> lista = null;
        Grafico grafico = new Grafico();
        Aggregation agregacion = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            List<Mes> listameses = obtenerListaMeses();
            int totaldias = listameses.get(contenido.getMes()).getDiascantidad();
            if ((contenido.getMes() + 1) == formatodefechas.obtenerMes(new Date())) {
                totaldias = formatodefechas.obtenerDia(new Date());
            }
            for (int i = 0; i < totaldias; i++) {
                grafico.getValores_entrantes().add(0);
                grafico.getValores_contestadas().add(0);
            }
            agregacion = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("id_cola").in(contenido.getColas()).and("fecha_ingreso").regex(contenido.getAno() + "-" + ((contenido.getMes() + 1) < 10 ? "0" + (contenido.getMes() + 1) : (contenido.getMes() + 1))).and("tipomail").is("entrada")
                            .and("estado").in(Arrays.asList(0, 1))),
                    Aggregation.project().andExpression("fecha_ingreso").substring(0, 10).as("fecha").andExpression("fecha_ingreso").substring(8, 2).as("dia"),
                    Aggregation.project().andExpression("fecha").as("fecha").and(ConvertOperators.valueOf("dia").convertToInt()).as("dia"),
                    Aggregation.group("fecha", "dia").count().as("cantidad"),
                    Aggregation.project().andExpression("_id.fecha").as("fecha").andExpression("_id.dia").as("dia").andExpression("cantidad").as("cantidad")
            );
            AggregationResults<ReporteGrafico> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrafico.class);
            System.out.println(agregacion.toString());
            lista = resultado.getMappedResults();
            lista.stream().forEach((mail) -> {
                grafico.getValores_entrantes().set(mail.getDia() - 1, mail.getCantidad());
            });

            agregacion = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("id_cola").in(contenido.getColas()).and("fecha_ingreso").regex(contenido.getAno() + "-" + ((contenido.getMes() + 1) < 10 ? "0" + (contenido.getMes() + 1) : (contenido.getMes() + 1))).and("tipomail").is("entrada")
                            .and("estado").is(1).and("hilocerrado").is(true)),
                    Aggregation.project().andExpression("fecha_ingreso").substring(0, 10).as("fecha").andExpression("fecha_ingreso").substring(8, 2).as("dia"),
                    Aggregation.project().andExpression("fecha").as("fecha").and(ConvertOperators.valueOf("dia").convertToInt()).as("dia"),
                    Aggregation.group("fecha", "dia").count().as("cantidad"),
                    Aggregation.project().andExpression("_id.fecha").as("fecha").andExpression("_id.dia").as("dia").andExpression("cantidad").as("cantidad")
            );
            AggregationResults<ReporteGrafico> resultadoatendidos = mongoops.aggregate(agregacion, "mail", ReporteGrafico.class);
            System.out.println(agregacion.toString());
            lista = resultadoatendidos.getMappedResults();
            lista.stream().forEach((mail) -> {
                grafico.getValores_contestadas().set(mail.getDia() - 1, mail.getCantidad());
            });

        } catch (Exception e) {
            log.error("error en el metodo reporteMensual", e);
        }

        return grafico;
    }

    public List<Mes> obtenerListaMeses() {
        List<Mes> listameses = new ArrayList<>();
        listameses.add(new Mes(31, "enero"));
        listameses.add(new Mes(28, "febrero"));
        listameses.add(new Mes(31, "marzo"));
        listameses.add(new Mes(30, "abril"));
        listameses.add(new Mes(31, "mayo"));
        listameses.add(new Mes(30, "junio"));
        listameses.add(new Mes(31, "julio"));
        listameses.add(new Mes(31, "agosto"));
        listameses.add(new Mes(30, "setiembre"));
        listameses.add(new Mes(31, "octubre"));
        listameses.add(new Mes(30, "nomviembre"));
        listameses.add(new Mes(31, "diciemrbe"));
        return listameses;
    }

    @Override
    public List<AtendidosPorCola> atendidosPorCola(Contenido contenido) {
        MongoOperations mongoops;
        List<AtendidosPorCola> lista = null;
        Aggregation agregacion = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            agregacion = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("campana").is(contenido.getCampana()).and("fechafingestion").regex(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA)).and("tipomail").is("entrada")
                            .and("estado").is(1)),
                    Aggregation.project().andExpression("id_cola").as("id_cola"),
                    Aggregation.group("id_cola").count().as("cantidad"),
                    Aggregation.project().andExpression("_id").as("id_cola").andExpression("cantidad").as("cantidad")
            );
            AggregationResults<AtendidosPorCola> resultado = mongoops.aggregate(agregacion, "mail", AtendidosPorCola.class);
            System.out.println(agregacion.toString());
            lista = resultado.getMappedResults();

        } catch (Exception e) {
            log.error("error en el metodo atendidosPorCola", e);
        }

        return lista;
    }

}
