/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Cola;
import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.MailAjustes;
import com.netvox.mail.servicios.ClienteMongoServicio;
import com.netvox.mail.utilidades.FormatoDeFechas;
import java.io.File;
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
import com.netvox.mail.utilidades.Utilidades;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    @Qualifier("utilidades")
    Utilidades utilidades;

    private FormatoDeFechas formato = new FormatoDeFechas();
    private Store store = null;
    private Folder folder = null;
    private List<Mail> listamails;
    private Properties props;
    private Session sesion;
    private int id;
    private List<Message> listacorreosnoleidos;
    private boolean activo = true;

    @Override
    public void run() {
        while (isActivo()) {
            try {
                coremailservicio.llenarListaAjustesMail(); // lee las configuraciones por campaña y setea emailporcampana
                System.out.println("----------------------------------------------------------------------------------------");
                System.out.println("LEYENDO CUENTAS DE CORREO");
                System.out.println("COLAS CON MAIL ACTIVO " + coremailservicio.getMapadeajustesmail().size()); //en este punto ya se tiene con el metodo anterior los mails por campaña
                
                
                
                
               
                
                
                
                for (MailAjustes mailajustes : coremailservicio.getMapadeajustesmail().values()) {
                    System.out.println("shost => " + mailajustes.getHost());
                    System.out.println("puerto => " + mailajustes.getPuerto());
                    props = ObtenerPropiedades(mailajustes.getHost(), mailajustes.getPuerto());
                    listamails = new ArrayList<>(); //solo se usa para imprimir la lista de mails.
                    try {
                        System.out.println("STORE INBOUND => " + mailajustes.getStore());
                        System.out.println("mail_setup.getHost() => " + mailajustes.getHost());
                        System.out.println("mail_setup.getUser() => " + mailajustes.getUser());
                        System.out.println("mail_setup.getPass() => " + mailajustes.getPass());
                        sesion = obtenerSesion(props, mailajustes.getUser(), mailajustes.getPass());
                        store = sesion.getStore(mailajustes.getStore());
                        store.connect(mailajustes.getHost(), mailajustes.getUser(), mailajustes.getPass());
                        folder = store.getFolder("INBOX");
                        folder.open(Folder.READ_WRITE);
                        id = 0; //inicializa el id de la cola
                        listacorreosnoleidos = obtenerCorreosNoLeidos(folder, 20);//el segundo parametro indica cuantos correos debo tomar.
                        System.out.println("CORREOS NO LEIDOS : " + listacorreosnoleidos.size());
                        for (Message mensaje : listacorreosnoleidos) {
                            if (mensaje.getSubject() != null && mensaje.getSubject().equalsIgnoreCase(mailajustes.getUser())) {
                                System.out.println("***** EL CLIENTE Y EL SERVIDOR SON EL MISMO.... IGNORANDO");
                            } else {
                                for (Cola cola : mailajustes.getColas()) {//se debe buscar un mejor nombre, la consulta encuentra una clase mail y cada mail tiene una lista de colas
                                    id = coremailservicio.obtenerNuevoId("entrada", mailajustes.getId(), cola.getId_cola());
                                    Mail mail = new Mail();
                                    mail.setIdconfiguracion(mailajustes.getId());//setIdconfiguracion
                                    mail.setCola(cola);
                                    mail.setId_cola(cola.getId_cola());
                                    mail.setIdcorreo(id);
                                    mail.setCampana(cola.getId_campana());
                                    if (mensaje.getSubject() != null) {
                                        mail.setAsunto(mensaje.getSubject().trim());
                                    }
                                    String remitente = null;
                                    for (Address froms : mensaje.getFrom()) {
                                        if (froms.toString().contains("<")) {
                                            remitente = froms.toString().split("<")[1];
                                        } else {
                                            remitente = froms.toString();
                                        }
                                        remitente = remitente.replace(">", "");
                                        remitente = remitente.trim();
                                    }
                                    mail.setRemitente(remitente);
                                    if (!utilidades.createFileHTML(mensaje, mail, mailajustes.getMaximo_adjunto())) {
                                        System.out.println("FALLO EN EL CREATE HTML");
                                        mensaje.setFlag(Flags.Flag.SEEN, true);
                                        continue;
                                    }
                                    if (mail.getPeso_adjunto() > mailajustes.getMaximo_adjunto()) {
                                        FileUtils.deleteDirectory(new File(mail.getRuta()));
                                        mailautomatico.enviarEmail(mail);
                                        coremailservicio.eliminarEmailOnline(mail.getIdcorreo());
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
                                    mail.setFecha_ingreso(formato.convertirFechaString(new Date(), formato.FORMATO_FECHA_HORA_SLASH));//esto solo le facilita al front el manejo de fecha
                                    mail.setDestino(mailajustes.getUser());
                                    listamails.add(mail);
                                    Mail nuevomail = coremailservicio.guardarMail(mail);
                                    mail.setListadeadjuntos(nuevomail.getListadeadjuntos());
                                    coremailservicio.notificarCorreoNuevoEnCola(mail);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        cerrarFolderYstore(folder, store);
                    }
                    listamails.forEach((mail) -> {
                        System.out.println(mail.toString());
                    });
                    listamails.clear();
                }
                System.out.println("FIN LECTURA DE CUENTAS");
                System.out.println("----------------------------------------------------------------------------------------");
                Thread.sleep(1000 * 5);
            } catch (Exception ex) {
                utilidades.printException(ex);
                ex.printStackTrace();
            }
        }
    }

    public Properties ObtenerPropiedades(String host, int puerto) {
        Properties props = System.getProperties();
        props.put("mail.smtp.port", puerto);
        props.put("mail.smtp.host", host);
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.starttls.enable", "true");
        return props;
    }

    public Session obtenerSesion(Properties props, String usuario, String clave) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });
    }

    public List<Message> obtenerCorreosNoLeidos(Folder folder, int limitecorreos) {
        List<Message> lista = new ArrayList<>();
        try {
            FlagTerm flagterm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            Message[] mensajes = folder.search(flagterm);
            if (mensajes.length > 0) {
                for (Message mensaje : mensajes) {
                    lista.add(mensaje);
                }
                lista = lista.stream().limit(limitecorreos).collect(Collectors.toList());
            }
        } catch (MessagingException ex) {
            ex.printStackTrace();
            utilidades.printException(ex);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return lista;
    }

    public void cerrarFolderYstore(Folder folder, Store store) {
        try {
            if (folder != null) {
                folder.close(true);
            }
            if (store != null) {
                store.close();
            }
        } catch (MessagingException ex) {
            utilidades.printException(ex);
            ex.printStackTrace();
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}
