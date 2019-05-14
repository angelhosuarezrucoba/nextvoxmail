/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Cola;
import com.netvox.mail.entidades.Configuraciones;
import com.netvox.mail.entidades.Rutas;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.MailFront;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.servicios.ResumenServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.netvox.mail.utilidades.Utilidades;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.client.RestTemplate;

@Service("coremailservicio")
public class CoreMailServicioImpl {

    public static String getPath_entrada() {
        return path_entrada;
    }

    public static void setPath_entrada(String aPath_entrada) {
        path_entrada = aPath_entrada;
    }

    public static String getPath_salida() {
        return path_salida;
    }

    public static void setPath_salida(String aPath_salida) {
        path_salida = aPath_salida;
    }

    @Autowired
    @Qualifier("clientemysqlservicio")
    ClienteMysqlServicioImpl clientemysqlservicio;

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    @Autowired
    @Qualifier("hiloentradaservicio")
    HiloEntradaServicio hiloentradaservicio;

    @Autowired
    @Qualifier("hiloasignacionservicio")
    HiloAsignacionServicio hiloasignacionservicio;

    //esto permite ejecutar los hilos.
    @Autowired
    @Qualifier("executor")
    ThreadPoolExecutor taskExecutor;

    @Autowired
    @Qualifier("utilidades")
    Utilidades utilidades;

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    @Autowired
    @Qualifier("resumenservicio")
    ResumenServicio resumenservicio;

    @Autowired
    @Qualifier("logconexionesservicio")
    LogConexionesServicio logconexionesservicio;

    @Autowired
    @Qualifier("formatodefechas")
    FormatoDeFechas formatodefechas;

    private static volatile HashMap<String, MailAjustes> mapadeajustesmail;
    private static Configuraciones configuraciones;
    private static List<Resumen> listaresumen = new ArrayList<>();
    private final int SERVICIO_MAIL = 2;
    private static String RUTA_IN;
    private static String RUTA_OUT;
    private static String path_entrada;
    private static String path_salida;
    RestTemplate resttemplate = new RestTemplate();

    public void cargarRutas() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Rutas rutas = mongoops.find(new Query(), Rutas.class).get(0); //este toma el unico resultado que hay en la base
        RUTA_IN = rutas.getRuta_in();
        RUTA_OUT = rutas.getRuta_out();
        path_entrada = rutas.getPath_entrada();
        path_salida = rutas.getPath_salida();
    }

