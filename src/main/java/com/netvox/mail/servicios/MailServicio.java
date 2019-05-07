/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.servicios;


import com.netvox.mail.Api.entidadessupervisor.FiltroIndividual;
import com.netvox.mail.entidadesfront.MailInbox;
import com.netvox.mail.entidadesfront.MailSalida;
import com.netvox.mail.entidadesfront.Mensaje;
import com.netvox.mail.entidadesfront.Tipificacion;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author desarrollo5
 */
public interface MailServicio {
    
    public abstract List<MailInbox> listarCorreos(Mensaje mensaje);

    public String obtenerContenidoMail(MailInbox mailconsultainbox);

    public List<MailInbox> listarCorreosEnCola(Mensaje mensaje);
    
    public void autoAsignarse(Mensaje mensaje);
    
    public MailInbox crearCorreo(MailSalida mailsalida);

    public void adjuntarcorreo(MultipartFile archivo,int idagente);

    public void enviarcorreo(MailSalida mailsalida);

    public void tipificarCorreo(MailSalida mailsalida);

    public  List<Tipificacion> listarTipificaciones();

    public List<MailSalida> listarCorreosSupervisor(FiltroIndividual filtro);


    
}
