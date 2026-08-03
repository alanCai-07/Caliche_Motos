package calichemotos.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import calichemotos.servicio.SistemaFacturacion;

public class MenuPrincipalFrame extends JFrame {

    private static final Color ROJO       = new Color(180, 45, 25);
    private static final Color VERDE      = new Color(30, 130, 76);
    private static final Color NARANJA    = new Color(200, 100, 20);
    private static final Color ROJO_OSCURO = new Color(140, 30, 30);
    private static final Color GRIS       = new Color(80, 80, 80);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenido = new JPanel(cardLayout);

    public MenuPrincipalFrame() {
        String cajero = SistemaFacturacion.getInstance().getCajeroActivo().getNombre();
        setTitle("Caliche Motos - Menu Principal  |  Usuario: " + cajero);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(AppIcon.getIcon());
        construirUI(cajero);
        setLocationRelativeTo(null);
    }

    private void construirUI(String cajero) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(ROJO);
        banner.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lblTitulo = new JLabel("CALICHE MOTOS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JButton btnInicio = new JButton("<- Menu principal");
        btnInicio.setFocusPainted(false);
        btnInicio.setBorderPainted(false);
        btnInicio.setOpaque(true);
        btnInicio.setBackground(Color.WHITE);
        btnInicio.setForeground(ROJO);
        btnInicio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnInicio.addActionListener(e -> mostrarVista("menu"));

        banner.add(btnInicio, BorderLayout.WEST);
        banner.add(lblTitulo, BorderLayout.CENTER);
        root.add(banner, BorderLayout.NORTH);

        panelContenido.setBackground(Color.WHITE);
        panelContenido.add(crearVistaMenu(cajero), "menu");
        panelContenido.add(crearVistaModulo(new InventarioFrame(), "Inventario"), "inventario");
        panelContenido.add(crearVistaModulo(new NuevaVentaFrame(this), "Nueva Venta"), "nuevaVenta");
        panelContenido.add(crearVistaModulo(new ClienteFrame(), "Clientes"), "clientes");
        panelContenido.add(crearVistaModulo(new ReportesFrame(), "Reportes"), "reportes");
        panelContenido.add(crearVistaModulo(new BuscarFacturaFrame(), "Buscar Factura"), "buscarFactura");
        panelContenido.setPreferredSize(new Dimension(1050, 600));

        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(250, 245, 243));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));

        JButton sInventario = botonMenu("Inventario", ROJO);
        JButton sNuevaVenta = botonMenu("Nueva Venta", VERDE);
        JButton sClientes   = botonMenu("Clientes", VERDE);
        JButton sReportes   = botonMenu("Reportes", NARANJA);
        JButton sBuscar     = botonMenu("Buscar Factura", new Color(80, 80, 150));
        JButton sSalir      = botonMenu("Cerrar Sesion", ROJO_OSCURO);

        for (JButton b : new JButton[]{sInventario, sNuevaVenta, sClientes, sReportes, sBuscar, sSalir})
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        sidebar.add(sInventario);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sNuevaVenta);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sClientes);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sReportes);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sBuscar);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sSalir);

        sInventario.addActionListener(e -> mostrarVista("inventario"));
        sNuevaVenta.addActionListener(e -> mostrarVista("nuevaVenta"));
        sClientes.addActionListener(e -> mostrarVista("clientes"));
        sReportes.addActionListener(e -> mostrarVista("reportes"));
        sBuscar.addActionListener(e -> mostrarVista("buscarFactura"));
        sSalir.addActionListener(e -> cerrarSesion());

        root.add(sidebar, BorderLayout.WEST);
        root.add(panelContenido, BorderLayout.CENTER);
        add(root);

        mostrarVista("menu");
    }

    private JPanel crearVistaMenu(String cajero) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel lblBienvenida = new JLabel("Bienvenido, " + cajero, SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBienvenida.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        panel.add(lblBienvenida, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));

        JButton btnInventario = botonMenuGrande("Inventario", "Ver repuestos disponibles", ROJO);
        JButton btnNuevaVenta = botonMenuGrande("Nueva Venta", "Registrar una nueva factura", VERDE);
        JButton btnClientes   = botonMenuGrande("Clientes", "Registrar o buscar clientes", VERDE);
        JButton btnReportes   = botonMenuGrande("Reportes", "Generar reportes de ventas", NARANJA);
        JButton btnBuscarFact = botonMenuGrande("Buscar Factura", "Consultar o anular facturas", new Color(80, 80, 150));
        JButton btnSalir      = botonMenuGrande("Cerrar Sesion", "Salir del sistema", ROJO_OSCURO);

        grid.add(btnInventario);
        grid.add(btnNuevaVenta);
        grid.add(btnClientes);
        grid.add(btnReportes);
        grid.add(btnBuscarFact);
        grid.add(btnSalir);

        panel.add(grid, BorderLayout.CENTER);

        btnInventario.addActionListener(e -> mostrarVista("inventario"));
        btnNuevaVenta.addActionListener(e -> mostrarVista("nuevaVenta"));
        btnClientes.addActionListener(e -> mostrarVista("clientes"));
        btnReportes.addActionListener(e -> mostrarVista("reportes"));
        btnBuscarFact.addActionListener(e -> mostrarVista("buscarFactura"));
        btnSalir.addActionListener(e -> cerrarSesion());

        return panel;
    }

    private JPanel crearVistaPendiente(String nombre, String detalle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("<html><center><b>" + nombre + "</b><br><br>"
                + "<span style='font-size:11px;color:#888'>" + detalle + "</span></center></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this,
                "Desea cerrar la sesion?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            SistemaFacturacion.getInstance().logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private JPanel crearVistaModulo(JFrame frame, String nombreVista) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        if (frame != null) {
            JPanel moduleRoot = null;
            try {
                java.lang.reflect.Method m = frame.getClass().getMethod("getRootPanel");
                Object res = m.invoke(frame);
                if (res instanceof JPanel)
                    moduleRoot = (JPanel) res;
            } catch (Exception ignored) {
            }

            if (moduleRoot != null) {
                if (moduleRoot.getParent() instanceof java.awt.Container) {
                    ((java.awt.Container) moduleRoot.getParent()).remove(moduleRoot);
                }
                panel.add(moduleRoot, BorderLayout.CENTER);
                frame.dispose();
            } else {
                frame.setVisible(false);
                panel.add(frame.getContentPane(), BorderLayout.CENTER);
            }
        }

        return panel;
    }

    private void mostrarVista(String nombreVista) {
        cardLayout.show(panelContenido, nombreVista);
    }

    private JButton botonMenu(String titulo, Color color) {
        JButton btn = new JButton(titulo);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 46));
        return btn;
    }

    private JButton botonMenuGrande(String titulo, String subtitulo, Color color) {
        JButton btn = new JButton("<html><center><b>" + titulo + "</b><br>"
                + "<span style='font-size:9px;color:#ddd'>" + subtitulo + "</span>"
                + "</center></html>");
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 90));

        Color hover = color.brighter();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(color); }
        });
        return btn;
    }
}
