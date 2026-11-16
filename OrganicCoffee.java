import java.time.LocalDate;

public class OrganicCoffee extends ParchmentCoffee {
    private final String certificationEntity;
    private final LocalDate sealValidityDate;

    public OrganicCoffee(String code, String producer, LocalDate entryDate, double receivedWeight, char quality,
                         double humidity, String certificationEntity, LocalDate sealValidityDate) {
        super(code, producer, entryDate, receivedWeight, quality, humidity);
        if (certificationEntity == null || certificationEntity.trim().isEmpty()) {
            throw new IllegalArgumentException("La entidad certificadora no puede estar vacía.");
        }
        if (sealValidityDate == null || sealValidityDate.isBefore(entryDate)) {
            throw new IllegalArgumentException("El sello de certificación orgánica está vencido en la fecha de ingreso.");
        }
        this.certificationEntity = certificationEntity.trim();
        this.sealValidityDate = sealValidityDate;
    }

    @Override
    public double calculatePayment() {
        double parchmentPayment = super.calculatePayment();
        double organicBonus = getReceivedWeight() * 0.75;
        return parchmentPayment + organicBonus;
    }

    @Override
    public String getCategoryName() {
        return "Café orgánico";
    }

    public String getCertificationEntity() {
        return certificationEntity;
    }

    public LocalDate getSealValidityDate() {
        return sealValidityDate;
    }
}