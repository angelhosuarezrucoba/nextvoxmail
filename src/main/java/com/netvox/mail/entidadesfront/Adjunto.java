/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.entidadesfront;

/**
 *
 * @author desarrollo5
 */
public class Adjunto {

    private String file_name;
    private int file_size;
    private String file_url;

    public Adjunto(String file_name, int file_size, String file_url) {
        this.file_name = file_name;
        this.file_size = file_size;
        this.file_url = file_url;
    }

    public Adjunto() {
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public int getFile_size() {
        return file_size;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

}
