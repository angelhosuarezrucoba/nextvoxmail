/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netvox.mail.Api.entidadessupervisor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author desarrollo5
 */
public class Grafico {

    private int max;
    private List<Integer> valores_entrantes;
    private List<Integer> valores_agentes;
    private List<Integer> valores_abandonadas;
    private List<Integer> valores_contestadas;
    private List<String> valores_rango;

    public Grafico() {
        valores_entrantes = new ArrayList<>();
        valores_agentes = new ArrayList<>();
        valores_abandonadas = new ArrayList<>();
        valores_contestadas = new ArrayList<>();
        valores_rango = new ArrayList<>();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public List<Integer> getValores_entrantes() {
        return valores_entrantes;
    }

    public void setValores_entrantes(List<Integer> valores_entrantes) {
        this.valores_entrantes = valores_entrantes;
    }

    public List<Integer> getValores_agentes() {
        return valores_agentes;
    }

    public void setValores_agentes(List<Integer> valores_agentes) {
        this.valores_agentes = valores_agentes;
    }

    public List<Integer> getValores_abandonadas() {
        return valores_abandonadas;
    }

    public void setValores_abandonadas(List<Integer> valores_abandonadas) {
        this.valores_abandonadas = valores_abandonadas;
    }

    public List<Integer> getValores_contestadas() {
        return valores_contestadas;
    }

    public void setValores_contestadas(List<Integer> valores_contestadas) {
        this.valores_contestadas = valores_contestadas;
    }

    public List<String> getValores_rango() {
        return valores_rango;
    }

    public void setValores_rango(List<String> valores_rango) {
        this.valores_rango = valores_rango;
    }
}
