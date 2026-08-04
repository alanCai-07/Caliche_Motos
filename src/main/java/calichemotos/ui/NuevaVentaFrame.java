package calichemotos.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import calichemotos.dao.CajeroDAO;
import calichemotos.dao.ClienteDAO;
import calichemotos.modelo.Cajero;
import calichemotos.modelo.Cliente;
import calichemotos.modelo.Factura;
import calichemotos.modelo.ItemFactura;
import calichemotos.modelo.Repuesto;
import calichemotos.pago.MetodoPago;
import calichemotos.pago.PagoEfectivo;
import calichemotos.pago.PagoTarjeta;
import calichemotos.servicio.SistemaFacturacion;

public class NuevaVentaFrame extends JFrame {

    private static final Color ROJO = new Color(180, 45, 25);

    private final JFrame parent;
    private final SistemaFacturacion sistema = SistemaFacturacion.getInstance();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final CajeroDAO cajeroDAO = new CajeroDAO();

    private Factura facturaActual;
    private Cliente clienteActual;
    private final List<ItemFactura> itemsCarrito = new ArrayList<>();

    private JTextField txtNitBuscar;
    private JLabel lblClienteNombre;
    private JLabel lblClienteInfo;
    private JPanel panelClienteInfo;

    private JTextField txtBuscarRepuesto;
    private JTable tablaRepuestos;
    private DefaultTableModel modeloRepuestos;
    private JTextField txtCant;

    private JTable tablaItems;
    private DefaultTableModel modeloItems;
    private JLabel lblSubtotal, lblIva, lblTotal;
    private JComboBox<String> cmbPago;
    private JComboBox<String> cmbTecnico;
    private JTextField txtMontoPago;
    private JButton btnCobrar;
    private JPanel panelCarritoBorder;
    private JPanel rootPanel;

