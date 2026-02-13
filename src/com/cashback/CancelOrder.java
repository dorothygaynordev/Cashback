package com.cashback;

import javax.swing.*;
import javax.swing.text.PlainDocument;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.cashback.models.cancel.CancelOrderResponse;
import com.cashback.models.cancel.CancelPartialRequest;
import com.cashback.models.cancel.OrderItem;
import com.cashback.models.order.confirm.response.OrderItemResponse;
import com.cashback.models.order.confirm.response.OrderResponse;
import com.cashback.models.token.TokenResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CancelOrder extends JFrame {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private JTextField orderIdField;
    private JButton cancelButton;
    private java.util.List<Object[]> _selectedItems;
    private final Dotenv dotenv;
    private App _app;
    private int totalItems;

    public CancelOrder(App app, java.util.List<Object[]> selectedItems) {
        _app = app;
        dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .load();

        _selectedItems = selectedItems;
        // totalItems = _totalItems;
        
        configurarVentana();
        inicializarComponentes();
        cargarUltimaOrden();
    }

    private void configurarVentana() {
        setTitle("Cancelar Orden");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 40));
    }

    private void inicializarComponentes() {
        orderIdField = new JTextField(20);
        orderIdField.setDocument(new PlainDocument());

        cancelButton = new JButton("Cancelar Orden");
        cancelButton.addActionListener(this::confirmarCancelacion);

        add(new JLabel("Id de la orden a cancelar:"));
        add(orderIdField);
        add(cancelButton);
    }

    private void confirmarCancelacion(ActionEvent e) {
        String orderId = orderIdField.getText().trim();
        final AtomicReference<String> orderTidRef = new AtomicReference<>("");

        if (orderId.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese el ID de la orden", 
                "Campo vacío", 
                JOptionPane.WARNING_MESSAGE);
            orderIdField.requestFocus();
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea cancelar la orden:\n" + orderId + "?",
            "Confirmar cancelación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        cancelButton.setEnabled(false);
    
        orderTidRef.set(getTidByVenta(orderId));
        String orderTid = orderTidRef.get();

        if (orderTid == null || orderTid.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No se encontró ID asociado a la venta: " + orderId, 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            cancelButton.setEnabled(true);
            return;
        }

        List<OrderItem> itemsToCancel = validateCancelacion(orderId, _selectedItems);

        if (itemsToCancel.isEmpty()) {
            cancelButton.setEnabled(true);
            return;
        }

        new Thread(() -> {
            boolean resultado;
            if (itemsToCancel.size() == totalItems) {
                resultado = procesarCancelacionByTid(orderTid);
            } else {
                resultado = procesarCancelacionByTid(orderTid, itemsToCancel);
            }
            
            SwingUtilities.invokeLater(() -> {
                showResultCancelacion(resultado);
                cancelButton.setEnabled(true);
            });
        }).start();
    }

    private void showResultCancelacion(boolean exito) {
        if (exito) {
            JOptionPane.showMessageDialog(this,
                "Orden cancelada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            orderIdField.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al cancelar la orden",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        cancelButton.setEnabled(true);
    }

    private boolean procesarCancelacionByTid(String orderTid) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            TokenResponse token = obtenerToken();
            if (token == null) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo obtener token de autenticación", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String apiUrl = dotenv.get("API_GUPER_URL");
            String cancelUrl = apiUrl + "/loyalty/cancelOrderByTransaction/" + java.net.URLEncoder.encode(orderTid, "UTF-8");
            
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(cancelUrl);
            
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-guper-authorization", token.getAccessToken());
            
            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                CancelOrderResponse cancelResponse = objectMapper.readValue(responseBody, CancelOrderResponse.class);
                System.out.println("Detalles de la cancelación - TID: " + cancelResponse.getTid() + 
                                 ", Order ID: " + cancelResponse.getOrder());
                System.out.println("Orden cancelada exitosamente: " + responseBody);
                return true;
            } else {
                System.err.println("Error al cancelar orden - Código: " + statusCode + 
                                 "\nRespuesta: " + responseBody);
                return false;
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }

    private boolean procesarCancelacionByTid(String orderTid, List<OrderItem> itemsToCancel) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            TokenResponse token = obtenerToken();
            if (token == null) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo obtener token de autenticación", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String apiUrl = dotenv.get("API_GUPER_URL");
            String cancelUrl = apiUrl + "/loyalty/cancelPartial/" + java.net.URLEncoder.encode(orderTid, "UTF-8");
            
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(cancelUrl);
            
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-guper-authorization", token.getAccessToken());

            CancelPartialRequest cancelRequest = new CancelPartialRequest(itemsToCancel);
            String requestBody = objectMapper.writeValueAsString(cancelRequest);
            StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == 200) {
                CancelOrderResponse cancelResponse = objectMapper.readValue(responseBody, CancelOrderResponse.class);
                System.out.println("Detalles de la cancelación - TID: " + cancelResponse.getTid() + 
                                 ", Order ID: " + cancelResponse.getOrder());
                System.out.println("Orden cancelada exitosamente: " + responseBody);
                return true;
            } else {
                System.err.println("Error al cancelar orden - Código: " + statusCode + 
                                 "\nRespuesta: " + responseBody);
                return false;
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }

    private TokenResponse obtenerToken() {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;

        try {
            String apiUrl = dotenv.get("API_GUPER_URL");
            String apiKey = dotenv.get("API_GUPER_APIKEY");
            String apiSecret = dotenv.get("API_GUPER_APISECRET");
            String headerApiKey = dotenv.get("HEADER_APIKEY");
            String headerApiSecret = dotenv.get("HEADER_APISECRET");

            String API_URL_TOKEN = apiUrl + "/connect/token";
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(API_URL_TOKEN);

            httpGet.setHeader(headerApiKey, apiKey);
            httpGet.setHeader(headerApiSecret, apiSecret);

            response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode == 200) {
                return objectMapper.readValue(responseBody, TokenResponse.class);
            } else {
                System.err.println("Error al obtener token. Código: " + statusCode);
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }

    private void cerrarRecursos(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        try {
            if (response != null) response.close();
            if (httpClient != null) httpClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cargarUltimaOrden() {
        String ultimoTid = _app.getLastOrderId();
        System.out.println("Último ID obtenido: " + ultimoTid);
        
        if (ultimoTid != null && !ultimoTid.isEmpty()) {
            orderIdField.setText(ultimoTid);
            System.out.println("ID cargado en campo: " + ultimoTid);
        } else {
            System.out.println("No hay ID reciente disponible");
        }
    }

    private String getTidByVenta(String idVenta) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT TID FROM Ventas WHERE IdVenta = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, idVenta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("TID");
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private List<OrderItem> validateCancelacion(String orderId, java.util.List<Object[]> selectedItems) {
        try {
            OrderResponse ordenGuper = getOrderById(orderId);

            if (ordenGuper == null) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo obtener la información de la orden.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return Collections.emptyList();
            }

            if (ordenGuper.getOrderStatus().equalsIgnoreCase("CANCELED")) {
                JOptionPane.showMessageDialog(this, 
                    "La orden ya se encuentra cancelada.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return Collections.emptyList();
            }

            List<OrderItemResponse> itemsOrdenGuper = ordenGuper.getItems();
            List<OrderItem> itemsToCanceled = new ArrayList<>();

            for (Object[] selectedItem : selectedItems) {
                String sku = selectedItem[1].toString();
                Integer quantityToCancel = (Integer) selectedItem[6];

                OrderItemResponse itemOrden = findItemBySku(itemsOrdenGuper, sku);

                if (itemOrden == null) continue;

                int availableToCancel = itemOrden.getQuantity() - itemOrden.getQuantityCanceled();
                int quantityFinalCancel = Math.min(quantityToCancel, availableToCancel);

                double priceValue = 0.0;
                if (selectedItem[7] instanceof Number) {
                    priceValue = ((Number) selectedItem[7]).doubleValue();
                } else if (selectedItem[7] instanceof String) {
                    try {
                        priceValue = Double.parseDouble((String) selectedItem[7]);
                    } catch (NumberFormatException ex) {
                        priceValue = 0.0;
                    }
                }

                Integer price = (int) Math.round(priceValue * 100);

                OrderItem orderItem = new OrderItem(sku, quantityFinalCancel, price);
                itemsToCanceled.add(orderItem);
            }

            if (itemsToCanceled.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "La orden ya se encuentra cancelada.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return Collections.emptyList();
            }
            
            return itemsToCanceled;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al validar la orden: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return Collections.emptyList();
        }
    }

    private OrderItemResponse findItemBySku(List<OrderItemResponse> items, String skuId) {
        if (items == null) return null;
        
        for (OrderItemResponse item : items) {
            if (item.getSkuId().equals(skuId) && item.getQuantity() > item.getQuantityCanceled()) {
                return item;
            }
        }
        return null;
    }

    private OrderResponse getOrderById(String orderId) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            TokenResponse token = obtenerToken();
            if (token == null) {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo obtener token de autenticación", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }

            String apiUrl = dotenv.get("API_GUPER_URL");
            String apiInterface = dotenv.get("API_GUPER_INTERFACE");
            String orderUrl = apiUrl + "/order/" + java.net.URLEncoder.encode(apiInterface, "UTF-8") + "/data/" + java.net.URLEncoder.encode(orderId, "UTF-8");
            
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(orderUrl);
            
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("x-guper-authorization", token.getAccessToken());
            
            response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                return objectMapper.readValue(responseBody, OrderResponse.class);
            } else {
                System.err.println("Error al cancelar orden - Código: " + statusCode + 
                                 "\nRespuesta: " + responseBody);
                return null;
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }
}
