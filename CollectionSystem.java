import java.time.LocalDate;
import java.util.*;

public class CollectionSystem {
    private LocalDate operationDate;
    private final List<Warehouse> warehouses;
    private final Set<String> registeredBatchCodes;
    private final Map<String, Double> paidTotals;
    private final Map<String, Double> chargedTotals;
    private final Map<String, Double> lossTotals;

    private static final List<String> CATEGORIES = Arrays.asList(
            "Café en pergamino", "Café orgánico", "Cardamomo", "Hortaliza", "Queso fresco"
    );

    public CollectionSystem(LocalDate initialDate) {
        if (initialDate == null) {
            throw new IllegalArgumentException("La fecha inicial no puede ser nula.");
        }
        this.operationDate = initialDate;
        this.warehouses = new ArrayList<>();
        this.registeredBatchCodes = new HashSet<>();
        this.paidTotals = new LinkedHashMap<>();
        this.chargedTotals = new LinkedHashMap<>();
        this.lossTotals = new LinkedHashMap<>();

        for (String cat : CATEGORIES) {
            paidTotals.put(cat, 0.0);
            chargedTotals.put(cat, 0.0);
            lossTotals.put(cat, 0.0);
        }
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        String normalized = category.trim();
        for (String validCategory : CATEGORIES) {
            if (validCategory.equalsIgnoreCase(normalized)) {
                return validCategory;
            }
        }
        throw new IllegalArgumentException("La categoría no es válida: " + category);
    }

