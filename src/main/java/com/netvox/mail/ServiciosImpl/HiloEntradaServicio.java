/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Cola;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.entidades.Parametros;
import com.netvox.mail.servicios.ClienteMongoServicio;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import utilidades.Utilidades;

@Service("hiloentradaservicio")
public class HiloEntradaServicio implements Runnable {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    @Autowired
    @Qualifier("clientemysqlservicio")
    ClienteMysqlServicioImpl clientemysqlservicio;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    @Autowired
    @Qualifier("mailautomatico")
    MailAutomatico mailautomatico;

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
    Store store = null;
    Folder folder = null;

    @Override
    public void run() {
        try {

            boolean new_message = true;

            while (true) {
                try {
                    coremailservicio.setMailporcampana(new HashMap<>());
                    //abriendo cuentas de correo de entrada
                    coremailservicio.ObtenerConfiguracionMail(); // lee las configuraciones por campaña
                    System.out.println("[IN] CANTIDAD COLAS CON MAIL ACTIVO " + coremailservicio.getMailporcampana().size());
                    System.out.println("::::LEYENDO CUENTAS DE CORREO:::::::");
                    for (MailAjustes mailajustes : coremailservicio.getMailporcampana().values()) {
                        System.out.println("shost => " + mailajustes.getHost());
                        System.out.println("puerto => " + mailajustes.getPuerto());
                        Parametros.USUARIO_MAIL_INBOUND = mailajustes.getUser();
                        Parametros.PWD_MAIL_INBOUND = mailajustes.getPass();
                        Parametros.HOST_MAIL_INBOUND = mailajustes.getHost();
                        Parametros.STORE_INBOUND = mailajustes.getStore();
                        String usuario = Parametros.USUARIO_MAIL_INBOUND;
                        String clave = Parametros.PWD_MAIL_INBOUND;
                        String popHost = Parametros.HOST_MAIL_INBOUND;
                        Parametros.MAXIMO_PESO_ADJUNTO_INBOUND = mailajustes.getMaximo_adjunto();
                        Properties props = System.getProperties();
                        props.put("mail.smtp.port", mailajustes.getPuerto());
                        props.put("mail.smtp.host", mailajustes.getHost());
                        props.setProperty("mail.smtp.auth", "true");
                        props.setProperty("mail.smtp.starttls.enable", "true");
                        props.setProperty("mail.store.protocol", "imaps");
                        props.setProperty("mail.imap.starttls.enable", "true");
                           System.out.println("[IN] " + popHost + "," + usuario + "," + clave + "," + Parametros.STORE_INBOUND);
                        LinkedList<Mail> listamails = coremailservicio.getListamails();
                        try {

                            final PasswordAuthentication auth = new PasswordAuthentication(usuario, clave);
                            Session sesion = Session.getInstance(props, new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return auth;
                                }
                            });

                            System.out.println("STORE INBOUND => " + Parametros.STORE_INBOUND);
                            System.out.println("mail_setup.getHost() => " + mailajustes.getHost());
                            System.out.println("mail_setup.getUser() => " + mailajustes.getUser());
                            System.out.println("mail_setup.getPass() => " + mailajustes.getPass());

                            store = sesion.getStore(Parametros.STORE_INBOUND);

                            store.connect(mailajustes.getHost(), mailajustes.getUser(), mailajustes.getPass());
                            folder = store.getFolder("INBOX");
                            folder.open(Folder.READ_WRITE);

                            int id = 0;
                            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                            Message msg[] = folder.search(ft);
                            int count = 0;
                            System.out.println("............................ MENSAJES : " + msg.length);
                            read_message:
                            for (Message mensaje : msg) {

                                if (mensaje.getSubject() != null && mensaje.getSubject().equalsIgnoreCase(mailajustes.getUser())) {
                                    System.out.println("***** EL CLIENTE Y EL SERVIDOR SON EL MISMO.... IGNORANDO");
                                    continue;
                                }

                                new_message = true;
                                for (Cola queue : mailajustes.getColas()) {
                                    id = coremailservicio.ObtenerNuevoId(true, mailajustes.getId(), queue.getId_cola());
                                    Mail mail = new Mail(id);
                                    mail.setIdconfiguracion(mailajustes.getId());//setIdconfiguracion
                                    mail.setCola(queue);
                                    mail.setTipo("IN");
                                    mail.setCampana(queue.getId_campana());
                                    if (mensaje.getSubject() != null) {
                                        mail.setSubject(mensaje.getSubject().trim());
                                    }
                                    String remitente = null;
                                    for (Address froms : mensaje.getFrom()) {
                                        // System.out.println("from : " + froms);
                                        if (froms.toString().contains("<")) {
                                            remitente = froms.toString().split("<")[1];

                                        } else {
                                            remitente = froms.toString();
                                        }
                                        remitente = remitente.replace(">", "");
                                        remitente = remitente.trim();
                                    }
                                    mail.setRemitente(remitente);
                                    // mensaje.setFlag(Flag.DELETED, true);
                                    if (!Utilidades.createFileHTML(mensaje, mail, true)) {
                                        System.out.println("FALLO EN EL CREATE HTML");
                                        mensaje.setFlag(Flags.Flag.SEEN, true);
                                        continue;
                                    }

                                    if (mail.getPeso_adjunto() > mailajustes.getMaximo_adjunto()) {
                                        FileUtils.deleteDirectory(new File(mail.getRuta()));
                                        //MailAutomatico automatico = new MailAutomatico(mail);
                                        mailautomatico.enviarEmail(mail);
                                        coremailservicio.eliminarEmailOnline(mail.getId());
                                        continue;
                                    }

                                    if (remitente == null) {
                                        System.out.println("REMITENTE NO CAPTURADO");
                                        mensaje.setFlag(Flags.Flag.SEEN, true);
                                        continue;
                                    }
                                    remitente = remitente.replace("\n", "");
                                    System.out.println("REMITENTE CAPTURADO " + remitente);
                                    mail.setRemitente(remitente.trim());
                                    Calendar hoy = Calendar.getInstance();
                                    mail.setFecha_index(formato.format(hoy.getTime()));
                                    listamails.add(mail);
                                    coremailservicio.guardarMail(mail, coremailservicio.anadirMails(queue));
                                    mensaje.setFlag(Flags.Flag.SEEN, true);

                                }
                                count += 1;
                                if (count == 20) {
                                    break read_message;
                                }
                            }

                        } catch (Exception ex) {
                            Utilidades.printException(ex);
                            ex.printStackTrace();
                        } finally {

                            try {
                                if (folder != null) {
                                    folder.close(true);
                                }
                            } catch (MessagingException ex) {
                                Utilidades.printException(ex);
                                ex.printStackTrace();
                            }
                            try {
                                if (store != null) {
                                    store.close();
                                }
                            } catch (MessagingException ex) {
                                Utilidades.printException(ex);
                                ex.printStackTrace();
                            }
                        }

                        if (new_message) {
                            Listar_Capturers(listamails);
                            new_message = false;
                            listamails.clear();
                            listamails = null;
                        } else {
                            System.out.print(".");
                        }

                    }
                    System.out.println("::::FIN LECTURA DE CUENTAS:::::::");
                    Thread.sleep(1000 * 5);
                } catch (Exception ex) {
                    Utilidades.printException(ex);
                    ex.printStackTrace();
                }

            }
        } catch (Exception ex) {
            Utilidades.printException(ex);
            ex.printStackTrace();
        } finally {
        }

    }

    public void Listar_Capturers(LinkedList<Mail> mails) {
        System.out.println("*******************");

        for (Mail mail : mails) {
            System.out.println(mail.getId() + " _ " + mail.getRemitente() + " / " + mail.getSubject() + " / " + mail.getFecha_index() + " /queue" + mail.getCola().getId_cola());
        }
        System.out.println("*******************");

    }

    public void continuar() {
        synchronized (this) {
            this.notifyAll();
        }
    }

    class GMailAuthenticator extends Authenticator {

        String user;
        String pw;

        public GMailAuthenticator(String username, String password) {
            super();
            this.user = username;
            this.pw = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, pw);
        }
    }
}
