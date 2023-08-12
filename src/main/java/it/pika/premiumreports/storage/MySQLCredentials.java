package it.pika.premiumreports.storage;

import it.pika.libs.config.Config;
import it.pika.premiumreports.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MySQLCredentials {

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public static MySQLCredentials fromConfig(Config config) {
        try {
            var host = config.getString("Storage.MySQL.Host");
            var port = config.getInt("Storage.MySQL.Port");
            var database = config.getString("Storage.MySQL.Database");
            var username = config.getString("Storage.MySQL.Username");
            var password = config.getString("Storage.MySQL.Password");

            return new MySQLCredentials(host, port, database, username, password);
        } catch (NullPointerException e) {
            Main.getConsole().warning("Unable to get MySQL credentials from config!");
            return null;
        }
    }

}
