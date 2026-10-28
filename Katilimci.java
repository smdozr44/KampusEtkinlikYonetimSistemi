
package model;

public class Katilimci {
    private int id;
    private String ad;
    private String soyad;
    private String ogrenciNo;
    private String eposta;

    public Katilimci(int id, String ad, String soyad,
                     String ogrenciNo, String eposta) {
        this.id = id;
        this.ad = ad;
        this.soyad = soyad;
        this.ogrenciNo = ogrenciNo;
        this.eposta = eposta;
    }

    public int getId() { return id; }
    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getOgrenciNo() { return ogrenciNo; }
    public String getEposta() { return eposta; }

    public void setAd(String ad) { this.ad = ad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }
    public void setOgrenciNo(String ogrenciNo) {
        this.ogrenciNo = ogrenciNo;
    }
    public void setEposta(String eposta) { this.eposta = eposta; }


    @Override
    public String toString() {
        return getAd() + " " + getSoyad()
                + " (" + getOgrenciNo() + ")";
    }
}
