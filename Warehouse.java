import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Warehouse implements Iterable<Batch> {
    private final String code;
    private final double maxCapacity;
    private final boolean coldStorage;
    private final List<Batch> batches;

    public Warehouse(String code, double maxCapacity, boolean coldStorage) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la bodega no puede estar vacío.");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("La capacidad máxima debe ser mayor a cero.");
        }
        this.code = code.trim();
        this.maxCapacity = maxCapacity;
        this.coldStorage = coldStorage;
        this.batches = new ArrayList<>();
    }

    public double getUsedCapacity() {
        double sum = 0.0;
        for (Batch b : batches) {
            if (b.isAvailable()) {
                sum += b.getAvailableWeight();
            }
        }
        return sum;
    }

    public double getAvailableCapacity() {
        return maxCapacity - getUsedCapacity();
    }

    public boolean canStore(Batch batch) {
        if (batch == null) return false;
        boolean thermalMatch = (batch.requiresColdStorage() == this.coldStorage);
        boolean spaceAvailable = (getUsedCapacity() + batch.getReceivedWeight() <= this.maxCapacity + 1e-9);
        return thermalMatch && spaceAvailable;
    }

    public void addBatch(Batch batch) {
        if (batch.requiresColdStorage() != this.coldStorage) {
            throw new IllegalArgumentException("Incompatibilidad térmica: la bodega " + code +
                    (coldStorage ? " es cámara fría" : " es bodega seca") + ".");
        }
        if (getUsedCapacity() + batch.getReceivedWeight() > this.maxCapacity + 1e-9) {
            throw new IllegalArgumentException("Capacidad insuficiente en la bodega " + code + ".");
        }
        batches.add(batch);
    }

    public void removeBatch(Batch batch) {
        batches.remove(batch);
    }

    @Override
    public Iterator<Batch> iterator() {
        Collections.sort(batches, (b1, b2) -> {
            int dateComparison = b1.getPriorityDate().compareTo(b2.getPriorityDate());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return Character.compare(b1.getQuality(), b2.getQuality());
        });
        return batches.iterator();
    }

    public String getCode() { return code; }
    public double getMaxCapacity() { return maxCapacity; }
    public boolean isColdStorage() { return coldStorage; }

    @Override
    public String toString() {
        return String.format("Bodega %s [%s] | Ocupado: %.2f / %.2f kg",
                code, coldStorage ? "Cámara Fría" : "Bodega Seca", getUsedCapacity(), maxCapacity);
    }

    java.lang.Iterable<Batch> getBatches() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}