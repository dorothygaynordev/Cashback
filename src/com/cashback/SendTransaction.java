package com.cashback;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.table.TableModel;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.cashback.models.loyalty.request.Attendants;
import com.cashback.models.loyalty.request.Client;
import com.cashback.models.loyalty.request.Item;
import com.cashback.models.loyalty.request.LoyaltyRequest;
import com.cashback.models.loyalty.request.Attendants.Salesperson;
import com.cashback.models.loyalty.response.LoyaltyResponse;
import com.cashback.models.order.confirm.request.ConfirmRequest;
import com.cashback.models.order.confirm.request.Payment;
import com.cashback.models.order.confirm.response.ConfirmResponse;
import com.cashback.models.pin.PinValidationRequest;
import com.cashback.models.token.TokenResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class SendTransaction extends JFrame {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JTextField telephone;
    private JButton sendTelephone;
    private JButton cancelTelephone;
    private final Dotenv dotenv;
    private static String lastOrderId;
    private static String lastOrderTid;

    public SendTransaction() {
        dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .load();

        setTitle("Alta Cashback");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 40));

        telephone = new JTextField(20);
        sendTelephone = new JButton("Enviar");
        cancelTelephone = new JButton("Cancelar");

        validationsPhoneNumber();

        sendTelephone.addActionListener(this::sendTelephoneGuper);
        cancelTelephone.addActionListener(e -> cancelSendTelephone(e));

        add(new JLabel("Ingrese el número telefónico del cliente"));
        add(telephone);
        add(sendTelephone);
        add(cancelTelephone);

        setupWindowClosingBehavior();
    }

    private void setupWindowClosingBehavior() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Agregar el listener para capturar el evento de cierre
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Usuario cerró la ventana - ejecutando cancelSendTelephone");
                cancelSendTelephone(null);
            }
        });
    }

    private void sendTelephoneGuper(ActionEvent e) {
        String numero = telephone.getText().trim();

        sendTelephone.setEnabled(false);

        new Thread(() -> {
            try {
                TokenResponse token = obtenerToken();
                if (token == null) {
                    showErrorMessage("No se pudo obtener el token de autenticación");
                    enableButton();
                    return;
                }

                // Obtener el TableModel
                TableModel tableModel = getTableModel();

                if (tableModel == null || tableModel.getRowCount() == 0) {
                    showErrorMessage("No hay productos en la tabla");
                    enableButton();
                    return;
                }

                LoyaltyResponse transaccionExitosa = enviarTransaccionReward(numero, token, tableModel);
                if (transaccionExitosa == null) {
                    showErrorMessage("Sin cashback que aplicar.");
                    enableButton();
                    return;
                }

                Integer cashbackAcumulado = transaccionExitosa.getCashback().getUserBalance().getAvailableAmount();
                Integer montoRedimible = transaccionExitosa.getCashback().getThisOrder().getRedeemable().getTotal();

                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                String cashbackAcumuladoFormateado = decimalFormat.format(cashbackAcumulado / 100.0);
                String montoRedimibleFormateado = decimalFormat.format(montoRedimible / 100.0);

                String titulo = "Ajuste de Cashback";
                String mensaje = String.format(
                    "<html><b>Cashback acumulado:</b> $%s<br>" +
                    "<b>Saldo disponible:</b> $%s</html>",
                    cashbackAcumuladoFormateado,
                    montoRedimibleFormateado
                );

                JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);

                if (montoRedimible > 0) {
                    boolean pinExitoso = solicitarPin(token, transaccionExitosa.getCustomerId());
                    if (!pinExitoso) {
                        showErrorMessage("Proceso cancelado por error en PIN");
                        enableButton();
                        return;
                    }
                }

                ConfirmResponse completeTransaction = confirmOrder(token, transaccionExitosa.getConfirmToken(), montoRedimible);

                if (completeTransaction != null) {
                    lastOrderTid = completeTransaction.getTid();
                    lastOrderId = completeTransaction.getOrderId();

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String sql = "INSERT INTO Ventas (IdVenta, TID) VALUES (?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, lastOrderId);
                        ps.setString(2, lastOrderTid);
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showErrorMessage("❌ Error al guardar: " + ex.getMessage());
                    }

                    showSuccessMessage("Transacción completada exitosamente.\n" +
                        "Order ID: " + lastOrderId + "\n" +
                        "TID: " + lastOrderTid);
                }

                enableButton();

            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error en el proceso: " + ex.getMessage());
                enableButton();
            }
        }).start();
    }

    private void cancelSendTelephone(ActionEvent e) {
        // String numero = "";
        String numero = telephone.getText().trim();
        new Thread(() -> {
            try {
                TokenResponse token = obtenerToken();
                if (token == null) {
                    showErrorMessage("No se pudo obtener el token de autenticación");
                    enableButton();
                    return;
                }

                // Obtener el TableModel
                TableModel tableModel = getTableModel();

                if (tableModel == null || tableModel.getRowCount() == 0) {
                    showErrorMessage("No hay productos en la tabla");
                    enableButton();
                    return;
                }

                LoyaltyResponse transaccionExitosa = enviarTransaccionReward(numero, token, tableModel);
                if (transaccionExitosa == null) {
                    showErrorMessage("Ocurrio un error al procesar la transacción.");
                    enableButton();
                    return;
                }

                ConfirmResponse completeTransaction = confirmOrder(token, transaccionExitosa.getConfirmToken(), null);

                if (completeTransaction != null) {
                    lastOrderTid = completeTransaction.getTid();
                    lastOrderId = completeTransaction.getOrderId();

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String sql = "INSERT INTO Ventas (IdVenta, TID) VALUES (?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, lastOrderId);
                        ps.setString(2, lastOrderTid);
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showErrorMessage("❌ Error al guardar: " + ex.getMessage());
                    }

                    showSuccessMessage("Transacción completada exitosamente.\n" +
                        "Order ID: " + lastOrderId + "\n" +
                        "TID: " + lastOrderTid);
                }

                enableButton();

            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Error en el proceso: " + ex.getMessage());
                enableButton();
            }
        }).start();

        dispose();
    }

    private void validationsPhoneNumber() {
        // Limitar la longitud máxima a 10 caracteres usando PlainDocument
        telephone.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) 
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                
                // Solo permitir dígitos y longitud máxima de 10
                if (str.matches("\\d+") && (getLength() + str.length()) <= 10) {
                    super.insertString(offset, str, attr);
                }
            }
        });
        
        // Agregar tooltip para informar al usuario
        telephone.setToolTipText("Solo números, máximo 10 dígitos");
        
        // Agregar DocumentListener para validación en tiempo real
        telephone.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validatePhoneRealTime();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validatePhoneRealTime();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validatePhoneRealTime();
            }
        });
        
        // Inicialmente deshabilitar el botón
        sendTelephone.setEnabled(false);
    }

    private void validatePhoneRealTime() {
        String numero = telephone.getText().trim();
        boolean esValido = true;
        
        // Validación 1: No vacío
        if (numero.isEmpty()) {
            esValido = false;
            telephone.setToolTipText("Ingrese el número de teléfono");
        }
        // Validación 2: Solo números
        else if (!numero.matches("\\d+")) {
            esValido = false;
            telephone.setToolTipText("Solo se permiten números");
        }
        // Validación 3: Longitud exacta de 10 dígitos
        else if (numero.length() != 10) {
            esValido = false;
            telephone.setToolTipText("Debe tener exactamente 10 dígitos (" + numero.length() + "/10)");
        }
        
        // Habilitar/deshabilitar botón basado en validación
        sendTelephone.setEnabled(esValido);
    }

    private TokenResponse obtenerToken() {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;

        try {
            // Validar variables de entorno
            if (!validarVariablesEntorno()) {
                return null;
            }

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

    private LoyaltyResponse enviarTransaccionReward(String phoneNumber, TokenResponse token, TableModel tableModel) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        LoyaltyResponse loyaltyResponse = new LoyaltyResponse();
        
        try {
            String apiUrl = dotenv.get("API_GUPER_URL");
            String rewardApiUrl = apiUrl + "/loyalty/rewardByOrder";
            String apiInterface = dotenv.get("API_GUPER_INTERFACE");

            LoyaltyRequest request = crearLoyaltyRequest(apiInterface, phoneNumber, tableModel);
            
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(rewardApiUrl);
            
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-guper-authorization", token.getAccessToken());
            
            String requestBody = objectMapper.writeValueAsString(request);

            StringEntity entity = new StringEntity(requestBody, "UTF-8");
            httpPost.setEntity(entity);

            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                loyaltyResponse = objectMapper.readValue(responseBody, LoyaltyResponse.class);
            } else {
                System.err.println("Error en rewardByOrder - Código: " + statusCode + 
                                 "\nRespuesta: " + responseBody);
            }

            return loyaltyResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
            return loyaltyResponse;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }

    private LoyaltyRequest crearLoyaltyRequest(String apiInterface, String phoneNumber, TableModel tableModel) {
        Client client = new Client();
        client.setCellphone(phoneNumber.isEmpty() ? null : phoneNumber);
        
        List<Item> items = convertTableModelToItems(tableModel);

        Attendants attendants = new Attendants(); 
        String vendedor = firstOrDefault(tableModel, 10);
        if (vendedor != null) {
            Salesperson salesperson = new Salesperson();
            salesperson.setId(vendedor);
            attendants.setSalesperson(salesperson);
        }
        
        LoyaltyRequest request = new LoyaltyRequest();
        request.setInterfaceName(apiInterface);
        request.setStoreId(dotenv.get("API_GUPER_TIENDA"));
        if (!phoneNumber.isEmpty()) {
            request.setClient(client);
        }
        request.setItems(items);
        request.setAttendants(attendants);
        request.setShipping(Arrays.asList());
        
        return request;
    }

    private List<Item> convertTableModelToItems(TableModel tableModel) {
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                Item item = new Item();
                
                // SKU (columna 0)
                if (tableModel.getValueAt(i, 1) != null) {
                    item.setId(tableModel.getValueAt(i, 1).toString());
                } else {
                    System.err.println("Fila " + i + ": SKU faltante, omitiendo...");
                    continue; // Saltar filas sin SKU
                }
                
                // Nombre (columna 2)
                if (tableModel.getValueAt(i, 3) != null) {
                    item.setName(tableModel.getValueAt(i, 3).toString());
                } else {
                    System.err.println("Fila " + i + ": Descripcion faltante, omitiendo...");
                    continue;
                }
                
                // Precio (columna 6) - convertir de double a centavos
                if (tableModel.getValueAt(i, 7) != null) {
                    try {
                        double precioDouble = (Double) tableModel.getValueAt(i, 7);
                        int precioEntero = (int) Math.round(precioDouble * 100);
                        item.setPrice(precioEntero);
                    } catch (ClassCastException e) {
                        // Si no es Double, intentar convertir desde String
                        try {
                            double precioDouble = Double.parseDouble(tableModel.getValueAt(i, 7).toString());
                            int precioEntero = (int) (precioDouble * 100);
                            item.setPrice(precioEntero);
                        } catch (NumberFormatException ex) {
                            System.err.println("Fila " + i + ": Precio inválido, usando 0");
                            item.setPrice(0);
                        }
                    }
                } else {
                    item.setPrice(0);
                }
                
                // Cantidad (columna 5)
                if (tableModel.getValueAt(i, 6) != null) {
                    try {
                        int cantidad = (Integer) tableModel.getValueAt(i, 6);
                        item.setQuantity(Math.max(1, cantidad)); // Mínimo 1
                    } catch (ClassCastException e) {
                        // Si no es Integer, intentar convertir desde String
                        try {
                            int cantidad = Integer.parseInt(tableModel.getValueAt(i, 6).toString());
                            item.setQuantity(Math.max(1, cantidad));
                        } catch (NumberFormatException ex) {
                            System.err.println("Fila " + i + ": Cantidad inválida, usando 1");
                            item.setQuantity(1);
                        }
                    }
                } else {
                    item.setQuantity(1);
                }
                
                items.add(item);
                
            } catch (Exception e) {
                System.err.println("Error al procesar fila " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return items;
    }

    private String firstOrDefault(TableModel tableModel, int columnIndex) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object value = tableModel.getValueAt(i, columnIndex);
            if (value != null && !value.toString().trim().isEmpty()) {
                return value.toString().trim();
            }
        }
        return null;
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

    private boolean solicitarPin(TokenResponse token, Long customerId) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            String apiUrl = dotenv.get("API_GUPER_URL");
            String pinUrl = apiUrl + "/register/customer/" + customerId + "/pin";
            
            httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(pinUrl);
            
            httpGet.setHeader("Content-Type", "application/json");
            httpGet.setHeader("x-guper-authorization", token.getAccessToken());

            response = httpClient.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                System.out.println("PIN enviado exitosamente");
            
                String pinIngresado = mostrarDialogoPin();
                if (pinIngresado != null && pinIngresado.length() == 4) {
                    return validatePin(token, customerId, pinIngresado);
                } else if (pinIngresado == null) {
                    showWarningMessage("Proceso cancelado por el usuario");
                    return false;
                } else {
                    showErrorMessage("El PIN debe tener exactamente 4 dígitos");
                    return false;
                }
            } else {
                System.err.println("Error al enviar el PIN: " + statusCode + 
                                 "\nRespuesta: " + responseBody);

                showErrorMessage("Error al enviar el PIN al cliente");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error de conexión al solicitar PIN: " + ex.getMessage());
            return false;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }

    private boolean validatePin(TokenResponse token, Long customerId, String pin) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            String apiUrl = dotenv.get("API_GUPER_URL");
            String validatePinUrl = apiUrl + "/register/customer/" + customerId + "/pin/validate";
            
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(validatePinUrl);
            
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("x-guper-authorization", token.getAccessToken());
            
            PinValidationRequest pinRequest = new PinValidationRequest(pin);
            String requestBody = objectMapper.writeValueAsString(pinRequest);

            StringEntity entity = new StringEntity(requestBody, "UTF-8");
            httpPost.setEntity(entity);

            response = httpClient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

            if (statusCode == 200) {
                System.out.println("PIN validado exitosamente");
                return true;
            } else {
                System.err.println("Error al validar PIN - Código: " + statusCode + 
                                 "\nRespuesta: " + responseBody);
                
                showErrorMessage("PIN incorrecto");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error de conexión al validar PIN: " + ex.getMessage());
            return false;
        } finally {
            cerrarRecursos(httpClient, response);
        }
    }

    private String mostrarDialogoPin() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        // Crear 4 JTextField con tamaño limitado
        JTextField[] pinFields = new JTextField[4];
        
        for (int i = 0; i < 4; i++) {
            pinFields[i] = new JTextField(1);
            pinFields[i].setHorizontalAlignment(JTextField.CENTER);
            pinFields[i].setDocument(new LimitDocument(1)); // Limitar a 1 carácter
            
            final int index = i;
            
            // Agregar KeyListener para navegación automática
            pinFields[i].addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!Character.isDigit(c)) {
                        e.consume(); // Solo aceptar dígitos
                        return;
                    }
                    
                    // Programar el focus al siguiente campo después de ingresar un dígito
                    SwingUtilities.invokeLater(() -> {
                        if (index < 3) {
                            pinFields[index + 1].requestFocus();
                        } else {
                            // En el último campo, simular Enter
                            pinFields[index].transferFocus();
                        }
                    });
                }
                
                @Override
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                        if (pinFields[index].getText().isEmpty() && index > 0) {
                            // Retroceder al campo anterior si está vacío y presionamos backspace
                            pinFields[index - 1].requestFocus();
                        }
                    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && index == 3) {
                        // En el último campo, procesar el PIN al presionar Enter
                        procesarPinIngresado(pinFields);
                    }
                }
            });
            
            panel.add(pinFields[i]);
            if (i < 3) {
                panel.add(Box.createHorizontalStrut(10)); // Espacio entre campos
            }
        }
        
        int result = JOptionPane.showConfirmDialog(
            this, 
            panel, 
            "Ingrese el PIN de 4 dígitos", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            // Construir el PIN completo
            StringBuilder pin = new StringBuilder();
            for (JTextField field : pinFields) {
                pin.append(field.getText());
            }
            return pin.toString();
        }
        
        return null;
    }

    private void procesarPinIngresado(JTextField[] pinFields) {
        // Verificar que todos los campos tengan un dígito
        for (JTextField field : pinFields) {
            if (field.getText().isEmpty()) {
                field.requestFocus();
                return;
            }
        }
        
        // Cerrar el diálogo programáticamente
        Window window = SwingUtilities.getWindowAncestor(pinFields[0]);
        if (window != null) {
            window.setVisible(false);
        }
    }

    private boolean validarVariablesEntorno() {
        String apiUrl = dotenv.get("API_GUPER_URL");
        String apiKey = dotenv.get("API_GUPER_APIKEY");
        String apiSecret = dotenv.get("API_GUPER_APISECRET");
        
        if (apiUrl == null || apiKey == null || apiSecret == null) {
            showErrorMessage("Error al obtener datos de conexión (.env incompleto)");
            return false;
        }
        return true;
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

    private void showSuccessMessage(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE));
    }

    private void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void showWarningMessage(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE));
    }

    private void enableButton() {
        SwingUtilities.invokeLater(() -> sendTelephone.setEnabled(true));
    }

    class LimitDocument extends javax.swing.text.PlainDocument {
        private int limit;
        
        public LimitDocument(int limit) {
            this.limit = limit;
        }
        
        @Override
        public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) 
                throws javax.swing.text.BadLocationException {
            if (str == null) return;
            
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }

    private TableModel getTableModel() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof App) {
                App app = (App) window;
                return app.getTable().getModel();
            }
        }
        return null;
    }

    public static String getLastOrderId() {
        return lastOrderId;
    }

    public static String getLastOrderTid() {
        return lastOrderTid;
    }
}