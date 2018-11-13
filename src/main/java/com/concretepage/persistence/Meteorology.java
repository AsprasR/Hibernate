package com.concretepage.persistence;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="Meteorology")//, schema = "targetSchemaName")
public class Meteorology implements Comparable<Meteorology> {
    @Id
    @Column(name = "id_stacji", nullable = false)
    private int id_stacji;

    @Column(name="stacja", nullable = false)
    private String stacja;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="data_pomiaru", nullable = false)
    private Date data_pomiaru;

    @Column(name="temperatura")
    private Float temperatura;

    @Column(name="predkosc_wiatru")
    private Integer predkosc_wiatru;

    @Column(name="kierunek_wiatru")
    private Integer kierunek_wiatru;

    @Column(name="wilgotnosc_wzgledna")
    private Float wilgotnosc_wzgledna;

    @Column(name="suma_opadu")
    private Double suma_opadu;

    @Column(name="cisnienie")
    private Float cisnienie;

    public Meteorology(){}

    public Meteorology(int id_stacji, String stacja, Date data_pomiaru, Float temperatura,
                       Integer predkosc_wiatru, Integer kierunek_wiatru, Float wilgotnosc_wzgledna,
                       Double suma_opadu, Float cisnienie) {
        this.setId_stacji(id_stacji);
        this.setStacja(stacja);
        this.setData_pomiaru(data_pomiaru);
        this.setTemperatura(temperatura);
        this.setPredkosc_wiatru(predkosc_wiatru);
        this.setKierunek_wiatru(kierunek_wiatru);
        this.setWilgotnosc_wzgledna(wilgotnosc_wzgledna);
        this.setSuma_opadu(suma_opadu);
        this.setCisnienie(cisnienie);
    }

    public int getId_stacji() {
        return id_stacji;
    }

    public void setId_stacji(int id_stacji) {
        this.id_stacji = id_stacji;
    }

    public String getStacja() {
        return stacja;
    }

    public void setStacja(String stacja) {
        this.stacja = stacja;
    }

    public Date getData_pomiaru() {
        return data_pomiaru;
    }

    public void setData_pomiaru(Date data_pomiaru) {
        this.data_pomiaru = data_pomiaru;
    }

    public Float getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Float temperatura) {
        this.temperatura = temperatura;
    }

    public Integer getPredkosc_wiatru() {
        return predkosc_wiatru;
    }

    public void setPredkosc_wiatru(Integer predkosc_wiatru) {
        this.predkosc_wiatru = predkosc_wiatru;
    }

    public Integer getKierunek_wiatru() {
        return kierunek_wiatru;
    }

    public void setKierunek_wiatru(Integer kierunek_wiatru) {
        this.kierunek_wiatru = kierunek_wiatru;
    }

    public Float getWilgotnosc_wzgledna() {
        return wilgotnosc_wzgledna;
    }

    public void setWilgotnosc_wzgledna(Float wilgotnosc_wzgledna) {
        this.wilgotnosc_wzgledna = wilgotnosc_wzgledna;
    }

    public Double getSuma_opadu() {
        return suma_opadu;
    }

    public void setSuma_opadu(Double suma_opadu) {
        this.suma_opadu = suma_opadu;
    }

    public Float getCisnienie() {
        return cisnienie;
    }

    public void setCisnienie(Float cisnienie) {
        this.cisnienie = cisnienie;
    }

    public int compareTo(Meteorology o) {
        if( id_stacji > o.id_stacji ) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
