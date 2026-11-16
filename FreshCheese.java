import java.time.LocalDate;

public class FreshCheese extends PerishableBatch {
    private static final int CHEESE_SHELF_LIFE = 21;
    private final double fatPercentage;

    public FreshCheese(String code, String producer, LocalDate entryDate, double receivedWeight, char quality, double fatPercentage) {
        super(code, producer, entryDate, receivedWeight, quality, CHEESE_SHELF_LIFE);
        if (getQuality() == 'C') {
            throw new IllegalArgumentException("No se acepta queso fresco de calidad C.");
        }
        if (fatPercentage < 10.0 || fatPercentage > 40.0) {
            throw new IllegalArgumentException("El porcentaje de grasa del queso fresco debe estar entre 10% y 40%.");
        }
        this.fatPercentage = fatPercentage;
    }

    @Override
    public double calculatePayment() {
        double baseRate = (getQuality() == 'A') ? 38.0 : 30.0;
        double grossPayment = getReceivedWeight() * baseRate;
        double packagingCost = getReceivedWeight() * 1.50;
        return grossPayment - packagingCost;
    }

    @Override
    public boolean requiresColdStorage() {
        return true;
    }

    @Override
    public String getCategoryName() {
        return "Queso fresco";
    }

    public double getFatPercentage() {
        return fatPercentage;
    }

    @Override
    public boolean requieresColdStorage() {
        return true;
    }
}