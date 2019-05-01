/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Resumen;
import com.netvox.mail.entidades.Usuario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("hiloasignacionservicio")
public class HiloAsignacionServicio implements Runnable {

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    private HashMap<Integer, List<Mail>> mailsporcola;
    // private List<Usuario> listausuarios;
    private boolean activo = true;
    // private Usuario usuarioasignado;

    @Override
    public void run() {
        while (isActivo()) {
            try {
                System.out.println("----------------------------------------------------------------------------------------");
                System.out.println("Hilo de Asignacion");
                List<Mail> mails = coremailservicio.listarMailsEnCola();//las colas representan el correo que se agrega a la campaña , si hay 2 => son dos colas.
                if (!mails.isEmpty()) {
                    System.out.println("mensajes sin asignar : " + mails.size());
                    mails.forEach((mail) -> {
                        MailConfiguracion mailconfiguracion = coremailservicio.ObtenerMailConfiguracion(mail.getIdconfiguracion());
                        Resumen usuarioresumen = obtenerUsuarioDelResumen(coremailservicio.getListaresumen(), mailconfiguracion.getMaximo_pendiente(), mail.getId_cola());
                        if (usuarioresumen != null) {
                            coremailservicio.asignarMailAgente(usuarioresumen, mail);
                        }
                    });
                } else {
                    System.out.println("Sin mensajes por asignar");
                }
                System.out.println("Fin Hilo asignacion");
                Thread.sleep(5000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Resumen obtenerUsuarioDelResumen(List<Resumen> listaresumen, int maximopendiente, int cola) {
        Resumen resumenelegido = null;
        int menor = 1000000;
        List<Resumen> listaporcola = listaresumen.stream().filter((resumen) -> resumen.getListacolas().contains(cola)).collect(Collectors.toList());
        if (listaporcola.size() > 0) {
            System.out.println("******ELIGIENDO A USUARIOS*********");
            System.out.println("NOMBRE\t PENDIENTES\t MAXPENDIENTES \tCOLA");
            for (Resumen candidato : listaporcola) {
                System.out.println(candidato.getNombre() + "\t " + candidato.getPendiente() + "\t " + maximopendiente + "\t" + candidato.getListacolas());
                if (candidato.getPendiente() < menor && maximopendiente > candidato.getPendiente()) {
                    resumenelegido = candidato;
                    menor = candidato.getPendiente();
                }
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
