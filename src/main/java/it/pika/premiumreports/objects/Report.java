package it.pika.premiumreports.objects;

import it.pika.premiumreports.Main;
import it.pika.premiumreports.enums.Messages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Getter @Setter
public class Report {

    private UUID uuid;
    private String reporter;
    private String reported;
    private String reason;
    private Date date;
    private Result result;
    private String completedBy;
    private Date completedDate;

    public static Report of(UUID uuid) {
        return Main.getStorage().getReport(uuid);
    }

    @AllArgsConstructor
    public enum Result {
        UNDEFINED(0, Messages.RESULT_UNDEFINED.get()),
        VALID(1, Messages.RESULT_VALID.get()),
        INVALID(2, Messages.RESULT_INVALID.get());

        @Getter
        private final int id;
        @Getter
        private final String displayName;

        public static Result byId(int id) {
            for (Result value : values()) {
                if (value.getId() != id)
                    continue;

                return value;
            }

            return null;
        }
    }

}
