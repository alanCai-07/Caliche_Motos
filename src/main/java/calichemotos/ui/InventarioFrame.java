package calichemotos.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import calichemotos.modelo.Repuesto;
import calichemotos.servicio.SistemaFacturacion;

/**
 * Version inicial de solo lectura. La edicion/alta de repuestos
 * (equivalente a InventarioFrame del supermercado en modo admin)
 * se agrega en la siguiente iteracion.
 */
public class InventarioFrame extends JFrame {

    private final SistemaFacturacion sistema = SistemaFacturacion.getInstance();
    private DefaultTableModel modelo;
    private JTable tabla;
    private JTextField txtFiltro;
    private JPanel rootPanel;

    public InventarioFrame() {
        setTitle("Inventario de Repuestos");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(AppIcon.getIcon());
        construirUI();
        setLocationRelativeTo(null);
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        this.rootPanel = root;
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Inventario de Repuestos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(new Color(180, 45, 25));
        root.add(titulo, BorderLayout.NORTH);

        JPanel filaFiltro = new JPanel(new BorderLayout(6, 0));
        filaFiltro.setBackground(Color.WHITE);
        txtFiltro = new JTextField();
        UIUtils.estilizarCampo(txtFiltro);
        txtFiltro.setToolTipText("Filtrar por nombre, marca o referencia...");
        filaFiltro.add(new JLabel("Buscar: "), BorderLayout.WEST);
        filaFiltro.add(txtFiltro, BorderLayout.CENTER);

        JButton btnRecargar = new JButton("Recargar");
        UIUtils.estilizarBoton(btnRecargar, new Color(80, 80, 80));
        filaFiltro.add(btnRecargar, BorderLayout.EAST);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 8));
        panelCentro.setBackground(Color.WHITE);
        panelCentro.add(filaFiltro, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[] { "Codigo", "Nombre", "Marca", "Modelo compatible",
                        "Precio", "IVA%", "Stock", "Precio c/IVA" },
                0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 195)));
        panelCentro.add(scroll, BorderLayout.CENTER);

        root.add(panelCentro, BorderLayout.CENTER);
        add(root);

        txtFiltro.addCaretListener(e -> {
            String txt = txtFiltro.getText().trim();
            sorter.setRowFilter(txt.isEmpty() ? null
                    : javax.swing.RowFilter.regexFilter("(?i)" + txt, 1, 2, 3));
        });
        btnRecargar.addActionListener(e -> recargar());

        cargarDatos();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void recargar() {
        try {
            sistema.getInventario().cargarDesdeDB();
            cargarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al recargar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        List<Repuesto> lista = new ArrayList<>(sistema.getInventario().getTodos().values());
        lista.sort(Comparator.comparing(Repuesto::getNombre));
        for (Repuesto r : lista)
            modelo.addRow(new Object[] {
                    r.getId(), r.getNombre(), r.getMarca(), r.getModeloCompatible(),
                    String.format("$%,.0f", r.getPrecio()),
                    String.format("%.0f%%", r.getImpuesto() * 100),
                    r.getStock(),
                    String.format("$%,.0f", r.getPrecioConIva())
            });
    }
}
