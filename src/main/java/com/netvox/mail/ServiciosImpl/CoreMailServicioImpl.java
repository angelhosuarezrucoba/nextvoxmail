/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Cola;
import com.netvox.mail.entidades.Rutas;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidades.Usuario;
import com.netvox.mail.entidadesfront.Adjunto;
import com.netvox.mail.entidadesfront.MailFront;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.MailSalida;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

@Service("coremailservicio")
public class CoreMailServicioImpl {

    public static List<Resumen> getListaresumen() {
        return listaresumen;
    }

    public static void setListaresumen(List<Resumen> aListaresumen) {
        listaresumen = aListaresumen;
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

    private static volatile HashMap<String, MailAjustes> mapadeajustesmail;
    private static List<Resumen> listaresumen = new ArrayList<>();
    private FormatoDeFechas formato = new FormatoDeFechas();
    private final int SERVICIO_MAIL = 2;
    private static String RUTA_IN;
    private static String RUTA_OUT;

    public void cargarRutas() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Rutas rutas = mongoops.find(new Query(), Rutas.class).get(0); //este toma el unico resultado que hay en la base
        RUTA_IN = rutas.getRuta_in();
        RUTA_OUT = rutas.getRuta_out();
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
                    + "where cola.service_mail = 1 and cola.campana <> 0 and email_configuracion.id=35";

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
                mongoops.insert(new Mail(1, 0, tipomail, formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA), idconfiguracion, idcola));
            } else {
                nuevoid = mail.getIdcorreo() + 1;
                mongoops.insert(new Mail(nuevoid, 0, tipomail, formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA), idconfiguracion, idcola));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return nuevoid;
    }

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

    public void guardarMail(Mail mail) {
        Connection conexion = null;
        ResultSet resultado = null;
        PreparedStatement preparedstatement;
        CallableStatement procedimientoalmacenado;
        MongoOperations mongoops = null;
        try {

            //convertir mongo
            conexion = clientemysqlservicio.obtenerConexion();
            preparedstatement = conexion.prepareStatement("select campana.nombre from campana where campana.id=?");
            preparedstatement.setInt(1, mail.getCola().getId_campana());
            resultado = preparedstatement.executeQuery();
            while (resultado.next()) {
                mail.setNombre_campana(resultado.getString(1));
            }
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query(Criteria.where("idcorreo").is(mail.getIdcorreo())),
                    new Update().set("remitente", mail.getRemitente())
                            .set("asunto", mail.getAsunto())
                            .set("estado", 0)
                            .set("campana", mail.getCola().getId_campana())
                            .set("nombre_campana", mail.getNombre_campana())
                            .set("id_cola", mail.getId_cola())
                            .set("nombre_cola", mail.getCola().getNombre_cola())
                            .set("mensaje", mail.getMensaje())
                            .set("destino", mail.getDestino()),
                    Mail.class);

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
    }

    public int obtenerCantidadPendientesActual(int idUsuario, int idCampana) {
        int cantidad = 0;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            cantidad = (int) mongoops.count(
                    new Query(Criteria.where("usuario").is(idUsuario).
                            and("campana").is(idCampana).and("estado").is(1)), Mail.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cantidad;
    }

    public int obtenerCantidadPendientesPorCola(int idUsuario, int idCampana, int idcola) {
        int cantidad = 0;
        MongoOperations mongoops;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            cantidad = (int) mongoops.count(
                    new Query(Criteria.where("usuario").is(idUsuario).
                            and("campana").is(idCampana).and("estado").is(1).and("id_cola").is(idcola)), Mail.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cantidad;
    }

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

    public List<Mail> listarMailsEnCola() {
        List<Mail> listado = new ArrayList<>();
        MongoOperations mongoops;
        Query query;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            query = new Query(Criteria.where("estado").is(0).and("campana").ne(0));
            query.fields().include("idcorreo").include("campana").include("fecha_ingreso").
                    include("asunto").include("idconfiguracion").include("id_cola").include("remitente").include("nombre_cola").include("nombre_campana").include("destino");
            listado = mongoops.find(query, Mail.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listado;
    }

    public HashMap<Integer, List<Usuario>> obtenerListaDeUsuariosPorCola() { // este metodo recurre a la memoria.
        HashMap<Integer, List<Usuario>> usuariosporcola = new HashMap<>();
        List<Resumen> listadodeusuarios = new ArrayList<>();
        try {
            listadodeusuarios = getListaresumen();
            listadodeusuarios.forEach((resumen) -> {
                Usuario usuario = new Usuario(resumen.getAgente(),
                        resumen.getPendiente(), resumen.getCola(),
                        resumen.getNombre(), resumen.getCampana());

                if (!usuariosporcola.containsKey(resumen.getCola())) {
                    List<Usuario> lista = new ArrayList<>();
                    lista.add(usuario);
                    usuariosporcola.put(resumen.getCola(), lista);
                } else {
                    usuariosporcola.get(resumen.getCola()).add(usuario);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usuariosporcola;
    }

    public void asignarMailAgente(Usuario usuario, Mail mail) {
        /* 1 disponible 2 atendiendo 4 pausa 5 timbrando*/
        Connection conexion;
        CallableStatement procedimientoalmacenado = null;
        MongoOperations mongoops;
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            mongoops = clientemongoservicio.clienteMongo();
            mongoops.updateFirst(new Query(
                    Criteria.where("campana").is(usuario.getCampana()).and("agente").is(usuario.getId())),
                    new Update().set("estadoagente", 2).set("pendiente", usuario.getPendientes() + 1),
                    Resumen.class);

            getListaresumen().stream().filter(
                    (agente) -> agente.getCampana() == usuario.getCampana()
                    && agente.getAgente() == usuario.getId()).forEach((agente) -> {
                        agente.setEstadoagente(2);
                        agente.setPendiente(usuario.getPendientes() + 1);                    
                    });
            mongoops.updateFirst(new Query(
                    Criteria.where("idcorreo").is(mail.getIdcorreo())),
                    new Update()
                            .set("estado", 1)
                            .set("tiempoencola", formato.restaDeFechasEnSegundos(
                                    formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA), mail.getFecha_ingreso()))
                            .set("usuario", usuario.getId())
                            .set("nombre", usuario.getNombre())
                            .set("fechainiciogestion", formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA)),
                    Mail.class);
            Mensaje mensaje = new Mensaje();
            MailInbox mailinbox = new MailInbox();
            mailinbox.setId(mail.getIdcorreo());
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

            mensaje.setEvento("CORREOASIGNADO");
            mensaje.setNew_mail(mailinbox);
            websocket.enviarMensajeParaUnUsuario(mensaje, usuario.getId());
            System.out.println("acabo de mandar por el socket");

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
        List<MailFront> correos = new ArrayList<>();
        MongoOperations mongoops;
        try {
            Statement statement = conexion.createStatement();
            ResultSet resultset = statement.executeQuery("select queue,usuario from email_configuracion");
            while (resultset.next()) {
                correos.add(new MailFront(resultset.getInt(1), resultset.getString(2)));
            }
            correos = correos.stream().filter((cola) -> mensaje.getColas().contains(cola.getId())).collect(Collectors.toList());
            statement.close();
            resultset.close();
            conexion.close();
            mongoops = clientemongoservicio.clienteMongo();
            int mailspendiente = 0;
            if (!getListaresumen().stream().anyMatch((Resumen resumen) -> {
                return resumen.getAgente() == mensaje.getIdagente();
            })) {
                mensaje.getColas().forEach((cola) -> {
                    int mailspendienteporcola = obtenerCantidadPendientesPorCola(mensaje.getIdagente(), mensaje.getCampana(), cola);
                    getListaresumen().add(
                            new Resumen(mensaje.getCampana(), mensaje.getIdagente(), mensaje.getAgente(), mailspendienteporcola, cola, 1));//primero habria que revisar todas sus colasp ara ver su estado
                });
                
                
                
                if (getListaresumen().stream().mapToInt((resumen) -> resumen.getPendiente()).sum() > 0) {
                    getListaresumen().stream().filter(resumen -> resumen.getAgente() == mensaje.getIdagente()).findFirst().get().setEstadoagente(2);
                }
                mongoops.insertAll(getListaresumen().stream().filter((resumen) -> {
                    return resumen.getAgente() == mensaje.getIdagente();
                }).collect(Collectors.toList()));
            }
            
            mailspendiente = getListaresumen().stream().filter((resumen) -> resumen.getAgente() == mensaje.getIdagente()).mapToInt(resumen -> resumen.getPendiente()).sum();
            mensaje.setAcumulado_mail(mailspendiente);// se le puso por nombre acumulado mail solo para coordinar con el front, esto es la suma de mails pendiente.
            mensaje.setEstado_mail(mailspendiente == 0 ? 1 : 2);//disponible si tiene 0 pendientes,2 si tiene pendientes,es decir atendiendo
            mensaje.setCantidad_cola_mail(obtenerCantidadNoAsignadosPorColas(mensaje.getColas())); // todos los mails que estan sin asignar dependiendo de la cola
            mensaje.setEvento("LOGINRESPONSE");
            mensaje.setListacorreos(correos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mensaje;
    }

    public void borrarListaResumen(int idagente) {
        getListaresumen().removeIf((resumen) -> resumen.getAgente() == idagente);
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

    public void ejecutarHiloEntrada() {
        resumenservicio.borrarResumenTotal();
        taskExecutor.submit(hiloentradaservicio);
    }

    public void ejecutarHiloAsignacion() {
        taskExecutor.submit(hiloasignacionservicio);
    }

    public static String getRUTA_IN() {
        return RUTA_IN;
    }

    public static void setRUTA_IN(String aCARPETA_IN) {
        RUTA_IN = aCARPETA_IN;
    }

    public static String getCARPETA_OUT() {
        return RUTA_OUT;
    }

    public static void setCARPETA_OUT(String aCARPETA_OUT) {
        RUTA_OUT = aCARPETA_OUT;
    }

    public static HashMap<String, MailAjustes> getMapadeajustesmail() {
        return mapadeajustesmail;
    }

    public static void setMapadeajustesmail(HashMap<String, MailAjustes> mapadeajustesmail) {
        CoreMailServicioImpl.mapadeajustesmail = mapadeajustesmail;
    }

}
