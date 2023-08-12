package it.pika.premiumreports.enums;

import it.pika.premiumreports.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor
public enum Language {

    ENGLISH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_en.yml")),
    CZECH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_cs.yml")),
    DUTCH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_nl.yml")),
    FRENCH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_fr.yml")),
    GERMAN(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_de.yml")),
    ITALIAN(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_it.yml")),
    PORTUGUESE(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_pt.yml")),
    SPANISH(new File(Main.getInstance().getDataFolder() + File.separator + "Languages" + File.separator
            + "messages_es.yml"));

    @Getter
    private final File file;

    public static Language fromFile(File file) {
        for (Language value : values()) {
            if (!value.getFile().getName().equalsIgnoreCase(file.getName()))
                continue;

            return value;
        }

        return ENGLISH;
    }

}
