/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Cola;
import com.netvox.mail.entidades.Configuracion;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.entidades.Parametros;
import com.netvox.mail.entidades.Usuario;
import com.netvox.mail.servicios.ClienteMongoServicio;
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
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import utilidades.Utilidades;

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
    TaskExecutor taskExecutor;

    private static volatile HashMap<String, MailAjustes> mailporcampana;
    private static volatile HashMap<Integer, Integer> capturasporcola; // el original decia captures ... no entiendo , significa mails? cantidad?
    private static volatile HashMap<Integer, Integer> asignadosporcola;
    private static volatile HashMap<Integer, Integer> respondidosporcola;
    private static volatile LinkedList<Mail> listamails;
    private final int SERVICIO_MAIL = 2;

    public CoreMailServicioImpl() {
        capturasporcola = new HashMap<>();
        asignadosporcola = new HashMap<>();
        respondidosporcola = new HashMap<>();
        listamails = new LinkedList<>();
        mailporcampana = new HashMap<>();
    }

    public void cargarConfiguracionGlobal() {
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Configuracion configuracion = mongoops.find(new Query(), Configuracion.class).get(0); //este toma el unico resultado que hay en la base
        Parametros.URL_IN = configuracion.getUrl_in();
        Parametros.CARPETA_IN = configuracion.getRuta_in();
        Parametros.CARPETA_OUT = configuracion.getRuta_out();
    }

    public void cargarCantidadesPorCola() { //esto consulta la tabla email_configuracion

        Connection conexion = null;
        ResultSet resultado ;

        try {
            conexion = clientemysqlservicio.obtenerConexion();
            CallableStatement procedimientoalmacenado = conexion.prepareCall("call sp_email_load_quantitys()");//esto coloca las cantidades por cola en 0 0 0 usando el id 17
            resultado = procedimientoalmacenado.executeQuery();
            while (resultado.next()) {
                capturasporcola.put(resultado.getInt(1), 0);
                asignadosporcola.put(resultado.getInt(1), 0);
                respondidosporcola.put(resultado.getInt(1), 0);
            }
            resultado.close();
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
//        finally {            
//            clientemysqlservicio.cerrarConexion(conexion);
//        }
    }

    //este procedimiento consulta la tabla email , se han conservado todos los campos identico como los tiene en la bd
    // para evitar confusion por los nombres. 
    public void cargarListaMailsPorEstado() {
        Connection conexion = null;
        ResultSet resultado = null;
        LinkedList<Mail> lista = new LinkedList<>();

        try {
            conexion = clientemysqlservicio.obtenerConexion();
            CallableStatement procedimientoalmacenado = conexion.prepareCall("call sp_email_get_by_state(?)");
            procedimientoalmacenado.setInt(1, 0);//esto lo enviaban por defecto con 0 , se busco y no existe otra parte donde se use con otro valor
            resultado = procedimientoalmacenado.executeQuery();
            while (resultado.next()) {
                Mail mail = new Mail();
                mail.setId(resultado.getInt(1));
                mail.setRemitente(resultado.getString(2));
                mail.setSubject(resultado.getString(3));
                mail.setCola(new Cola(resultado.getInt(8), resultado.getInt(4)));
                mail.setFecha_index(resultado.getString(5));
                mail.setUsuario(resultado.getInt(6));//se reemplazo agente por usuario , dado el nombre en la bd
                mail.setTipo(resultado.getString(7));
                lista.add(mail);
            }
            resultado.close();
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//            listamails = lista;
//        }

    }

    public void ObtenerConfiguracionMail(/*HashMap<String, MailConfiguracion> email_by_colas*/) {
        Connection conexion = clientemysqlservicio.obtenerConexion();
        ResultSet resultado = null;
        LinkedList<Mail> lista = new LinkedList<>();
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
            //System.out.println(sql);
            resultado = statement.executeQuery(sql);
            while (resultado.next()) {
                MailAjustes mail;
                if (getMailporcampana().containsKey(resultado.getString(2))) {
                    mail = getMailporcampana().get(resultado.getString(2));
                    mail.getColas().add(new Cola(
                            resultado.getInt(1),
                            resultado.getString("nombre_cola"),
                            "",
                            resultado.getInt(10)));
                } else {
                    mail = new MailAjustes(resultado.getString(2), resultado.getString(3), resultado.getString(4), resultado.getString(5), resultado.getInt(6), resultado.getInt(7), resultado.getInt(9));
                    mail.getColas().add(new Cola(
                            resultado.getInt(1),
                            resultado.getString("nombre_cola"),
                            "",
                            resultado.getInt(10)));
                }
                getMailporcampana().put(mail.getUser(), mail);
            }
            statement.close();
            resultado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }
    }

    public int ObtenerNuevoId(boolean inbound, int idconfiguracion, int idCola) {
        Connection conexion = null;
        int nuevoid = 0;
        try {
            conexion = clientemysqlservicio.obtenerConexion();

            CallableStatement procedimientoalmacenado = conexion.prepareCall("call sp_email_get_id(?,?,?,?)");
            procedimientoalmacenado.setString(1, (inbound == true ? "IN" : "OUT"));
            procedimientoalmacenado.registerOutParameter(2, java.sql.Types.INTEGER);
            procedimientoalmacenado.setInt(3, idconfiguracion);
            procedimientoalmacenado.setInt(4, idCola);
            procedimientoalmacenado.execute();
            nuevoid = procedimientoalmacenado.getInt(2);
            procedimientoalmacenado.close();
            conexion.close();
        } catch (SQLException ex) {
            //Utilidades.printException(ex);
            ex.printStackTrace();
        } 
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }
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
            Utilidades.printException(ex);
        }
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }

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
            Utilidades.printException(ex);
            elimino = false;
        } 
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }
        return elimino;
    }

    public void guardarMail(Mail mail, int total) {
        Connection conexion = null;
        try {

            conexion = clientemysqlservicio.obtenerConexion();
            CallableStatement procedimientoalmacenado = conexion.prepareCall("call sp_email_IN_guardar02(?,?,?,?,? ,?,?)");
            procedimientoalmacenado.setInt(1, mail.getId());
            procedimientoalmacenado.setString(2, mail.getRemitente());
            procedimientoalmacenado.setString(3, mail.getSubject() == null ? "" : mail.getSubject());
            procedimientoalmacenado.setInt(4, total);
            procedimientoalmacenado.setInt(5, mail.getCola().getId_campana());
            procedimientoalmacenado.setString(6, mail.getCola().getNombre_cola());
            procedimientoalmacenado.setInt(7, mail.getCola().getId_cola());
            procedimientoalmacenado.execute();

            procedimientoalmacenado = conexion.prepareCall("call sp_actualiza_resumen_servicio_en_cola(?,?,?)");
            procedimientoalmacenado.setInt(1, mail.getCola().getId_campana());
            procedimientoalmacenado.setInt(2, SERVICIO_MAIL);
            procedimientoalmacenado.setBoolean(3, true);
            procedimientoalmacenado.execute();

            String sql = "insert into servicios_cola_online set id = ?, campana = ?, servicio = 2, fecha_cola = now()";
            PreparedStatement preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, mail.getId());
            preparedstatement.setInt(2, mail.getCola().getId_campana());
            preparedstatement.execute();
            preparedstatement.close();
            
            procedimientoalmacenado.close();            
            conexion.close();
        } catch (SQLException ex) {
            Utilidades.printException(ex);
        } 
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }
    }

    public synchronized int anadirMails(Cola queue) {
        try {
            System.out.println("********* queue " + queue.toString());
            System.out.println("********* queue " + getCapturasporcola().get(queue.getId_cola()));
            if (getCapturasporcola().get(getCapturasporcola().get(queue.getId_cola())) == null) {
                System.out.println("********* queue NUEVA INSERTANDO EN MEMORIA IDCOLA: " + queue.getId_cola());
                getCapturasporcola().put(queue.getId_cola(), 0);
            }
            int cantidad_actual = getCapturasporcola().get(queue.getId_cola());
            getCapturasporcola().put(queue.getId_cola(), cantidad_actual + 1);
        } catch (Exception ex) {
            Utilidades.printException(ex);
        }
        return getCapturasporcola().get(queue.getId_cola());
    }

    public synchronized int removerMails(Cola queue) {
        try {
            int cantidad_actual = getCapturasporcola().get(queue.getId_cola());
            if (cantidad_actual < 1) {
                return 0;
            }
            getCapturasporcola().put(queue.getId_cola(), cantidad_actual - 1);

        } catch (Exception ex) {
            Utilidades.printException(ex);
        }
        return getCapturasporcola().get(queue.getId_cola());
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
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }

        return cantidad;
    }

    public LinkedList<Mail> listarMailsEnCola() {
        LinkedList<Mail> listado = new LinkedList<>();
        Connection conexion;
        ResultSet resultado;
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            Statement statement = conexion.createStatement();
            resultado = statement.executeQuery("select id, campana , fecha_ingreso_cola ,asunto,idconfiguracion, cola  from log_mail_online where estado = 0 and campana <> 0 ");
            System.out.println("email => " + "select id, campana , fecha_ingreso_cola ,asunto,idconfiguracion  from log_mail_online where estado = 0 and campana <> 0  ");
            while (resultado.next()) {
                listado.add(new Mail(resultado.getInt("id"), resultado.getInt("campana"), resultado.getString("fecha_ingreso_cola"), resultado.getString("asunto"), resultado.getInt("idconfiguracion"), resultado.getInt("cola")));
            }
            statement.close();
            resultado.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
//        finally {
//            clientemysqlservicio.obtenerConexion();
//        }
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
                listado.get(resultado.getInt("cola")).setList_user_queue(new Usuario(resultado.getInt("agente"), resultado.getInt("PENDIENTE"), resultado.getInt("cola"), resultado.getString("nombre"), resultado.getInt("campana")));
            }

            if (!listado.isEmpty()) {
                for (Usuario users : listado.values()) {
                    LinkedList<Usuario> list_users = users.getList_user_queue();
                    for (int i = 0; i < list_users.size(); i++) {
                        users = list_users.get(i);
                    }
                }
            }
            
            resultado.close();
            statement.close();
            conexion.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
