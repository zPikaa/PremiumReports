package it.pika.premiumreports.utils;

import com.tchristofferson.configupdater.ConfigUpdater;
import it.pika.libs.config.Config;
import it.pika.premiumreports.Main;
import it.pika.premiumreports.enums.Language;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;

public class LanguageManager {

    @Getter
    private Language language;

    public void init() {
        createFiles();
        var configLanguage = Main.getConfigFile().getString("Options.Language");

        var languageFile = new File(Main.getInstance().getDataFolder() + File.separator + "Languages"
                + File.separator + "messages_%s.yml".formatted(configLanguage));
        if (!languageFile.exists()) {
            Main.getConsole().warning("Language not found: %s. Using 'en' instead.".formatted(configLanguage));
            language = Language.ENGLISH;
            return;
        }

        language = Language.fromFile(languageFile);
        Main.getConsole().info("Using language: %s".formatted(language.name()));
    }

    @SneakyThrows
    private void createFiles() {
        var folder = new File(Main.getInstance().getDataFolder(), "Languages");
        if (!folder.exists())
            folder.mkdir();

        var oldMessagesFile = new File(Main.getInstance().getDataFolder(), "messages.yml");
        if (oldMessagesFile.exists())
            Files.delete(oldMessagesFile.toPath());

        for (Language value : Language.values()) {
            var config = new Config(Main.getInstance(), value.getFile(), true);
            ConfigUpdater.update(Main.getInstance(), "Languages/%s".formatted(config.getFileName()), config.getFile());
            config.reload();
        }
    }

}
