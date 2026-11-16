import java.time.LocalDate;

public abstract class NonPerishableBatch extends Batch {
    public NonPerishableBatch(String code, String producer, LocalDate entryDate, double receivedWeight, char quality) {
        super(code, producer, entryDate, receivedWeight, quality);
    }

    @Override
    protected boolean isPerishableFlag() {
        return false;
    }

    @Override
    protected LocalDate getPriorityDate() {
        return getEntryDate();
    }

    @Override
    public String toString() {
        return super.toString() + " | Ingreso: " + getEntryDate() + " (No perecedero)";
    }
}