//        finally {
//            clientemysqlservicio.cerrarConexion(conexion);
//        }
        return listado;
    }

    public void asignarMailAgente(Usuario user) {
        Connection conexion = null;
        if (user.getMails().size() > 0) {
            try {
                System.out.println("Asignando a " + user.getNombre() + " - " + user.getId());
                conexion = clientemysqlservicio.obtenerConexion();
                Statement statement = conexion.createStatement();
                CallableStatement procedimientoalmacenado = null;
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
                    query += " where id = " + mail.getId();
                    statement.execute(query);

                    query = "insert into servicio_conversacion set "
                            + "idagente = " + user.getId() + ", "
                            + "idcampana = " + mail.getCola().getId_campana() + ", "
                            + "fec_ini_conver = now(), "
                            + "servicio = 2, "
                            + "idconversacion = " + mail.getId();
                    statement.execute(query);

                    query = "delete from servicios_cola_online "
                            + " where id = " + mail.getId()
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
                Utilidades.printException(ex);
                ex.printStackTrace();
            } 
//            finally {
//                clientemysqlservicio.cerrarConexion(conexion);
//            }
        }
    }

    // esto genera el hilo que va a estar constantemente buscando nuevos correos.
    public void ejecutarHiloEntrada() {
        taskExecutor.execute(hiloentradaservicio);
    }

    public void ejecutarHiloAsignacion() {
        taskExecutor.execute(hiloasignacionservicio);
    }

    public static HashMap<String, MailAjustes> getMailporcampana() {
        return mailporcampana;
    }

    public static void setMailporcampana(HashMap<String, MailAjustes> Mailporcampana) {
        mailporcampana = Mailporcampana;
    }

    public HashMap<Integer, Integer> getCapturasporcola() {
        return capturasporcola;
    }

    public void setCapturasporcola(HashMap<Integer, Integer> capturasporcola) {
        this.capturasporcola = capturasporcola;
    }

    public HashMap<Integer, Integer> getAsignadosporcola() {
        return asignadosporcola;
    }

    public void setAsignadosporcola(HashMap<Integer, Integer> asignadosporcola) {
        this.asignadosporcola = asignadosporcola;
    }

    public HashMap<Integer, Integer> getRespondidosporcola() {
        return respondidosporcola;
    }

    public void setRespondidosporcola(HashMap<Integer, Integer> respondidosporcola) {
        this.respondidosporcola = respondidosporcola;
    }

    public LinkedList<Mail> getListamails() {
        return listamails;
    }

    public void setListamails(LinkedList<Mail> listamails) {
        this.listamails = listamails;
    }

}
