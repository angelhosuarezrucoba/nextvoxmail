/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.servicios;

import com.netvox.mail.Api.entidadessupervisor.AtendidosPorCola;
import com.netvox.mail.Api.entidadessupervisor.Contenido;
import com.netvox.mail.Api.entidadessupervisor.FiltroIndividual;
import com.netvox.mail.Api.entidadessupervisor.Grafico;
import com.netvox.mail.Api.entidadessupervisor.Pausa;
import com.netvox.mail.Api.entidadessupervisor.ReporteGrupal;
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

    public String abrirCorreo(MailInbox mailconsultainbox);

    public List<MailInbox> listarCorreosEnCola(Mensaje mensaje);

    public String autoAsignarse(Mensaje mensaje);

    public MailInbox crearCorreo(MailSalida mailsalida);

    public void adjuntarcorreo(MultipartFile archivo, int idagente);

    public Mensaje enviarcorreo(MailSalida mailsalida);

    public Mensaje tipificarCorreo(MailSalida mailsalida);

    public List<Tipificacion> listarTipificaciones();

    public List<MailSalida> listarCorreosPendientes(FiltroIndividual filtro);

    public List<MailSalida> listarCorreoInvalidos(FiltroIndividual filtro);

    public List<MailSalida> listarCorreos(FiltroIndividual filtro);

    public List<Pausa> detalleTiemposeEnPausa(FiltroIndividual filtro);

    public List<ReporteGrupal> detalleGrupalDeCorreosPorDias(FiltroIndividual filtro);

    public List<ReporteGrupal> detalleGrupalDeCorreosPorHoras(FiltroIndividual filtro);

    public List<ReporteGrupal> detalleGrupalDeCorreosPorAgente(FiltroIndividual filtro);

    public List<MailSalida> detalleGrupalDeCorreosPorCola(FiltroIndividual filtro);

    public Grafico graficoMultiCanal(Contenido contenido);

    public Grafico reporteMensual(Contenido contenido);

    public List<AtendidosPorCola> atendidosPorCola(Contenido contenido);


}
