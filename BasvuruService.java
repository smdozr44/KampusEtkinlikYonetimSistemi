
package service;

import model.Basvuru;
import model.Etkinlik;
import model.Katilimci;
import storage.DosyaYonetimi;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BasvuruService {

    private static final String DOSYA = "basvurular.txt";

    private List<Basvuru> basvurulariOku() throws IOException {
        List<Basvuru> liste = new ArrayList<>();

        for (String satir : DosyaYonetimi.dosyayiOku(DOSYA)) {
            String[] p = satir.split("\\|", -1);

            if (p.length != 5) {
                continue;
            }

            try {
                liste.add(new Basvuru(
                        Integer.parseInt(p[0]),
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[2]),
                        LocalDateTime.parse(p[3]),
                        p[4]
                ));
            } catch (RuntimeException ex) {
                throw new IOException(
                        "Başvuru dosyasında hatalı kayıt: " + satir,
                        ex
                );
            }
        }

        return liste;
    }

    private void basvurulariKaydet(List<Basvuru> liste)
            throws IOException {

        List<String> satirlar = new ArrayList<>();

        for (Basvuru b : liste) {
            satirlar.add(
                    b.getId() + "|" +
                            b.getEtkinlikId() + "|" +
                            b.getKatilimciId() + "|" +
                            b.getBasvuruZamani() + "|" +
                            b.getDurum()
            );
        }

        DosyaYonetimi.dosyayiYenidenYaz(DOSYA, satirlar);
    }

    public List<Basvuru> listele() throws IOException {
        return basvurulariOku();
    }

    public void basvur(
            int basvuruId,
            Etkinlik etkinlik,
            Katilimci katilimci
    ) throws IOException {

        if (etkinlik == null || katilimci == null) {
            throw new IllegalArgumentException(
                    "Etkinlik ve katılımcı seçilmelidir."
            );
        }

        if (basvuruId <= 0) {
            throw new IllegalArgumentException(
                    "Başvuru numarası pozitif olmalıdır."
            );
        }

        if (etkinlik.getKontenjan() < 1) {
            throw new IllegalArgumentException(
                    "Etkinlik kontenjanı geçersiz."
            );
        }

        List<Basvuru> liste = basvurulariOku();

        for (Basvuru b : liste) {
            if (b.getId() == basvuruId) {
                throw new IllegalArgumentException(
                        "Bu başvuru numarası zaten kullanılıyor."
                );
            }

            if (b.getEtkinlikId() == etkinlik.getId()
                    && b.getKatilimciId() == katilimci.getId()) {
                throw new IllegalArgumentException(
                        "Bu kişi bu etkinliğe zaten başvurmuş."
                );
            }
        }

        long asilSayisi = liste.stream()
                .filter(b -> b.getEtkinlikId() == etkinlik.getId())
                .filter(b -> "ASIL".equals(b.getDurum()))
                .count();

        String durum = asilSayisi < etkinlik.getKontenjan()
                ? "ASIL"
                : "YEDEK";

        liste.add(new Basvuru(
                basvuruId,
                etkinlik.getId(),
                katilimci.getId(),
                LocalDateTime.now(),
                durum
        ));

        basvurulariKaydet(liste);
    }

    public void basvuruyuSil(int basvuruId) throws IOException {
        List<Basvuru> liste = basvurulariOku();

        Basvuru silinecek = liste.stream()
                .filter(b -> b.getId() == basvuruId)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Başvuru bulunamadı."
                        )
                );

        int etkinlikId = silinecek.getEtkinlikId();
        boolean asilSilindi = "ASIL".equals(silinecek.getDurum());

        liste.remove(silinecek);

        if (asilSilindi) {
            liste.stream()
                    .filter(b -> b.getEtkinlikId() == etkinlikId)
                    .filter(b -> "YEDEK".equals(b.getDurum()))
                    .min(Comparator.comparing(Basvuru::getBasvuruZamani))
                    .ifPresent(b -> b.setDurum("ASIL"));
        }

        basvurulariKaydet(liste);
    }

    // Bir etkinlik silinirse ona bağlı bütün başvuruları da sil.
    public void etkinlikBasvurulariniSil(int etkinlikId)
            throws IOException {

        List<Basvuru> liste = basvurulariOku();

        liste.removeIf(
                b -> b.getEtkinlikId() == etkinlikId
        );

        basvurulariKaydet(liste);
    }

    // Bir katılımcı silinirse ona bağlı başvuruları da sil.
    public void katilimciBasvurulariniSil(int katilimciId)
            throws IOException {

        List<Basvuru> liste = basvurulariOku();

        liste.removeIf(
                b -> b.getKatilimciId() == katilimciId
        );

        basvurulariKaydet(liste);
    }
}
