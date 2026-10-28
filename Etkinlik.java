
package model;

import java.time.LocalDateTime;

public class Etkinlik {
    private int id;
    private String ad;
    private String tur;
    private LocalDateTime tarih;
    private String yer;
    private int kontenjan;

    public Etkinlik(int id, String ad, String tur,
                    LocalDateTime tarih, String yer, int kontenjan) {
        this.id = id;
        this.ad = ad;
        this.tur = tur;
        this.tarih = tarih;
        this.yer = yer;
        this.kontenjan = kontenjan;
    }

    public int getId() { return id; }
    public String getAd() { return ad; }
    public String getTur() { return tur; }
    public LocalDateTime getTarih() { return tarih; }
    public String getYer() { return yer; }
    public int getKontenjan() { return kontenjan; }

    public void setAd(String ad) { this.ad = ad; }
    public void setTur(String tur) { this.tur = tur; }
    public void setTarih(LocalDateTime tarih) { this.tarih = tarih; }
    public void setYer(String yer) { this.yer = yer; }
    public void setKontenjan(int kontenjan) {
        this.kontenjan = kontenjan;
    }

    @Override
    public String toString() {
        return getAd() + " (No: " + getId() + ")";
    }
}
