/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Usuario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    mailsporcola = obtenerMailsPorCola(mails);
                    mailsporcola.forEach((idcola, lista) -> {
                        System.out.println("Id cola: " + idcola + ", cantidad de mails: " + lista.size());
                        if (!lista.isEmpty()) {
                            HashMap<Integer, List<Usuario>> listadeusuariosporcola = coremailservicio.obtenerListaDeUsuariosPorCola();//toma la coleccion resumen
                            lista.forEach((mail) -> {
                                MailConfiguracion mailconfiguracion = coremailservicio.ObtenerMailConfiguracion(mail.getIdconfiguracion());
                                if (listadeusuariosporcola.containsKey(idcola)) {//tiene que ver , porque como se trae //todos los conectados sin verificar que colas existen
                                    List<Usuario> listausuarios = listadeusuariosporcola.get(idcola);
                                    Usuario usuarioasignado = obtenerUsuario(listausuarios, mailconfiguracion.getMaximo_pendiente(), mail.getId_cola());
                                    if (usuarioasignado != null) {                                   
                                        coremailservicio.asignarMailAgente(usuarioasignado,mail);
                                        System.out.println("El usuario asignado es : "+ usuarioasignado.getNombre() +" id: "+ usuarioasignado.getId() + " , tiene  " + usuarioasignado.getPendientes() + " mails pendientes ");
                                    }else{
                                        System.out.println("No hay usuarios disponibles");
                                    }
                                }
                            });
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

    public Usuario obtenerUsuario(List<Usuario> listausuarios, int maximopendiente, int cola) {
        Usuario elegido = null;
        int menor = 1000000;
        if (listausuarios.size() > 0) {
            System.out.println("******ELIGIENDO A USUARIOS*********");
            System.out.println("NOMBRE\t PENDIENTES\t MAXPENDIENTES \tCOLA");
            for (Usuario candidato : listausuarios) {
                candidato.setPendientes(coremailservicio.obtenerCantidadPendientesActual(candidato.getId(), candidato.getCampana()));
                System.out.println(candidato.getNombre() + "\t " + candidato.getPendientes() + "\t " + maximopendiente + "\t" + candidato.getCola());
                if (candidato.getPendientes() < menor && maximopendiente > candidato.getPendientes()) {
                    elegido = candidato;
                    menor = candidato.getPendientes();
                }
            }
        }
        return elegido;
    }

    public HashMap<Integer, List<Mail>> obtenerMailsPorCola(List<Mail> mails) {
        HashMap<Integer, List<Mail>> listamailsporcola = new HashMap<>();
        try {
            mails.forEach((mail) -> {
                if (listamailsporcola.containsKey(mail.getId_cola())) {
                    listamailsporcola.get(mail.getId_cola()).add(mail);
                } else {
                    List<Mail> lista = new ArrayList<>();
                    lista.add(mail);
                    listamailsporcola.put(mail.getId_cola(), lista);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listamailsporcola;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
