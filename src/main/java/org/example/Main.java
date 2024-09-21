package org.example;

import java.sql.*;
import java.util.concurrent.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/tienda_online";
    private static final String USER = "root"; // Cambia esto según tu configuración
    private static final String PASSWORD = "tom1"; // Cambia esto según tu configuración

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.schedule(() -> processOrder("Ana", 10), getDelay("11:26"), TimeUnit.MILLISECONDS);
        scheduler.schedule(() -> processOrder("Juan", 10), getDelay("11:26"), TimeUnit.MILLISECONDS);
    }

    private static long getDelay(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime targetTime = LocalTime.parse(time, formatter);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDateTime = now.with(targetTime);

        if (targetDateTime.isBefore(now)) {
            // Si la hora deseada ya ha pasado hoy, ajustar para mañana
            targetDateTime = targetDateTime.plusDays(1);
            System.out.println("La hora programada ya ha pasado. Ajustando para mañana.");
        }

        long delay = Duration.between(now, targetDateTime).toMillis();
        System.out.println("Retraso calculado: " + delay + " milisegundos.");
        return delay;
    }

    public static void processOrder(String clientName, int quantity) {
        System.out.println(clientName + " intenta realizar una compra de " + quantity + " unidades.");

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Leer el stock
            String readStockSQL = "SELECT stock FROM productos WHERE id = ?";
            try (PreparedStatement readStockStmt = connection.prepareStatement(readStockSQL)) {
                readStockStmt.setInt(1, 1); // ID del producto
                ResultSet rs = readStockStmt.executeQuery();
                rs.next();
                int stock = rs.getInt("stock");
                System.out.println("Stock actual para el producto antes de la compra: " + stock);

                // Verificar y actualizar stock
                if (stock >= quantity) {
                    String updateStockSQL = "UPDATE productos SET stock = stock - ? WHERE id = ?";
                    try (PreparedStatement updateStockStmt = connection.prepareStatement(updateStockSQL)) {
                        updateStockStmt.setInt(1, quantity);
                        updateStockStmt.setInt(2, 1); // ID del producto
                        updateStockStmt.executeUpdate();
                        System.out.println(clientName + " ha realizado la compra de " + quantity + " unidades.");
                    }
                } else {
                    System.out.println("Stock insuficiente para " + clientName + ". Stock actual: " + stock);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al procesar la compra de " + clientName + ": " + e.getMessage());
        }
    }
}
