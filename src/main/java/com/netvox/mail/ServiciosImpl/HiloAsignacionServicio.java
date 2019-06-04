/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.MailConfiguracion;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Resumen;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("hiloasignacionservicio")
public class HiloAsignacionServicio implements Runnable {

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    private boolean activo = true;
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run() {
        while (isActivo()) {
            try {
                log.info("----------------------------------------------------------------------------------------");
                log.info("Hilo de Asignacion");
                List<Mail> mails = coremailservicio.listarMailsEnCola();//las colas representan el correo que se agrega a la campaña , si hay 2 => son dos colas.
                if (!mails.isEmpty()) {
                    log.info("mensajes sin asignar : " + mails.size());
                    mails.forEach((mail) -> {
                        MailConfiguracion mailconfiguracion = coremailservicio.ObtenerMailConfiguracion(mail.getIdconfiguracion());
                        Resumen usuarioresumen = obtenerUsuarioDelResumen(coremailservicio.getListaresumen(), mailconfiguracion.getMaximo_pendiente(), mail.getId_cola());
                        if (usuarioresumen != null) {
                            coremailservicio.asignarMailAgente(usuarioresumen, mail);
                            log.info("se asigno al agente : " + usuarioresumen.getNombre() + " id: " + usuarioresumen.getAgente());
                        } else {
                            log.info("No se pudo asignar a un Agente");
                        }
                    });
                } else {
                    log.info("Sin mensajes por asignar");
                }
                log.info("Fin Hilo asignacion");
                log.info("----------------------------------------------------------------------------------------");
                Thread.sleep(5000);
            } catch (Exception ex) {
                log.error("error en el HiloAsignacionServicio", ex);
            }
        }
    }

    public Resumen obtenerUsuarioDelResumen(List<Resumen> listaresumen, int maximopendiente, int cola) {
        Resumen resumenelegido = null;
        int menor = 1000000;
        List<Resumen> listaporcola = listaresumen.stream().filter((resumen) -> resumen.getEstadoagente() != 4 && resumen.getPedido_pausa() != 1 && resumen.getListacolas().contains(cola)).collect(Collectors.toList());
        if (listaporcola.size() > 0) {
            log.info("----------------------------------------------------------------------------------------");
            log.info("******ELIGIENDO A USUARIOS*********");
            log.info("NOMBRE\t PENDIENTES\t MAXPENDIENTES \tCOLA");
            for (Resumen candidato : listaporcola) {
                log.info(candidato.getNombre() + "\t " + candidato.getPendiente() + "\t " + maximopendiente + "\t" + candidato.getListacolas());
                if (candidato.getPendiente() < menor && maximopendiente > candidato.getPendiente()) {
                    resumenelegido = candidato;
                    menor = candidato.getPendiente();
                }
                log.info("----------------------------------------------------------------------------------------");
            }
        }
        return resumenelegido;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
