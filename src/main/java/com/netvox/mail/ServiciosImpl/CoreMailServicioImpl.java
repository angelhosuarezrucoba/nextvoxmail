/*
 * Esta clase contiene los metodos en general que requiere la aplicacion 
 * Se deben migrar algunos metodos a los servicios correspondientes, se hizo 
 * siguiendo el modelo del viejo core pero no es practico . debe cambiarse en una version posterior.
 * 
 *  
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.MailConfiguracion;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Configuraciones;
import com.netvox.mail.entidades.Rutas;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.CuentaDeCorreo;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidadesfront.MailFront;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.servicios.PausaServicio;
import com.netvox.mail.servicios.ResumenServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.client.RestTemplate;

@Service("coremailservicio")
public class CoreMailServicioImpl {

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

    @Autowired
    @Qualifier("resumendiarioservicio")
    ResumenDiarioServicioImpl resumendiarioservicio;

    @Autowired
    @Qualifier("pausaservicio")
    PausaServicio pausaservicio;

    private static Configuraciones configuraciones;
    private static List<Resumen> listaresumen = new ArrayList<>();
    private final int SERVICIO_MAIL = 2;
    private static String RUTA_IN;
    private static String RUTA_OUT;
    private static String path_entrada;
    private static String path_salida;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public void cargarRutas() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Rutas rutas = mongoops.find(new Query(), Rutas.class).get(0); //este toma el unico resultado que hay en la base
        RUTA_IN = rutas.getRuta_in();
        RUTA_OUT = rutas.getRuta_out();
        path_entrada = rutas.getPath_entrada();
        path_salida = rutas.getPath_salida();
    }

    public List<CuentaDeCorreo> obtenerCuentasDeCorreo() {
        Connection conexion = clientemysqlservicio.obtenerConexion();
        ResultSet resultado = null;
        List<CuentaDeCorreo> lista = new ArrayList<>();
        try {

            String sql = "select "
                    + "cola.idCola as id_cola, "
                    + "email_configuracion.usuario as usuario, "
                    + "email_configuracion.pass as clave, "
                    + "email_configuracion.host as host, "
                    + "email_configuracion.store as store, "
                    + "email_configuracion.puerto as puerto, "
                    + "email_configuracion.maximo_adjunto as maximo_adjunto, "
                    + "cola.nombre as nombre_cola, "
                    + "email_configuracion.id as id_configuracion, "
                    + "cola.campana as id_campana "
                    + "from email_configuracion "
                    + "inner join cola on cola.idCola = email_configuracion.queue "
                    + "where cola.service_mail = 1 and cola.campana <> 0";

            resultado = conexion.createStatement().executeQuery(sql);
            while (resultado.next()) {
                lista.add(new CuentaDeCorreo(
                        resultado.getString("usuario"),
                        resultado.getString("clave"),
                        resultado.getString("host"),
                        resultado.getString("store"),
                        resultado.getInt("puerto"),
                        resultado.getInt("maximo_adjunto"),
                        resultado.getInt("id_cola"),
                        resultado.getString("nombre_cola"),
                        resultado.getInt("id_campana"),
                        resultado.getInt("id_configuracion"))
                );
            }
            resultado.close();
            conexion.close();
        } catch (SQLException ex) {
            log.error("error en el metodo obtenerCuentasDeCorreo", ex);
        }
        return lista;
    }

    public Mail insertarNuevoMail(int idconfiguracion, int idcola, String nombre_cola, int id_campana, String asunto, String remitente, String destino) {//  mongo, tipomail es entrada o salida
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Mail nuevomail = null;
        try {
            nuevomail = new Mail(generadorId(), 0, "entrada", formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA), idconfiguracion, idcola, nombre_cola, id_campana, asunto, remitente, destino);
            mongoops.insert(nuevomail);
        } catch (Exception ex) {
            log.error("error en el metodo insertarNuevoMail", ex);
        }
        return nuevomail;
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
            log.error("error en el metodo ObtenerMailConfiguracion", ex);
        }

        return mailconfiguracion;
    }

    public Mail ActualizarMail(Mail mail) {
        Connection conexion = null;
        ResultSet resultado = null;
        PreparedStatement preparedstatement;
        CallableStatement procedimientoalmacenado;
        MongoOperations mongoops = null;
        Mail nuevomail = null;
        FindAndModifyOptions opciones = new FindAndModifyOptions();
        opciones.returnNew(true);
        try {
            //seguimos dependiendo del sql . eso debe ser eliminado
            conexion = clientemysqlservicio.obtenerConexion();
            preparedstatement = conexion.prepareStatement("select campana.nombre from campana where campana.id=?");
            preparedstatement.setInt(1, mail.getCampana());
            resultado = preparedstatement.executeQuery();
            while (resultado.next()) {
                mail.setNombre_campana(resultado.getString(1));
            }
            mongoops = clientemongoservicio.clienteMongo();
            Update update = new Update();
            update.set("nombre_campana", mail.getNombre_campana())
                    .set("tipificacion", 0)
                    .set("descripcion_tipificacion", "nuevo")
                    .set("hilocerrado", false)
                    .set("idhilo", mail.getIdcorreo())
                    .set("tiempo_atencion", 0);

            nuevomail = mongoops.findAndModify(new Query(Criteria.where("idcorreo").is(mail.getIdcorreo())), update, opciones, Mail.class);
//todo esto sigue sin tener sentido para el uso del mail
            procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
            procedimientoalmacenado.setInt(1, mail.getCampana());
            procedimientoalmacenado.setInt(2, SERVICIO_MAIL);
            procedimientoalmacenado.setBoolean(3, true);
            procedimientoalmacenado.execute();

            String sql = "insert into servicios_cola_online set id = ?, campana = ?, servicio = 2, fecha_cola = now()";
            preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, mail.getIdcorreo());
            preparedstatement.setInt(2, mail.getCampana());
            preparedstatement.execute();
            preparedstatement.close();

            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            log.error("error en el metodo ActualizarMail", ex);
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
                    mailinbox.setCc("");//sera vacio hasta que podamos encontrar el codigo que llene eso en el metodo Utilidades.createhtml
                    mailinbox.setId_cola(mail.getId_cola());
                    mailinbox.setAsunto(mail.getAsunto());
                    mailinbox.setFecha_ingreso(formatodefechas.cambiarFormatoFechas(mail.getFecha_ingreso(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));
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
            log.error("error en el metodo obtenerCantidadPendientesPorCola", ex);
        }
        return cantidad;
    }

//hay que moverlo a memoria en algun momento y solo grabar en bd cuando sea necesario y no volver a consultarle a la bd
    public int obtenerCantidadSinAsignarPorColas(List<Integer> listacolas) {
        int cantidad = 0;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            cantidad = (int) mongoops.count(
                    new Query(Criteria.where("estado").is(0).and("id_cola").in(listacolas)), Mail.class);
        } catch (Exception ex) {
            log.error("error en el metodo obtenerCantidadNoAsignadosPorColas", ex);
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
            log.error("error en el metodo listarMailsEnCola", e);
        }
        return listado;
    }

    public void asignarMailAgente(Resumen usuarioresumen, Mail mail) {
        /* 1 disponible 2 atendiendo 4 pausa 5 timbrando*/
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            resumenservicio.modificarPendientes(usuarioresumen.getAgente(), usuarioresumen.getPendiente() + 1);
            resumendiarioservicio.actualizarPendientes(1, usuarioresumen.getAgente());
            if (resumenservicio.obtenerEstado(usuarioresumen.getAgente()) == 1) {
                resumenservicio.modificarEstado(usuarioresumen.getAgente(), 2);
                resumendiarioservicio.actualizarEstado(usuarioresumen.getAgente(), 2, 0);
            }
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
            mailinbox.setFecha_ingreso(formatodefechas.cambiarFormatoFechas(mail.getFecha_ingreso(), formatodefechas.FORMATO_FECHA_HORA, formatodefechas.FORMATO_FECHA_HORA_SLASH));
            mailinbox.setAdjuntos(mail.getListadeadjuntos());
            mailinbox.setIdhilo(mail.getIdhilo());
            mailinbox.setId_cola(mail.getId_cola());
            mailinbox.setNombre_cola(mail.getNombre_cola());
            mailinbox.setCc("");
            mensaje.setEvento("CORREOASIGNADO");
            getListaresumen().stream().filter((resumen) -> resumen.getListacolas().contains(mail.getId_cola())) // aqui le envio al resto
                    .forEach((resumen) -> {
                        mensaje.setIdcorreoasignado(mail.getIdcorreo());
                        websocket.enviarMensajeParaUnUsuario(mensaje, resumen.getAgente());
                    });
            mensaje.setNew_mail(mailinbox);
            websocket.enviarMensajeParaUnUsuario(mensaje, usuarioresumen.getAgente());//aqui envio el mensaje a un usuario asignado
        } catch (ParseException ex) {

            log.error("error en el metodo asignarMailAgente", ex);
        }
    }

    //este metodo trae los correos que se usan en todas las campañas
    public List<MailFront> obtenerCorreos(Mensaje mensaje) {
        List<MailFront> correos = new ArrayList<>();
        try {
            Connection conexion = clientemysqlservicio.obtenerConexion();
            Statement statement = conexion.createStatement();
            ResultSet resultset = statement.executeQuery("select queue,usuario from email_configuracion");
            while (resultset.next()) {
                correos.add(new MailFront(resultset.getInt(1), resultset.getString(2)));
            }
            correos = correos.stream().filter((cola) -> mensaje.getColas().contains(cola.getId())).collect(Collectors.toList());
            statement.close();
            resultset.close();
            conexion.close();
        } catch (Exception e) {
            log.error("error en el metodo obtenerCorreos", e);
        }
        return correos;
    }

    public String obtenerFirma(int idagente) {
        String firma = "";
        try {
            Connection conexionfirma = clientemysqlservicio.obtenerConexion();
            PreparedStatement preparedstatement = conexionfirma.prepareStatement("select firma_mail from usuario where idUsuario=?");
            preparedstatement.setInt(1, idagente);
            ResultSet resultsetfirma = preparedstatement.executeQuery();
            while (resultsetfirma.next()) {
                firma = resultsetfirma.getString("firma_mail");
            }
            resultsetfirma.close();
            preparedstatement.close();
            conexionfirma.close();
        } catch (Exception e) {
            log.error("error en el metodo obtenerFirma", e);
        }
        return firma;
    }

    public Mensaje obtenerRespuestaDeLogin(Mensaje mensaje) {
        try {
            MongoOperations mongoops = clientemongoservicio.clienteMongo();
            mensaje.setFirma(obtenerFirma(mensaje.getIdagente()));
            if (!resumenservicio.hayResumen(mensaje.getIdagente())) {
                int mailspendienteporcola = obtenerCantidadPendientesPorCola(mensaje.getIdagente(), mensaje.getCampana(), mensaje.getColas());
                Resumen resumen = new Resumen(mensaje.getCampana(), mensaje.getIdagente(), mensaje.getAgente(), mailspendienteporcola, mensaje.getColas(), mensaje.getEstado_mail() == 4 ? 4 : (mailspendienteporcola == 0) ? 1 : 2, mensaje.getPedido_pausa());
                getListaresumen().add(resumen);
                if (mensaje.getEstado_mail() == 4) { // esto es la validacion para la pausa   
                    resumenservicio.modificarEstado(mensaje.getIdagente(), 4);
                    pausaservicio.pausar(mensaje.getIdagente());
                }
                mongoops.insert(resumen);
                logconexionesservicio.grabarConexion(resumen);
            }
            if (mensaje.getEstado_mail() == 4) { // esto es la validacion para la pausa                  
                mensaje.setEstado_mail(4);
                mensaje.setPedido_pausa(0);
            } else if (mensaje.getPedido_pausa() == 1) {//este caso es si o si estado 2.
                mensaje.setEstado_mail(2);
                mensaje.setPedido_pausa(1);
            } else {//caso 1 , o 2 sin pausa.
                mensaje.setEstado_mail(resumenservicio.obtenerPendientes(mensaje.getIdagente()) == 0 ? 1 : 2);//disponible si tiene 0 pendientes,2 si tiene pendientes,es decir atendiendo
                mensaje.setPedido_pausa(0);
            }          
            

            mensaje.setAcumulado_mail(resumenservicio.obtenerPendientes(mensaje.getIdagente()));// se le puso por nombre acumulado mail solo para coordinar con el front, esto es la suma de mails pendiente.
            mensaje.setCantidad_cola_mail(obtenerCantidadSinAsignarPorColas(mensaje.getColas())); // todos los mails que estan sin asignar dependiendo de la cola
            mensaje.setEvento("LOGINRESPONSE");
            mensaje.setListacorreos(obtenerCorreos(mensaje));// estos son para eneviar mensaje.
            mensaje.setTiempo_pausa(resumendiarioservicio.tiempoEnPausa(mensaje.getIdagente()));
            mensaje.setPeso_maximo_adjunto(getConfiguraciones().getPeso_maximo_adjunto());
        } catch (Exception e) {
            log.error("error en el metodo obtenerRespuestaDeLogin", e);
        }
        return mensaje;
    }

    public void cargarConfiguraciones() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        try {
            configuraciones = mongoops.findOne(new Query(), Configuraciones.class);
        } catch (Exception e) {
            log.error("error en el metodo cargarConfiguraciones", e);
        }
    }

    public int generadorId() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        FindAndModifyOptions opciones = new FindAndModifyOptions();
        opciones.returnNew(true);
        return mongoops.findAndModify(new Query(Criteria.where("id").is(1)), new Update().inc("idcorreo", 1), opciones, Configuraciones.class).getIdcorreo();
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
            log.error("error en el metodo listarMailsEnCola", e);
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

}
