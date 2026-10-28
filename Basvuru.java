
package model;

import java.time.LocalDateTime;

public class Basvuru {
    private int id;
    private int etkinlikId;
    private int katilimciId;
    private LocalDateTime basvuruZamani;
    private String durum;

    public Basvuru(int id, int etkinlikId, int katilimciId,
                   LocalDateTime basvuruZamani, String durum) {
        this.id = id;
        this.etkinlikId = etkinlikId;
        this.katilimciId = katilimciId;
        this.basvuruZamani = basvuruZamani;
        this.durum = durum;
    }

    public int getId() { return id; }
    public int getEtkinlikId() { return etkinlikId; }
    public int getKatilimciId() { return katilimciId; }
    public LocalDateTime getBasvuruZamani() {
        return basvuruZamani;
    }
    public String getDurum() { return durum; }

    public void setDurum(String durum) {
        this.durum = durum;
    }
}
