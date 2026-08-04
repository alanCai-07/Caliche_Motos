package calichemotos.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import calichemotos.dao.CajeroDAO;
import calichemotos.dao.RepuestoDAO;
import calichemotos.modelo.Cajero;
import calichemotos.modelo.Repuesto;
import calichemotos.servicio.SistemaFacturacion;
import calichemotos.util.GestorImagenes;

public class InventarioFrame extends JFrame {

    private static final Color ROJO = new Color(180, 45, 25);

    private final SistemaFacturacion sistema = SistemaFacturacion.getInstance();
    private final RepuestoDAO repuestoDAO = new RepuestoDAO();
    private final CajeroDAO cajeroDAO = new CajeroDAO();
    private final boolean esAdmin = sistema.getCajeroActivo().esAdmin();

    private DefaultTableModel modelo;
    private JTable tabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtFiltro;
    private JButton btnAgregar, btnEditar, btnAjustarStock, btnEliminar;
    private JPanel rootPanel;

    public InventarioFrame() {
        setTitle("Inventario de Repuestos" + (esAdmin ? "  [Modo Administrador]" : ""));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(AppIcon.getIcon());
        construirUI();
        setSize(esAdmin ? 980 : 900, esAdmin ? 640 : 580);
        setLocationRelativeTo(null);
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        this.rootPanel = root;
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(Color.WHITE);

        // ---- NORTH: titulo + badge + filtro ----
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.setBackground(Color.WHITE);

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(Color.WHITE);
        filaTitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel titulo = new JLabel("Inventario de Repuestos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(ROJO);
        JLabel badge = new JLabel(esAdmin ? "  ADMINISTRADOR  " : "  SOLO LECTURA  ");
        badge.setOpaque(true);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        badge.setBackground(esAdmin ? ROJO : new Color(120, 120, 120));
        badge.setForeground(Color.WHITE);
        filaTitulo.add(titulo, BorderLayout.WEST);
        filaTitulo.add(badge, BorderLayout.EAST);
        panelNorth.add(filaTitulo);
        panelNorth.add(Box.createVerticalStrut(8));

        JPanel filaFiltro = new JPanel(new BorderLayout(6, 0));
        filaFiltro.setBackground(Color.WHITE);
        filaFiltro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtFiltro = new JTextField();
        UIUtils.estilizarCampo(txtFiltro);
        txtFiltro.setToolTipText("Filtrar por nombre, marca o modelo compatible...");
        filaFiltro.add(new JLabel("Buscar: "), BorderLayout.WEST);
        filaFiltro.add(txtFiltro, BorderLayout.CENTER);
        panelNorth.add(filaFiltro);
        panelNorth.add(Box.createVerticalStrut(4));

        root.add(panelNorth, BorderLayout.NORTH);

        // ---- CENTER: tabla ----
        modelo = new DefaultTableModel(
                new String[] { "Codigo", "Nombre", "Marca", "Modelo compatible",
                        "Precio", "IVA%", "Categoria", "Stock", "Precio c/IVA", "Estado", "Imagen" },
                0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(10).setPreferredWidth(74);
        tabla.getColumnModel().getColumn(10).setMaxWidth(74);

        tabla.setDefaultRenderer(Object.class, (t, val, sel, foc, row, col) -> {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            int stockVal = 0;
            try {
                stockVal = Integer.parseInt(modelo.getValueAt(row, 7).toString());
            } catch (Exception ignored) {
            }
            String estado = modelo.getValueAt(row, 9).toString();
            if (val instanceof ImageIcon icon) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setIcon(icon);
                lbl.setText(null);
            } else {
                lbl.setText(val == null ? "" : val.toString());
            }
            if (sel) {
                lbl.setBackground(new Color(232, 200, 195));
            } else if ("INACTIVO".equals(estado)) {
                lbl.setBackground(new Color(255, 218, 218));
                lbl.setForeground(new Color(140, 40, 40));
            } else if (stockVal <= 5) {
                lbl.setBackground(new Color(255, 245, 190));
                if (col == 7) {
                    lbl.setForeground(new Color(160, 80, 0));
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                }
            } else {
                lbl.setBackground(row % 2 == 0 ? new Color(250, 240, 237) : Color.WHITE);
            }
            return lbl;
        });

        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 195)));
        root.add(scroll, BorderLayout.CENTER);

        // ---- SOUTH: leyenda + botones ----
        JPanel panelSouth = new JPanel();
        panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.Y_AXIS));
        panelSouth.setBackground(Color.WHITE);
        panelSouth.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        leyenda.setBackground(Color.WHITE);
        leyenda.setAlignmentX(Component.LEFT_ALIGNMENT);
        leyenda.add(pastilla(new Color(255, 245, 190), "Stock <= 5 unidades"));
        leyenda.add(pastilla(new Color(255, 218, 218), "Repuesto inactivo"));
        panelSouth.add(leyenda);

        panelSouth.add(Box.createVerticalStrut(4));
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panelSouth.add(sep);
        panelSouth.add(Box.createVerticalStrut(6));

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filaBotones.setBackground(Color.WHITE);
        filaBotones.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnRecargar = new JButton("Recargar");
        UIUtils.estilizarBoton(btnRecargar, new Color(80, 80, 80));
        btnRecargar.setPreferredSize(new Dimension(110, 36));
        filaBotones.add(btnRecargar);

        if (esAdmin) {
            btnAgregar = new JButton("+ Agregar");
            btnEditar = new JButton("Editar");
            btnAjustarStock = new JButton("Ajustar stock");
            btnEliminar = new JButton("Activar / Desactivar");
            JButton btnTecnicos = new JButton("Tecnicos");
            JButton btnCategorias = new JButton("Categorias");

            Dimension dimBtn = new Dimension(150, 36);
            UIUtils.estilizarBoton(btnAgregar, new Color(30, 130, 76));
            UIUtils.estilizarBoton(btnEditar, ROJO);
            UIUtils.estilizarBoton(btnAjustarStock, new Color(140, 80, 10));
            UIUtils.estilizarBoton(btnEliminar, new Color(160, 40, 40));
            UIUtils.estilizarBoton(btnTecnicos, new Color(80, 80, 150));
            UIUtils.estilizarBoton(btnCategorias, new Color(120, 90, 40));

            btnAgregar.setPreferredSize(dimBtn);
            btnEditar.setPreferredSize(dimBtn);
            btnAjustarStock.setPreferredSize(dimBtn);
            btnEliminar.setPreferredSize(new Dimension(170, 36));
            btnTecnicos.setPreferredSize(new Dimension(140, 36));
            btnCategorias.setPreferredSize(new Dimension(145, 36));

            btnEditar.setEnabled(false);
            btnAjustarStock.setEnabled(false);
            btnEliminar.setEnabled(false);

            tabla.getSelectionModel().addListSelectionListener(e -> {
                boolean sel = tabla.getSelectedRow() >= 0;
                btnEditar.setEnabled(sel);
                btnAjustarStock.setEnabled(sel);
                btnEliminar.setEnabled(sel);
            });

            tabla.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0)
                        editarSeleccionado();
                }
            });

            btnAgregar.addActionListener(e -> abrirDialogoRepuesto(null));
            btnEditar.addActionListener(e -> editarSeleccionado());
            btnAjustarStock.addActionListener(e -> ajustarStock());
            btnEliminar.addActionListener(e -> toggleActivoSeleccionado());
            btnTecnicos.addActionListener(e -> abrirDialogoTecnico());
            btnCategorias.addActionListener(e -> abrirDialogoCategoria());

            filaBotones.add(btnAgregar);
            filaBotones.add(btnEditar);
            filaBotones.add(btnAjustarStock);
            filaBotones.add(btnEliminar);
            filaBotones.add(btnTecnicos);
            filaBotones.add(btnCategorias);
        }

        panelSouth.add(filaBotones);
        root.add(panelSouth, BorderLayout.SOUTH);

        add(root);

        txtFiltro.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String txt = txtFiltro.getText().trim();
                sorter.setRowFilter(txt.isEmpty() ? null : RowFilter.regexFilter("(?i)" + txt, 1, 2, 3));
            }
        });

        btnRecargar.addActionListener(e -> recargar());
        cargarDatos();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    // =====================================================================
    // DATOS
    // =====================================================================
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
                    r.getCategoria(), r.getStock(),
                    String.format("$%,.0f", r.getPrecioConIva()),
                    r.isActivo() ? "ACTIVO" : "INACTIVO",
                    construirIconoMiniatura(r.getRutaImagen())
            });
    }

    // =====================================================================
    // DIALOGO AGREGAR / EDITAR
    // =====================================================================
    private void abrirDialogoRepuesto(Repuesto rep) {
        boolean nuevo = (rep == null);
        JDialog dlg = new JDialog(this, nuevo ? "Agregar repuesto" : "Editar repuesto", true);
        dlg.setSize(460, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCod = campo(nuevo ? "" : rep.getId());
        JTextField txtNom = campo(nuevo ? "" : rep.getNombre());
        JTextField txtRef = campo(nuevo ? "" : rep.getReferenciaOem());
        JTextField txtMarca = campo(nuevo ? "" : rep.getMarca());
        JTextField txtModelo = campo(nuevo ? "" : rep.getModeloCompatible());
        JTextField txtPrecio = campo(nuevo ? "" : String.format("%.0f", rep.getPrecio()));
        JTextField txtStock = campo(nuevo ? "0" : String.valueOf(rep.getStock()));

        JLabel lblPreview = new JLabel("Sin imagen", SwingConstants.CENTER);
        lblPreview.setPreferredSize(new Dimension(90, 90));
        lblPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 190, 180)));
        lblPreview.setFont(new Font("Segoe UI", Font.ITALIC, 10));

        final File[] imagenSeleccionada = { null };
        final String[] rutaImagenActual = { nuevo ? null : rep.getRutaImagen() };

        if (!nuevo && rep.getRutaImagen() != null) {
            File imgActual = GestorImagenes.obtenerArchivo(rep.getRutaImagen());
            if (imgActual != null)
                setPreview(lblPreview, imgActual);
        }

        JButton btnImagen = new JButton("Seleccionar imagen...");
        UIUtils.estilizarBoton(btnImagen, new Color(80, 80, 150));
        btnImagen.addActionListener(e -> {
            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Imagenes", "jpg", "jpeg", "png"));
            int res = chooser.showOpenDialog(dlg);
            if (res == javax.swing.JFileChooser.APPROVE_OPTION) {
                imagenSeleccionada[0] = chooser.getSelectedFile();
                setPreview(lblPreview, imagenSeleccionada[0]);
            }
        });

        txtCod.setEditable(nuevo);
        if (!nuevo)
            txtCod.setBackground(new Color(235, 235, 235));

        Map<Integer, String> cats = cargarCategorias();
        JComboBox<String> cmbCat = new JComboBox<>(cats.values().toArray(new String[0]));
        if (!nuevo) {
            int i = 0;
            for (String v : cats.values()) {
                if (v.equals(rep.getCategoria())) {
                    cmbCat.setSelectedIndex(i);
                    break;
                }
                i++;
            }
        }

        String[] etiq = { "Codigo:", "Nombre:", "Referencia OEM:", "Marca:", "Modelo compatible:",
                "Precio ($):", "Stock:", "Categoria:" };
        Component[] ctrl = { txtCod, txtNom, txtRef, txtMarca, txtModelo, txtPrecio, txtStock, cmbCat };
        for (int i = 0; i < etiq.length; i++) {
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0.35;
            panel.add(new JLabel(etiq[i]), g);
            g.gridx = 1;
            g.weightx = 0.65;
            panel.add(ctrl[i], g);
        }

        JLabel lblErr = new JLabel(" ", SwingConstants.CENTER);
        lblErr.setForeground(Color.RED);
        lblErr.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        g.gridx = 0;
        g.gridy = etiq.length;
        panel.add(new JLabel("Imagen:"), g);
        g.gridx = 1;
        panel.add(btnImagen, g);

        g.gridx = 0;
        g.gridy = etiq.length + 1;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER;
        panel.add(lblPreview, g);
        g.anchor = GridBagConstraints.WEST;

        g.gridy = etiq.length + 2;
        panel.add(lblErr, g);

        JPanel bp = new JPanel(new GridLayout(1, 2, 10, 0));
        bp.setBackground(Color.WHITE);
        JButton btnOk = new JButton(nuevo ? "Agregar" : "Guardar cambios");
        JButton btnCan = new JButton("Cancelar");
        UIUtils.estilizarBoton(btnOk, new Color(30, 130, 76));
        UIUtils.estilizarBoton(btnCan, new Color(120, 120, 120));
        bp.add(btnOk);
        bp.add(btnCan);
        g.gridy = etiq.length + 3;
        panel.add(bp, g);

        dlg.add(panel);
        btnCan.addActionListener(e -> dlg.dispose());

        btnOk.addActionListener(e -> {
            String cod = txtCod.getText().trim().toUpperCase();
            if (cod.isBlank() && nuevo) {
                cod = generarCodigoConsecutivo();
            }
            String nom = txtNom.getText().trim();
            String prS = txtPrecio.getText().trim().replace(",", "").replace("$", "");
            String stS = txtStock.getText().trim();

            if (cod.isEmpty() || nom.isEmpty() || prS.isEmpty()) {
                lblErr.setText("Codigo, nombre y precio son obligatorios.");
                return;
            }
            double pr;
            int st;
            try {
                pr = Double.parseDouble(prS);
            } catch (NumberFormatException ex) {
                lblErr.setText("Precio invalido.");
                return;
            }
            try {
                st = Integer.parseInt(stS);
            } catch (NumberFormatException ex) {
                lblErr.setText("Stock invalido.");
                return;
            }
            if (pr <= 0) {
                lblErr.setText("El precio debe ser mayor a 0.");
                return;
            }
            if (st < 0) {
                lblErr.setText("El stock no puede ser negativo.");
                return;
            }

            String catNom = (String) cmbCat.getSelectedItem();
            int idCat = cats.entrySet().stream()
                    .filter(en -> en.getValue().equals(catNom))
                    .mapToInt(Map.Entry::getKey).findFirst().orElse(1);

            String rutaImagenFinal = rutaImagenActual[0];
            if (imagenSeleccionada[0] != null) {
                try {
                    rutaImagenFinal = GestorImagenes.guardarImagenRepuesto(cod, imagenSeleccionada[0]);
                } catch (Exception exImg) {
                    lblErr.setText("Error al guardar imagen: " + exImg.getMessage());
                    return;
                }
            }

            try {
                if (nuevo) {
                    if (repuestoDAO.existe(cod)) {
                        lblErr.setText("Ya existe el codigo " + cod);
                        return;
                    }
                    Repuesto nuevoRep = new Repuesto(cod, nom, pr, catNom, 0, st);
                    nuevoRep.setReferenciaOem(txtRef.getText().trim());
                    nuevoRep.setMarca(txtMarca.getText().trim());
                    nuevoRep.setModeloCompatible(txtModelo.getText().trim());
                    nuevoRep.setRutaImagen(rutaImagenFinal);
                    repuestoDAO.insertar(nuevoRep, idCat);
                } else {
                    Repuesto actualizado = new Repuesto(cod, nom, pr, catNom, rep.getImpuesto(), st);
                    actualizado.setReferenciaOem(txtRef.getText().trim());
                    actualizado.setMarca(txtMarca.getText().trim());
                    actualizado.setModeloCompatible(txtModelo.getText().trim());
                    actualizado.setRutaImagen(rutaImagenFinal);
                    repuestoDAO.actualizar(actualizado, idCat);
                }
                recargar();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        nuevo ? "Repuesto \"" + nom + "\" agregado correctamente."
                                : "Repuesto \"" + nom + "\" actualizado correctamente.",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    private void abrirDialogoTecnico() {
        JDialog dlg = new JDialog(this, "Registrar tecnico", true);
        dlg.setSize(420, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtId = campo("T001");
        JTextField txtNombre = campo("");
        JComboBox<String> cmbTurno = new JComboBox<>(new String[] { "MAÑANA", "TARDE", "NOCHE" });
        JTextField txtPass = campo("tecnico123");

        String[] etiq = { "Codigo tecnico:", "Nombre:", "Turno:", "Contrasena:" };
        Component[] ctrl = { txtId, txtNombre, cmbTurno, txtPass };
        for (int i = 0; i < etiq.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35;
            panel.add(new JLabel(etiq[i]), g);
            g.gridx = 1; g.weightx = 0.65;
            panel.add(ctrl[i], g);
        }

        JLabel lblErr = new JLabel(" ", SwingConstants.CENTER);
        lblErr.setForeground(Color.RED);
        lblErr.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g.gridx = 0; g.gridy = etiq.length; g.gridwidth = 2;
        panel.add(lblErr, g);

        JPanel bp = new JPanel(new GridLayout(1, 2, 10, 0));
        bp.setBackground(Color.WHITE);
        JButton btnOk = new JButton("Guardar tecnico");
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
            String id = txtId.getText().trim().toUpperCase();
            String nombre = txtNombre.getText().trim();
            String turno = (String) cmbTurno.getSelectedItem();
            String pass = txtPass.getText().trim();
            if (id.isBlank() || nombre.isBlank() || pass.isBlank()) {
                lblErr.setText("Codigo, nombre y contrasena son obligatorios.");
                return;
            }
            try {
                Cajero c = new Cajero(id, nombre, turno, Cajero.hashPassword(pass), "TECNICO");
                cajeroDAO.guardar(c);
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "Tecnico \"" + nombre + "\" guardado correctamente.",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    // =====================================================================
    // DIALOGO AJUSTAR STOCK
    // =====================================================================
    private void ajustarStock() {
        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        if (fila < 0)
            return;
        String id = (String) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 1);
        int actual = (int) modelo.getValueAt(fila, 7);

        JDialog dlg = new JDialog(this, "Ajustar stock — " + nombre, true);
        dlg.setSize(340, 240);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblAct = new JLabel("Stock actual: " + actual + " unidades");
        lblAct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAct.setForeground(ROJO);

        JComboBox<String> cmbOp = new JComboBox<>(new String[] {
                "Establecer cantidad exacta", "Agregar unidades", "Restar unidades" });
        JTextField txtCant = campo("0");
        JLabel lblErr = new JLabel(" ", SwingConstants.CENTER);
        lblErr.setForeground(Color.RED);

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        panel.add(lblAct, g);
        g.gridy = 1;
        panel.add(cmbOp, g);
        g.gridwidth = 1;
        g.gridy = 2;
        g.gridx = 0;
        g.weightx = 0.4;
        panel.add(new JLabel("Cantidad:"), g);
        g.gridx = 1;
        g.weightx = 0.6;
        panel.add(txtCant, g);
        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 2;
        panel.add(lblErr, g);

        JPanel bp = new JPanel(new GridLayout(1, 2, 8, 0));
        bp.setBackground(Color.WHITE);
        JButton btnOk = new JButton("Aplicar");
        JButton btnCan = new JButton("Cancelar");
        UIUtils.estilizarBoton(btnOk, new Color(140, 80, 10));
        UIUtils.estilizarBoton(btnCan, new Color(120, 120, 120));
        bp.add(btnOk);
        bp.add(btnCan);
        g.gridy = 4;
        panel.add(bp, g);

        dlg.add(panel);
        btnCan.addActionListener(e -> dlg.dispose());

        btnOk.addActionListener(e -> {
            int cant;
            try {
                cant = Integer.parseInt(txtCant.getText().trim());
            } catch (NumberFormatException ex) {
                lblErr.setText("Cantidad invalida.");
                return;
            }
            if (cant < 0) {
                lblErr.setText("Ingrese un numero positivo.");
                return;
            }

            String operacion = switch (cmbOp.getSelectedIndex()) {
                case 0 -> "EXACTA";
                case 1 -> "AGREGAR";
                case 2 -> "RESTAR";
                default -> "EXACTA";
            };
            int nuevoStock = calcularNuevoStock(actual, cant, operacion);
            try {
                repuestoDAO.actualizarStock(id, nuevoStock);
                recargar();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "Stock de \"" + nombre + "\" actualizado: " + actual + " -> " + nuevoStock,
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    /** Metodo puro, facil de testear por separado. */
    static int calcularNuevoStock(int stockActual, int cantidad, String operacion) {
        return switch (operacion) {
            case "AGREGAR" -> stockActual + cantidad;
            case "RESTAR" -> Math.max(0, stockActual - cantidad);
            case "EXACTA" -> Math.max(0, cantidad);
            default -> stockActual;
        };
    }

    // =====================================================================
    // ACTIVAR / DESACTIVAR
    // =====================================================================
    private void toggleActivoSeleccionado() {
        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        if (fila < 0)
            return;
        String id = (String) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 1);
        boolean activo = "ACTIVO".equals(modelo.getValueAt(fila, 9));
        String accion = activo ? "desactivar" : "reactivar";

        int r = JOptionPane.showConfirmDialog(this,
                "Desea " + accion + " el repuesto:\n\"" + nombre + "\"?\n\n" +
                        (activo ? "No aparecera en nuevas ventas."
                                : "Volvera a estar disponible para ventas."),
                "Confirmar " + accion,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION)
            return;

        try {
            repuestoDAO.cambiarActivo(id, !activo);
            recargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarSeleccionado() {
        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        if (fila < 0)
            return;
        Repuesto r = sistema.getInventario().buscarRepuesto((String) modelo.getValueAt(fila, 0));
        if (r != null)
            abrirDialogoRepuesto(r);
    }

    private void abrirDialogoCategoria() {
        JDialog dlg = new JDialog(this, "Agregar categoria", true);
        dlg.setSize(360, 220);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtCat = campo("");
        JTextField txtImpuesto = campo("0");
        JLabel lblErr = new JLabel(" ", SwingConstants.CENTER);
        lblErr.setForeground(Color.RED);
        lblErr.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        g.gridx = 0; g.gridy = 0; g.weightx = 0.35;
        panel.add(new JLabel("Categoria:"), g);
        g.gridx = 1; g.weightx = 0.65;
        panel.add(txtCat, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0.35;
        panel.add(new JLabel("IVA (%):"), g);
        g.gridx = 1; g.weightx = 0.65;
        panel.add(txtImpuesto, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        panel.add(lblErr, g);

        JPanel bp = new JPanel(new GridLayout(1, 2, 10, 0));
        bp.setBackground(Color.WHITE);
        JButton btnOk = new JButton("Guardar");
        JButton btnCan = new JButton("Cancelar");
        UIUtils.estilizarBoton(btnOk, new Color(30, 130, 76));
        UIUtils.estilizarBoton(btnCan, new Color(120, 120, 120));
        bp.add(btnOk);
        bp.add(btnCan);
        g.gridy = 3;
        panel.add(bp, g);

        dlg.add(panel);
        btnCan.addActionListener(e -> dlg.dispose());
        btnOk.addActionListener(e -> {
            String nombre = txtCat.getText().trim();
            String impTxt = txtImpuesto.getText().trim().replace("%", "");
            if (nombre.isBlank()) {
                lblErr.setText("La categoria es obligatoria.");
                return;
            }
            double impuesto;
            try {
                impuesto = Double.parseDouble(impTxt) / 100.0;
            } catch (NumberFormatException ex) {
                lblErr.setText("IVA invalido.");
                return;
            }
            try {
                repuestoDAO.insertarCategoria(nombre, impuesto);
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "Categoria \"" + nombre + "\" guardada correctamente.",
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
                recargar();
            } catch (Exception ex) {
                lblErr.setText("Error: " + ex.getMessage());
            }
        });

        dlg.setVisible(true);
    }

    private Map<Integer, String> cargarCategorias() {
        Map<Integer, String> cats = new LinkedHashMap<>();
        try {
            cats.putAll(repuestoDAO.listarCategorias());
        } catch (Exception ex) {
            cats.put(1, "General");
        }
        if (cats.isEmpty())
            cats.put(1, "General");
        return cats;
    }

    private String generarCodigoConsecutivo() {
        int max = 0;
        for (String id : sistema.getInventario().getTodos().keySet()) {
            String codigo = id == null ? "" : id.trim().toUpperCase();
            if (codigo.matches(".*\\d+")) {
                String num = codigo.replaceAll("\\D+", "");
                if (!num.isBlank()) {
                    try {
                        max = Math.max(max, Integer.parseInt(num));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return "R" + String.format("%03d", max + 1);
    }

    private ImageIcon construirIconoMiniatura(String rutaImagen) {
        File img = GestorImagenes.obtenerArchivo(rutaImagen);
        if (img == null || !img.exists())
            return null;
        try {
            java.awt.Image imgScaled = new ImageIcon(img.getAbsolutePath())
                    .getImage().getScaledInstance(46, 46, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(imgScaled);
        } catch (Exception ex) {
            return null;
        }
    }

    // =====================================================================
    // UTILIDADES
    // =====================================================================
    private JTextField campo(String v) {
        JTextField t = new JTextField(v, 16);
        UIUtils.estilizarCampo(t);
        return t;
    }

    private JLabel pastilla(Color bg, String txt) {
        JLabel l = new JLabel("  " + txt + "  ");
        l.setOpaque(true);
        l.setBackground(bg);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));
        return l;
    }

    private void setPreview(JLabel label, File archivo) {
        try {
            java.awt.Image img = new javax.swing.ImageIcon(archivo.getAbsolutePath())
                    .getImage().getScaledInstance(90, 90, java.awt.Image.SCALE_SMOOTH);
            label.setIcon(new javax.swing.ImageIcon(img));
            label.setText(null);
        } catch (Exception ex) {
            label.setIcon(null);
            label.setText("Sin imagen");
        }
    }
}
