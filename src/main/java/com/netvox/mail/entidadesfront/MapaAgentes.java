/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

import java.util.HashMap;
import java.util.List;

public class MapaAgentes {

    private static HashMap<Integer, List<Agente>> mapadeagentes = new HashMap<>();

    public static HashMap<Integer, List<Agente>> getMapa() {
        return mapadeagentes;
    }

    public static void setMapa(HashMap<Integer, List<Agente>> aMapa) {
        mapadeagentes = aMapa;
    }

}
