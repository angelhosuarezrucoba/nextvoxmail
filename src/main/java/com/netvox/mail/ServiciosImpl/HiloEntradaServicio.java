/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.entidades.Prueba;
import com.netvox.mail.servicios.ClienteMongoServicio;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.search.FlagTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

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

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void run() {
        try {

            boolean new_message = true;

            while (true) {

                try {
                    //  main.email_by_campana = new HashMap<String, MailAjustes>();
                    coremailservicio.setMailporcampana(new HashMap<>());
                    //abriendo cuentas de correo de entrada
                    managerbd.executeGetMailsSetup(main.email_by_campana); // lee las configuraciones por campaña
                    System.out.println("[IN] CANTIDAD COLAS CON MAIL ACTIVO " + main.email_by_campana.size());
                    System.out.println("::::LEYENDO CUENTAS DE CORREO:::::::");
                    for (MailSetupBean mail_setup : main.email_by_campana.values()) {
                        System.out.println("shost => " + mail_setup.getHost());
                        System.out.println("puerto => " + mail_setup.getPuerto());
                        Parametros.USUARIO_MAIL_INBOUND = mail_setup.getUser();
                        Parametros.PWD_MAIL_INBOUND = mail_setup.getPass();
                        Parametros.HOST_MAIL_INBOUND = mail_setup.getHost();
                        Parametros.STORE_INBOUND = mail_setup.getStore();

                        String usuario = Parametros.USUARIO_MAIL_INBOUND;
                        String clave = Parametros.PWD_MAIL_INBOUND;

                        String popHost = Parametros.HOST_MAIL_INBOUND;
                        Parametros.MAXIMO_PESO_ADJUNTO_INBOUND = mail_setup.getMaximo_adjunto();
                        Properties props = System.getProperties();
                        props.put("mail.smtp.port", mail_setup.getPuerto());
                        props.put("mail.smtp.host", mail_setup.getHost());
                        props.setProperty("mail.smtp.auth", "true");
                        props.setProperty("mail.smtp.starttls.enable", "true");
                        props.setProperty("mail.store.protocol", "imaps");
                        // props.setProperty("mail.store.protocol", "imaps");
                        props.setProperty("mail.imap.starttls.enable", "true");
                        // props.setProperty("ssl.SocketFactory.provider", "my.package.name.ExchangeSSLSocketFactory");
                        // props.setProperty("mail.imap.socketFactory.class", "my.package.name.ExchangeSSLSocketFactory");

                        System.out.println("[IN] " + popHost + "," + usuario + "," + clave + "," + Parametros.STORE_INBOUND);
                        LinkedList<MailBean> capturers = main.capturers;
                        try {

                            final PasswordAuthentication auth = new PasswordAuthentication(usuario, clave);
                            Session sesion = Session.getInstance(props, new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return auth;
                                }
                            });

                            //sesion.setDebug(true);
                            System.out.println("STORE INBOUND => " + Parametros.STORE_INBOUND);
                            System.out.println("mail_setup.getHost() => " + mail_setup.getHost());
                            System.out.println("mail_setup.getUser() => " + mail_setup.getUser());
                            System.out.println("mail_setup.getPass() => " + mail_setup.getPass());

                            store = sesion.getStore(Parametros.STORE_INBOUND);

                            store.connect(mail_setup.getHost(), mail_setup.getUser(), mail_setup.getPass());
                            folder = store.getFolder("INBOX");
                            folder.open(Folder.READ_WRITE);

                            int id = 0;
                            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                            Message msg[] = folder.search(ft);
                            // for (Message mensaje : folder.getMessages()) {
                            int count = 0;
                            System.out.println("............................ MENSAJES : " + msg.length);
                            read_message:
                            for (Message mensaje : msg) {

                                if (mensaje.getSubject() != null && mensaje.getSubject().equalsIgnoreCase(mail_setup.getUser())) {
                                    System.out.println("***** EL CLIENTE Y EL SERVIDOR SON EL MISMO.... IGNORANDO");
                                    continue;
                                }

                                new_message = true;
                                for (ColaBean queue : mail_setup.getColas()) {
                                    id = managerbd.executeGetNewId(true, mail_setup.getId(), queue.getId_cola());
                                    MailBean mail = new MailBean(id);
                                    mail.setIdconfiguracion(mail_setup.getId());
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

                                    if (mail.getPeso_adjunto() > mail_setup.getMaximo_adjunto()) {
                                        FileUtils.deleteDirectory(new File(mail.getRuta()));
                                        MailAutomatico automatico = new MailAutomatico(mail);
                                        automatico.enviarEmail();
                                        managerbd.eliminarEmailOnline(mail.getId());
                                        continue;
                                    }

                                    if (remitente == null) {
                                        System.out.println("REMITENTE NO CAPTURADO");
                                        // mensaje.setFlag(Flag.DELETED, true);
                                        mensaje.setFlag(Flags.Flag.SEEN, true);
                                        continue;
                                    }
                                    remitente = remitente.replace("\n", "");
                                    System.out.println("REMITENTE CAPTURADO " + remitente);
                                    mail.setRemitente(remitente.trim());
                                    Calendar hoy = Calendar.getInstance();
                                    mail.setFecha_index(formato.format(hoy.getTime()));
                                    capturers.add(mail);
                                    managerbd.executeGuardarMail(mail, main.add_capturers(queue));
                                    // mensaje.setFlag(Flag.DELETED, true);

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
                            Listar_Capturers(capturers);
                            new_message = false;
                            capturers.clear();
                            capturers = null;
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

    public void Listar_Capturers(LinkedList<MailBean> mails) {
        System.out.println("*******************");

        for (MailBean mail : mails) {
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
