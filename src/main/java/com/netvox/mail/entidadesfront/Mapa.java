/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

import java.util.HashMap;
import java.util.List;

public class Mapa {

    private static HashMap<Integer, List<String>> mapa = new HashMap<>();

    public static HashMap<Integer, List<String>> getMapa() {
        return mapa;
    }

    public static void setMapa(HashMap<Integer, List<String>> aMapa) {
        mapa = aMapa;
    }

  

}
