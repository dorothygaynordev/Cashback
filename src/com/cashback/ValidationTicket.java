package com.cashback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.cashback.models.order.confirm.request.ConfirmRequest;
import com.cashback.models.order.confirm.request.Payment;
import com.cashback.models.order.confirm.response.ConfirmResponse;
import com.cashback.models.token.TokenResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class ValidationTicket {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final App _app;
    private final Dotenv dotenv;

    public ValidationTicket(App app) {
        _app = app;
        dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .load();
    }

    public void validateTransaction() {
        JTable table = _app.getTable();
        App.DefaultTableModel tableModel = (App.DefaultTableModel) table.getModel();
        java.util.List<Object[]> selectedItems = tableModel.getSelectedRows();

        double totalItemsActual = 0.0;
        double totalVenta = _app.getMontoTotalVenta();

        for (Object[] row : selectedItems) {
            double precio = Double.parseDouble(row[7].toString());
            int cantidad = Integer.parseInt(row[6].toString());
            totalItemsActual += precio * cantidad;
        }

        if (totalItemsActual != totalVenta) {
            _app.showErrorMessage("El monto total de la orden fue modificado. Favor de recalcular el saldo a redimir.");
            return;
        }

        if (_app.getMontoRedimible() > 0 && !_app.getPinUserValido()) {
            _app.showErrorMessage("El monto a redimir no ha sido validado.");
            return;
        }

        if (tableModel.getSelectedRowCount() != _app.getTotalItems()) {
            _app.showErrorMessage("La orden de compra ha sido modificada, el numero de articulos cambio. Favor de recalcular el saldo a redimir.");
            return;
        }

        new Thread(() -> {
            try {         
                TokenResponse token = obtenerToken();
                if (token == null) {
                    _app.showErrorMessage("No se pudo obtener el token de autenticación");
                    return;
                }

                int montoRedimible = (int)(_app.getMontoRedimible() * 100);
                String confirmToken = _app.getConfirmToken();

                ConfirmResponse completeTransaction = confirmOrder(token, confirmToken, montoRedimible);

                if (completeTransaction != null) {
                    String tid = completeTransaction.getTid();
                    String orderId = completeTransaction.getOrderId();

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String sql = "INSERT INTO Ventas (IdVenta, TID) VALUES (?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, orderId);
                        ps.setString(2, tid);
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        _app.showErrorMessage("❌ Error al guardar: " + ex.getMessage());
                    }

                    _app.showSuccessMessage("Transacción completada exitosamente.\n" +
                        "Order ID: " + orderId + "\n" +
                        "TID: " + tid);

                    _app.CleanStateTransaction();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                _app.showErrorMessage("Error en el proceso: " + ex.getMessage());
            }
        }).start();
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

            // Agregar encabezados
            httpGet.setHeader(headerApiKey, apiKey);
            httpGet.setHeader(headerApiSecret, apiSecret);

            response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                TokenResponse token = objectMapper.readValue(responseBody, TokenResponse.class);
                return token;
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
            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ConfirmResponse confirmOrder(TokenResponse token, String confirmToken, Integer montoRedimido) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        ConfirmResponse confirmResponse = new ConfirmResponse();
        
        try {
            String apiUrl = dotenv.get("API_GUPER_URL");
            String confirmOrderUrl = apiUrl + "/loyalty/confirmOrder/" + java.net.URLEncoder.encode(confirmToken, "UTF-8");

            ConfirmRequest request = createOrderConfirmRequest(montoRedimido);
            
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(confirmOrderUrl);
            
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-guper-authorization", token.getAccessToken());
            
            String requestBody = objectMapper.writeValueAsString(request);

            StringEntity entity = new StringEntity(requestBody, "UTF-8");
            httpPost.setEntity(entity);

            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                confirmResponse = objectMapper.readValue(responseBody, ConfirmResponse.class);
                return confirmResponse;
            } else {
                System.err.println("Error en rewardByOrder - Código: " + statusCode + 
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

    private ConfirmRequest createOrderConfirmRequest(Integer montoRedimido) {
        Payment payment = new Payment();

        List<Payment> payments = Arrays.asList(payment);

        ConfirmRequest request = new ConfirmRequest();
        request.setId("id-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        request.setAmountToRedeem(montoRedimido);
        request.setPayments(payments);

        return request;
    }
}
