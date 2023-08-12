package it.pika.premiumreports.enums;

import it.pika.libs.config.Config;
import it.pika.premiumreports.Main;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Messages {

    NO_PERMISSION,
    WRONG_PARAMETERS,
    ONLY_BY_PLAYERS,
    MUST_BE_ONLINE,
    RESULT_UNDEFINED,
    RESULT_VALID,
    RESULT_INVALID,
    REPORT_CREATED,
    REPORT_YOURSELF,
    REPORTED_OFFLINE,
    REPORT_VALIDATED,
    REPORT_INVALIDATED,
    REPORT_VALID,
    REPORT_INVALID,
    USER_BLOCKED,
    USER_UNBLOCKED,
    CANT_CREATE,
    PLUGIN_RELOADED,
    NEW_UPDATE,
    YES_MESSAGE,
    NO_MESSAGE,
    INVALID_USER,
    INVALID_NUMBER,
    POINTS_SET;

    public String get() {
        var language = Main.getLanguageManager().getLanguage();
        var config = new Config(Main.getInstance(), language.getFile(), false);

        return config.getString(name().replaceAll("_", "-").toLowerCase());
    }

}
