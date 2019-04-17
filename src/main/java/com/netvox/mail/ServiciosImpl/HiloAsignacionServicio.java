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
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("hiloasignacionservicio")
public class HiloAsignacionServicio implements Runnable {

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    public volatile boolean alive = false;
    String usuario = "";
    String clave = "";
    String popHost = "";
    private HashMap<Integer, LinkedList<Mail>> correocolas;
    private LinkedList<Usuario> listausuarios;

    @Override
    public void run() {
        while (true) {
            System.out.println("THREAD-ASIGNACION");
            try {

                System.out.println("Asignando.....");
                correocolas = new HashMap<>();
                System.out.println("...............................................................");
                //me traigo una lista de mails que ya fueron asignados a una campaña
                LinkedList<Mail> mails = coremailservicio.listarMailsEnCola();
                if (mails.isEmpty()) {
                    TimeUnit.MILLISECONDS.sleep(5000);
                    continue;
                }

                System.out.println("CANTIDAD MENSAJES : " + mails.size());
                //recorro los emails
                for (Mail mail : mails) {
                    //si la campañas esta contenida
                    //System.out.println("MAIL " + mail);
                    //System.out.println("correoColas " + correoColas.toString());
                    if (correocolas.containsKey(mail.getId_cola())) {
                        LinkedList<Mail> listado = correocolas.get(mail.getId_cola());
                        //System.out.println("listado " + listado);
                        //agrego la lista de mails de esta campaña al linkedlist
                        listado.add(mail);
                        //agrego la lista de mails a una campaña
                        correocolas.put(mail.getId_cola(), listado);
                    } else { // aqui entra primero.
                        //el nuevo listado
                        LinkedList<Mail> listado = new LinkedList<>();
                        listado.add(mail);
                        correocolas.put(mail.getId_cola(), listado);
                    }
                }

                //recorremos toda las lista de campañas para recorres todos los correos para despues asignar
                //a cada usuario asociados a esta.
                for (Integer cola : correocolas.keySet()) {
                    //recorrer por colas
                    //   System.out.println("----->> CAMPANA  " + campana);

                    //traemos las colas  de una campaña con todos sus datos referentes al correo como
                    //la idcola
                    //asunto
                    //nombre de la cola
                    //HashMap<Integer, ColaBean> list_queue = this.managerbd.colas_campana(campana);
                    //traemos el listado de todos los correos asignados para una campaña
                    LinkedList<Mail> listado = correocolas.get(cola);
                    System.out.println("LISTADO COLA ID: " + cola + ", SIZE MAILS: " + listado.size());

                    //recorremos todos los listado de correos
                    if (!listado.isEmpty()) {
                        //traemos todos los usuarios que se encuentra disponibles de una campaña amarrado a una cola  
                        HashMap<Integer, Usuario> listausuarioscola = coremailservicio.listarUsuariosCampanaCola();//se ha cmabiado el lugar de esta expression porque no tiene sentido 
                        //listar a todos los usuarios de esa cola si no existe un listado de correos en esa cola.
                        
                        //recorremos todo el listad de mails
                        for (Mail mail : listado) {
                            //recorremos todas las colas de una campaña
                            MailConfiguracion mailconfiguracion = coremailservicio.ObtenerMailConfiguracion(mail.getIdconfiguracion());
                            listausuarios = new LinkedList<>();
                            ///validamos si la cola se encuentra activa
                            if (listausuarioscola.containsKey(cola)) {
                                //nos traemos la lista de usuarios pertenecientes a una cola
                                listausuarios = listausuarioscola.get(cola).getList_user_queue();
                                //System.out.println("SIZE USER : " + lista_usuarios.size());
                                if (listausuarios != null) {
                                    if (mail.getSubject_in() == null) {
                                        mail.setSubject_in("");
                                    }//
                                    //metodo que sirve para retornar al usuario que tiene menos emails recibidos
                                    //System.out.println("COLA MAIL ID " + mail.getCola().getId_cola());
                                    Usuario usuario_asignado = getUsuario(listausuarios, mailconfiguracion.getMaximo_pendiente(), mail.getCola().getId_cola());
                                    if (usuario_asignado != null) {
                                        System.out.println("USUARIO : " + usuario_asignado.getNombre() + " , ID : " + usuario_asignado.getId());
                                        //System.out.println(" ..... " + usuario_asignado.getId());
                                        //añadimos el listado de emails para ese usuario
                                        usuario_asignado.getMails().add(mail);
                                        //
                                        coremailservicio.asignarMailAgente(usuario_asignado);
                                        System.out.println(usuario_asignado.getId() + " = " + usuario_asignado.getPendientes() + " ... " + usuario_asignado.getMails().size());
                                    }

                                    listausuarios.clear();
                                }
                            }
                        }
                    }
                    listado.clear();

                }
                System.out.println("Fin asignacion...");
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
}
