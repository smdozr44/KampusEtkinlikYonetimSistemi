
package ui;

import model.Etkinlik;
import model.Katilimci;
import model.Basvuru;
import service.EtkinlikService;
import service.KatilimciService;
import service.BasvuruService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AnaEkran extends JFrame {

    private final EtkinlikService etkinlikService = new EtkinlikService();
    private final KatilimciService katilimciService = new KatilimciService();
    private final BasvuruService basvuruService = new BasvuruService();

    private JTable etkinlikTablosu;
    private JTable katilimciTablosu;
    private JTable basvuruTablosu;

    private DefaultTableModel etkinlikModeli;
    private DefaultTableModel katilimciModeli;
    private DefaultTableModel basvuruModeli;

    private static final DateTimeFormatter TARIH_FORMATI =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Locale TR = Locale.forLanguageTag("tr-TR");

    public AnaEkran() {
        setTitle("Kampüs Etkinlik Yönetim Sistemi");
        setSize(1150, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane sekmeler = new JTabbedPane();
        sekmeler.addTab("Etkinlikler", etkinlikPaneliOlustur());
        sekmeler.addTab("Katılımcılar", katilimciPaneliOlustur());
        sekmeler.addTab("Başvurular", basvuruPaneliOlustur());

        add(sekmeler);

        etkinlikleriYukle();
        katilimcilariYukle();
        basvurulariYukle();
    }

    private DefaultTableModel tabloModeliOlustur(String[] sutunlar) {
        return new DefaultTableModel(sutunlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // ==================== ETKİNLİKLER ====================

    private JPanel etkinlikPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel aramaPaneli = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField adAra = new JTextField(12);
        JTextField turAra = new JTextField(10);
        JButton ara = new JButton("Ara");
        JButton tumunuGoster = new JButton("Tümünü Göster");

        aramaPaneli.add(new JLabel("Etkinlik adı:"));
        aramaPaneli.add(adAra);
        aramaPaneli.add(new JLabel("Etkinlik türü:"));
        aramaPaneli.add(turAra);
        aramaPaneli.add(ara);
        aramaPaneli.add(tumunuGoster);

        panel.add(aramaPaneli, BorderLayout.NORTH);

        etkinlikModeli = tabloModeliOlustur(new String[]{
                "Etkinlik No", "Etkinlik Adı", "Tür",
                "Tarih", "Yer", "Kontenjan", "Doluluk"
        });

        etkinlikTablosu = new JTable(etkinlikModeli);
        etkinlikTablosu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(etkinlikTablosu), BorderLayout.CENTER);

        JPanel butonlar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton ekle = new JButton("Etkinlik Ekle");
        JButton guncelle = new JButton("Etkinliği Güncelle");
        JButton sil = new JButton("Etkinlik Sil");
        JButton yenile = new JButton("Listeyi Yenile");
        JButton tariheGore = new JButton("Tarihe Göre Sırala");
        JButton dolulugaGore = new JButton("Doluluğa Göre Sırala");

        butonlar.add(ekle);
        butonlar.add(guncelle);
        butonlar.add(sil);
        butonlar.add(yenile);
        butonlar.add(tariheGore);
        butonlar.add(dolulugaGore);

        panel.add(butonlar, BorderLayout.SOUTH);

        ara.addActionListener(e -> etkinlikAra(adAra.getText(), turAra.getText()));
        tumunuGoster.addActionListener(e -> etkinlikleriYukle());
        ekle.addActionListener(e -> etkinlikEkle());
        guncelle.addActionListener(e -> etkinlikGuncelle());
        sil.addActionListener(e -> etkinlikSil());
        yenile.addActionListener(e -> etkinlikleriYukle());

        tariheGore.addActionListener(e -> {
            try {
                etkinlikTablosunuGoster(etkinlikService.tariheGoreSirala());
            } catch (Exception ex) {
                hataGoster(ex);
            }
        });

        dolulugaGore.addActionListener(e -> {
            try {
                etkinlikTablosunuGoster(etkinlikService.dolulugaGoreSirala());
            } catch (Exception ex) {
                hataGoster(ex);
            }
        });

        return panel;
    }

    private void etkinlikAra(String ad, String tur) {
        try {
            String adAnahtari = ad.trim().toLowerCase(TR);
            String turAnahtari = tur.trim().toLowerCase(TR);

            List<Etkinlik> sonuc = etkinlikService.listele().stream()
                    .filter(e -> e.getAd().toLowerCase(TR).contains(adAnahtari))
                    .filter(e -> e.getTur().toLowerCase(TR).contains(turAnahtari))
                    .toList();

            etkinlikTablosunuGoster(sonuc);

            if (sonuc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Eşleşen etkinlik bulunamadı.");
            }
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void etkinlikleriYukle() {
        try {
            etkinlikTablosunuGoster(etkinlikService.listele());
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }


    private void etkinlikTablosunuGoster(List<Etkinlik> etkinlikler)
            throws Exception {

        etkinlikModeli.setRowCount(0);

        List<Basvuru> basvurular = basvuruService.listele();

        for (Etkinlik etkinlik : etkinlikler) {

            long asilSayisi = basvurular.stream()
                    .filter(b -> b.getEtkinlikId() == etkinlik.getId())
                    .filter(b -> "ASIL".equals(b.getDurum()))
                    .count();

            String doluluk = asilSayisi + "/" + etkinlik.getKontenjan();

            etkinlikModeli.addRow(new Object[]{
                    etkinlik.getId(),
                    etkinlik.getAd(),
                    etkinlik.getTur(),
                    etkinlik.getTarih().format(TARIH_FORMATI),
                    etkinlik.getYer(),
                    etkinlik.getKontenjan(),
                    doluluk
            });
        }
    }

    private void etkinlikEkle() {
        JTextField ad = new JTextField();
        JTextField tur = new JTextField();
        JTextField tarih = new JTextField();
        JTextField yer = new JTextField();
        JTextField kontenjan = new JTextField();

        Object[] alanlar = {
                "Etkinlik adı:", ad,
                "Etkinlik türü:", tur,
                "Tarih (gg.aa.yyyy ss:dd):", tarih,
                "Yer:", yer,
                "Kontenjan:", kontenjan
        };

        if (JOptionPane.showConfirmDialog(this, alanlar, "Etkinlik Ekle",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            if (ad.getText().isBlank() || tur.getText().isBlank()
                    || tarih.getText().isBlank() || yer.getText().isBlank()
                    || kontenjan.getText().isBlank()) {
                throw new IllegalArgumentException("Bütün alanları doldurmalısın.");
            }

            int id = etkinlikService.listele().stream()
                    .mapToInt(Etkinlik::getId).max().orElse(0) + 1;

            Etkinlik yeni = new Etkinlik(
                    id,
                    ad.getText().trim(),
                    tur.getText().trim(),
                    LocalDateTime.parse(tarih.getText().trim(), TARIH_FORMATI),
                    yer.getText().trim(),
                    Integer.parseInt(kontenjan.getText().trim())
            );

            etkinlikService.ekle(yeni);
            etkinlikleriYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void etkinlikGuncelle() {
        int satir = etkinlikTablosu.getSelectedRow();
        if (satir == -1) {
            JOptionPane.showMessageDialog(this, "Önce bir etkinlik seç.");
            return;
        }

        try {
            int id = (int) etkinlikModeli.getValueAt(satir, 0);
            Etkinlik mevcut = etkinlikService.listele().stream()
                    .filter(e -> e.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Etkinlik bulunamadı."));

            JTextField ad = new JTextField(mevcut.getAd());
            JTextField tur = new JTextField(mevcut.getTur());
            JTextField tarih = new JTextField(mevcut.getTarih().format(TARIH_FORMATI));
            JTextField yer = new JTextField(mevcut.getYer());
            JTextField kontenjan = new JTextField(String.valueOf(mevcut.getKontenjan()));

            Object[] alanlar = {
                    "Etkinlik adı:", ad,
                    "Tür:", tur,
                    "Tarih (gg.aa.yyyy ss:dd):", tarih,
                    "Yer:", yer,
                    "Kontenjan:", kontenjan
            };

            if (JOptionPane.showConfirmDialog(this, alanlar, "Etkinliği Güncelle",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }

            etkinlikService.guncelle(new Etkinlik(
                    id,
                    ad.getText().trim(),
                    tur.getText().trim(),
                    LocalDateTime.parse(tarih.getText().trim(), TARIH_FORMATI),
                    yer.getText().trim(),
                    Integer.parseInt(kontenjan.getText().trim())
            ));

            etkinlikleriYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void etkinlikSil() {
        int satir = etkinlikTablosu.getSelectedRow();
        if (satir == -1) {
            JOptionPane.showMessageDialog(this, "Önce bir etkinlik seç.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Etkinliği ve ona ait tüm başvuruları silmek istiyor musun?",
                "Etkinlik Sil", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = (int) etkinlikModeli.getValueAt(satir, 0);
            basvuruService.etkinlikBasvurulariniSil(id);
            etkinlikService.sil(id);

            etkinlikleriYukle();
            basvurulariYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    // ==================== KATILIMCILAR ====================

    private JPanel katilimciPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        katilimciModeli = tabloModeliOlustur(new String[]{
                "Katılımcı No", "Ad", "Soyad", "Öğrenci No", "E-posta"
        });

        katilimciTablosu = new JTable(katilimciModeli);
        katilimciTablosu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(katilimciTablosu), BorderLayout.CENTER);

        JPanel butonlar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton ekle = new JButton("Katılımcı Ekle");
        JButton guncelle = new JButton("Katılımcıyı Güncelle");
        JButton sil = new JButton("Katılımcıyı Sil");
        JButton yenile = new JButton("Listeyi Yenile");

        butonlar.add(ekle);
        butonlar.add(guncelle);
        butonlar.add(sil);
        butonlar.add(yenile);

        panel.add(butonlar, BorderLayout.SOUTH);

        ekle.addActionListener(e -> katilimciEkle());
        guncelle.addActionListener(e -> katilimciGuncelle());
        sil.addActionListener(e -> katilimciSil());
        yenile.addActionListener(e -> katilimcilariYukle());

        return panel;
    }

    private void katilimcilariYukle() {
        try {
            katilimciModeli.setRowCount(0);

            for (Katilimci k : katilimciService.listele()) {
                katilimciModeli.addRow(new Object[]{
                        k.getId(), k.getAd(), k.getSoyad(),
                        k.getOgrenciNo(), k.getEposta()
                });
            }
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void katilimciEkle() {
        JTextField ad = new JTextField();
        JTextField soyad = new JTextField();
        JTextField no = new JTextField();
        JTextField eposta = new JTextField();

        Object[] alanlar = {
                "Ad:", ad, "Soyad:", soyad,
                "Öğrenci numarası:", no, "E-posta:", eposta
        };

        if (JOptionPane.showConfirmDialog(this, alanlar, "Katılımcı Ekle",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int id = katilimciService.listele().stream()
                    .mapToInt(Katilimci::getId).max().orElse(0) + 1;

            katilimciService.ekle(new Katilimci(
                    id, ad.getText().trim(), soyad.getText().trim(),
                    no.getText().trim(), eposta.getText().trim()
            ));

            katilimcilariYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void katilimciGuncelle() {
        int satir = katilimciTablosu.getSelectedRow();
        if (satir == -1) {
            JOptionPane.showMessageDialog(this, "Önce bir katılımcı seç.");
            return;
        }

        try {
            int id = (int) katilimciModeli.getValueAt(satir, 0);
            Katilimci mevcut = katilimciService.listele().stream()
                    .filter(k -> k.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Katılımcı bulunamadı."));

            JTextField ad = new JTextField(mevcut.getAd());
            JTextField soyad = new JTextField(mevcut.getSoyad());
            JTextField no = new JTextField(mevcut.getOgrenciNo());
            JTextField eposta = new JTextField(mevcut.getEposta());

            Object[] alanlar = {
                    "Ad:", ad, "Soyad:", soyad,
                    "Öğrenci numarası:", no, "E-posta:", eposta
            };

            if (JOptionPane.showConfirmDialog(this, alanlar, "Katılımcıyı Güncelle",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }

            katilimciService.guncelle(new Katilimci(
                    id, ad.getText().trim(), soyad.getText().trim(),
                    no.getText().trim(), eposta.getText().trim()
            ));

            katilimcilariYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void katilimciSil() {
        int satir = katilimciTablosu.getSelectedRow();
        if (satir == -1) {
            JOptionPane.showMessageDialog(this, "Önce bir katılımcı seç.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Katılımcıyı ve tüm başvurularını silmek istiyor musun?",
                "Katılımcıyı Sil", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = (int) katilimciModeli.getValueAt(satir, 0);
            List<Basvuru> basvurular = basvuruService.listele();

            for (Basvuru b : basvurular) {
                if (b.getKatilimciId() == id && "ASIL".equals(b.getDurum())) {
                    basvuruService.basvuruyuSil(b.getId());
                }
            }

            basvuruService.katilimciBasvurulariniSil(id);
            katilimciService.sil(id);

            katilimcilariYukle();
            basvurulariYukle();
            etkinlikleriYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    // ==================== BAŞVURULAR ====================

    private JPanel basvuruPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel filtrePaneli = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField ogrenciNoAra = new JTextField(10);
        JComboBox<String> durum = new JComboBox<>(
                new String[]{"Tümü", "ASIL", "YEDEK"}
        );
        JButton filtrele = new JButton("Filtrele");
        JButton tumunuGoster = new JButton("Tümünü Göster");

        filtrePaneli.add(new JLabel("Öğrenci No:"));
        filtrePaneli.add(ogrenciNoAra);
        filtrePaneli.add(new JLabel("Başvuru durumu:"));
        filtrePaneli.add(durum);
        filtrePaneli.add(filtrele);
        filtrePaneli.add(tumunuGoster);

        panel.add(filtrePaneli, BorderLayout.NORTH);

        basvuruModeli = tabloModeliOlustur(new String[]{
                "Başvuru No", "Etkinlik", "Katılımcı",
                "Başvuru Zamanı", "Durum"
        });

        basvuruTablosu = new JTable(basvuruModeli);
        basvuruTablosu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(basvuruTablosu), BorderLayout.CENTER);

        JPanel butonlar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton ekle = new JButton("Başvuru Oluştur");
        JButton sil = new JButton("Başvuruyu Sil");
        JButton yenile = new JButton("Listeyi Yenile");

        butonlar.add(ekle);
        butonlar.add(sil);
        butonlar.add(yenile);

        panel.add(butonlar, BorderLayout.SOUTH);

        filtrele.addActionListener(e ->
                basvuruFiltrele(
                        ogrenciNoAra.getText(),
                        (String) durum.getSelectedItem()
                )
        );

        tumunuGoster.addActionListener(e -> basvurulariYukle());
        ekle.addActionListener(e -> basvuruOlustur());
        sil.addActionListener(e -> basvuruSil());
        yenile.addActionListener(e -> basvurulariYukle());

        return panel;
    }

    private void basvurulariYukle() {
        try {
            basvuruModeli.setRowCount(0);

            List<Etkinlik> etkinlikler = etkinlikService.listele();
            List<Katilimci> katilimcilar = katilimciService.listele();

            for (Basvuru b : basvuruService.listele()) {
                Etkinlik etkinlik = etkinlikler.stream()
                        .filter(e -> e.getId() == b.getEtkinlikId())
                        .findFirst().orElse(null);

                Katilimci katilimci = katilimcilar.stream()
                        .filter(k -> k.getId() == b.getKatilimciId())
                        .findFirst().orElse(null);

                basvuruModeli.addRow(new Object[]{
                        b.getId(),
                        etkinlik == null ? "Silinmiş etkinlik" : etkinlik.getAd(),
                        katilimci == null ? "Silinmiş katılımcı"
                                : katilimci.getAd() + " " + katilimci.getSoyad(),
                        b.getBasvuruZamani().format(TARIH_FORMATI),
                        b.getDurum()
                });
            }
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void basvuruFiltrele(String ogrenciNo, String durum) {
        try {
            List<Basvuru> basvurular = basvuruService.listele();
            List<Katilimci> katilimcilar = katilimciService.listele();
            List<Etkinlik> etkinlikler = etkinlikService.listele();

            basvuruModeli.setRowCount(0);
            String arananNo = ogrenciNo.trim();

            for (Basvuru b : basvurular) {
                if (!"Tümü".equals(durum) && !b.getDurum().equals(durum)) {
                    continue;
                }

                Katilimci katilimci = katilimcilar.stream()
                        .filter(k -> k.getId() == b.getKatilimciId())
                        .findFirst().orElse(null);

                String no = katilimci == null ? "" : katilimci.getOgrenciNo();
                if (!no.contains(arananNo)) {
                    continue;
                }

                Etkinlik etkinlik = etkinlikler.stream()
                        .filter(e -> e.getId() == b.getEtkinlikId())
                        .findFirst().orElse(null);

                basvuruModeli.addRow(new Object[]{
                        b.getId(),
                        etkinlik == null ? "Silinmiş etkinlik" : etkinlik.getAd(),
                        katilimci == null ? "Silinmiş katılımcı"
                                : katilimci.getAd() + " " + katilimci.getSoyad(),
                        b.getBasvuruZamani().format(TARIH_FORMATI),
                        b.getDurum()
                });
            }
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void basvuruOlustur() {
        try {
            List<Etkinlik> etkinlikler = etkinlikService.listele();
            List<Katilimci> katilimcilar = katilimciService.listele();

            if (etkinlikler.isEmpty() || katilimcilar.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Önce en az bir etkinlik ve katılımcı eklemelisin.");
                return;
            }

            JComboBox<Etkinlik> etkinlikSec = new JComboBox<>();
            for (Etkinlik e : etkinlikler) {
                etkinlikSec.addItem(e);
            }

            JComboBox<Katilimci> katilimciSec = new JComboBox<>();
            for (Katilimci k : katilimcilar) {
                katilimciSec.addItem(k);
            }

            Object[] alanlar = {
                    "Etkinlik:", etkinlikSec,
                    "Katılımcı:", katilimciSec
            };

            if (JOptionPane.showConfirmDialog(this, alanlar, "Yeni Başvuru",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }

            Etkinlik etkinlik = (Etkinlik) etkinlikSec.getSelectedItem();
            Katilimci katilimci = (Katilimci) katilimciSec.getSelectedItem();

            if (etkinlik == null || katilimci == null) {
                return;
            }

            int id = basvuruService.listele().stream()
                    .mapToInt(Basvuru::getId).max().orElse(0) + 1;

            basvuruService.basvur(id, etkinlik, katilimci);

            basvurulariYukle();
            etkinlikleriYukle();

            JOptionPane.showMessageDialog(this, "Başvuru oluşturuldu.");
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void basvuruSil() {
        int satir = basvuruTablosu.getSelectedRow();
        if (satir == -1) {
            JOptionPane.showMessageDialog(this, "Önce bir başvuru seç.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Seçili başvuruyu silmek istiyor musun?",
                "Başvuruyu Sil", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = (int) basvuruModeli.getValueAt(satir, 0);
            basvuruService.basvuruyuSil(id);

            basvurulariYukle();
            etkinlikleriYukle();
        } catch (Exception ex) {
            hataGoster(ex);
        }
    }

    private void hataGoster(Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "İşlem başarısız: " + ex.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
