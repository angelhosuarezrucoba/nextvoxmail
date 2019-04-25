/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api;



import java.text.SimpleDateFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis")
public class Api {

//    @Autowired
//    private SimpMessagingTemplate template;

    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


//
//    @GetMapping("prueba")
//    public void mail() {
//        Prueba ejemplo = new Prueba();
//        ejemplo.setTexto("esta es una prueba");
//        Mapa.getMapa().get(18).forEach((sesion) -> {
//            this.template.convertAndSend("/mailcore/respuestas-" + sesion, ejemplo);
//        });
//    }
//
//    @GetMapping("todos")
//    public void todos() {
//        Prueba ejemplo = new Prueba();
//        ejemplo.setTexto("esta es una prueba para todos");
//        this.template.convertAndSend("/mailcore/respuestas", ejemplo);
//    }
//   @MessageMapping("/prueba") // este es el destino al que se envian los mensajes y lo redirige a controlmensajes
//    public void enviar(Prueba mensaje) {
//        Mapa.getMapa().get(18).forEach((sesion) -> {
//            this.template.convertAndSend("/mailcore/respuestas-" + sesion, mensaje);
//        });
//
//    }
}
