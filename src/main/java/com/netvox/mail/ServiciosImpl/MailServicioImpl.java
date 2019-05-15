/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.Api.entidadessupervisor.FiltroIndividual;
import com.netvox.mail.Api.entidadessupervisor.Pausa;
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
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
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
            mailinbox.setTipificacion(t.getTipificacion());
            mailinbox.setDescripcion_tipificacion(t.getDescripcion_tipificacion());
            mailinbox.setAdjuntos(t.getListadeadjuntos());
            mailinbox.setIdhilo(t.getIdhilo());
            mailinbox.setDestino(t.getDestino());
            mailinbox.setId_cola(t.getCola());
            mailinbox.setCc(t.getCopia());
            listamailinbox.add(mailinbox);
        });
        return listamailinbox;
    }

    @Override
    public String abrirCorreo(MailInbox mailconsultainbox) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();

        System.out.println("Entre al metodo abrirr correo y el tipomail es : " + mailconsultainbox.getTipo());
        Query query = new Query(Criteria.where("idcorreo").is(mailconsultainbox.getId()));
        query.fields().include("idcorreo").include("mensaje").include("listadeembebidos");
        MailSalida mail = mongoops.findOne(query, MailSalida.class);
        String mensajefinal = mail.getMensaje();
        if (mailconsultainbox.getTipo().equals("salida")) {//con esto transformo los base64 de salida
            if (!mail.getListadeembebidos().isEmpty()) {
                for (String embebido : mail.getListadeembebidos()) {
                    mensajefinal = mail.getMensaje().replace("cid:" + embebido, coremailservicio.getPath_salida() + "/" + mailconsultainbox.getId() + "/" + embebido);
                }
            }
        }
        System.out.println("el mensaje es " + mail.getMensaje());
        return mensajefinal;
    }

    @Override
    public List<MailInbox> listarCorreosEnCola(Mensaje mensaje
    ) {
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
            mailinbox.setFecha_ingreso(t.getFecha_ingreso());
            mailinbox.setTipificacion(t.getTipificacion());
            mailinbox.setDescripcion_tipificacion(t.getDescripcion_tipificacion());
            mailinbox.setAdjuntos(t.getListadeadjuntos());
            mailinbox.setIdhilo(t.getIdhilo());
            listamailinbox.add(mailinbox);
        });

        return listamailinbox;
    }

    @Override
    public void autoAsignarse(Mensaje mensaje) {//deberia enviar un 500 si es que no se puede, y garantizar el query con estado 1
        Connection conexion;
        CallableStatement procedimientoalmacenado = null;
        MongoOperations mongoops;
        Resumen usuarioresumen = getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get();
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query(Criteria.where("agente").is(usuarioresumen.getAgente())), new Update().//aumento los pendientes en la coleccion resumen
                    set("estadoagente", 2).set("pendiente", usuarioresumen.getPendiente() + 1), Resumen.class);
            getListaresumen().stream().filter((agente) -> agente.getAgente() == usuarioresumen.getAgente()).//aqui hago lo mismo pero en memoria
                    forEach((agente) -> {
                        agente.setEstadoagente(2);
                        agente.setPendiente(usuarioresumen.getPendiente() + 1);
                    });
            resumendiarioservicio.actualizarPendientes(1, usuarioresumen.getAgente());
            resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), 2, 2);
            mongoops.updateFirst(new Query(Criteria.where("agente").is(usuarioresumen.getAgente())), new Update().set("estado", 2), Resumen.class);

            Mail mail = mongoops.findOne(new Query(Criteria.where("idcorreo").is(mensaje.getIdcorreoasignado())), Mail.class);  //obtengo esto para tener la fecha de ingreso
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mail.getIdcorreo())), new Update()
                    .set("estado", 1)
                    .set("tiempoencola", formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), mail.getFecha_ingreso()))
                    .set("usuario", usuarioresumen.getAgente())
                    .set("nombre", usuarioresumen.getNombre())
                    .set("fechainiciogestion", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)), Mail.class);
            getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola())) // aqui le envio al resto
                    .forEach((resumen) -> {
                        if (usuarioresumen.getAgente() != resumen.getAgente()) {
                            mensaje.setEvento("CORREOASIGNADO");
                            websocket.enviarMensajeParaUnUsuario(mensaje, resumen.getAgente());
                        }
                    });
            procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
            procedimientoalmacenado.setInt(1, mail.getCampana());
            procedimientoalmacenado.setInt(2, 2);
            procedimientoalmacenado.setBoolean(3, false);
            procedimientoalmacenado.execute();
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException | ParseException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public MailInbox crearCorreo(MailSalida mailsalida
    ) {
        System.out.println("ESTOY ENTRANDO A CREARCORREO");
        MailSalida nuevomail = null;
        MailInbox respuestamail;
        File directorio;
        switch (mailsalida.getTipificacion()) {
            case -1://correo nuevo
                nuevomail = generarNuevoCorreo(mailsalida);
                break;
            case 2: //standby
                nuevomail = generarCorreo(mailsalida);
                break;
            default:
                nuevomail = generarCorreo(mailsalida);
                break;
        }

        System.out.println("este esel directorio en el crearcorreo " + coremailservicio.getRUTA_OUT() + "/" + nuevomail.getId());
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
            Query query = new Query().with(new Sort(Direction.DESC, "$natural"));
            query.fields().include("idcorreo");
            mailsalida.setFecha_ingreso(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setFechainiciogestion(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            mailsalida.setEstado(2);//correo enviado
            mailsalida.setTipo("salida");
            Mail ultimomail = mongoops.findOne(query, Mail.class);

            if (ultimomail == null) {
                mailsalida.setId(1);
            } else {
                mailsalida.setId(ultimomail.getIdcorreo() + 1);
            }

            ///este pedazo de codigo era para cuando alguien manda un correo , no puedo atarlos a un hilo
//             me quedaria pensar que puedo unirlo siempre que la tipificacion sea -1 
//            List<Mail> hilomail = mongoops.find(
//                    new Query(Criteria.where("remitente").is(mailsalida.getRemitente()).
//                            and("destino").is(mailsalida.getDestino()).and("tipificacion").is(1). 
//                            and("hilocerrado").is(false)), Mail.class);
//
//            if (hilomail.size() > 0) {
//                update.set("idhilo", hilomail.get(0).getIdhilo());
//            } else {
//                update.set("idhilo", mail.getIdcorreo());
//            }
            mailsalida.setIdhilo(mailsalida.getId());
            mongoops.insert(mailsalida);

        } catch (Exception e) {
            e.printStackTrace();
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
        System.out.println("estoy dentro de adjuntar correo , el idcorreo es " + idcorreo);
        try {
            mongoops = clientemongoservicio.clienteMongo();
            directorio = new File(coremailservicio.getRUTA_OUT() + "/" + idcorreo + "/adjuntos");
            directorio.mkdir();
            System.out.println("este es el directorio " + directorio.getAbsolutePath());
            nuevoarchivo = new File(directorio.getAbsolutePath(), archivo.getOriginalFilename());
            archivosalidastream = new FileOutputStream(nuevoarchivo);
            buferdesalida = new BufferedOutputStream(archivosalidastream);
            buferdesalida.write(archivo.getBytes());
            buferdesalida.close();
            archivosalidastream.close();
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(idcorreo)),
                    new Update().push("listadeadjuntos", new Adjunto(archivo.getOriginalFilename(), (int) archivo.getSize(), nuevoarchivo.getAbsolutePath())), Mail.class);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void enviarcorreo(MailSalida mailsalida) {
        System.out.println("ESTOY ENTRANDO A ENVIARCORREO");
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

            archivosalidastream = new FileOutputStream(archivo);
            buferdesalida = new BufferedOutputStream(archivosalidastream);
            buferdesalida.write(nuevomail.getMensaje().getBytes());
            buferdesalida.close();
            archivosalidastream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        prepararMensaje(nuevomail);
    }

    public MimeMultipart agregarCuerpoDelMensaje(Object mensaje) {
        MimeMultipart multipart = new MimeMultipart("related");// mensaje en general.          
        BodyPart bodypartmensaje = new MimeBodyPart();
        try {
            bodypartmensaje.setContent(mensaje, "text/html; charset=\"UTF-8\"");
            multipart.addBodyPart(bodypartmensaje);
        } catch (MessagingException e) {
            e.printStackTrace();
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
                    e.printStackTrace();
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
                    e.printStackTrace();
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
            e.printStackTrace();
        }
        return mailconfiguracion;
    }

    public MimeMessage agregarDestinatariosEnCopia(MimeMessage mimemensaje, String copia) {
        if (!copia.equals("")) {
            String listadedestinosencopia[] = copia.split(";");
            for (String destinoencopia : listadedestinosencopia) {
                try {
                    mimemensaje.addRecipient(Message.RecipientType.CC, new InternetAddress(destinoencopia));
                } catch (Exception ex) {
                    ex.printStackTrace();
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
            System.out.println("Se envio correctamente el mail");
        } catch (MessagingException ex) {
            ex.printStackTrace();
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
            e.printStackTrace();
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
    public void tipificarCorreo(MailSalida mailsalida) {

        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            Query querymailaresponder = new Query(Criteria.where("idcorreo").is(mailsalida.getId()));
            querymailaresponder.fields().include("idcorreo").include("tipificacion").include("usuario").include("campana").include("id_cola");
            MailSalida mailaresponder = mongoops.findOne(querymailaresponder, MailSalida.class);

            if (mailaresponder.getTipificacion() == 0) {//esto es para evitar que reduzca los pendientes de correos ya tipificados
                System.out.println("entre a donde debo reducir los pendientes");
                coremailservicio.getListaresumen().stream().filter(
                        (agente) -> agente.getCampana() == mailaresponder.getId_campana()
                        && agente.getAgente() == mailaresponder.getId_agente()).forEach((agente) -> {
                            agente.setPendiente(agente.getPendiente() - 1);
                        });
                resumendiarioservicio.actualizarAtendidosPorCola(mailaresponder.getId_agente(), mailaresponder.getId_campana(), mailaresponder.getCola());
                Resumen resumen = coremailservicio.getListaresumen().stream().filter(
                        (agente) -> agente.getCampana() == mailaresponder.getId_campana()
                        && agente.getAgente() == mailaresponder.getId_agente()).findFirst().get();

                Update update = new Update().inc("pendiente", -1);
                if (resumen.getPendiente() == 0) {
                    update.set("estado", 1);
                    resumendiarioservicio.actualizarEstado(resumen.getAgente(), 1, 1);
                    coremailservicio.getListaresumen().stream().filter(
                            (agente) -> agente.getCampana() == mailaresponder.getId_campana()
                            && agente.getAgente() == mailaresponder.getId_agente()).forEach((agente) -> {
                                agente.setEstadoagente(1);
                            });
                }
                mongoops.updateFirst(new Query(Criteria.where("agente").is(mailaresponder.getId_agente())), update, Resumen.class);
                resumendiarioservicio.actualizarPendientes(-1, mailaresponder.getId_agente());
            }

            MailSalida mail = mongoops.findOne(new Query(Criteria.where("idcorreo").is(mailsalida.getId())), MailSalida.class);
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mailsalida.getId())),
                    new Update().set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).set("tipificacion", mailsalida.getTipificacion())
                            .set("tiempo_atencion", formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), mail.getFechainiciogestion()))
                            .set("fechafingestion", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA))
                            .set("hilocerrado", true),
                    MailSalida.class);//estoy usando esta clase solo por lo conveniente que es ya que tiene id y tipificacion
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listatipificaciones;
    }

    private MailSalida generarCorreo(MailSalida mailsalida) {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            Query querymailaresponder = new Query(Criteria.where("idcorreo").is(mailsalida.getId()));
            querymailaresponder.fields().include("idcorreo").include("tipificacion").include("usuario").include("campana").include("id_cola");
            MailSalida mailaresponder = mongoops.findOne(querymailaresponder, MailSalida.class);

            if (mailaresponder.getTipificacion() == 0) {//esto es para evitar que reduzca los pendientes de correos ya tipificados
                resumendiarioservicio.actualizarAtendidosPorCola(mailaresponder.getId_agente(), mailaresponder.getId_campana(), mailaresponder.getCola());
                System.out.println("entre a donde debo reducir los pendientes");
                coremailservicio.getListaresumen().stream().filter(
                        (agente) -> agente.getCampana() == mailaresponder.getId_campana()
                        && agente.getAgente() == mailaresponder.getId_agente()).forEach((agente) -> {
                            agente.setPendiente(agente.getPendiente() - 1);
                        });

                Resumen resumen = coremailservicio.getListaresumen().stream().filter(
                        (agente) -> agente.getCampana() == mailaresponder.getId_campana()
                        && agente.getAgente() == mailaresponder.getId_agente()).findFirst().get();

                Update update = new Update().inc("pendiente", -1);
                if (resumen.getPendiente() == 0) {
                    update.set("estado", 1);
                    resumendiarioservicio.actualizarEstado(resumen.getAgente(), 1, 1);
                    coremailservicio.getListaresumen().stream().filter(
                            (agente) -> agente.getCampana() == mailaresponder.getId_campana()
                            && agente.getAgente() == mailaresponder.getId_agente()).forEach((agente) -> {
                                agente.setEstadoagente(1);
                            });
                }
                mongoops.updateFirst(new Query(Criteria.where("agente").is(mailaresponder.getId_agente())), new Update().inc("pendiente", -1), Resumen.class);
                resumendiarioservicio.actualizarPendientes(-1, mailaresponder.getId_agente());
            }

            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mailsalida.getId())), new Update().set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).set("tipificacion", mailsalida.getTipificacion()), MailSalida.class);

            Query query = new Query().with(new Sort(Direction.DESC, "$natural"));
            query.fields().include("idcorreo").include("usuario");
            MailSalida ultimomail = mongoops.findOne(query, MailSalida.class);

            mailsalida.setId(ultimomail.getId() + 1);
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
                query.fields().include("idcorreo").include("fechainiciogestion");
                List<MailSalida> listahilo = mongoops.find(queryhilo, MailSalida.class);
                MailSalida minimo = listahilo.stream().min((mail_1, mail_2) -> Integer.compare(mail_1.getId(), mail_2.getId())).get();
                MailSalida maximo = listahilo.stream().max((mail_1, mail_2) -> Integer.compare(mail_1.getId(), mail_2.getId())).get();
                mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(minimo.getId())),
                        new Update().set("tiempo_atencion", formatodefechas.restaDeFechasEnSegundos(maximo.getFechainiciogestion(), minimo.getFechainiciogestion()))
                                .set("fechafingestion", maximo.getFechainiciogestion()),
                        MailSalida.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("entre al error de generarcorreo");
        }
        return mailsalida;
    }

    @Override
    public List<MailSalida> listarCorreosPendientes(FiltroIndividual filtro) {
        MongoOperations mongoops;
        List<MailSalida> lista = null;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            lista
                    = mongoops.find(
                            new Query(new Criteria().andOperator(
                                    Criteria.where("fecha_ingreso").gte(filtro.getFecha_inicio()),
                                    Criteria.where("fecha_ingreso").lte(filtro.getFecha_fin()))
                                    .and("estado").is(1).and("usuario").in(filtro.getListadeagentes())
                                    .and("id_cola").in(filtro.getListadecolas())),
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

        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Pausa> detalleTiemposeEnPausa(FiltroIndividual filtro) {
        System.out.println(filtro.toString());
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
            e.printStackTrace();
        }
        System.out.println("la lista tiene " + lista.size());
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
            e.printStackTrace();
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
            AggregationResults<ReporteGrupal> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrupal.class
            );
            lista = resultado.getMappedResults();

        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println(agregacion.toString());
            AggregationResults<ReporteGrupal> resultado = mongoops.aggregate(agregacion, "mail", ReporteGrupal.class
            );
            lista = resultado.getMappedResults();
            lista.stream().forEach((resultados) -> {
                try {
                    resultados.setTiempo_atencion(formatodefechas.convertirSegundosAFechaNormal(resultados.getTiempo_atencion_int()));
                    resultados.setTiempo_promedio_atencion(formatodefechas.convertirSegundosAFechaNormal((int) resultados.getTiempo_promedio_atencion_double()));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
            System.out.println(agregacion.toString());
            AggregationResults<MailSalida> resultado = mongoops.aggregate(agregacion, "mail", MailSalida.class
            );
            lista = resultado.getMappedResults();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
