/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import java.util.HashMap;
import org.springframework.stereotype.Service;

@Service("coremailservicio")
public class CoreMailServicioImpl {

    public int hola = 0;
    public volatile HashMap<Integer, Integer> capturasporcola = new HashMap<Integer, Integer>();
    public volatile HashMap<Integer, Integer> asignadosporcola = new HashMap<Integer, Integer>();
    public volatile HashMap<Integer, Integer> respondidosporcola = new HashMap<Integer, Integer>();

    public void imprimir(int hola) {
        this.hola += hola;
        System.out.println(this.hola);
    }

}
