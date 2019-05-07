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

    private FormatoDeFechas formato = new FormatoDeFechas();
    private BodyPart bodypartmensaje;
    private MimeBodyPart mimebodypart;
    private DataSource filedatasource;

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
    public void autoAsignarse(Mensaje mensaje) {
        Connection conexion;
        CallableStatement procedimientoalmacenado = null;
        MongoOperations mongoops;
        Resumen usuarioresumen = getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get();
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query( //aumento los pendientes en la coleccion resumen
                    Criteria.where("agente").is(usuarioresumen.getAgente())),
                    new Update().set("estadoagente", 2).set("pendiente", usuarioresumen.getPendiente() + 1),
                    Resumen.class);

            getListaresumen().stream().filter( //aqui hago lo mismo pero en memoria
                    (agente) -> agente.getAgente() == usuarioresumen.getAgente()).forEach((agente) -> {
                        agente.setEstadoagente(2);
                        agente.setPendiente(usuarioresumen.getPendiente() + 1);
                    });
            //obtengo esto para tener la fecha de ingreso
            Mail mail = mongoops.findOne(new Query(Criteria.where("idcorreo").is(/*mailinbox.getId()*/mensaje.getIdcorreoasignado())), Mail.class);
            mongoops.updateFirst(new Query(
                    Criteria.where("idcorreo").is(mail.getIdcorreo())),
                    new Update()
                            .set("estado", 1)
                            .set("tiempoencola", formato.restaDeFechasEnSegundos(
                                    formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA), mail.getFecha_ingreso()))
                            .set("usuario", usuarioresumen.getAgente())
                            .set("nombre", usuarioresumen.getNombre())
                            .set("fechainiciogestion", formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA)),
                    Mail.class);
            getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola())) // aqui le envio al resto
                    .forEach((resumen) -> {
                        System.out.println("estoy en el autoasignarse , el agente es :" + resumen.getAgente());
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
    public MailInbox crearCorreo(MailSalida mailsalida) {
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
                if (mailsalida.getReenvio() != 1) { //esto es para que no cierre el hilo en el caso de un reenvio
                    mailsalida.setHilocerrado(true);
                }
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
            mailsalida.setFecha_ingreso(formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA));
            mailsalida.setFechainiciogestion(formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA));
            mailsalida.setEstado(2);//correo enviado
            mailsalida.setTipo("salida");
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
        try {
            mongoops = clientemongoservicio.clienteMongo();
            nuevomail = mongoops.findAndModify(new Query(Criteria.where("idcorreo").is(mailsalida.getId())),
                    new Update().set("mensaje", reemplazarImagenesEnBase64(mailsalida)), new FindAndModifyOptions().returnNew(true), MailSalida.class);
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

    public void agregarCuerpoDelMensaje(){
        
    }
    
    
    public void prepararMensaje(MailSalida mailsalida) {
        try {
            //cuerpo mensaje
            MimeMultipart multipart = new MimeMultipart("related");// mensaje en general.
            bodypartmensaje = new MimeBodyPart();
            bodypartmensaje.setContent(mailsalida.getMensaje(), "text/html; charset=\"UTF-8\"");
            multipart.addBodyPart(bodypartmensaje);

            //embebidos
            String rutaembebidos = coremailservicio.getRUTA_OUT() + "/" + mailsalida.getId();
            File directorioembebidos = new File(rutaembebidos);
            System.out.println("ruta embebidos " + directorioembebidos.getAbsolutePath());

            for (File archivo : directorioembebidos.listFiles()) {
                if (!archivo.isDirectory() && !archivo.getName().equalsIgnoreCase(mailsalida.getId() + ".txt")) {
                    try {
                        MimeBodyPart mimebodypartembebido = new MimeBodyPart();
                        mimebodypartembebido.setHeader("Content-ID", "<" + archivo.getName() + ">");
                        mimebodypartembebido.setDisposition(MimeBodyPart.INLINE);
                        mimebodypartembebido.attachFile(archivo);
                        multipart.addBodyPart(mimebodypartembebido);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //archivos adjuntos     
            System.out.println("Estoy antes de la ruta y el idcorreo es " + mailsalida.getId());
            String ruta = coremailservicio.getRUTA_OUT() + "/" + mailsalida.getId() + "/adjuntos";
            File adjuntos = new File(ruta);
            System.out.println(adjuntos.getAbsolutePath());
            System.out.println(adjuntos.isDirectory());
            if (adjuntos.listFiles() != null) {
                for (File archivo : adjuntos.listFiles()) {
                    try {
                        mimebodypart = new MimeBodyPart();
                        filedatasource = new FileDataSource(archivo);
                        mimebodypart.setDataHandler(new DataHandler(filedatasource));
                        mimebodypart.setFileName(archivo.getName());
                        multipart.addBodyPart(mimebodypart);
                        System.out.println("archivo adjunto : " + archivo.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("adjuntoslistfiles es nulo");
            }
            MailConfiguracion mailconfiguracion = null;
            try {
                Connection conexion = clientemysqlservicio.obtenerConexion();
                ResultSet resultset = conexion.createStatement().executeQuery("select * from email_configuracion where usuario='" + mailsalida.getRemitente() + "'");
                while (resultset.next()) {
                    mailconfiguracion = new MailConfiguracion();
                    mailconfiguracion.setSmtp(resultset.getString("smtp"));
                    mailconfiguracion.setUsuario(resultset.getString("usuario"));
                    mailconfiguracion.setPass(resultset.getString("pass"));
                    mailconfiguracion.setPuertosalida(resultset.getInt("puerto_salida"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.host", mailconfiguracion.getSmtp());
            props.setProperty("mail.user", mailconfiguracion.getUsuario());
            props.setProperty("mail.smtp.port", mailconfiguracion.getPuertosalida() + "");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.password", mailconfiguracion.getPass());
            props.setProperty("mail.smtp.auth", "true");

            try {
                Session sesion = obtenerSesion(props, mailconfiguracion.getUsuario(), mailconfiguracion.getPass());
                MimeMessage mimemensaje = new MimeMessage(sesion);
                mimemensaje.setSubject(mailsalida.getTitulo());
                mimemensaje.setContent(multipart);
                mimemensaje.setFrom(new InternetAddress(mailsalida.getRemitente()));
                mimemensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(mailsalida.getDestino()));
                if (!mailsalida.getCopia().equals("")) {
                    System.out.println("entre a la copia");
                    String listadedestinosencopia[] = mailsalida.getCopia().split(";");
                    System.out.println("hay " + listadedestinosencopia.length + " en copia");
                    for (String destinoencopia : listadedestinosencopia) {
                        try {
                            mimemensaje.addRecipient(Message.RecipientType.CC, new InternetAddress(destinoencopia));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                Transport.send(mimemensaje);
                System.out.println("******* SE ENVIO EMAIL ***********");
                //editar el mensaje de salida (hay quever primero que se hace 

            } catch (MessagingException ex) {
                ex.printStackTrace();
            }
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    public String reemplazarImagenesEnBase64(MailSalida mailsalida) {
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

    public Session obtenerSesion(Properties props, String usuario, String clave) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });
    }

    @Override
    public void tipificarCorreo(MailSalida mailsalida) {
        System.out.println("entre a tipificarcorreo con " + mailsalida.getDescripcion_tipificacion());
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mailsalida.getId())),
                    new Update().set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).set("tipificacion", mailsalida.getTipificacion()), MailSalida.class);//estoy usando esta clase solo por lo conveniente que es ya que tiene id y tipificacion
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
            resultset = conexion.createStatement().executeQuery("select * from tipificacion_mail where activo=1");
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
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            System.out.println("estoy en el generarcorreo y la tipificacion es " + mailsalida.getTipificacion());
            mongoops.updateFirst(new Query(
                    Criteria.where("idcorreo").is(mailsalida.getId())),
                    new Update().set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).set("tipificacion", mailsalida.getTipificacion()), MailSalida.class);
            Query query = new Query().with(new Sort(Direction.DESC, "$natural"));
            query.fields().include("idcorreo");
            Mail ultimomail = mongoops.findOne(query, Mail.class);
            mailsalida.setId(ultimomail.getIdcorreo() + 1);
            mailsalida.setFecha_ingreso(formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA));
            mailsalida.setFechainiciogestion(formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA));
            mailsalida.setTipo("salida");
            mailsalida.setEstado(2);//correo enviado           
            mongoops.insert(mailsalida);

            if (mailsalida.isHilocerrado()) {
                System.out.println("entre a hilocerrado y es " + mailsalida.getIdhilo());
                mongoops.updateMulti(new Query(Criteria.where("idhilo").is(mailsalida.getIdhilo())),
                        new Update().set("hilocerrado", true).
                                set("descripcion_tipificacion", mailsalida.getDescripcion_tipificacion()).
                                set("tipificacion", mailsalida.getTipificacion()),
                        MailSalida.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("entre al error de generarcorreo");
        }
        return mailsalida;
    }
}
