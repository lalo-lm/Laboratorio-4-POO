import java.time.LocalDate;

public interface Perishable {
    LocalDate getExpirationDate();
    boolean isExpired(LocalDate operationDate);
}