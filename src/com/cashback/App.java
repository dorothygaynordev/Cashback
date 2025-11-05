package com.cashback;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import java.awt.*;

public class App extends JFrame {
    private JButton openCashback;
    private JButton cancelOrder;
    private JButton confirmCancellation;
    private JTable table;
    private DefaultTableModel model;

    public App() {
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setTitle("POS DG - Integration Guper");
        setSize(1200, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
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

    private void inicializarComponentes() {
        model = new DefaultTableModel();

        table = new JTable(model);
        configurarTabla();

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel panelInferior = new JPanel(new FlowLayout());
        openCashback = new JButton("Cashback");
        openCashback.addActionListener((e) -> {
            new SendTransaction().setVisible(true);
        });
        cancelOrder = new JButton("Cancelar Orden");
        cancelOrder.addActionListener((e) -> {
            selectAllRows(true);
        });
        confirmCancellation = new JButton("Confirmar Cancelacion");
        confirmCancellation.addActionListener((e) -> {
            java.util.List<Object[]> selectedItems = model.getSelectedRows();
            CancelOrder cancelOrderWindow = new CancelOrder(selectedItems, model.getRowCount());
            cancelOrderWindow.setVisible(true);
        });
        
        panelInferior.add(openCashback);
        panelInferior.add(cancelOrder);
        panelInferior.add(confirmCancellation);
        add(panelInferior, BorderLayout.SOUTH);
    }

    class DefaultTableModel extends AbstractTableModel {
        private String[] columnNames = {"Seleccionar", "SKU", "Color", "Descripcion", "ColorNombre", "Talla", "Cantidad", "Precio", "Descuento", "Total", "Movimiento", "Vendedor"};
        
        private Object[][] data = {
            {false, "D12220275501", "501", "Senties 14-180", "Negro", 230, 1, 779.00, 0, 779.00, 525, 32972},
            {false, "D17020012501", "523", "Senties 14-181", "Azul Marino", 210, 1, 579.00, 0, 579.00, 525, 32972},
            {false, "D02380175501", "556", "Senties 14-182", "Café", 230, 1, 879.00, 0, 879.00, 525, 32972}
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
}
