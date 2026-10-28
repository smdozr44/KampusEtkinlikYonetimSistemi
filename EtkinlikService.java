
package service;

import model.Etkinlik;
import model.Basvuru;
import storage.DosyaYonetimi;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EtkinlikService {

    private static final String DOSYA = "etkinlikler.txt";

    private List<Etkinlik> etkinlikleriOku() throws IOException {
        List<Etkinlik> etkinlikler = new ArrayList<>();

        for (String satir : DosyaYonetimi.dosyayiOku(DOSYA)) {
            String[] p = satir.split("\\|", -1);

            if (p.length != 6) {
                continue;
            }

            try {
                etkinlikler.add(new Etkinlik(
                        Integer.parseInt(p[0]),
                        p[1],
                        p[2],
                        LocalDateTime.parse(p[3]),
                        p[4],
                        Integer.parseInt(p[5])
                ));
            } catch (RuntimeException ex) {
                throw new IOException(
                        "Etkinlik dosyasında hatalı kayıt var: " + satir,
                        ex
                );
            }
        }

        return etkinlikler;
    }

    private void etkinlikleriKaydet(List<Etkinlik> etkinlikler)
            throws IOException {

        List<String> satirlar = new ArrayList<>();

        for (Etkinlik e : etkinlikler) {
            satirlar.add(
                    e.getId() + "|" +
                            e.getAd() + "|" +
                            e.getTur() + "|" +
                            e.getTarih() + "|" +
                            e.getYer() + "|" +
                            e.getKontenjan()
            );
        }

        DosyaYonetimi.dosyayiYenidenYaz(DOSYA, satirlar);
    }

    public List<Etkinlik> listele() throws IOException {
        return etkinlikleriOku();
    }

    public void ekle(Etkinlik yeni) throws IOException {
        List<Etkinlik> etkinlikler = etkinlikleriOku();

        if (yeni.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Etkinlik numarası pozitif olmalıdır."
            );
        }

        for (Etkinlik e : etkinlikler) {
            if (e.getId() == yeni.getId()) {
                throw new IllegalArgumentException(
                        "Bu etkinlik numarası zaten kullanılıyor."
                );
            }
        }

        if (yeni.getKontenjan() < 1) {
            throw new IllegalArgumentException(
                    "Kontenjan en az 1 olmalıdır."
            );
        }

        etkinlikler.add(yeni);
        etkinlikleriKaydet(etkinlikler);
    }

    public void sil(int id) throws IOException {
        List<Etkinlik> etkinlikler = etkinlikleriOku();

        boolean bulundu = etkinlikler.removeIf(
                e -> e.getId() == id
        );

        if (!bulundu) {
            throw new IllegalArgumentException(
                    "Etkinlik bulunamadı."
            );
        }

        etkinlikleriKaydet(etkinlikler);
    }

    public void guncelle(Etkinlik guncel) throws IOException {
        List<Etkinlik> etkinlikler = etkinlikleriOku();

        if (guncel.getKontenjan() < 1) {
            throw new IllegalArgumentException(
                    "Kontenjan en az 1 olmalıdır."
            );
        }

        // Kontenjanı mevcut asil katılımcı sayısının altına düşürme.
        BasvuruService basvuruService = new BasvuruService();

        long asilSayisi = basvuruService.listele().stream()
                .filter(b -> b.getEtkinlikId() == guncel.getId())
                .filter(b -> "ASIL".equals(b.getDurum()))
                .count();

        if (guncel.getKontenjan() < asilSayisi) {
            throw new IllegalArgumentException(
                    "Kontenjan mevcut asil katılımcı sayısından az olamaz. "
                            + "Asil katılımcı sayısı: " + asilSayisi
            );
        }

        for (int i = 0; i < etkinlikler.size(); i++) {
            if (etkinlikler.get(i).getId() == guncel.getId()) {
                etkinlikler.set(i, guncel);
                etkinlikleriKaydet(etkinlikler);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Etkinlik bulunamadı."
        );
    }

    // Etkinlikleri tarihe göre, en yakından en ileri tarihe sırala.
    public List<Etkinlik> tariheGoreSirala() throws IOException {
        List<Etkinlik> etkinlikler = etkinlikleriOku();

        etkinlikler.sort(
                Comparator.comparing(Etkinlik::getTarih)
        );

        return etkinlikler;
    }

    // Doluluk oranı en yüksek etkinlik önce gelir.
    public List<Etkinlik> dolulugaGoreSirala() throws IOException {
        List<Etkinlik> etkinlikler = etkinlikleriOku();
        List<Basvuru> basvurular =
                new BasvuruService().listele();

        etkinlikler.sort((e1, e2) -> {
            double oran1 = dolulukOrani(e1, basvurular);
            double oran2 = dolulukOrani(e2, basvurular);

            return Double.compare(oran2, oran1);
        });

        return etkinlikler;
    }

    private double dolulukOrani(
            Etkinlik etkinlik,
            List<Basvuru> basvurular
    ) {
        if (etkinlik.getKontenjan() <= 0) {
            return 0;
        }

        long asilSayisi = basvurular.stream()
                .filter(b -> b.getEtkinlikId() == etkinlik.getId())
                .filter(b -> "ASIL".equals(b.getDurum()))
                .count();

        return (double) asilSayisi / etkinlik.getKontenjan();
    }

    // Etkinlik adının bir kısmıyla arama yap.
    public List<Etkinlik> adaGoreAra(String aranan)
            throws IOException {

        List<Etkinlik> sonuc = new ArrayList<>();
        String anahtar = aranan.trim().toLowerCase(
                java.util.Locale.forLanguageTag("tr-TR")
        );

        for (Etkinlik e : etkinlikleriOku()) {
            if (e.getAd().toLowerCase(
                    java.util.Locale.forLanguageTag("tr-TR")
            ).contains(anahtar)) {
                sonuc.add(e);
            }
        }

        return sonuc;
    }

    // Etkinlik türüne göre arama yap.
    public List<Etkinlik> tureGoreAra(String tur)
            throws IOException {

        List<Etkinlik> sonuc = new ArrayList<>();

        for (Etkinlik e : etkinlikleriOku()) {
            if (e.getTur().equalsIgnoreCase(tur.trim())) {
                sonuc.add(e);
            }
        }

        return sonuc;
    }
}
