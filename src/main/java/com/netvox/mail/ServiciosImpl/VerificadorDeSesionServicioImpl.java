package com.netvox.mail.ServiciosImpl;

import com.netvox.mail.entidades.RespuestaIdentificador;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service("verificadordesesionservicio")
public class VerificadorDeSesionServicioImpl {

    RestTemplate resttemplate = new RestTemplate();

    public boolean sesionvalida(String identificador) {
        boolean valido = false;
        try {
            RespuestaIdentificador respuesta = resttemplate.postForObject("http://192.168.10.206:8080/autenticacion/consultaridentificador", identificador, RespuestaIdentificador.class);
            valido = true;
        } catch (HttpClientErrorException e) {
            System.out.println("error en la sesion");
            e.printStackTrace();
        }
        return valido;
    }

}
