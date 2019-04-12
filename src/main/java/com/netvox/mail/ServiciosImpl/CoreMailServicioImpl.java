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
import com.netvox.mail.servicios.ClienteMongoServicio;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

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

    //esto permite ejecutar los hilos.
    @Autowired
    @Qualifier("executor")
    TaskExecutor taskExecutor;

    private static volatile HashMap<String, MailAjustes> mailporcampana;
    private static volatile HashMap<Integer, Integer> capturasporcola; // el original decia captures ... no entiendo , significa mails? cantidad?
    private static volatile HashMap<Integer, Integer> asignadosporcola;
    private static volatile HashMap<Integer, Integer> respondidosporcola;
    private static volatile LinkedList<Mail> listamails;

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
        ResultSet resultado = null;

        try {
            conexion = clientemysqlservicio.obtenerConexion();
            CallableStatement procedimientoalmacenado = conexion.prepareCall("call sp_email_load_quantitys()");//esto coloca las cantidades por cola en 0 0 0 usando el id 17
            resultado = procedimientoalmacenado.executeQuery();
            while (resultado.next()) {
                capturasporcola.put(resultado.getInt(1), 0);
                asignadosporcola.put(resultado.getInt(1), 0);
                respondidosporcola.put(resultado.getInt(1), 0);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            clientemysqlservicio.cerrarConexion(conexion);
        }
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
                mail.setAgente(resultado.getInt(6));//se reemplazo agente por usuario , dado el nombre en la bd
                mail.setTipo(resultado.getString(7));
                lista.add(mail);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            clientemysqlservicio.cerrarConexion(conexion);
            listamails = lista;
        }

    }

    // esto genera el hilo que va a estar constantemente buscando nuevos correos.
    public void ejecutarHiloEntrada() {
        taskExecutor.execute(hiloentradaservicio);
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
