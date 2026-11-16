import java.time.LocalDate;

public class Vegetable extends PerishableBatch {
    public Vegetable(String code, String producer, LocalDate entryDate, double receivedWeight, char quality, int shelfLifeDays) {
        super(code, producer, entryDate, receivedWeight, quality, shelfLifeDays);
        if (shelfLifeDays < 3 || shelfLifeDays > 14) {
            throw new IllegalArgumentException("La vida útil de una hortaliza debe estar entre 3 y 14 días.");
        }
    }

    @Override
    public double calculatePayment() {
        double baseRate;
        switch (getQuality()) {
            case 'A': baseRate = 4.0; break;
            case 'B': baseRate = 3.0; break;
            default:  baseRate = 2.0; break;
        }
        double payableWeight = getReceivedWeight() * 0.95; // 5% merma de transporte
        return payableWeight * baseRate;
    }

    @Override
    public boolean requiresColdStorage() {
        return true;
    }

    @Override
    public String getCategoryName() {
        return "Hortaliza";
    }

    @Override
    public boolean requieresColdStorage() {
        return true;
    }
}