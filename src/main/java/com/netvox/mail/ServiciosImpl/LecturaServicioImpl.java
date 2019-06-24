/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.ServiciosImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("lecturaservicio")
public class LecturaServicioImpl {

    private FileReader fr;
    private BufferedReader br;
    private File file;
    private String cad;
    Logger log = LoggerFactory.getLogger(this.getClass());

    public LecturaServicioImpl() {
        try {
            //file = new File("C:\\Users\\desarrollo5\\Desktop\\netvox\\configuracion.txt");
            file = new File("/usr/local/Tomcat8.0/webapps/configuracion.txt");
            fr = new FileReader(file);
            br = new BufferedReader(fr);
        } catch (FileNotFoundException ex) {
            log.error("error en el metodo LecturaServicioImpl", ex);
        }
    }

    public List<String> obtenerConfiguraciones() {
        List<String> lista = new ArrayList<>();
        try {
            while ((cad = br.readLine()) != null) {
                lista.add(cad.substring(0, cad.indexOf(';')).trim());
            }
            br.close();
            fr.close();
        } catch (IOException ex) {
            log.error("error en el metodo obtenerConfiguraciones , clase LecturaServicioImpl", ex);
        }
        return lista;
    }
}
