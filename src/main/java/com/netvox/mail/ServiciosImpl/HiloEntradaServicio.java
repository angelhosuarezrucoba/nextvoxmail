/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.Mail;
import com.netvox.mail.entidades.CuentaDeCorreo;
import com.netvox.mail.servicios.ClienteMongoServicio;
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
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Store store = null;
    private Folder folder = null;
    private Properties props;
    private Session sesion;
    private List<Message> listacorreosnoleidos;
    private boolean activo = true;
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run() {
        while (isActivo()) {
            try {
                log.info("----------------------------------------------------------------------------------------");
                log.info("LEYENDO CUENTAS DE CORREO");
                List<CuentaDeCorreo> listadecuentasdecorreo = coremailservicio.obtenerCuentasDeCorreo();
                for (CuentaDeCorreo cuentadecorreo : listadecuentasdecorreo) {
                    log.info("cuentadecorreo : " + cuentadecorreo.getUsuario());
                    props = ObtenerPropiedades(cuentadecorreo.getHost(), cuentadecorreo.getPuerto());
                    sesion = obtenerSesion(props, cuentadecorreo.getUsuario(), cuentadecorreo.getClave());
                    store = sesion.getStore(cuentadecorreo.getStore());
                    store.connect(cuentadecorreo.getHost(), cuentadecorreo.getUsuario(), cuentadecorreo.getClave());
                    folder = store.getFolder("INBOX");
                    folder.open(Folder.READ_WRITE);
                    listacorreosnoleidos = obtenerCorreosNoLeidos(folder, 20);//el segundo parametro indica cuantos correos debo tomar.
                    for (Message mensaje : listacorreosnoleidos) {
                        String remitente = obtenerRemitente(mensaje);
                        if (remitente.equals("")) {
                            log.info("REMITENTE NO CAPTURADO");
                        } else {

                            List<String> listacopia = new ArrayList<>();
                            List<String> listacopiaoculta = new ArrayList<>();
                            Address[] copia = mensaje.getRecipients(Message.RecipientType.CC) == null
                                    ? new Address[0] : mensaje.getRecipients(Message.RecipientType.CC);
                            Address[] copiaoculta = mensaje.getRecipients(Message.RecipientType.BCC) == null
                                    ? new Address[0] : mensaje.getRecipients(Message.RecipientType.BCC);
                            for (Address correo : copia) {
                                listacopia.add(correo.toString());
                            }
                            for (Address correo : copiaoculta) {
                                listacopiaoculta.add(correo.toString());
                            }
                            Mail mail = coremailservicio.insertarNuevoMail(
                                    cuentadecorreo.getIdconfiguracion(),
                                    cuentadecorreo.getId_cola(),
                                    cuentadecorreo.getNombre_cola(),
                                    cuentadecorreo.getId_campana(),
                                    mensaje.getSubject() == null ? "" : mensaje.getSubject(),
                                    remitente,/*esto es el destino*/
                                    cuentadecorreo.getUsuario(),
                                    listacopia, listacopiaoculta
                            );
                            if (!utilidades.createFileHTML(mensaje, mail, cuentadecorreo.getMaximo_adjunto())) {//aqui ya le tengo puesto el mensaje y los adjuntos
                                log.info("FALLO EN EL CREATE HTML");
                            } else {
                                if (mail.getPeso_adjunto() > cuentadecorreo.getMaximo_adjunto()) {
                                    FileUtils.deleteDirectory(new File(mail.getRuta()));
                                    mailautomatico.enviarEmail(mail);
                                } else {
                                    Mail nuevomail = coremailservicio.ActualizarMail(mail);
                                    coremailservicio.notificarCorreoNuevoEnCola(nuevomail);
                                }
                            }
                        }
                        mensaje.setFlag(Flags.Flag.SEEN, true);
                    }
                    cerrarFolderYstore(folder, store);
                }                
                log.info("FIN LECTURA DE CUENTAS");
                log.info("----------------------------------------------------------------------------------------");
                Thread.sleep(1000 * 30);
            } catch (Exception ex) {
                log.error("error en el HiloEntradaServicio", ex);
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
            log.error("error en el metodo obtenerCorreosNoLeidos", ex);
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
            log.error("error en el metodo cerrarFolderYstore", ex);
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    private String obtenerRemitente(Message mensaje) {
        String remitente = "";
        try {
            for (Address froms : mensaje.getFrom()) {
                if (froms.toString().contains("<")) {
                    remitente = froms.toString().split("<")[1];
                } else {
                    remitente = froms.toString();
                }
                remitente = remitente.replace(">", "");
                remitente = remitente.trim();
                remitente = remitente.replace("\n", "");
            }
        } catch (MessagingException ex) {
            log.error("error en el metodo obtenerRemitente", ex);
        }
        return remitente;
    }

}
