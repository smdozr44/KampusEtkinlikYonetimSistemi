
package service;

import model.Katilimci;
import storage.DosyaYonetimi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KatilimciService {

    private static final String DOSYA = "katilimcilar.txt";

    private List<Katilimci> katilimcilariOku() throws IOException {
        List<Katilimci> katilimcilar = new ArrayList<>();

        for (String satir : DosyaYonetimi.dosyayiOku(DOSYA)) {
            String[] p = satir.split("\\|", -1);

            if (p.length != 5) {
                throw new IOException("Katılımcı dosyasında hatalı kayıt var: " + satir);
            }

            try {
                katilimcilar.add(new Katilimci(
                        Integer.parseInt(p[0]),
                        p[1],
                        p[2],
                        p[3],
                        p[4]
                ));
            } catch (RuntimeException ex) {
                throw new IOException("Katılımcı kaydı okunamadı: " + satir, ex);
            }
        }

        return katilimcilar;
    }

    private void katilimcilariKaydet(List<Katilimci> katilimcilar)
            throws IOException {

        List<String> satirlar = new ArrayList<>();

        for (Katilimci k : katilimcilar) {
            satirlar.add(
                    k.getId() + "|" +
                            k.getAd() + "|" +
                            k.getSoyad() + "|" +
                            k.getOgrenciNo() + "|" +
                            k.getEposta()
            );
        }

        DosyaYonetimi.dosyayiYenidenYaz(DOSYA, satirlar);
    }

    public List<Katilimci> listele() throws IOException {
        return katilimcilariOku();
    }

    public void ekle(Katilimci yeni) throws IOException {
        List<Katilimci> katilimcilar = katilimcilariOku();

        if (yeni.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Katılımcı numarası pozitif olmalıdır."
            );
        }

        if (yeni.getAd().isBlank()
                || yeni.getSoyad().isBlank()
                || yeni.getOgrenciNo().isBlank()
                || yeni.getEposta().isBlank()) {
            throw new IllegalArgumentException(
                    "Bütün alanları doldurmalısın."
            );
        }

        for (Katilimci k : katilimcilar) {
            if (k.getId() == yeni.getId()) {
                throw new IllegalArgumentException(
                        "Bu katılımcı numarası zaten kullanılıyor."
                );
            }

            if (k.getOgrenciNo().equalsIgnoreCase(yeni.getOgrenciNo())) {
                throw new IllegalArgumentException(
                        "Bu öğrenci numarası zaten kayıtlı."
                );
            }
        }

        katilimcilar.add(yeni);
        katilimcilariKaydet(katilimcilar);
    }

    public void guncelle(Katilimci guncel) throws IOException {
        List<Katilimci> katilimcilar = katilimcilariOku();

        if (guncel.getAd().isBlank()
                || guncel.getSoyad().isBlank()
                || guncel.getOgrenciNo().isBlank()
                || guncel.getEposta().isBlank()) {
            throw new IllegalArgumentException(
                    "Bütün alanları doldurmalısın."
            );
        }

        for (Katilimci k : katilimcilar) {
            if (k.getId() != guncel.getId()
                    && k.getOgrenciNo().equalsIgnoreCase(guncel.getOgrenciNo())) {
                throw new IllegalArgumentException(
                        "Bu öğrenci numarası başka bir katılımcıda kayıtlı."
                );
            }
        }

        for (int i = 0; i < katilimcilar.size(); i++) {
            if (katilimcilar.get(i).getId() == guncel.getId()) {
                katilimcilar.set(i, guncel);
                katilimcilariKaydet(katilimcilar);
                return;
            }
        }

        throw new IllegalArgumentException("Katılımcı bulunamadı.");
    }

    public void sil(int id) throws IOException {
        List<Katilimci> katilimcilar = katilimcilariOku();

        boolean bulundu = katilimcilar.removeIf(k -> k.getId() == id);

        if (!bulundu) {
            throw new IllegalArgumentException("Katılımcı bulunamadı.");
        }

        katilimcilariKaydet(katilimcilar);
    }
}
