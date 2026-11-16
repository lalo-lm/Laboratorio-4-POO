import java.time.LocalDate;

public abstract class PerishableBatch extends Batch implements Perishable {
    private final int shelfLifeDays;

    public PerishableBatch(String code, String producer, LocalDate entryDate, double receivedWeight, char quality, int shelfLifeDays) {
        super(code, producer, entryDate, receivedWeight, quality);
        if (shelfLifeDays <= 0) {
            throw new IllegalArgumentException("La vida útil debe ser mayor a 0 días.");
        }

        this.shelfLifeDays = shelfLifeDays;
    }

    @Override
    public LocalDate getExpirationDate() {
        return getEntryDate().plusDays(shelfLifeDays);
    }

    @Override
    public boolean isExpired(LocalDate operationDate) {
        return operationDate.isAfter(getExpirationDate());
    }

    @Override
    protected boolean isPerishableFlag() {
        return true;
    }

    @Override
    protected LocalDate getPriorityDate() {
        return getExpirationDate();
    }

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }

    @Override
    public String toString() {
        return super.toString() + " | Vence: " + getExpirationDate();
    }
}