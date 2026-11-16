import java.time.LocalDate;

public abstract class Batch implements Comparable<Batch>, Storable {
    private final String code;
    private final String producer;
    private final LocalDate entryDate;
    private final double receivedWeight;
    private double availableWeight;
    private final char quality;
    private BatchStatus status;


    public Batch(String code, String producer, LocalDate entryDate, double receivedWeight, char quality) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lote no puede estar vacío.");
        }
        if (producer == null || producer.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del productor no puede estar vacío.");
        }
        if (entryDate == null) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser nula.");
        }
        if (receivedWeight <= 0) {
            throw new IllegalArgumentException("El peso recibido debe ser mayor a cero.");
        }
        char upperQuality = Character.toUpperCase(quality);
        if (upperQuality != 'A' && upperQuality != 'B' && upperQuality != 'C') {
            throw new IllegalArgumentException("La calidad debe ser A, B o C.");
        }

        this.code = code.trim();
        this.producer = producer.trim();
        this.entryDate = entryDate;
        this.receivedWeight = receivedWeight;
        this.availableWeight = receivedWeight;
        this.quality = upperQuality;
        this.status = BatchStatus.Available;
    }

    public abstract double calculatePayment();
    public abstract String getCategoryName();

    public double calculateUnitCost() {
        return calculatePayment() / receivedWeight;
    }

    public void reduceWeight(double amount) {
        if (amount <= 0 || amount > availableWeight + 1e-9) {
            throw new IllegalArgumentException("Cantidad inválida a descontar del lote " + code);
        }
        this.availableWeight -= amount;
        if (this.availableWeight <= 1e-9) {
            this.availableWeight = 0.0;
            this.status = BatchStatus.Depleted;
        }
    }

    public void discard() {
        if (this.status == BatchStatus.Available) {
            this.status = BatchStatus.Discarded;
        }
    }

    public boolean isAvailable() {
        return this.status == BatchStatus.Available && this.availableWeight > 0;
    }

    public boolean requiresColdStorage() {
        return false;
    }

    @Override
    public int compareTo(Batch other) {
        boolean thisPerishable = (this instanceof Perishable);
        boolean otherPerishable = (other instanceof Perishable);

        if (this.isPerishableFlag() != other.isPerishableFlag()) {
            return this.isPerishableFlag() ? -1 : 1;
        }

        int dateComparison = this.getPriorityDate().compareTo(other.getPriorityDate());
        if (dateComparison != 0) {
            return dateComparison;
        }

        int qualityComparison = Character.compare(this.quality, other.quality);
        if (qualityComparison != 0) {
            return qualityComparison;
        }

        return this.code.compareToIgnoreCase(other.code);
    }

    protected abstract boolean isPerishableFlag();
    protected abstract LocalDate getPriorityDate();

    public String getCode() { 
        return code; 
    }
    public String getProducer() { 
        return producer; 
    }
    public LocalDate getEntryDate() { 
        return entryDate; 
    }
    public double getReceivedWeight() { 
        return receivedWeight; 
    }
    public double getAvailableWeight() { 
        return availableWeight; 
    }
    public char getQuality() { 
        return quality; 
    }
    public BatchStatus getStatus() { 
        return status; 
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Prod: %s | Calidad: %c | Disp: %.2f/%.2f kg | Costo/kg: Q%.2f | Estado: %s",
                code, getCategoryName(), producer, quality, availableWeight, receivedWeight, calculateUnitCost(), status);
    }
}