//id 36 cambiar a mongo despues
    public void llenarListaAjustesMail() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        setMapadeajustesmail(new HashMap<>());
        Connection conexion = clientemysqlservicio.obtenerConexion();
        ResultSet resultado = null;
        try {
            Statement statement = conexion.createStatement();
            String sql = "select "
                    + "cola.idCola , "
                    + "email_configuracion.usuario, "
                    + "email_configuracion.pass, "
                    + "email_configuracion.host, "
                    + "email_configuracion.store, "
                    + "email_configuracion.puerto, "
                    + "email_configuracion.maximo_adjunto, "
                    + "cola.nombre nombre_cola, "
                    + "email_configuracion.id, "
                    + "cola.campana "
                    + "from email_configuracion "
                    + "inner join cola on cola.idCola = email_configuracion.queue "
                    + "where cola.service_mail = 1 and cola.campana <> 0";

            resultado = statement.executeQuery(sql);
            while (resultado.next()) {
                MailAjustes mail;
                if (getMapadeajustesmail().containsKey(resultado.getString(2))) {
                    mail = getMapadeajustesmail().get(resultado.getString(2));
                    mail.getColas().add(new Cola(
                            resultado.getInt(1),
                            resultado.getString(8),
                            "",
                            resultado.getInt(10)));
                } else {
                    mail = new MailAjustes(resultado.getString(2), resultado.getString(3),
                            resultado.getString(4), resultado.getString(5), resultado.getInt(6),
                            resultado.getInt(7), resultado.getInt(9));
                    mail.getColas().add(new Cola(
                            resultado.getInt(1),
                            resultado.getString(8),
                            "",
                            resultado.getInt(10)));
                }
                getMapadeajustesmail().put(mail.getUser(), mail);
            }
            statement.close();
            resultado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int obtenerNuevoId(String tipomail, int idconfiguracion, int idcola) {//  mongo, tipomail es entrada o salida
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        int nuevoid = 0;
        try {
            Mail mail = mongoops.findOne(new Query().with(new Sort(Direction.DESC, "$natural")), Mail.class);
            if (mail == null) {
                nuevoid = 1;
                mongoops.insert(new Mail(1, 0, tipomail, formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), idconfiguracion, idcola));
            } else {
                nuevoid = mail.getIdcorreo() + 1;
                mongoops.insert(new Mail(nuevoid, 0, tipomail, formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), idconfiguracion, idcola));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return nuevoid;
    }

    //este metodo debe combinarse con el de obtener configuracion mail . no deberian ser 2 , solo siguio el modelo viejo
    public MailConfiguracion ObtenerMailConfiguracion(int idconfiguracion) {
        MailConfiguracion mailconfiguracion = new MailConfiguracion();
        Connection conexion = null;

        try {
            conexion = clientemysqlservicio.obtenerConexion();
            String sql = "SELECT \n"
                    + "  ec.`id`,\n"
                    + "  ec.`usuario`,\n"
                    + "  ec.`pass`,\n"
                    + "  ec.`host`,\n"
                    + "  ec.`smtp`,\n"
                    + "  ec.`store`,\n"
                    + "  ec.`puerto`,\n"
                    + "  ec.`puerto_salida`,\n"
                    + "  ec.`tipo`,\n"
                    + "  ec.`maximo_adjunto`,\n"
                    + "  ec.`campana`, \n"
                    + "  c.`max_acumulado_mail`, \n"
                    + "  ec.`mensaje_mail_pesado` \n"
                    + "FROM \n"
                    + "  `email_configuracion` ec \n"
                    + "INNER JOIN \n"
                    + "  `campana`  c \n"
                    + " on ec.campana = c.id \n"
                    + "  where ec.`id` = ?\n"
                    + "  LIMIT 1";
            PreparedStatement preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, idconfiguracion);
            ResultSet resultado = preparedstatement.executeQuery();
            while (resultado.next()) {
                mailconfiguracion.setId(resultado.getInt(1));
                mailconfiguracion.setUsuario(resultado.getString(2));
                mailconfiguracion.setPass(resultado.getString(3));
                mailconfiguracion.setHost(resultado.getString(4));
                mailconfiguracion.setSmtp(resultado.getString(5));
                mailconfiguracion.setStore(resultado.getString(6));
                mailconfiguracion.setPuertoentrada(resultado.getInt(7));
                mailconfiguracion.setPuertosalida(resultado.getInt(8));
                mailconfiguracion.setTipo(resultado.getString(9));
                mailconfiguracion.setMax_adjunto(resultado.getInt(10));
                mailconfiguracion.setIdcampana(resultado.getInt(11));
                mailconfiguracion.setMaximo_pendiente(resultado.getInt(12));
                mailconfiguracion.setMensaje_mail_pesado(resultado.getString(13));
            }
            preparedstatement.close();
            resultado.close();
            conexion.close();
        } catch (Exception ex) {
            utilidades.printException(ex);
        }

        return mailconfiguracion;
    }

    //esto se debe revalidar , 
    public boolean eliminarEmailOnline(int idemail) {
        Connection conexion = null;
        boolean elimino = false;
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            String sql = "DELETE FROM `log_mail_online` where id = ?";
            PreparedStatement preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, idemail);
            preparedstatement.executeUpdate();
            elimino = true;
            preparedstatement.close();
            conexion.close();
        } catch (Exception ex) {
            utilidades.printException(ex);
            elimino = false;
        }

        return elimino;
    }

    public Mail guardarMail(Mail mail) {
        Connection conexion = null;
        ResultSet resultado = null;
        PreparedStatement preparedstatement;
        CallableStatement procedimientoalmacenado;
        MongoOperations mongoops = null;
        Mail nuevomail = null;
        try {
            //seguimos dependiendo del sql . eso debe ser eliminado
            conexion = clientemysqlservicio.obtenerConexion();
            preparedstatement = conexion.prepareStatement("select campana.nombre from campana where campana.id=?");
            preparedstatement.setInt(1, mail.getCola().getId_campana());
            resultado = preparedstatement.executeQuery();
            while (resultado.next()) {
                mail.setNombre_campana(resultado.getString(1));
            }
            mongoops = clientemongoservicio.clienteMongo();
//            List<Mail> hilomail = mongoops.find(
//                    new Query(Criteria.where("remitente").is(mail.getRemitente()).
//                            and("destino").is(mail.getDestino()).and("tipificacion").is(2).
//                            and("hilocerrado").is(false)), Mail.class);

            Update update = new Update();
            update.set("remitente", mail.getRemitente())
                    .set("asunto", mail.getAsunto())
                    .set("estado", 0)
                    .set("campana", mail.getCola().getId_campana())
                    .set("nombre_campana", mail.getNombre_campana())
                    .set("id_cola", mail.getId_cola())
                    .set("nombre_cola", mail.getCola().getNombre_cola())
                    .set("mensaje", mail.getMensaje())
                    .set("destino", mail.getDestino())
                    .set("tipificacion", 0)
                    .set("descripcion_tipificacion", "nuevo")
                    .set("hilocerrado", false)
                    .set("idhilo", mail.getIdcorreo())
                    .set("tiempo_atencion", 0);
//            if (hilomail.size() > 0) {
//                update.set("idhilo", hilomail.get(0).getIdhilo());
//            } else {
//               update.set("idhilo", mail.getIdcorreo());
//            }
            nuevomail = mongoops.findAndModify(new Query(Criteria.where("idcorreo").is(mail.getIdcorreo())), update, new FindAndModifyOptions().returnNew(true), Mail.class);
//todo esto sigue sin tener sentido para el uso del mail
            procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
            procedimientoalmacenado.setInt(1, mail.getCola().getId_campana());
            procedimientoalmacenado.setInt(2, SERVICIO_MAIL);
            procedimientoalmacenado.setBoolean(3, true);
            procedimientoalmacenado.execute();

            String sql = "insert into servicios_cola_online set id = ?, campana = ?, servicio = 2, fecha_cola = now()";
            preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, mail.getIdcorreo());
            preparedstatement.setInt(2, mail.getCola().getId_campana());
            preparedstatement.execute();
            preparedstatement.close();

            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            utilidades.printException(ex);
            ex.printStackTrace();
        }
        return nuevomail;
    }

    //este metodo debe combinarse con el metodo de asignar. ya que tiene una parte del contenido de este metodo
    public void notificarCorreoNuevoEnCola(Mail mail) {
        getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola()))
                .forEach((resumen) -> {
                    Mensaje mensaje = new Mensaje();
                    MailInbox mailinbox = new MailInbox();
                    mailinbox.setId(mail.getIdcorreo());
                    mailinbox.setEstado(0);
                    mailinbox.setRemitente(mail.getRemitente());
                    mailinbox.setDestino(mail.getDestino());
                    mailinbox.setAsunto(mail.getAsunto());
                    mailinbox.setFecha_ingreso(mail.getFecha_ingreso());
                    mailinbox.setAdjuntos(mail.getListadeadjuntos());
                    mensaje.setEvento("CORREOCOLA");
                    mensaje.setNew_mail(mailinbox);
                    websocket.enviarMensajeParaUnUsuario(mensaje, resumen.getAgente());
                });
    }

    //hay que moverlo a memoria en algun momento y solo grabar en bd cuando sea necesario y no volver a consultarle a la bd
    public int obtenerCantidadPendientesPorCola(int idUsuario, int idCampana, List<Integer> listacolas) {
        int cantidad = 0;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            cantidad = (int) mongoops.count(
                    new Query(Criteria.where("usuario").is(idUsuario).
                            and("campana").is(idCampana).and("estado").is(1).and("tipificacion").is(0).and("id_cola").in(listacolas)), Mail.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cantidad;
    }

//hay que moverlo a memoria en algun momento y solo grabar en bd cuando sea necesario y no volver a consultarle a la bd
    public int obtenerCantidadNoAsignadosPorColas(List<Integer> listacolas) {
        int cantidad = 0;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            cantidad = (int) mongoops.count(
                    new Query(Criteria.where("estado").is(0).and("id_cola").in(listacolas)), Mail.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cantidad;
    }
//hay que moverlo a memoria en algun momento y solo grabar en bd cuando sea necesario y no volver a consultarle a la bd

    public List<Mail> listarMailsEnCola() {
        List<Mail> listado = new ArrayList<>();
        MongoOperations mongoops;
        Query query;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            query = new Query(Criteria.where("estado").is(0).and("campana").ne(0).and("tipomail").is("entrada"));
            query.fields().include("idcorreo").include("campana").include("fecha_ingreso").include("idhilo").
                    include("asunto").include("idconfiguracion").include("id_cola").include("remitente").include("nombre_cola").include("nombre_campana").include("destino");
            listado = mongoops.find(query, Mail.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listado;
    }

    public void asignarMailAgente(Resumen usuarioresumen, Mail mail) {
        /* 1 disponible 2 atendiendo 4 pausa 5 timbrando*/
        Connection conexion;
        CallableStatement procedimientoalmacenado = null;
        MongoOperations mongoops;
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query(
                    Criteria.where("campana").is(usuarioresumen.getCampana()).and("agente").is(usuarioresumen.getAgente())),
                    new Update().set("estadoagente", 2).set("pendiente", usuarioresumen.getPendiente() + 1),
                    Resumen.class);

            getListaresumen().stream().filter(
                    (agente) -> agente.getCampana() == usuarioresumen.getCampana()
                    && agente.getAgente() == usuarioresumen.getAgente()).forEach((agente) -> {
                        agente.setEstadoagente(2);
                        agente.setPendiente(usuarioresumen.getPendiente() + 1);
                    });
            mongoops.updateFirst(new Query(
                    Criteria.where("idcorreo").is(mail.getIdcorreo())),
                    new Update()
                            .set("estado", 1)
                            .set("tiempoencola", formatodefechas.restaDeFechasEnSegundos(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), mail.getFecha_ingreso()))
                            .set("usuario", usuarioresumen.getAgente())
                            .set("nombre", usuarioresumen.getNombre())
                            .set("fechainiciogestion", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)),
                    Mail.class);
            Mensaje mensaje = new Mensaje();
            MailInbox mailinbox = new MailInbox();
            mailinbox.setId(mail.getIdcorreo());
            mailinbox.setEstado(1);
            mailinbox.setRemitente(mail.getRemitente());
            mailinbox.setDestino(mail.getDestino());
            mailinbox.setAsunto(mail.getAsunto());
            mailinbox.setFecha_ingreso(mail.getFecha_ingreso());
            mailinbox.setAdjuntos(mail.getListadeadjuntos());
            mailinbox.setIdhilo(mail.getIdhilo());

            //este envia a todos.
            getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola())) // aqui le envio al resto
                    .forEach((resumen) -> {
                        mensaje.setEvento("CORREOASIGNADO");
                        mensaje.setIdcorreoasignado(mail.getIdcorreo());
                        websocket.enviarMensajeParaUnUsuario(mensaje, resumen.getAgente());
                    });

            mensaje.setEvento("CORREOASIGNADO");
            mensaje.setNew_mail(mailinbox);
            websocket.enviarMensajeParaUnUsuario(mensaje, usuarioresumen.getAgente());//aqui envio el mensaje a un usuario asignado
            //esto debe borrarse no me sirve.
            procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
            procedimientoalmacenado.setInt(1, mail.getCampana());
            procedimientoalmacenado.setInt(2, SERVICIO_MAIL);
            procedimientoalmacenado.setBoolean(3, false);
            procedimientoalmacenado.execute();
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        //}
    }

    public Mensaje obtenerRespuestaDeLogin(Mensaje mensaje) {
        Connection conexion = clientemysqlservicio.obtenerConexion();
        Connection conexionfirma = clientemysqlservicio.obtenerConexion();

        List<MailFront> correos = new ArrayList<>();
        boolean pausa = false;
        try {
            MongoOperations mongoops;
            int mailspendiente = 0;
            Statement statement = conexion.createStatement();
            ResultSet resultset = statement.executeQuery("select queue,usuario from email_configuracion");
            while (resultset.next()) {
                correos.add(new MailFront(resultset.getInt(1), resultset.getString(2)));
            }
            correos = correos.stream().filter((cola) -> mensaje.getColas().contains(cola.getId())).collect(Collectors.toList());
            statement.close();
            resultset.close();
            conexion.close();

            PreparedStatement preparedstatement = conexionfirma.prepareStatement("select firma_mail from usuario where idUsuario=?");
            preparedstatement.setInt(1, mensaje.getIdagente());
            ResultSet resultsetfirma = preparedstatement.executeQuery();
            while (resultsetfirma.next()) {
                mensaje.setFirma(resultsetfirma.getString("firma_mail"));
            }
            resultsetfirma.close();
            conexionfirma.close();

            mongoops = clientemongoservicio.clienteMongo();

            if (!getListaresumen().stream().anyMatch((Resumen resumen) -> {
                return resumen.getAgente() == mensaje.getIdagente();
            })) {
                int mailspendienteporcola = obtenerCantidadPendientesPorCola(mensaje.getIdagente(), mensaje.getCampana(), mensaje.getColas());
                Resumen resumen = new Resumen(mensaje.getCampana(), mensaje.getIdagente(), mensaje.getAgente(), mailspendienteporcola, mensaje.getColas(), pausa ? 4 : (mailspendienteporcola == 0) ? 1 : 2);
                getListaresumen().add(resumen);
                if (mensaje.getEstado_mail() == 4) { // esto es la validacion para la pausa     
                    resttemplate.postForObject("http://localhost:8084/mail/apis/pausar", mensaje, Mensaje.class);
                }
                mongoops.insert(resumen);
                logconexionesservicio.grabarConexion(resumen);
            }

            if (mensaje.getEstado_mail() == 4) { // esto es la validacion para la pausa     
                pausa = true;
            }
            mailspendiente = getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get().getPendiente(); // es la misma variable que mailspendienteporcola pero aqui lo uso para memoria porque si no siempre consultaria a la bd , de este modo solo se consulta la bd una vez con cada login
            mensaje.setAcumulado_mail(mailspendiente);// se le puso por nombre acumulado mail solo para coordinar con el front, esto es la suma de mails pendiente.
            mensaje.setEstado_mail(pausa ? 4 : (mailspendiente == 0) ? 1 : 2);//disponible si tiene 0 pendientes,2 si tiene pendientes,es decir atendiendo
            mensaje.setCantidad_cola_mail(obtenerCantidadNoAsignadosPorColas(mensaje.getColas())); // todos los mails que estan sin asignar dependiendo de la cola
            mensaje.setEvento("LOGINRESPONSE");
            mensaje.setListacorreos(correos);
            mensaje.setPeso_maximo_adjunto(getConfiguraciones().getPeso_maximo_adjunto());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mensaje;
    }

    public void cargarConfiguraciones() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            configuraciones = mongoops.findOne(new Query(), Configuraciones.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void borrarListaResumen(int idagente) {
        getListaresumen().removeIf((resumen) -> resumen.getAgente() == idagente);
    }

    public void ejecutarHiloEntrada() {
        resumenservicio.borrarResumenTotal();
        taskExecutor.submit(hiloentradaservicio);
    }

    public void ejecutarHiloAsignacion() {
        taskExecutor.submit(hiloasignacionservicio);
    }

    public void grabarFirma(Mensaje mensaje) {
        Connection conexion = clientemysqlservicio.obtenerConexion();
        PreparedStatement preparedstatement = null;
        try {
            preparedstatement = conexion.prepareStatement("update usuario set firma_mail=? where idUsuario=?");
            preparedstatement.setString(1, mensaje.getFirma());
            preparedstatement.setInt(2, mensaje.getIdagente());
            preparedstatement.execute();
            preparedstatement.close();
            conexion.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getRUTA_IN() {
        return RUTA_IN;
    }

    public static void setRUTA_IN(String aCARPETA_IN) {
        RUTA_IN = aCARPETA_IN;
    }

    public static String getRUTA_OUT() {
        return RUTA_OUT;
    }

    public static void setRUTA_OUT(String aCARPETA_OUT) {
        RUTA_OUT = aCARPETA_OUT;
    }

    public static HashMap<String, MailAjustes> getMapadeajustesmail() {
        return mapadeajustesmail;
    }

    public static void setMapadeajustesmail(HashMap<String, MailAjustes> mapadeajustesmail) {
        CoreMailServicioImpl.mapadeajustesmail = mapadeajustesmail;
    }

    public static List<Resumen> getListaresumen() {
        return listaresumen;
    }

    public static void setListaresumen(List<Resumen> aListaresumen) {
        listaresumen = aListaresumen;
    }

    public static Configuraciones getConfiguraciones() {
        return configuraciones;
    }

    public static void setConfiguraciones(Configuraciones aConfiguraciones) {
        configuraciones = aConfiguraciones;
    }

}
