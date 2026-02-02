package br.com.uds.tools.ged.service.storage.file;

public abstract class AbstractStorageService {

    protected static final String[] ALLOWED_EXTENSIONS = {".pdf", ".png", ".jpg", ".jpeg"};


    /** Nome do arquivo para download: parte do fileKey após a última barra. */
    public static String filenameFromFileKey(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) return "download";
        int last = fileKey.lastIndexOf('/');
        return last >= 0 && last < fileKey.length() - 1 ? fileKey.substring(last + 1) : fileKey;
    }

    protected static String sanitizeForFileKey(String name) {
        if (name == null || name.isBlank()) return "";
        String s = name.replaceAll(".*[/\\\\]", "").trim();
        s = s.replaceAll("[\\x00\\\\/:*?\"<>|]", "_");
        return s.length() > 220 ? s.substring(0, 220) : s;
    }

    protected static String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return i > 0 ? filename.substring(i).toLowerCase() : "";
    }

    protected static void validateExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) return;
        }
        throw new IllegalArgumentException("Formato não permitido. Use PDF, PNG ou JPG.");
    }

}
