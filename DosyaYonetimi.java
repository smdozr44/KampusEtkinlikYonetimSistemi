
package storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class DosyaYonetimi {

    private static final Path DATA_KLASORU = Paths.get("data");

    public static void dosyayaEkle(String dosyaAdi, String satir)
            throws IOException {
        Files.createDirectories(DATA_KLASORU);

        Path dosya = DATA_KLASORU.resolve(dosyaAdi);

        Files.writeString(
                dosya,
                satir + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public static List<String> dosyayiOku(String dosyaAdi)
            throws IOException {
        Files.createDirectories(DATA_KLASORU);

        Path dosya = DATA_KLASORU.resolve(dosyaAdi);

        if (!Files.exists(dosya)) {
            Files.createFile(dosya);
        }

        return Files.readAllLines(dosya, StandardCharsets.UTF_8);
    }

    public static void dosyayiYenidenYaz(
            String dosyaAdi, List<String> satirlar)
            throws IOException {
        Files.createDirectories(DATA_KLASORU);

        Path dosya = DATA_KLASORU.resolve(dosyaAdi);

        Files.write(
                dosya,
                satirlar,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
