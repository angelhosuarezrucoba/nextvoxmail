/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.controladores;

import com.netvox.mail.entidades.Rutas;
import com.netvox.mail.entidades.Prueba;
import com.netvox.mail.servicios.ClienteMongoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("")
public class IndexControlador {

    @Autowired
    @Qualifier("clientemongoservicio")
    ClienteMongoServicio clientemongoservicio;

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelo = new ModelAndView("index");
        //modelo.addObject("usuario", usuarioservicio.obtenerUsuario());
        MongoOperations mongoops = clientemongoservicio.clienteMongo();
        return modelo;
    }

}
