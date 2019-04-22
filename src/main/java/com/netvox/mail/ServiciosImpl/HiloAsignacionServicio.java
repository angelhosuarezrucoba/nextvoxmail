/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.Usuario;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("hiloasignacionservicio")
public class HiloAsignacionServicio implements Runnable {

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    String usuario = "";
    String clave = "";
    String popHost = "";
    private HashMap<Integer, LinkedList<Mail>> mailsporcola;
    private LinkedList<Usuario> listausuarios;
    private boolean activo = true;

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
                    mailsporcola.keySet().stream().map((cola) -> {
                        LinkedList<Mail> listado = mailsporcola.get(cola);
                        System.out.println("Id cola: " + cola + ", cantidad de mails: " + listado.size());
                        if (!listado.isEmpty()) {
                            HashMap<Integer, Usuario> listausuarioscola = coremailservicio.listarUsuariosCampanaCola();//se ha cmabiado el lugar de esta expression porque no tiene sentido 
                            listado.forEach((mail) -> {
                                MailConfiguracion mailconfiguracion = coremailservicio.ObtenerMailConfiguracion(mail.getIdconfiguracion());
                                listausuarios = new LinkedList<>();
                                ///validamos si la cola se encuentra activa
                                if (listausuarioscola.containsKey(cola)) {
                                    listausuarios = listausuarioscola.get(cola).getList_user_queue();
                                    if (listausuarios != null) {
                                        if (mail.getAsunto() == null) {
                                            mail.setAsunto("");
                                        }
                                        Usuario usuario_asignado = getUsuario(listausuarios, mailconfiguracion.getMaximo_pendiente(), mail.getCola().getId_cola());
                                        if (usuario_asignado != null) {
                                            System.out.println("USUARIO : " + usuario_asignado.getNombre() + " , ID : " + usuario_asignado.getId());
                                            usuario_asignado.getMails().add(mail);
                                            coremailservicio.asignarMailAgente(usuario_asignado);
                                            System.out.println(usuario_asignado.getId() + " = " + usuario_asignado.getPendientes() + " ... " + usuario_asignado.getMails().size());
                                        }
                                        listausuarios.clear();
                                    }
                                }
                            });
                        }
                        return listado;
                    }).forEachOrdered((listado) -> {
                        listado.clear();
                    });
                } else {
                    System.out.println("Sin mensajes por asignar");
                }
                System.out.println("Fin Hilo asignacion");
                System.out.println("----------------------------------------------------------------------------------------");
                Thread.sleep(5000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Usuario getUsuario(LinkedList<Usuario> usuarios, int maximopendiente, int cola) {
        Usuario elegido = null;
        int menor = 1000000;
        if (usuarios.size() > 0) {
            System.out.println("******ELIGIENDO A USUARIOS*********");
            System.out.println("NOMBRE\t\t\t PENDIENTES\t MAXIMO_PENDIENTES\t COLA");
            for (Usuario u : usuarios) {
                u.setPendientes(coremailservicio.obtenerCantidadPendientesActual(u.getId(), u.getCampana_asing()));
                System.out.println(u.getNombre() + "\t\t " + u.getPendientes() + "\t\t " + maximopendiente + "\t\t" + u.getCola_asign());
                if (u.getCola_asign() == cola) {
                    if (u.getPendientes() < menor && maximopendiente > u.getPendientes()) {
                        elegido = u;
                        menor = u.getPendientes();
                    }
                }
            }
            System.out.println("************************************");
        }

        if (elegido != null) {
            elegido.setPendientes(elegido.getPendientes() + 1);
            int index = -1;

            for (int x = 0; x < usuarios.size(); x++) {
                Usuario u = usuarios.get(x);
                if (u.getId() == elegido.getId()) {
                    index = x;
                    break;
                }
            }
            if (index > -1) {
                usuarios.set(index, elegido);
                System.out.println("SET USUARIO=" + elegido.getId() + "=" + elegido.getPendientes());
            }
        }
        return elegido;
    }

    public HashMap<Integer, LinkedList<Mail>> obtenerMailsPorCola(List<Mail> mails) {
        HashMap<Integer, LinkedList<Mail>> listamailsporcola = new HashMap<>();
        mails.forEach((mail) -> {//este bucle solo distribuye segun la cola un listado de correos 
            if (listamailsporcola.containsKey(mail.getId_cola())) {
                LinkedList<Mail> listado = listamailsporcola.get(mail.getId_cola());
                listado.add(mail);
                listamailsporcola.put(mail.getId_cola(), listado);
            } else { // aqui entra primero.                            
                LinkedList<Mail> listado = new LinkedList<>();
                listado.add(mail);
                listamailsporcola.put(mail.getId_cola(), listado);
            }
        });
        return listamailsporcola;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
