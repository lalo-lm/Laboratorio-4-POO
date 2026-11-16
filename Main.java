import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        CollectionSystem system = new CollectionSystem(LocalDate.of(2026, 3, 10));
        loadInitialData(system);

        boolean running = true;
        while (running) {
            showMenu(system.getOperationDate());
            try {
                int option = Integer.parseInt(scanner.nextLine().trim());
                System.out.println();

                switch (option) {
                    case 1:
                        handleBatchRegistration(system);
                        break;
                    case 2:
                        handleWarehouseRegistration(system);
                        break;
                    case 3:
                        handleDispatch(system);
                        break;
                    case 4:
                        handleAdvanceDate(system);
                        break;
                    case 5:
                        System.out.println(system.getStockByCategoryReport());
                        System.out.println(system.getWarehousesContentReport());
                        break;
                    case 6:
                        handleReports(system);
                        break;
                    case 0:
                        running = false;
                        System.out.println("Saliendo del sistema AcopioGT...");
                        break;
                    default:
                        System.out.println("Error: Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error de formato: Debe ingresar un número válido.");
            } catch (DateTimeParseException e) {
                System.out.println("Error de fecha: Use el formato AAAA-MM-DD (ej. 2026-03-10).");
            } catch (IllegalArgumentException e) {
                System.out.println("Operación rechazada: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
        }
    }

    private static void showMenu(LocalDate currentDate) {
        System.out.println("\n=============================================");
        System.out.println("   ACOPIO GT - Fecha de Operación: " + currentDate);
        System.out.println("=============================================");
        System.out.println("1. Pre-liquidar y recibir lote");
        System.out.println("2. Registrar nueva bodega");
        System.out.println("3. Registrar pedido y despachar");
        System.out.println("4. Avanzar fecha de operación");
        System.out.println("5. Consultar lotes y bodegas (Orden de salida)");
        System.out.println("6. Generar reportes gerenciales");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void loadInitialData(CollectionSystem system) {
        system.registerWarehouse(new Warehouse("BOD-S1", 5000.0, false));
        system.registerWarehouse(new Warehouse("BOD-S2", 3000.0, false));
        system.registerWarehouse(new Warehouse("BOD-F1", 2000.0, true));

        system.loadInitialBatch(new ParchmentCoffee("CP-001", "Finca El Mirador", LocalDate.of(2026, 3, 1), 500.0, 'A', 13.4), "BOD-S1");
        system.loadInitialBatch(new ParchmentCoffee("CP-002", "Cooperativa Sur", LocalDate.of(2026, 3, 5), 400.0, 'B', 11.5), "BOD-S2");

        system.loadInitialBatch(new OrganicCoffee("CO-001", "Asociación Verde", LocalDate.of(2026, 3, 5), 300.0, 'B', 11.0, "Mayacert", LocalDate.of(2026, 12, 31)), "BOD-S1");
        system.loadInitialBatch(new OrganicCoffee("CO-002", "EcoCafé", LocalDate.of(2026, 3, 5), 250.0, 'A', 10.5, "BioLatina", LocalDate.of(2026, 10, 15)), "BOD-S2");

        system.loadInitialBatch(new Cardamom("CA-001", "AgroAlta", LocalDate.of(2026, 2, 15), 80.0, 'B', 90.0), "BOD-S1");
        system.loadInitialBatch(new Cardamom("CA-002", "Cobán Export", LocalDate.of(2026, 3, 2), 120.0, 'A', 78.0), "BOD-S2");

        system.loadInitialBatch(new Vegetable("HO-001", "Huertos del Valle", LocalDate.of(2026, 3, 5), 200.0, 'A', 7), "BOD-F1");
        system.loadInitialBatch(new Vegetable("HO-002", "Verduras Frescas", LocalDate.of(2026, 3, 8), 150.0, 'B', 10), "BOD-F1");

        system.loadInitialBatch(new FreshCheese("QF-001", "Lácteos Oriente", LocalDate.of(2026, 3, 1), 40.0, 'A', 28.0), "BOD-F1");
        system.loadInitialBatch(new FreshCheese("QF-002", "Granja San José", LocalDate.of(2026, 3, 7), 60.0, 'B', 22.0), "BOD-F1");
    }

    private static void handleBatchRegistration(CollectionSystem system) {
        System.out.println("Categorías disponibles: 1. Café en pergamino | 2. Café orgánico | 3. Cardamomo | 4. Hortaliza | 5. Queso fresco");
        System.out.print("Seleccione categoría: ");
        int catOption = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Código del lote: ");
        String code = scanner.nextLine();
        System.out.print("Nombre del productor: ");
        String producer = scanner.nextLine();
        System.out.print("Peso recibido (kg): ");
        double weight = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Calidad (A, B, C): ");
        char quality = scanner.nextLine().trim().charAt(0);

        Batch batch;
        LocalDate entryDate = system.getOperationDate();

        switch (catOption) {
            case 1:
                System.out.print("Humedad (%): ");
                double hum = Double.parseDouble(scanner.nextLine().trim());
                batch = new ParchmentCoffee(code, producer, entryDate, weight, quality, hum);
                break;
            case 2:
                System.out.print("Humedad (%): ");
                double orgHum = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Entidad certificadora: ");
                String entity = scanner.nextLine();
                System.out.print("Fecha validez del sello (AAAA-MM-DD): ");
                LocalDate validDate = LocalDate.parse(scanner.nextLine().trim());
                batch = new OrganicCoffee(code, producer, entryDate, weight, quality, orgHum, entity, validDate);
                break;
            case 3:
                System.out.print("Porcentaje grano verde (%): ");
                double green = Double.parseDouble(scanner.nextLine().trim());
                batch = new Cardamom(code, producer, entryDate, weight, quality, green);
                break;
            case 4:
                System.out.print("Vida útil (días, 3 a 14): ");
                int shelf = Integer.parseInt(scanner.nextLine().trim());
                batch = new Vegetable(code, producer, entryDate, weight, quality, shelf);
                break;
            case 5:
                System.out.print("Porcentaje de grasa (%): ");
                double fat = Double.parseDouble(scanner.nextLine().trim());
                batch = new FreshCheese(code, producer, entryDate, weight, quality, fat);
                break;
            default:
                throw new IllegalArgumentException("Categoría no válida.");
        }

        System.out.println("\n" + system.preLiquidateBatch(batch));
        System.out.print("¿El productor acepta la pre-liquidación? (S/N): ");
        String accept = scanner.nextLine().trim();

        if (accept.equalsIgnoreCase("S")) {
            System.out.print("Ingrese el código de la bodega destino: ");
            String whCode = scanner.nextLine();
            system.receiveBatch(batch, whCode);
            System.out.println("Lote recibido y almacenado exitosamente.");
        } else {
            System.out.println("Pre-liquidación cancelada. No se registró ningún pago ni existencia.");
        }
    }

    private static void handleWarehouseRegistration(CollectionSystem system) {
        System.out.print("Código de la bodega: ");
        String code = scanner.nextLine();
        System.out.print("Capacidad máxima (kg): ");
        double cap = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("¿Es cámara fría? (S/N): ");
        boolean cold = scanner.nextLine().trim().equalsIgnoreCase("S");

        system.registerWarehouse(new Warehouse(code, cap, cold));
        System.out.println("Bodega registrada exitosamente.");
    }

    private static void handleDispatch(CollectionSystem system) {
        System.out.print("Categoría solicitada (ej. Café en pergamino, Cardamomo, Hortaliza, Queso fresco, Café orgánico): ");
        String category = scanner.nextLine();
        System.out.print("Cantidad solicitada (kg): ");
        double weight = Double.parseDouble(scanner.nextLine().trim());

        double totalCharged = system.processDispatch(category, weight);
        System.out.printf("Despacho completado exitosamente. Total cobrado al comprador: Q%.2f\n", totalCharged);
    }

    private static void handleAdvanceDate(CollectionSystem system) {
        System.out.print("¿Cuántos días desea avanzar?: ");
        int days = Integer.parseInt(scanner.nextLine().trim());
        List<String> discarded = system.advanceOperationDate(days);
        System.out.println("Nueva fecha de operación: " + system.getOperationDate());
        if (discarded.isEmpty()) {
            System.out.println("No venció ningún lote en este período.");
        } else {
            System.out.println("Lotes descartados por vencimiento:");
            for (String log : discarded) {
                System.out.println("  * " + log);
            }
        }
    }

    private static void handleReports(CollectionSystem system) {
        System.out.println("=== LOTES PERECEDEROS QUE VENCEN EN LOS PRÓXIMOS 3 DÍAS ===");
        List<Batch> expiring = system.getExpiringBatchesReport();
        if (expiring.isEmpty()) {
            System.out.println("Ningún lote perecedero vence en los próximos 3 días.\n");
        } else {
            for (Batch b : expiring) {
                System.out.println("  * " + b);
            }
            System.out.println();
        }
        System.out.println(system.getFinancialReport());
    }
}