    public NuevaVentaFrame(JFrame parent) {
        this.parent = parent;
        setTitle("Nueva Venta");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(AppIcon.getIcon());
        setLocationRelativeTo(parent);
        construirUI();
        asignarConsumidorFinal();
        buscarRepuestos("");
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        root.setBackground(Color.WHITE);
        this.rootPanel = root;

        root.add(construirPanelCliente(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.setBackground(Color.WHITE);
        centro.add(construirPanelRepuestos(), BorderLayout.WEST);
        centro.add(construirPanelCarrito(), BorderLayout.CENTER);
        root.add(centro, BorderLayout.CENTER);

        add(root);
    }

    // ---- Panel cliente ----
    private JPanel construirPanelCliente() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(255, 245, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(ROJO, 1),
                        "Cliente de la factura",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 11), ROJO),
                BorderFactory.createEmptyBorder(4, 8, 6, 8)));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        izq.setBackground(new Color(255, 245, 240));

        txtNitBuscar = new JTextField(16);
        UIUtils.estilizarCampo(txtNitBuscar);
        txtNitBuscar.setToolTipText("NIT/CC o nombre del cliente");

        JButton btnBuscarCliente = new JButton("Buscar cliente");
        JButton btnNuevoCliente = new JButton("+ Nuevo cliente");
        JButton btnConsumidorFinal = new JButton("Consumidor final");

        UIUtils.estilizarBoton(btnBuscarCliente, ROJO);
        UIUtils.estilizarBoton(btnNuevoCliente, new Color(30, 130, 76));
        UIUtils.estilizarBoton(btnConsumidorFinal, new Color(100, 100, 100));

        Dimension dimBtn = new Dimension(150, 30);
        btnBuscarCliente.setPreferredSize(dimBtn);
        btnNuevoCliente.setPreferredSize(dimBtn);
        btnConsumidorFinal.setPreferredSize(dimBtn);

        izq.add(new JLabel("NIT o Nombre:"));
        izq.add(txtNitBuscar);
        izq.add(btnBuscarCliente);
        izq.add(btnNuevoCliente);
        izq.add(btnConsumidorFinal);
        panel.add(izq, BorderLayout.WEST);

        panelClienteInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        panelClienteInfo.setBackground(new Color(255, 245, 240));
        panelClienteInfo.setBorder(BorderFactory.createEmptyBorder(2, 12, 2, 8));

        lblClienteNombre = new JLabel("Consumidor Final");
        lblClienteNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblClienteNombre.setForeground(ROJO);

        lblClienteInfo = new JLabel("NIT: 222222222");
        lblClienteInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblClienteInfo.setForeground(Color.GRAY);

        panelClienteInfo.add(lblClienteNombre);
        panelClienteInfo.add(lblClienteInfo);
        panel.add(panelClienteInfo, BorderLayout.CENTER);

        txtNitBuscar.addActionListener(e -> buscarCliente());
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        btnConsumidorFinal.addActionListener(e -> asignarConsumidorFinal());
        btnNuevoCliente.addActionListener(e -> abrirDialogoNuevoCliente());

        return panel;
    }

    // ---- Panel repuestos (WEST) ----
    private JPanel construirPanelRepuestos() {
        JPanel panel = new JPanel(new BorderLayout(4, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Repuestos"));
        panel.setPreferredSize(new Dimension(420, 0));

        JPanel barraBusq = new JPanel(new BorderLayout(4, 0));
        barraBusq.setBackground(Color.WHITE);
        txtBuscarRepuesto = new JTextField();
        UIUtils.estilizarCampo(txtBuscarRepuesto);
        JButton btnBuscar = new JButton("Buscar");
        UIUtils.estilizarBoton(btnBuscar, ROJO);
        barraBusq.add(new JLabel("Buscar: "), BorderLayout.WEST);
        barraBusq.add(txtBuscarRepuesto, BorderLayout.CENTER);
        barraBusq.add(btnBuscar, BorderLayout.EAST);
        panel.add(barraBusq, BorderLayout.NORTH);

        modeloRepuestos = new DefaultTableModel(
                new String[] { "Codigo", "Nombre", "Marca", "Precio", "Stock" }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaRepuestos = new JTable(modeloRepuestos);
        tablaRepuestos.setRowHeight(26);
        tablaRepuestos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaRepuestos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaRepuestos), BorderLayout.CENTER);

        JPanel panelAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panelAgregar.setBackground(Color.WHITE);

        txtCant = new JTextField("1", 6);
        UIUtils.estilizarCampo(txtCant);
        txtCant.setHorizontalAlignment(JTextField.CENTER);
        txtCant.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton btnMenos = new JButton("-");
        JButton btnMas = new JButton("+");
        estilizarBtnCant(btnMenos, new Color(180, 60, 60));
        estilizarBtnCant(btnMas, new Color(30, 130, 76));

        JButton btnAgregar = new JButton("  Agregar al carrito  ");
        UIUtils.estilizarBoton(btnAgregar, new Color(30, 130, 76));
        btnAgregar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAgregar.setPreferredSize(new Dimension(180, 34));

        panelAgregar.add(new JLabel("Cantidad:"));
        panelAgregar.add(btnMenos);
        panelAgregar.add(txtCant);
        panelAgregar.add(btnMas);
        panelAgregar.add(Box.createHorizontalStrut(8));
        panelAgregar.add(btnAgregar);
        panel.add(panelAgregar, BorderLayout.SOUTH);

        ActionListener accionBuscar = e -> buscarRepuestos(txtBuscarRepuesto.getText());
        btnBuscar.addActionListener(accionBuscar);
        txtBuscarRepuesto.addActionListener(accionBuscar);
        btnMenos.addActionListener(e -> cambiarCantidad(-1));
        btnMas.addActionListener(e -> cambiarCantidad(+1));
        txtCant.addActionListener(e -> btnAgregar.doClick());

        btnAgregar.addActionListener(e -> {
            int fila = tablaRepuestos.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un repuesto de la lista.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int cant = leerCantidad();
            if (cant > 0)
                agregarAlCarrito((String) modeloRepuestos.getValueAt(fila, 0), cant);
        });

        tablaRepuestos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaRepuestos.getSelectedRow();
                    if (fila >= 0)
                        agregarAlCarrito((String) modeloRepuestos.getValueAt(fila, 0), leerCantidad());
                }
            }
        });

        return panel;
    }

    // ---- Panel carrito (CENTER) ----
    private JPanel construirPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout(4, 6));
        panel.setBackground(Color.WHITE);

        panelCarritoBorder = new JPanel(new BorderLayout(4, 4));
        panelCarritoBorder.setBorder(BorderFactory.createTitledBorder("Carrito"));
        panelCarritoBorder.setBackground(Color.WHITE);

        modeloItems = new DefaultTableModel(
                new String[] { "Repuesto", "Cant.", "Unitario", "IVA", "Total" }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaItems = new JTable(modeloItems);
        tablaItems.setRowHeight(26);
        tablaItems.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelCarritoBorder.add(new JScrollPane(tablaItems), BorderLayout.CENTER);

        JButton btnEliminar = new JButton("Eliminar item seleccionado");
        UIUtils.estilizarBoton(btnEliminar, new Color(170, 40, 40));
        btnEliminar.addActionListener(e -> eliminarItemSeleccionado());
        panelCarritoBorder.add(btnEliminar, BorderLayout.SOUTH);

        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 6, 4));
        panelTotales.setBackground(new Color(255, 240, 235));
        panelTotales.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        lblSubtotal = new JLabel("$0", javax.swing.SwingConstants.RIGHT);
        lblIva = new JLabel("$0", javax.swing.SwingConstants.RIGHT);
        lblTotal = new JLabel("$0", javax.swing.SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTotal.setForeground(ROJO);

        JLabel lbTot = new JLabel("TOTAL:");
        lbTot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelTotales.add(new JLabel("Subtotal:"));
        panelTotales.add(lblSubtotal);
        panelTotales.add(new JLabel("IVA:"));
        panelTotales.add(lblIva);
        panelTotales.add(lbTot);
        panelTotales.add(lblTotal);

        JPanel panelCobro = new JPanel(new GridBagLayout());
        panelCobro.setBackground(Color.WHITE);
        panelCobro.setBorder(BorderFactory.createTitledBorder("Cobro"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        cmbPago = new JComboBox<>(new String[] { "EFECTIVO", "TARJETA_DEBITO", "TARJETA_CREDITO" });
        cmbTecnico = new JComboBox<>();
        cargarTecnicosFactura();
        txtMontoPago = new JTextField("0", 12);
        UIUtils.estilizarCampo(txtMontoPago);
        btnCobrar = new JButton("COBRAR");
        UIUtils.estilizarBoton(btnCobrar, ROJO);
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCobrar.setPreferredSize(new Dimension(0, 46));

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.35;
        panelCobro.add(new JLabel("Metodo pago:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        panelCobro.add(cmbPago, gc);
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0.35;
        panelCobro.add(new JLabel("Tecnico asignado:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        panelCobro.add(cmbTecnico, gc);
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0.35;
        panelCobro.add(new JLabel("Monto recibido:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        panelCobro.add(txtMontoPago, gc);
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        panelCobro.add(btnCobrar, gc);

        JPanel inferior = new JPanel(new BorderLayout(4, 4));
        inferior.setBackground(Color.WHITE);
        inferior.add(panelTotales, BorderLayout.NORTH);
        inferior.add(panelCobro, BorderLayout.CENTER);

        panel.add(panelCarritoBorder, BorderLayout.CENTER);
        panel.add(inferior, BorderLayout.SOUTH);

        btnCobrar.addActionListener(e -> procesarCobro());
        return panel;
    }

    // ---- Cliente ----
    private void buscarCliente() {
        String texto = txtNitBuscar.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el NIT, cedula o nombre del cliente.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            List<Cliente> resultados = clienteDAO.buscarInteligente(texto);
            if (resultados.isEmpty()) {
                String msg = texto.matches("\\d+")
                        ? "No existe cliente con NIT: " + texto
                        : "No existe cliente con nombre: \"" + texto + "\"";
                int resp = JOptionPane.showConfirmDialog(this,
                        msg + "\n\nDesea registrarlo como cliente nuevo?",
                        "Cliente no encontrado",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.YES_OPTION)
                    abrirDialogoNuevoCliente(texto.matches("\\d+") ? texto : "");
            } else if (resultados.size() == 1) {
                asignarCliente(resultados.get(0));
            } else {
                Cliente seleccionado = mostrarDialogoSeleccion(resultados);
                if (seleccionado != null)
                    asignarCliente(seleccionado);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Cliente mostrarDialogoSeleccion(List<Cliente> clientes) {
        JDialog dlg = new JDialog(this, "Seleccionar cliente", true);
        dlg.setSize(560, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel("Se encontraron " + clientes.size() + " clientes:"), BorderLayout.NORTH);

        String[] cols = { "NIT / CC", "Nombre", "Telefono", "Email" };
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Cliente c : clientes)
            modelo.addRow(new Object[] { c.getNit(), c.getNombre(), c.getTelefono(), c.getEmail() });

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowSelectionInterval(0, 0);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Color.WHITE);
        JButton btnElegir = new JButton("Elegir");
        JButton btnCancelar = new JButton("Cancelar");
        UIUtils.estilizarBoton(btnElegir, ROJO);
        UIUtils.estilizarBoton(btnCancelar, new Color(120, 120, 120));
        btnPanel.add(btnCancelar);
        btnPanel.add(btnElegir);
        panel.add(btnPanel, BorderLayout.SOUTH);
        dlg.add(panel);

        final Cliente[] resultado = { null };
        Runnable elegir = () -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                resultado[0] = clientes.get(fila);
                dlg.dispose();
            }
        };
        btnElegir.addActionListener(e -> elegir.run());
        btnCancelar.addActionListener(e -> dlg.dispose());
        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) elegir.run();
            }
        });

        dlg.setVisible(true);
        return resultado[0];
    }

    private void asignarConsumidorFinal() {
        try {
            Cliente c = clienteDAO.consumidorFinal();
            asignarCliente(c);
            txtNitBuscar.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void asignarCliente(Cliente c) {
        clienteActual = c;
        try {
            facturaActual = sistema.crearFactura(clienteActual);
            for (ItemFactura item : itemsCarrito)
                facturaActual.agregarItem(item);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear factura: " + ex.getMessage());
        }

        boolean esConsumidorFinal = "222222222".equals(c.getNit());
        lblClienteNombre.setText(c.getNombre());
        lblClienteNombre.setForeground(esConsumidorFinal ? new Color(120, 120, 120) : ROJO);

        String info = "NIT: " + c.getNit();
        if (!c.getTelefono().isBlank()) info += "  |  Tel: " + c.getTelefono();
        lblClienteInfo.setText(info);

        if (panelCarritoBorder != null && facturaActual != null) {
            ((TitledBorder) panelCarritoBorder.getBorder())
                    .setTitle("Carrito  —  N " + facturaActual.getNumero() + "  |  Cliente: " + c.getNombre());
            panelCarritoBorder.repaint();
        }
        txtNitBuscar.setText(esConsumidorFinal ? "" : c.getNit());
    }

    private void abrirDialogoNuevoCliente() {
        abrirDialogoNuevoCliente("");
    }

    private void abrirDialogoNuevoCliente(String nitPrellenado) {
        JDialog dlg = new JDialog(this, "Registrar nuevo cliente", true);
        dlg.setSize(420, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField fNit = campoDlg(nitPrellenado);
        JTextField fNom = campoDlg("");
        JTextField fTel = campoDlg("");
        JTextField fMail = campoDlg("");
        JTextField fDir = campoDlg("");

        String[] etiq = { "NIT / Cedula: *", "Nombre completo: *", "Telefono:", "Email:", "Direccion:" };
        JTextField[] campos = { fNit, fNom, fTel, fMail, fDir };
        for (int i = 0; i < etiq.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35;
            panel.add(new JLabel(etiq[i]), g);
            g.gridx = 1; g.weightx = 0.65;
            panel.add(campos[i], g);
        }

        JLabel lblErr = new JLabel(" ");
        lblErr.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblErr.setForeground(Color.RED);
        g.gridx = 0; g.gridy = etiq.length; g.gridwidth = 2;
        panel.add(lblErr, g);

        JPanel bp = new JPanel(new GridLayout(1, 2, 10, 0));
        bp.setBackground(Color.WHITE);
        JButton btnOk = new JButton("Registrar y asignar");
        JButton btnCan = new JButton("Cancelar");
        UIUtils.estilizarBoton(btnOk, new Color(30, 130, 76));
        UIUtils.estilizarBoton(btnCan, new Color(120, 120, 120));
        bp.add(btnOk);
        bp.add(btnCan);
        g.gridy = etiq.length + 1;
        panel.add(bp, g);

        dlg.add(panel);
        btnCan.addActionListener(e -> dlg.dispose());
        btnOk.addActionListener(e -> {
            String nit = fNit.getText().trim();
            String nom = fNom.getText().trim();
            if (!nit.matches("\\d{6,15}")) {
                lblErr.setText("NIT invalido: solo digitos, minimo 6.");
                return;
            }
            if (nom.isEmpty()) {
                lblErr.setText("El nombre es obligatorio.");
                return;
            }
            try {
                Cliente nuevo = new Cliente(nit, nom, fTel.getText().trim(),
                        fMail.getText().trim(), fDir.getText().trim());
                clienteDAO.guardar(nuevo);
                asignarCliente(nuevo);
                dlg.dispose();
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    // ---- Carrito ----
    private void agregarAlCarrito(String idRepuesto, int cantidad) {
        if (facturaActual == null) {
            JOptionPane.showMessageDialog(this, "No hay factura activa. Seleccione un cliente primero.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Repuesto r = sistema.getInventario().buscarRepuesto(idRepuesto);
        if (r == null) return;

        int yaEnCarrito = itemsCarrito.stream()
                .filter(i -> i.getRepuesto().getId().equals(idRepuesto))
                .mapToInt(ItemFactura::getCantidad).sum();
        int stockDisponible = r.getStock() - yaEnCarrito;

        if (cantidad > stockDisponible) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuficiente.\nDisponible: " + stockDisponible,
                    "Sin stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ItemFactura item = new ItemFactura(r, cantidad);
        facturaActual.agregarItem(item);
        itemsCarrito.add(item);

        modeloItems.addRow(new Object[] {
                r.getNombre(), cantidad,
                String.format("$%,.0f", item.getPrecioUnitario()),
                String.format("$%,.0f", item.getImpuesto()),
                String.format("$%,.0f", item.getTotal())
        });

        actualizarTotales();
        txtCant.setText("1");
        txtCant.requestFocus();
    }

    private void eliminarItemSeleccionado() {
        int fila = tablaItems.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un item del carrito para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        itemsCarrito.remove(fila);
        modeloItems.removeRow(fila);
        reconstruirFactura();
        actualizarTotales();
    }

    private void reconstruirFactura() {
        try {
            facturaActual = sistema.crearFactura(clienteActual);
            for (ItemFactura item : itemsCarrito)
                facturaActual.agregarItem(item);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void actualizarTotales() {
        if (facturaActual == null) return;
        lblSubtotal.setText(String.format("$%,.0f", facturaActual.calcularSubtotal()));
        lblIva.setText(String.format("$%,.0f", facturaActual.calcularIva()));
        lblTotal.setText(String.format("$%,.0f", facturaActual.calcularTotal()));
        txtMontoPago.setText(String.format("%.0f", facturaActual.calcularTotal()));
    }

    // ---- Busqueda de repuestos ----
    private void buscarRepuestos(String texto) {
        modeloRepuestos.setRowCount(0);
        List<Repuesto> lista = texto.isBlank()
                ? new ArrayList<>(sistema.getInventario().getTodos().values())
                : sistema.getInventario().buscarPorNombreOMarca(texto);
        for (Repuesto r : lista)
            if (r.isActivo())
                modeloRepuestos.addRow(new Object[] {
                        r.getId(), r.getNombre(), r.getMarca(),
                        String.format("$%,.0f", r.getPrecio()), r.getStock()
                });
    }

    public void recargarTecnicos() {
        cargarTecnicosFactura();
    }

    private void cargarTecnicosFactura() {
        cmbTecnico.removeAllItems();
        cmbTecnico.addItem("Sin tecnico");
        try {
            List<Cajero> tecnicos = cajeroDAO.listarPorRol("TECNICO");
            for (Cajero t : tecnicos)
                cmbTecnico.addItem(t.getNombre() + " (" + t.getId() + ")");
        } catch (Exception ex) {
            System.err.println("[TEC] Error cargando tecnicos: " + ex.getMessage());
        }
    }

    private String obtenerTecnicoSeleccionadoId() {
        String seleccion = (String) cmbTecnico.getSelectedItem();
        if (seleccion == null || seleccion.equals("Sin tecnico"))
            return null;
        int idx = seleccion.lastIndexOf('(');
        return idx >= 0 ? seleccion.substring(idx + 1, seleccion.length() - 1) : null;
    }

    // ---- Cobro ----
    private void procesarCobro() {
        if (facturaActual == null || facturaActual.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito esta vacio.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double montoIngresado;
        try {
            montoIngresado = Double.parseDouble(
                    txtMontoPago.getText().replace(",", "").replace("$", "").trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto de pago invalido.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tecnicoId = obtenerTecnicoSeleccionadoId();
        if (tecnicoId != null)
            facturaActual.setTecnicoAsignado(tecnicoId);

        String tipoPago = (String) cmbPago.getSelectedItem();
        MetodoPago pago;
        if ("EFECTIVO".equals(tipoPago)) {
            pago = new PagoEfectivo(montoIngresado);
        } else {
            String digitos = JOptionPane.showInputDialog(this, "Ultimos 4 digitos de la tarjeta:");
            if (digitos == null) return;
            pago = new PagoTarjeta(digitos, tipoPago.contains("DEBITO") ? "DEBITO" : "CREDITO");
        }

        btnCobrar.setEnabled(false);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return sistema.procesarPago(facturaActual, pago);
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        JOptionPane.showMessageDialog(NuevaVentaFrame.this,
                                "Pago rechazado. Verifique el monto.",
                                "Pago fallido", JOptionPane.ERROR_MESSAGE);
                        btnCobrar.setEnabled(true);
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(NuevaVentaFrame.this,
                            "Error al procesar el pago: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    btnCobrar.setEnabled(true);
                    return;
                }

                String cambioTxt = (pago instanceof PagoEfectivo pe)
                        ? "\nCambio: $" + String.format("%,.0f", pe.getCambio())
                        : "";

                String rutaPdf = null;
                try {
                    rutaPdf = calichemotos.reporte.GeneradorReportePDF.generarFacturaPDF(facturaActual);
                } catch (Exception ex) {
                    System.err.println("[PDF] Error al generar factura: " + ex.getMessage());
                    JOptionPane.showMessageDialog(NuevaVentaFrame.this,
                            "La venta se registro correctamente, pero no se pudo generar el PDF.\n" + ex.getMessage(),
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                }

                if (rutaPdf != null) {
                    int abrir = JOptionPane.showConfirmDialog(NuevaVentaFrame.this,
                            "Factura " + facturaActual.getNumero() + " registrada correctamente.\n" +
                                    "Cliente: " + clienteActual.getNombre() + cambioTxt +
                                    "\n\nPDF generado en:\n" + rutaPdf +
                                    "\n\nDesea abrirlo?",
                            "Venta completada", JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (abrir == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(new java.io.File(rutaPdf));
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(NuevaVentaFrame.this,
                                    "No se pudo abrir el PDF: " + ex.getMessage(),
                                    "Aviso", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(NuevaVentaFrame.this,
                            "Factura " + facturaActual.getNumero() + " registrada correctamente.\n" +
                                    "Cliente: " + clienteActual.getNombre() + cambioTxt,
                            "Venta completada", JOptionPane.INFORMATION_MESSAGE);
                }

                int otraVenta = JOptionPane.showConfirmDialog(NuevaVentaFrame.this,
                        "Desea realizar otra venta?", "Nueva venta", JOptionPane.YES_NO_OPTION);
                dispose();
                if (otraVenta == JOptionPane.YES_OPTION)
                    new NuevaVentaFrame(parent).setVisible(true);
            }
        };
        worker.execute();
    }

    // ---- Utilidades ----
    private int leerCantidad() {
        try {
            int v = Integer.parseInt(txtCant.getText().trim());
            return Math.max(1, v);
        } catch (NumberFormatException ex) {
            txtCant.setText("1");
            return 1;
        }
    }

    private void cambiarCantidad(int delta) {
        txtCant.setText(String.valueOf(Math.max(1, leerCantidad() + delta)));
    }

    private void estilizarBtnCant(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(32, 32));
    }

    private JTextField campoDlg(String valor) {
        JTextField t = new JTextField(valor, 18);
        UIUtils.estilizarCampo(t);
        return t;
    }
}
