package org.example;

import java.sql.*;

public class Main {

    // Configuración de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/tienda_online";
    private static final String USER = "root"; // Cambia esto según tu configuración
    private static final String PASSWORD = "tuconstraseña"; // Cambia esto según tu configuración

    public static void main(String[] args) {
        // Ejecutar las compras de Ana y Juan simultáneamente
        Thread thread1 = new Thread(() -> processOrder("Ana", 10));
        Thread thread2 = new Thread(() -> processOrder("Juan", 10));

        thread1.start();
        thread2.start();

        // Esperar a que ambos hilos terminen
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método para procesar un pedido
    public static void processOrder(String clientName, int quantity) {
        System.out.println(clientName + " intenta realizar una compra de " + quantity + " unidades.");

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Leer el stock del producto
            String readStockSQL = "SELECT stock FROM productos WHERE id = ?";
            try (PreparedStatement readStockStmt = connection.prepareStatement(readStockSQL)) {
                readStockStmt.setInt(1, 1); // ID del producto
                ResultSet rs = readStockStmt.executeQuery();

                // Verificar si se encontró el producto
                if (rs.next()) {
                    int stock = rs.getInt("stock"); // Obtener el stock actual
                    System.out.println("Stock actual para el producto antes de la compra: " + stock);

                    // Verificar y actualizar el stock
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
                } else {
                    System.out.println("No se encontró el producto.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al procesar la compra de " + clientName + ": " + e.getMessage());
        }
    }
}
