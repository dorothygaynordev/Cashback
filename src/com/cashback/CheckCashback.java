package com.cashback;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.*;

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
import com.cashback.models.pin.PinValidationRequest;
import com.cashback.models.token.TokenResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class CheckCashback extends JFrame {
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private JTextField telephone;
    private JButton sendTelephone;
    private JButton cancelTelephone;
    private App _app;
    private final Dotenv dotenv;

    public CheckCashback(App app) {
        _app = app;
        dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .load();
        
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("Consultar Cashback");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 40));
    }

    private void inicializarComponentes() {
        telephone = new JTextField(20);
        sendTelephone = new JButton("Enviar");
        cancelTelephone = new JButton("Cancelar");

        validationsPhoneNumber();

        sendTelephone.addActionListener(e -> {
            if (sendTelephone.isEnabled()) {
                checkCashback(e);
            }
        });

        telephone.addActionListener(e -> {
            if (sendTelephone.isEnabled()) {
                checkCashback(e);
            } else {
                _app.showErrorMessage("Ingrese un número de teléfono válido.");
            }
        });

        cancelTelephone.addActionListener(e -> dispose());

        add(new JLabel("Ingrese el número telefónico del cliente"));
        add(telephone);
        add(sendTelephone);
        add(cancelTelephone);
    }

    private void checkCashback(ActionEvent e) {
        String numero = telephone.getText().trim();

        JTable table = _app.getTable();
        if (table.getSelectedRowCount() == 0) {
            _app.showErrorMessage("No hay articulos seleccionados.");
            enableButton();
            return;
        }

        new Thread(() -> {
            try {         
                TokenResponse token = obtenerToken();
                if (token == null) {
                    _app.showErrorMessage("No se pudo obtener el token de autenticación");
                    enableButton();
                    return;
                }

                LoyaltyResponse transaccionExitosa = enviarTransaccionReward(numero, token, table);
                if (transaccionExitosa == null) {
                    _app.showErrorMessage("Sin cashback que aplicar.");
                    enableButton();
                    return;
                }

                saveVariablesTransaction(transaccionExitosa);

                SwingUtilities.invokeLater(() -> {
                    aplicarCashback(CheckCashback.this);
                });

                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                _app.showErrorMessage("Error en el proceso: " + ex.getMessage());
                enableButton();
            }
        }).start();
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

    private LoyaltyResponse enviarTransaccionReward(String phoneNumber, TokenResponse token, JTable table) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        LoyaltyResponse loyaltyResponse = new LoyaltyResponse();
        
        try {
            String apiUrl = dotenv.get("API_GUPER_URL");
            String rewardApiUrl = apiUrl + "/loyalty/rewardByOrder";
            String apiInterface = dotenv.get("API_GUPER_INTERFACE");

            LoyaltyRequest request = crearLoyaltyRequest(apiInterface, phoneNumber, table);
            
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
                String requestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(loyaltyResponse);
                System.out.println("Request JSON: " + requestJson);
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

    private LoyaltyRequest crearLoyaltyRequest(String apiInterface, String phoneNumber, JTable table) {
        Client client = new Client();
        client.setCellphone(phoneNumber.isEmpty() ? null : phoneNumber);
        
        List<Item> items = convertTableModelToItems(table);
        _app.setTotalItems(items.size());

        int filaSeleccionada = table.getSelectedRow();
        Object vendorObject = table.getValueAt(filaSeleccionada, 11);

        Attendants attendants = new Attendants(); 
        String vendedor = vendorObject.toString();
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

    private List<Item> convertTableModelToItems(JTable table) {
        List<Item> items = new ArrayList<>();
        App.DefaultTableModel tableModel = (App.DefaultTableModel) table.getModel();
        java.util.List<Object[]> selectedRows = tableModel.getSelectedRows();
        double totalVenta = 0.0;

        System.out.println("=== DEBUG CONVERSIÓN ===");

        for (Object[] rowData : selectedRows) {
            try {
                Item item = new Item();
                
                // SKU (columna 1)
                if (rowData[1] != null) {
                    item.setId(rowData[1].toString());
                } else {
                    System.err.println("SKU faltante, omitiendo...");
                    continue;
                }
                
                // Nombre (columna 3)
                if (rowData[3] != null) {
                    item.setName(rowData[3].toString());
                } else {
                    System.err.println("Descripcion faltante, omitiendo...");
                    continue;
                }
                
                // Precio (columna 7)
                double precio = 0.0;
                if (rowData[7] != null) {
                    try {
                        precio = Double.parseDouble(rowData[7].toString());
                        item.setPrice((int) Math.round(precio * 100));
                    } catch (NumberFormatException e) {
                        item.setPrice(0);
                    }
                }
                
                // Cantidad (columna 6)
                int cantidad = 1;
                if (rowData[6] != null) {
                    try {
                        cantidad = Integer.parseInt(rowData[6].toString());
                        item.setQuantity(Math.max(1, cantidad));
                    } catch (NumberFormatException e) {
                        item.setQuantity(1);
                    }
                }
                
                items.add(item);
                totalVenta += precio * cantidad;
                
            } catch (Exception e) {
                System.err.println("Error al procesar fila: " + e.getMessage());
            }
        }
        
        System.out.println("Total items generados: " + items.size());
        _app.setMontoTotalVenta(totalVenta);
        return items;
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
                    _app.showWarningMessage("Proceso cancelado por el usuario");
                    return false;
                } else {
                    _app.showErrorMessage("El PIN debe tener exactamente 4 dígitos");
                    return false;
                }
            } else {
                System.err.println("Error al enviar el PIN: " + statusCode + 
                                 "\nRespuesta: " + responseBody);

                _app.showErrorMessage("Error al enviar el PIN al cliente");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            _app.showErrorMessage("Error de conexión al solicitar PIN: " + ex.getMessage());
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
                
                _app.showErrorMessage("PIN incorrecto");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            _app.showErrorMessage("Error de conexión al validar PIN: " + ex.getMessage());
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
            _app.showErrorMessage("Error al obtener datos de conexión (.env incompleto)");
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

    private void saveVariablesTransaction(LoyaltyResponse response) {
        _app.setCustomerId(response.getCustomerId());
        _app.setMontoRedimible(response.getCashback().getThisOrder().getRedeemable().getTotal() / 100.0);
        _app.setCashbackAcumulado(response.getCashback().getUserBalance().getAvailableAmount() / 100.0);
        _app.setConfirmToken(response.getConfirmToken());
    }

    private void aplicarCashback(Component parent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Ajuste de Cashback", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 280);
        dialog.setLocationRelativeTo(parent);
        
        DecimalFormat format = new DecimalFormat("#0.00");
        format.setMinimumFractionDigits(2);
        
        double maxMonto = _app.getMontoRedimible();
        
        // Panel principal
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Label
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new Label("Venta Total: $" + format.format(_app.getMontoTotalVenta())));

        // Cashback acumulado
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Cashback acumulado: $" + format.format(_app.getCashbackAcumulado())), gbc);
        
        // Monto máximo
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Monto máximo a redimir: $" + format.format(maxMonto)), gbc);

        // Total items
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Total items: " + _app.getTotalItems()), gbc);

        // Label "Monto a aplicar"
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Monto a aplicar:"), gbc);
        
        // Campo de texto - PRECARGADO con el valor máximo
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        JFormattedTextField input = new JFormattedTextField(format);
        input.setValue(maxMonto);
        input.setColumns(10);
        input.selectAll();
        panel.add(input, gbc);
        
        // Botones
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAplicar = new JButton("Aplicar");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnAplicar.addActionListener(ev -> {
            try {
                Number monto = (Number) input.getValue();
                double montoDouble = monto.doubleValue();
                
                if (montoDouble > _app.getMontoRedimible()) {
                    _app.showErrorMessage("El monto no puede exceder el máximo disponible");
                    return;
                }
                
                if (montoDouble < 0) {
                    _app.showErrorMessage("El monto no puede ser menor que 0");
                    return;
                }

                TokenResponse token = obtenerToken();
                if (token == null) {
                    _app.showErrorMessage("No se pudo obtener el token de autenticación");
                    enableButton();
                    return;
                }

                boolean pinValido = true;
                if (montoDouble > 0) {
                    pinValido = solicitarPin(token, _app.getCustomerId());
                }

                if (pinValido) {
                    _app.showSuccessMessage("Monto aplicado: $" + new DecimalFormat("#0.00").format(montoDouble));
                    _app.setMontoRedimible(montoDouble);
                    _app.setPinUserValido(true);
                    dialog.dispose();
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Ingrese un monto válido", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(ev -> {
            _app.setMontoRedimible(0.0);
            dialog.dispose();
        });
        
        buttonPanel.add(btnAplicar);
        buttonPanel.add(btnCancelar);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}