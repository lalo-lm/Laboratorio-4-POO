import java.time.LocalDate;

public class ParchmentCoffee extends NonPerishableBatch {
    private final double humidity;

    public ParchmentCoffee(String code, String producer, LocalDate entryDate, double receivedWeight, char quality, double humidity) {
        super(code, producer, entryDate, receivedWeight, quality);
        if (humidity < 8.0 || humidity > 14.0) {
            throw new IllegalArgumentException("La humedad del café debe estar entre 8% y 14%.");
        }
        this.humidity = humidity;
    }

    @Override
    public double calculatePayment() {
        double baseRate;
        switch (getQuality()) {
            case 'A': baseRate = 12.0; break;
            case 'B': baseRate = 9.0; break;
            default:  baseRate = 6.0; break;
        }
        double basePayment = getReceivedWeight() * baseRate;

        if (humidity > 12.0) {
            int excessPoints = (int) Math.ceil(humidity - 12.0);
            double discountRate = excessPoints * 0.03;
            basePayment -= (basePayment * discountRate);
        }
        return basePayment;
    }

    @Override
    public String getCategoryName() {
        return "Café en pergamino";
    }

    public double getHumidity() {
        return humidity;
    }

    @Override
    public boolean requieresColdStorage() {
        return false;
    }
}