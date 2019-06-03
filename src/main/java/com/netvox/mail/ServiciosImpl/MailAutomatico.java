/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.MailConfiguracion;
import com.netvox.mail.entidades.Mail;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author Home
 */
@Service("mailautomatico")
public class MailAutomatico {

    Mail mail;

    @Autowired
    @Qualifier("coremailservicio")
    CoreMailServicioImpl coremailservicio;

    Logger log = LoggerFactory.getLogger(this.getClass());

    public MailAutomatico() {
    }

    public void enviarEmail(Mail mail) {
        MailConfiguracion email = coremailservicio.ObtenerMailConfiguracion(mail.getIdconfiguracion());
        String smtpServer = email.getSmtp();
        String smtpUser = email.getUsuario();
        String smtpPass = email.getPass();
        int smtpPort = email.getPuertosalida();

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", smtpServer);
        props.setProperty("mail.user", smtpUser);
        props.setProperty("mail.smtp.port", smtpPort + "");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.password", smtpPass);
        props.setProperty("mail.smtp.auth", "true");

        try {
            final PasswordAuthentication auth = new PasswordAuthentication(smtpUser, smtpPass);
            Session mailSession = Session.getInstance(props, new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return auth;
                }
            });
            MimeMessage message = new MimeMessage(mailSession);
            message.setSubject("RESPUESTA AUTOMATICA DEL SERVIDOR");

            String mensaje = "AUTORESPUESTA TAMAÑO DE EMAIL DEMASIADO GRANDE "
                    + "SE SUPERÓ EL TAMAÑO PERMITIDO " + email.getMax_adjunto() + "MB";
            message.setText(mensaje);
            message.setFrom(new InternetAddress(email.getUsuario()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getRemitente()));

            Transport.send(message);
            System.out.println("SE ENVIO EMAIL DE AUTORESPUESTA");
        } catch (MessagingException ex) {
            log.error("error en el metodo enviarEmail , clase MailAutomatico", ex.getCause());
        }
    }
}
