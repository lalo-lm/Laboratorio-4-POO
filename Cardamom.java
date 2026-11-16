import java.time.LocalDate;

public class Cardamom extends PerishableBatch {
    private static final int CARDAMOM_SHELF_LIFE = 180;
    private final double greenGrainPercentage;

    public Cardamom(String code, String producer, LocalDate entryDate, double receivedWeight, char quality, double greenGrainPercentage) {
        super(code, producer, entryDate, receivedWeight, quality, CARDAMOM_SHELF_LIFE);
        if (greenGrainPercentage < 0.0 || greenGrainPercentage > 100.0) {
            throw new IllegalArgumentException("El porcentaje de grano verde debe estar entre 0 y 100.");
        }
        this.greenGrainPercentage = greenGrainPercentage;
    }

    @Override
    public double calculatePayment() {
        double baseRate;
        switch (getQuality()) {
            case 'A': baseRate = 60.0; break;
            case 'B': baseRate = 48.0; break;
            default:  baseRate = 35.0; break;
        }
        double payment = getReceivedWeight() * baseRate;
        if (greenGrainPercentage >= 85.0) {
            payment *= 1.10;
        }
        return payment;
    }

    @Override
    public String getCategoryName() {
        return "Cardamomo";
    }

    public double getGreenGrainPercentage() {
        return greenGrainPercentage;
    }

    @Override
    public boolean requieresColdStorage() {
        return false;
    }
}