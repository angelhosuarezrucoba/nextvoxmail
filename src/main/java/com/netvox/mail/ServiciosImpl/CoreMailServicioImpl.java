/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Cola;
import com.netvox.mail.entidades.Rutas;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.entidades.Usuario;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.netvox.mail.utilidades.Utilidades;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

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

    private static volatile HashMap<String, MailAjustes> mapadeajustesmail;

    private FormatoDeFechas formato = new FormatoDeFechas();
    private final int SERVICIO_MAIL = 2;
    private static String RUTA_IN;
    private static String RUTA_OUT;

    public CoreMailServicioImpl() {

    }

    public void cargarRutas() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Rutas rutas = mongoops.find(new Query(), Rutas.class).get(0); //este toma el unico resultado que hay en la base
        RUTA_IN = rutas.getRuta_in();
        RUTA_OUT = rutas.getRuta_out();
    }

//id 36 cambiar a mongo despues
    public void llenarListaAjustesMail() {
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

    /*  public int ObtenerNuevoId(int idconfiguracion, int idCola) {// convertir  mongo
        Connection conexion = null;
        int nuevoid = 0;
        try {
            conexion = clientemysqlservicio.obtenerConexion();

            CallableStatement procedimientoalmacenado = conexion.prepareCall("call sp_email_get_id(?,?,?)");
          
            procedimientoalmacenado.registerOutParameter(1, java.sql.Types.INTEGER);
            procedimientoalmacenado.setInt(2, idconfiguracion);
            procedimientoalmacenado.setInt(4, idCola);
            procedimientoalmacenado.execute();
            nuevoid = procedimientoalmacenado.getInt(1);
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return nuevoid;
    }*/
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
                            // .set("rango_fecha", formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA))
                            .set("campana", mail.getCola().getId_campana())
                            .set("nombre_campana", mail.getNombre_campana())
                            .set("id_cola", mail.getId_cola())
                            .set("nombre_cola", mail.getCola().getNombre_cola())
                            .set("texto", mail.getTexto()),
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
        Connection conexion = null;
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            Statement statement = conexion.createStatement();
            ResultSet resultado = statement.executeQuery("SELECT count(idAgente) FROM servicio_conversacion "
                    + "where idagente = " + idUsuario + " and idcampana = " + idCampana + " and servicio = 2");
            if (resultado.next()) {
                cantidad = resultado.getInt(1);
            }
            statement.close();
            resultado.close();
            conexion.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cantidad;
    }

    public List<Mail> listarMailsEnCola() {
        List<Mail> listado = new LinkedList<>();
        MongoOperations mongoops;
        Query query;
        try {
            mongoops = clientemongoservicio.clienteMongo();
            query = new Query(Criteria.where("estado").is(0).and("campana").ne(0));
            query.fields().include("idcorreo").include("campana").include("fecha_ingreso").
                    include("asunto").include("idconfiguracion").include("id_cola");
            listado = mongoops.find(query, Mail.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listado;
    }

    public HashMap<Integer, Usuario> listarUsuariosCampanaCola() {
        HashMap<Integer, Usuario> listado = null;
        Connection conexion = null;
        ResultSet resultado;
        String query;
        Statement statement;
        try {
            listado = new HashMap<>();
            conexion = clientemysqlservicio.obtenerConexion();
            statement = conexion.createStatement();

            query = "select resumen_diario.campana, resumen_diario.agente, resumen_diario.nombre,  resumen_diario.acumulado_mails , cola_usuario_login.cola,"
                    + "IFNULL(q.C_PENDIENTE,0) as 'PENDIENTE' from resumen_diario  "
                    + "inner join cola_usuario_login on resumen_diario.agente =cola_usuario_login.usuario   "
                    + "LEFT JOIN (select COUNT(*) as 'C_PENDIENTE',lmo.`campana`,lmo.`usuario` from `log_mail_online` lmo where lmo.`estado` = 1 and rango_fecha IN (date(now()),DATE(DATE_ADD(NOW(), INTERVAL -1 DAY))) "
                    + "group by lmo.`campana`,lmo.`usuario`)q ON q.campana = resumen_diario.campana AND q.usuario = resumen_diario.agente  "
                    + "INNER JOIN cola_usuario cu ON cu.usuario = resumen_diario.agente  AND cu.campana = resumen_diario.campana AND cu.mail = 1  "
                    + "INNER JOIN cola ON cola.campana = resumen_diario.campana "
                    + "where  (resumen_diario.estado_mail= 1 or resumen_diario.estado_mail= 2) and pedido_pausa_mail <> 1 "
                    + "and cola.service_mail=1 "
                    + "group by  resumen_diario.agente,cola_usuario_login.cola,resumen_diario.acumulado_mails "
                    + "order by cola_usuario_login.cola";

            System.out.println("SQL(list user asign to queue)****** \n" + query);
            resultado = statement.executeQuery(query);
            while (resultado.next()) {
                if (!listado.containsKey(resultado.getInt("cola"))) {
                    listado.put(resultado.getInt("cola"), new Usuario(resultado.getInt("agente"), resultado.getInt("PENDIENTE"), resultado.getInt("cola"), resultado.getString("nombre"), resultado.getInt("campana")));
                }
                //aca no deberia ser lista de usuarios
                listado.get(resultado.getInt("cola")).setList_user_queue(new Usuario(resultado.getInt("agente"), resultado.getInt("PENDIENTE"), resultado.getInt("cola"), resultado.getString("nombre"), resultado.getInt("campana")));
            }

//            if (!listado.isEmpty()) {
//                for (Usuario users : listado.values()) {
//                    LinkedList<Usuario> list_users = users.getList_user_queue();
//                    for (int i = 0; i < list_users.size(); i++) {
//                        users = list_users.get(i);
//                    }
//                }
//            } // no se para que sirve ,no le encuentro sentido
            resultado.close();
            statement.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return listado;
    }

    public void asignarMailAgente(Usuario user) {
        Connection conexion;
        Statement statement;
        CallableStatement procedimientoalmacenado = null;
        if (user.getMails().size() > 0) {
            try {
                System.out.println("Asignando a " + user.getNombre() + " - " + user.getId());
                conexion = clientemysqlservicio.obtenerConexion();
                statement = conexion.createStatement();

                //aumentamos el contador de correos en proceso o encolados , cantidadd de correso que recibio
                String query = "update resumen_diario set estado_mail = 2, proceso_mails=(proceso_mails+1) ,"
                        + "acumulado_mails=(acumulado_mails+1) , "
                        + "nuevo_mail = true "
                        + "where campana = " + user.getCampana_asing() + " and "
                        + "agente=" + user.getId();

                System.out.println(query);
                procedimientoalmacenado.execute(query);

                for (Mail mail : user.getMails()) {
                    query = " update log_mail_online set estado=1 ,    ";
                    query += " tiempo_cola = time_to_sec(TIMEDIFF( now(),log_mail_online.fecha_ingreso_cola ))   , ";
                    query += " usuario = " + user.getId() + " , ";
                    query += " usuario_name ='" + user.getNombre() + "' ,";
                    query += " fecha_inicio_gestion = now() ";
                    query += " where id = " + mail.getIdcorreo();
                    statement.execute(query);

                    query = "insert into servicio_conversacion set "
                            + "idagente = " + user.getId() + ", "
                            + "idcampana = " + mail.getCola().getId_campana() + ", "
                            + "fec_ini_conver = now(), "
                            + "servicio = 2, "
                            + "idconversacion = " + mail.getIdcorreo();
                    statement.execute(query);

                    query = "delete from servicios_cola_online "
                            + " where id = " + mail.getIdcorreo()
                            + " and campana = " + mail.getCola().getId_campana()
                            + " and servicio = 2";
                    statement.execute(query);

                    procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
                    procedimientoalmacenado.setInt(1, mail.getCola().getId_campana());
                    procedimientoalmacenado.setInt(2, SERVICIO_MAIL);
                    procedimientoalmacenado.setBoolean(3, false);
                    procedimientoalmacenado.execute();

                }
                procedimientoalmacenado.close();
                statement.close();
                conexion.close();
            } catch (SQLException ex) {
                utilidades.printException(ex);
                ex.printStackTrace();
            }

        }
    }

    public void ejecutarHiloEntrada() {
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
