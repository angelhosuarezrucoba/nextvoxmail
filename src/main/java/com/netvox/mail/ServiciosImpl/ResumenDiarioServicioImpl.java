package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.Api.entidadessupervisor.Pausa;
import com.netvox.mail.configuraciones.WebSocket;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service("resumendiarioservicio")
public class ResumenDiarioServicioImpl {

    @Autowired
    @Qualifier("clientemysqlservicio")
    ClienteMysqlServicioImpl clientemysqlservicio;

    @Autowired
    @Qualifier("formatodefechas")
    FormatoDeFechas formatodefechas;

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicioImpl clientemongoservicio;

    @Autowired
    @Qualifier("websocket")
    WebSocket websocket;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public void insertarConexion(Resumen resumen) {
        try {
            Connection conexion = clientemysqlservicio.obtenerConexion();
            String sqlresumen_diario = ("select first_firmado,hora_logueo,session,tiempo_acumulado_logueado from resumen_diario where agente=" + resumen.getAgente() + " and campana=" + resumen.getCampana());
            ResultSet resultado = conexion.createStatement().executeQuery(sqlresumen_diario);
            String hora_logueo = "";
            String session = "";
            String primer_logueo = "";
            int tiempo_acumulado_logueado = 0;
            while (resultado.next()) {
                hora_logueo = resultado.getString("hora_logueo");
                session = resultado.getString("session");
                primer_logueo = resultado.getString("first_firmado");
                tiempo_acumulado_logueado = resultado.getInt("tiempo_acumulado_logueado");
            }

            conexion.createStatement().execute("delete from resumen_diario_correo where agente=" + resumen.getAgente());

            conexion.createStatement().execute("delete from resumen_diario_correo_cola where idagente=" + resumen.getAgente());

            String sql = ("insert into resumen_diario_correo(agente,nombre,campana,hora_logueo,pendientes,estado,hora_inicio_estado,atendidos,primer_logueo,session,tiempo_acumulado_logueado) "
                    + "values(?,?,?,?,?,?,?,?,?,?,?)");

            resumen.getListacolas().stream().forEach((cola) -> {
                try {
                    String sqlresumen_diario_correo_cola = ("insert into resumen_diario_correo_cola(idcampana,idagente,idcola,atendidos) values(?,?,?,?)");
                    PreparedStatement preparedstatement_diario = conexion.prepareStatement(sqlresumen_diario_correo_cola);
                    preparedstatement_diario.setInt(1, resumen.getCampana());
                    preparedstatement_diario.setInt(2, resumen.getAgente());
                    preparedstatement_diario.setInt(3, cola);
                    preparedstatement_diario.setInt(4, 0);
                    preparedstatement_diario.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            PreparedStatement preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, resumen.getAgente());
            preparedstatement.setString(2, resumen.getNombre());
            preparedstatement.setInt(3, resumen.getCampana());
            preparedstatement.setString(4, hora_logueo);
            preparedstatement.setInt(5, resumen.getPendiente());
            preparedstatement.setInt(6, resumen.getEstadoagente());
            preparedstatement.setString(7, formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA));
            preparedstatement.setInt(8, 0);
            preparedstatement.setString(9, primer_logueo);
            preparedstatement.setString(10, session);
            preparedstatement.setInt(11, tiempo_acumulado_logueado);
            preparedstatement.execute();
            resultado.close();
            preparedstatement.close();
            conexion.close();
        } catch (SQLException e) {
            log.error("error en el metodo insertarConexion", e);
        }
    }

    public void actualizarAtendidosPorCola(int idagente, int campana, int cola) {
        try {
            Connection conexion = clientemysqlservicio.obtenerConexion();
            String sql = ("update resumen_diario_correo_cola"
                    + " set atendidos=atendidos+1"
                    + " where idcampana=? and idagente=? and idcola=?");
            PreparedStatement preparedstatement = conexion.prepareStatement(sql);
            preparedstatement.setInt(1, campana);
            preparedstatement.setInt(2, idagente);
            preparedstatement.setInt(3, cola);
            preparedstatement.execute();
            conexion.close();
        } catch (Exception e) {
            log.error("error en el metodo actualizarAtendidosPorCola", e);
        }
    }

    public void actualizarPendientes(int pendiente, int idagente) {
        try {
            Connection conexion = clientemysqlservicio.obtenerConexion();
            String sql = ("update resumen_diario_correo set pendientes = pendientes +( " + pendiente + ") where agente=" + idagente + ";");
            conexion.createStatement().execute(sql);
            if (pendiente == -1) {
                String sqlatendidos = ("update resumen_diario_correo set atendidos = atendidos + 1 where agente=" + idagente + ";");
                conexion.createStatement().execute(sqlatendidos);
            }
            conexion.close();
        } catch (SQLException e) {
            log.error("error en el metodo actualizarPendientes", e);
        }
    }

    public void actualizaHoraLogueo(Resumen mensaje) {
        try {
            Connection conexion = clientemysqlservicio.obtenerConexion();

            String sqlresumen_diario = ("select hora_logueo,tiempo_acumulado_logueado from resumen_diario where agente=" + mensaje.getAgente() + " and campana=" + mensaje.getCampana());
            ResultSet resultado = conexion.createStatement().executeQuery(sqlresumen_diario);
            String hora_logueo = "";
            int tiempo_acumulado_logueado = 0;
            while (resultado.next()) {
                hora_logueo = resultado.getString("hora_logueo");
                tiempo_acumulado_logueado = resultado.getInt("tiempo_acumulado_logueado");
            }
            String sql = ("update resumen_diario_correo set tiempo_acumulado_logueado=" + tiempo_acumulado_logueado + ", hora_logueo='" + hora_logueo + "' where agente=" + mensaje.getAgente());
            conexion.createStatement().execute(sql);
            resultado.close();
            conexion.close();

        } catch (Exception e) {
            log.error("error en el metodo actualizaHoraLogueo", e);
        }
    }

    public void actualizarEstado(int idagente, int estadonuevo, int pedido_pausa) {
        Connection conexion;
        try {
            conexion = clientemysqlservicio.obtenerConexion();
            int duracion;
            String sql = "";
            duracion = tiempoEnPausa(idagente);
            switch (estadonuevo) {
                case 0: //////////recordar esto.
                    sql = ("update resumen_diario_correo set estado=" + estadonuevo
                            + ", hora_inicio_estado='" + formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)
                            + "', pedido_pausa=" + pedido_pausa                           
                            + " where agente=" + idagente);
                    break;
                case 1:
                    sql = ("update resumen_diario_correo set estado=" + estadonuevo
                            + ", hora_inicio_estado='" + formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)
                            + "', pedido_pausa=" + pedido_pausa
                            + ", tiempo_acumulado_pausa=" + duracion
                            + " where agente=" + idagente);
                    break;
                case 2:
                    sql = ("update resumen_diario_correo set estado=" + estadonuevo
                            + ", hora_inicio_estado='" + formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)
                            + "', tiempo_acumulado_pausa=" + duracion
                            + ", pedido_pausa=" + pedido_pausa
                            + " where agente=" + idagente);
                    break;
                case 4:
                    sql = ("update resumen_diario_correo set estado=" + estadonuevo
                            + ",hora_inicio_estado='" + formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)
                            + "', hora_inicio_pausa='" + formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA_HORA)
                            + "', pedido_pausa=" + pedido_pausa
                            + " where agente=" + idagente);
                    break;
                default:
                    break;

            }
            conexion.createStatement().execute(sql);
            conexion.close();
        } catch (SQLException e) {
            log.error("error en el metodo actualizarEstado", e);
        }
    }

    public int tiempoEnPausa(int idagente) {
        int duracion;
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        Aggregation agregacion = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("finpausa").regex(formatodefechas.convertirFechaString(new Date(), formatodefechas.FORMATO_FECHA)).
                        and("idagente").is(idagente)),
                Aggregation.group("idagente").
                        sum("duracion").as("duracion"),
                Aggregation.project().
                        andExpression("_id").as("idagente").
                        andExpression("duracion").as("duracion")
        );
        try {
            duracion = mongoops.aggregate(agregacion, "pausas", Pausa.class).getMappedResults().stream().filter((agente) -> agente.getIdagente() == idagente).findFirst().get().getDuracion();
        } catch (Exception e) {
            duracion = 0;
        }
        return duracion;
    }

}