    public void registerWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("La bodega no puede ser nula.");
        }
        if (warehouse.getCode() == null || warehouse.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la bodega no puede ser nulo o vacío.");
        }
        for (Warehouse w : warehouses) {
            if (w.getCode().equalsIgnoreCase(warehouse.getCode())) {
                throw new IllegalArgumentException("Ya existe una bodega con el código: " + warehouse.getCode());
            }
        }
        warehouses.add(warehouse);
    }

    public String preLiquidateBatch(Batch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        if (registeredBatchCodes.contains(batch.getCode().toUpperCase())) {
            throw new IllegalArgumentException("El código de lote ya existe en el sistema: " + batch.getCode());
        }
        String normalizedCategory = normalizeCategory(batch.getCategoryName());
        String expirationInfo = (batch instanceof Perishable)
                ? ((Perishable) batch).getExpirationDate().toString()
                : "No vence";
        return String.format("PRE-LIQUIDACIÓN -> Categoría: %s | Pago al productor: Q%.2f | Costo/kg: Q%.2f | Vencimiento: %s",
                normalizedCategory, batch.calculatePayment(), batch.calculateUnitCost(), expirationInfo);
    }

    public void receiveBatch(Batch batch, String warehouseCode) {
        loadBatchInternal(batch, warehouseCode, true);
    }

    public void loadInitialBatch(Batch batch, String warehouseCode) {
        loadBatchInternal(batch, warehouseCode, false);
    }

    private void loadBatchInternal(Batch batch, String warehouseCode, boolean registerPayment) {
        if (batch == null) {
            throw new IllegalArgumentException("El lote no puede ser nulo.");
        }
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de bodega no puede ser nulo o vacío.");
        }
        if (registeredBatchCodes.contains(batch.getCode().toUpperCase())) {
            throw new IllegalArgumentException("Ya existe un lote registrado con el código: " + batch.getCode());
        }
        Warehouse target = findWarehouseByCode(warehouseCode);
        if (target == null) {
            throw new IllegalArgumentException("No se encontró ninguna bodega con código: " + warehouseCode);
        }

        target.addBatch(batch);
        registeredBatchCodes.add(batch.getCode().toUpperCase());

        if (registerPayment) {
            String cat = normalizeCategory(batch.getCategoryName());
            paidTotals.put(cat, paidTotals.getOrDefault(cat, 0.0) + batch.calculatePayment());
        }
    }

    public double processDispatch(String category, double requestedWeight) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede ser nula o vacía.");
        }
        if (requestedWeight <= 0) {
            throw new IllegalArgumentException("El peso solicitado debe ser mayor a cero.");
        }

        String normalizedCategory = normalizeCategory(category);
        List<Batch> matchingBatches = new ArrayList<>();
        Map<Batch, Warehouse> batchWarehouseMap = new HashMap<>();
        double totalAvailable = 0.0;

        for (Warehouse w : warehouses) {
            for (Batch b : w.getBatches()) {
                if (b.isAvailable() && normalizeCategory(b.getCategoryName()).equals(normalizedCategory)) {
                    matchingBatches.add(b);
                    batchWarehouseMap.put(b, w);
                    totalAvailable += b.getAvailableWeight();
                }
            }
        }

        if (matchingBatches.isEmpty()) {
            throw new IllegalArgumentException("No hay existencias disponibles para la categoría: " + category);
        }
        if (totalAvailable + 1e-9 < requestedWeight) {
            throw new IllegalArgumentException(String.format(
                    "Existencias insuficientes para %s. Solicitado: %.2f kg | Disponible: %.2f kg.",
                    category, requestedWeight, totalAvailable));
        }

        Collections.sort(matchingBatches, Comparator.comparing(Batch::getEntryDate).thenComparing(Batch::getQuality));

        double remainingToDispatch = requestedWeight;
        double totalCharged = 0.0;

        for (Batch b : matchingBatches) {
            if (remainingToDispatch <= 1e-9) break;

            double taken = Math.min(b.getAvailableWeight(), remainingToDispatch);
            double chargeForChunk = taken * b.calculateUnitCost() * 1.35;
            totalCharged += chargeForChunk;

            b.reduceWeight(taken);
            remainingToDispatch -= taken;

            if (!b.isAvailable()) {
                batchWarehouseMap.get(b).removeBatch(b);
            }
        }

        chargedTotals.put(normalizedCategory, chargedTotals.getOrDefault(normalizedCategory, 0.0) + totalCharged);
        return totalCharged;
    }

    public List<String> advanceOperationDate(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Solo se puede avanzar la fecha hacia adelante (1 o más días).");
        }
        this.operationDate = this.operationDate.plusDays(days);
        List<String> discardedLog = new ArrayList<>();

        for (Warehouse w : warehouses) {
            Iterator<Batch> it = w.iterator();
            while (it.hasNext()) {
                Batch b = it.next();
                if (b instanceof Perishable) {
                    Perishable p = (Perishable) b;
                    if (p.isExpired(this.operationDate)) {
                        double loss = b.getAvailableWeight() * b.calculateUnitCost();
                        String cat = b.getCategoryName();
                        lossTotals.put(cat, lossTotals.getOrDefault(cat, 0.0) + loss);
                        discardedLog.add(String.format("Lote %s (%s) descartado de bodega %s | Pérdida: Q%.2f",
                                b.getCode(), cat, w.getCode(), loss));
                        b.discard();
                        it.remove();
                    }
                }
            }
        }
        return discardedLog;
    }

    public List<Batch> getExpiringBatchesReport() {
        List<Batch> expiringSoon = new ArrayList<>();
        LocalDate limitDate = operationDate.plusDays(3);

        for (Warehouse w : warehouses) {
                for (Batch b : w.getBatches()) {
                if (b.isAvailable() && b instanceof Perishable) {
                    LocalDate exp = ((Perishable) b).getExpirationDate();
                    if (!exp.isBefore(operationDate) && !exp.isAfter(limitDate)) {
                        expiringSoon.add(b);
                    }
                }
            }
        }
        Collections.sort(expiringSoon, Comparator.comparing(Batch::getPriorityDate).thenComparing(Batch::getQuality));
        return expiringSoon;
    }

    public String getStockByCategoryReport() {
        StringBuilder sb = new StringBuilder("=== EXISTENCIAS POR CATEGORÍA (EN ORDEN DE SALIDA) ===\n");
        for (String cat : CATEGORIES) {
            List<Batch> catBatches = new ArrayList<>();
            double totalKg = 0.0;
            for (Warehouse w : warehouses) {
                for (Batch b : w.getBatches()) {
                    if (b.isAvailable() && b.getCategoryName().equals(cat)) {
                        catBatches.add(b);
                        totalKg += b.getAvailableWeight();
                    }
                }
            }
            Collections.sort(catBatches, Comparator.comparing(Batch::getEntryDate).thenComparing(Batch::getQuality));
            sb.append(String.format("\n* %s (Total disponible: %.2f kg):\n", cat, totalKg));
            if (catBatches.isEmpty()) {
                sb.append("   Sin existencias.\n");
            } else {
                for (Batch b : catBatches) {
                    sb.append("   - ").append(b).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public String getWarehousesContentReport() {
        StringBuilder sb = new StringBuilder("=== CONTENIDO POR BODEGA (EN ORDEN DE SALIDA) ===\n");
        for (Warehouse w : warehouses) {
            sb.append("\n").append(w).append("\n");
            boolean empty = true;
            for (Batch b : w.getBatches()) {
                sb.append("   - ").append(b).append("\n");
                empty = false;
            }
            if (empty) {
                sb.append("   (Bodega vacía)\n");
            }
        }
        return sb.toString();
    }

    public String getFinancialReport() {
        StringBuilder sb = new StringBuilder("=== REPORTE FINANCIERO POR CATEGORÍA ===\n");
        sb.append(String.format("%-22s | %-15s | %-15s | %-15s\n", "Categoría", "Total Pagado", "Total Cobrado", "Total Pérdidas"));
        sb.append("--------------------------------------------------------------------------\n");
        for (String cat : CATEGORIES) {
            sb.append(String.format("%-22s | Q%-14.2f | Q%-14.2f | Q%-14.2f\n",
                    cat,
                    paidTotals.getOrDefault(cat, 0.0),
                    chargedTotals.getOrDefault(cat, 0.0),
                    lossTotals.getOrDefault(cat, 0.0)));
        }
        return sb.toString();
    }

    private Warehouse findWarehouseByCode(String code) {
        for (Warehouse w : warehouses) {
            if (w.getCode().equalsIgnoreCase(code.trim())) {
                return w;
            }
        }
        return null;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }
}