package utils;

import javafx.scene.Scene;
import java.io.*;
import java.util.Properties;

public class ThemeManager {
    private static final String CONFIG_FILE = "settings.properties";
    private static final String THEME_KEY = "theme";
    private static final String LIGHT_THEME = "light";
    private static final String DARK_THEME = "dark";
    private static final String DARK_CSS_PATH = "/resources/css/DarkMode.css";

    private static String currentTheme = LIGHT_THEME;

    static {
        loadSettings();
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }

    public static boolean isDarkMode() {
        return DARK_THEME.equalsIgnoreCase(currentTheme);
    }

    public static void setDarkMode(boolean darkMode) {
        currentTheme = darkMode ? DARK_THEME : LIGHT_THEME;
        saveSettings();
    }

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        
        try {
            var cssResource = ThemeManager.class.getResource(DARK_CSS_PATH);
            if (cssResource == null) {
                System.err.println("Không tìm thấy file css DarkMode: " + DARK_CSS_PATH);
                return;
            }
            String darkCssUrl = cssResource.toExternalForm();
            if (isDarkMode()) {
                if (!scene.getStylesheets().contains(darkCssUrl)) {
                    scene.getStylesheets().add(darkCssUrl);
                }
            } else {
                scene.getStylesheets().remove(darkCssUrl);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi áp dụng theme: " + e.getMessage());
        }
    }

    private static void loadSettings() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                props.load(input);
                currentTheme = props.getProperty(THEME_KEY, LIGHT_THEME);
            } catch (IOException e) {
                System.err.println("Không thể đọc file cấu hình cài đặt: " + e.getMessage());
            }
        }
    }

    private static void saveSettings() {
        Properties props = new Properties();
        props.setProperty(THEME_KEY, currentTheme);
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "SoleManager App Settings");
        } catch (IOException e) {
            System.err.println("Không thể ghi file cấu hình cài đặt: " + e.getMessage());
        }
    }
}
