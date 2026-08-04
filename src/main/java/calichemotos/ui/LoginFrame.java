package calichemotos.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import calichemotos.dao.CajeroDAO;
import calichemotos.db.ConexionDB;
import calichemotos.servicio.SistemaFacturacion;

public class LoginFrame extends JFrame {

    private static final Color COLOR_PRIMARIO = new Color(180, 45, 25);

    private JComboBox<String> cmbNombre;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JButton btnRecargar;
    private JLabel lblError;

    public LoginFrame() {
        setTitle("Caliche Motos — Inicio de sesion");
        setSize(420, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(AppIcon.getIcon());
        setLocationRelativeTo(null);
        setResizable(false);
        construirUI();
        cargarNombresAsync();
    }

    private void construirUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 44, 20, 44));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        JLabel titulo = new JLabel("CALICHE MOTOS", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titulo.setForeground(COLOR_PRIMARIO);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        panel.add(titulo, g);

        JLabel sub = new JLabel("Sistema de Facturacion - Repuestos de Moto", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Color.GRAY);
        g.gridy = 1;
        panel.add(sub, g);

        JSeparator sep = new JSeparator();
        g.gridy = 2;
        g.insets = new Insets(4, 0, 14, 0);
        panel.add(sep, g);
        g.insets = new Insets(6, 0, 6, 0);

        g.gridwidth = 1;
        g.gridy = 3;
        g.gridx = 0;
        g.weightx = 0.38;
        panel.add(new JLabel("Usuario:"), g);

        cmbNombre = new JComboBox<>(new String[] { "Cargando..." });
        cmbNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbNombre.setEnabled(false);
        g.gridx = 1;
        g.weightx = 0.62;
        panel.add(cmbNombre, g);

        g.gridy = 4;
        g.gridx = 0;
        g.weightx = 0.38;
        panel.add(new JLabel("Contrasena:"), g);

        txtPass = new JPasswordField();
        UIUtils.estilizarCampo(txtPass);
        g.gridx = 1;
        g.weightx = 0.62;
        panel.add(txtPass, g);

        lblError = new JLabel("Conectando a la base de datos...", SwingConstants.CENTER);
        lblError.setForeground(new Color(100, 100, 100));
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g.gridy = 5;
        g.gridx = 0;
        g.gridwidth = 2;
        panel.add(lblError, g);

        JPanel panelBtns = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        panelBtns.setBackground(Color.WHITE);

        btnRecargar = new JButton("Recargar");
        btnLogin = new JButton("Ingresar");
        UIUtils.estilizarBoton(btnRecargar, new Color(100, 100, 100));
        UIUtils.estilizarBoton(btnLogin, COLOR_PRIMARIO);
        btnLogin.setEnabled(false);
        btnRecargar.setEnabled(false);

        panelBtns.add(btnRecargar);
        panelBtns.add(btnLogin);

        g.gridy = 6;
        panel.add(panelBtns, g);

        add(panel);

        btnLogin.addActionListener(e -> intentarLogin());
        btnRecargar.addActionListener(e -> cargarNombresAsync());
        txtPass.addActionListener(e -> intentarLogin());
    }

    private void cargarNombresAsync() {
        cmbNombre.setEnabled(false);
        btnLogin.setEnabled(false);
        btnRecargar.setEnabled(false);
        lblError.setText("Conectando a la base de datos...");
        lblError.setForeground(new Color(100, 100, 100));
        cmbNombre.removeAllItems();
        cmbNombre.addItem("Cargando...");

        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                ConexionDB.cerrar();
                ConexionDB.getConexion();
                return new CajeroDAO().listarNombres();
            }

            @Override
            protected void done() {
                try {
                    List<String> nombres = get();
                    cmbNombre.removeAllItems();

                    if (nombres.isEmpty()) {
                        cmbNombre.addItem("(Sin cajeros registrados)");
                        lblError.setText("No hay usuarios activos en la BD.");
                        lblError.setForeground(Color.ORANGE.darker());
                    } else {
                        for (String n : nombres)
                            cmbNombre.addItem(n);
                        cmbNombre.setEnabled(true);
                        btnLogin.setEnabled(true);
                        lblError.setText("Seleccione su usuario e ingrese la contrasena.");
                        lblError.setForeground(new Color(60, 120, 60));
                        txtPass.requestFocus();
                    }
                } catch (Exception ex) {
                    cmbNombre.removeAllItems();
                    cmbNombre.addItem("(Sin conexion)");

                    String msg = ex.getCause() != null
                            ? ex.getCause().getMessage()
                            : ex.getMessage();

                    lblError.setText("<html><center>Error de conexion.<br>"
                            + "<font size='2'>" + truncar(msg, 55) + "</font></center></html>");
                    lblError.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "No se pudo conectar a la base de datos.\n\n"
                                    + "Verifique:\n"
                                    + "  1. Que el archivo config.properties exista\n"
                                    + "  2. Que el host/usuario/password sean correctos\n"
                                    + "  3. Que el schema en Neon sea 'caliche_motos'\n\n"
                                    + "Detalle: " + msg,
                            "Error de conexion",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnRecargar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void intentarLogin() {
        String nombre = (String) cmbNombre.getSelectedItem();
        String pass = new String(txtPass.getPassword());

        if (nombre == null || nombre.startsWith("(") || pass.isEmpty()) {
            lblError.setText("Seleccione un usuario e ingrese la contrasena.");
            lblError.setForeground(Color.RED);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Verificando...");
        btnRecargar.setEnabled(false);
        lblError.setText(" ");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return SistemaFacturacion.getInstance().login(nombre, pass);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        dispose();
                        new MenuPrincipalFrame().setVisible(true);
                    } else {
                        lblError.setText("Contrasena incorrecta. Intente de nuevo.");
                        lblError.setForeground(Color.RED);
                        txtPass.setText("");
                        txtPass.requestFocus();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Ingresar");
                        btnRecargar.setEnabled(true);
                    }
                } catch (Exception ex) {
                    lblError.setText("Error: " + ex.getMessage());
                    lblError.setForeground(Color.RED);
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Ingresar");
                    btnRecargar.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private static String truncar(String s, int max) {
        if (s == null)
            return "Sin detalles";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new LoginFrame().setVisible(true);
        });
    }
}
