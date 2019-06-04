package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.RespuestaIdentificador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service("verificadordesesionservicio")
public class VerificadorDeSesionServicioImpl {

    RestTemplate resttemplate = new RestTemplate();
    Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean sesionvalida(String identificador) {
        boolean valido = false;
        try {
            RespuestaIdentificador respuesta = resttemplate.postForObject("http://192.168.10.206:8080/autenticacion/consultaridentificador", identificador, RespuestaIdentificador.class);
            valido = true;
        } catch (HttpClientErrorException e) {
            log.error("error en el metodo sesionvalida", e);
        }
        return valido;
    }

}
