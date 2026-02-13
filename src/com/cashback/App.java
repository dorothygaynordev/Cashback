package com.cashback;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import java.awt.*;

public class App extends JFrame {
    private JButton checkCashback;
    private JButton cancelOrder;
    private JButton confirmCancellation;
    private JButton printTicket;
    private JTable table;
    private DefaultTableModel model;
    private ValidationTicket validationTicket;

    // Variables de Estado Global
    private String lastOrderId;
    private String lastOrderTid;
    private Long customerId;
    private Double montoRedimible = 0.0;
    private Double cashbackAcumulado;
    private Double montoTotalVenta;
    private String confirmToken;
    private int totalItems = 0;
    private boolean pinUserValido = false;

    public App() {
        configurarVentana();
        inicializarComponentes();
        updateStateCheckCashback();
    }

    private void configurarVentana() {
        setTitle("POS DG - Integration Guper");
        setSize(1200, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        model = new DefaultTableModel();
        table = new JTable(model);
        configurarTabla();

        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                updateStateCheckCashback();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel(new FlowLayout());
        validationTicket = new ValidationTicket(this);

        // Boton Consultar Cashback
        checkCashback = new JButton("Consultar Cashback");
        checkCashback.addActionListener((e) -> {
            new CheckCashback(App.this).setVisible(true);
        });

        // Boton Imprimir Ticket
        printTicket = new JButton("Imprimir Ticket");
        printTicket.addActionListener((e) -> {
            validationTicket.validateTransaction();
        });

        // Boton Cancelar Orden
        cancelOrder = new JButton("Cancelar Orden");
        cancelOrder.addActionListener((e) -> {
            selectAllRows(true);
        });

        // Boton Confirmar Cancelacion
        confirmCancellation = new JButton("Confirmar Cancelacion");
        confirmCancellation.addActionListener((e) -> {
            java.util.List<Object[]> selectedItems = model.getSelectedRows();
            new CancelOrder(App.this, selectedItems).setVisible(true);
        });
        
        panelInferior.add(checkCashback);
        panelInferior.add(printTicket);
        panelInferior.add(cancelOrder);
        panelInferior.add(confirmCancellation);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void configurarTabla() {
        // Configurar el header
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.LIGHT_GRAY);
        
        // Configurar la tabla
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.setSelectionBackground(Color.YELLOW);
        table.setSelectionForeground(Color.BLACK);
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // SKU
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  // Color
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Descripcion
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // ColorNombre
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Talla
        table.getColumnModel().getColumn(5).setPreferredWidth(70);  // Cantidad
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Precio
        table.getColumnModel().getColumn(7).setPreferredWidth(80);  // Descuento
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // Total
        table.getColumnModel().getColumn(9).setPreferredWidth(80);  // Movimiento
        table.getColumnModel().getColumn(10).setPreferredWidth(80); // Vendedor
    }

    private void updateStateCheckCashback() {
        java.util.List<Object[]> selectedItems = model.getSelectedRows();
        checkCashback.setEnabled(!selectedItems.isEmpty());
    }

    class DefaultTableModel extends AbstractTableModel {
        private String[] columnNames = {"Seleccionar", "SKU", "Color", "Descripcion", "ColorNombre", "Talla", "Cantidad", "Precio", "Descuento", "Total", "Movimiento", "Vendedor"};
        
        private Object[][] data = {
            {false, "D12220275501", "501", "Senties 14-180", "Negro", 230, 1, 779.00, 0, 779.00, 525, 32972},
            {false, "D17020012501", "523", "Senties 14-181", "Azul Marino", 210, 1, 579.00, 0, 579.00, 525, 32972},
            {false, "D02380175501", "556", "Senties 14-182", "Café", 230, 1, 879.00, 0, 879.00, 525, 32972},
            {false, "D16780027530", "530", "Senties 14-183", "Rojo", 220, 1, 719.00, 0, 719.00, 525, 32972},
            {false, "D80350001553", "553", "Senties 14-184", "Camel", 210, 1, 679.00, 0, 679.00, 525, 32972},
            {false, "D06001591532", "532", "Senties 14-182", "Vino", 230, 1, 559.00, 0, 559.00, 525, 32972}
        };

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Boolean.class;
            switch (columnIndex) {
                case 0: return String.class;
                case 1: return String.class;
                case 2: return String.class;
                case 3: return String.class;
                case 4: return Integer.class;
                case 5: return Integer.class;
                case 6: return Double.class;
                case 7: return Double.class;
                case 8: return Double.class;
                case 9: return String.class;
                case 10: return String.class;
                default: return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0 && aValue instanceof Boolean) {
                data[rowIndex][columnIndex] = aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        public java.util.List<Object[]> getSelectedRows() {
            java.util.List<Object[]> selected = new java.util.ArrayList<>();
            for (Object[] row : data) {
                if (Boolean.TRUE.equals(row[0])) {
                    selected.add(row);
                }
            }
            return selected;
        }

        public int getSelectedRowCount() {
            return getSelectedRows().size();
        }

        public boolean hasSelectedRows() {
            return getSelectedRowCount() > 0;
        }
    } 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            App app = new App();
            app.setVisible(true);
        });
    }

    public JTable getTable() {
        return table;
    }

    public void selectAllRows(boolean value) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(value, i, 0);
        }
    }

    public void showSuccessMessage(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE));
    }

    public void showWarningMessage(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Advertencia", JOptionPane.WARNING_MESSAGE));
    }

    public void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    // ===============================================
    //  GETTERS Y SETTERS
    // ===============================================
    public String getLastOrderId() { return lastOrderId; }
    public void setLastOrderId(String lastOrderId) { this.lastOrderId = lastOrderId; }

    public String getLastOrderTid() { return lastOrderTid; }
    public void setLastOrderTid(String lastOrderTid) { this.lastOrderId = lastOrderTid; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Double getMontoRedimible() { return montoRedimible; }
    public void setMontoRedimible(Double montoRedimible) { this.montoRedimible = montoRedimible; }

    public Double getCashbackAcumulado() { return cashbackAcumulado; }
    public void setCashbackAcumulado(Double cashbackAcumulado) { this.cashbackAcumulado = cashbackAcumulado; }

    public Double getMontoTotalVenta() { return montoTotalVenta; }
    public void setMontoTotalVenta(Double montoTotalVenta) { this.montoTotalVenta = montoTotalVenta; }

    public String getConfirmToken() { return confirmToken; }
    public void setConfirmToken(String confirmToken) { this.confirmToken = confirmToken; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public boolean getPinUserValido() { return pinUserValido; }
    public void setPinUserValido(boolean pinUserValido) { this.pinUserValido = pinUserValido; }

    public void CleanStateTransaction() {
        // this.lastOrderId = null;
        // this.lastOrderId = null;
        this.customerId = null;
        this.montoRedimible = null;
        this.cashbackAcumulado = null;
        this.montoTotalVenta = null;
        this.confirmToken = null;
        this.totalItems = 0;
        this.pinUserValido = false;
    }
}
