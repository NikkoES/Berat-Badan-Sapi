package com.hello.beratbadansapi.model;

import java.io.Serializable;

public class DataSet implements Serializable{

    String umur, jenisKelamin;
    int tinggiBadan, lebarDada, panjangBadan, bobotBadan;

    public DataSet(String umur, String jenisKelamin, int tinggiBadan, int lebarDada, int panjangBadan, int bobotBadan) {
        this.umur = umur;
        this.jenisKelamin = jenisKelamin;
        this.tinggiBadan = tinggiBadan;
        this.lebarDada = lebarDada;
        this.panjangBadan = panjangBadan;
        this.bobotBadan = bobotBadan;
    }

    public String getUmur() {
        return umur;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public int getTinggiBadan() {
        return tinggiBadan;
    }

    public int getLebarDada() {
        return lebarDada;
    }

    public int getPanjangBadan() {
        return panjangBadan;
    }

    public int getBobotBadan() {
        return bobotBadan;
    }
}
