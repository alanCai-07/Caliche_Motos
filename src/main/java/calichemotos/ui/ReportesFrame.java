package calichemotos.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import calichemotos.reporte.GeneradorReportePDF;
import calichemotos.servicio.SistemaFacturacion;

public class ReportesFrame extends JFrame {

    private static final Color ROJO = new Color(180, 45, 25);

    private final SistemaFacturacion sistema = SistemaFacturacion.getInstance();
    private JTextField txtDesde, txtHasta;
    private JLabel lblEstado;
    private JPanel rootPanel;

    public ReportesFrame() {
        setTitle("Generar Reportes");
        setSize(520, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(AppIcon.getIcon());
        setResizable(false);
        construirUI();
        setLocationRelativeTo(null);
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        root.setBackground(Color.WHITE);
        this.rootPanel = root;

        JLabel titulo = new JLabel("Generacion de Reportes", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(ROJO);
        root.add(titulo, BorderLayout.NORTH);

        JPanel panelFechas = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        panelFechas.setBackground(Color.WHITE);
        panelFechas.setBorder(BorderFactory.createTitledBorder("Rango de fechas (yyyy-MM-dd)"));

        String hoy = LocalDate.now().toString();
        String primerDiaMes = LocalDate.now().withDayOfMonth(1).toString();

        txtDesde = new JTextField(primerDiaMes, 12);
        txtHasta = new JTextField(hoy, 12);
        UIUtils.estilizarCampo(txtDesde);
        UIUtils.estilizarCampo(txtHasta);

        panelFechas.add(new JLabel("Desde:"));
        panelFechas.add(txtDesde);
        panelFechas.add(new JLabel("Hasta:"));
        panelFechas.add(txtHasta);
        root.add(panelFechas, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 0, 10));
        panelBotones.setBackground(Color.WHITE);

        JButton btnDiario = boton("Reporte de Ventas del Dia (hoy)", ROJO);
        JButton btnRepuestos = boton("Top 20 Repuestos Mas Vendidos (rango)", new Color(30, 130, 76));
        JButton btnUsuario = boton("Ventas por Usuario (rango)", new Color(140, 80, 10));

        lblEstado = new JLabel(" ", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblEstado.setForeground(new Color(30, 130, 76));

        panelBotones.add(btnDiario);
        panelBotones.add(btnRepuestos);
        panelBotones.add(btnUsuario);
        panelBotones.add(lblEstado);
        root.add(panelBotones, BorderLayout.SOUTH);

        add(root);

        btnDiario.addActionListener(e -> generarReporte("DIARIO"));
        btnRepuestos.addActionListener(e -> generarReporte("REPUESTOS"));
        btnUsuario.addActionListener(e -> generarReporte("USUARIO"));
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    private void generarReporte(String tipo) {
        LocalDate desde, hasta;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            desde = LocalDate.parse(txtDesde.getText().trim(), fmt);
            hasta = LocalDate.parse(txtHasta.getText().trim(), fmt);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha invalido. Use: yyyy-MM-dd", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblEstado.setText("Generando PDF...");
        lblEstado.setForeground(ROJO);
        final LocalDate d = desde, h = hasta;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return switch (tipo) {
                    case "DIARIO" -> GeneradorReportePDF.reporteVentasDiarias(LocalDate.now(), sistema.getFacturaDAO());
                    case "REPUESTOS" -> GeneradorReportePDF.reporteTopRepuestos(d, h, sistema.getFacturaDAO());
                    case "USUARIO" -> GeneradorReportePDF.reporteVentasPorCajero(d, h, sistema.getFacturaDAO());
                    default -> throw new Exception("Tipo desconocido");
                };
            }

            @Override
            protected void done() {
                try {
                    String ruta = get();
                    lblEstado.setText("PDF generado correctamente.");
                    lblEstado.setForeground(new Color(30, 130, 76));

                    int resp = JOptionPane.showConfirmDialog(ReportesFrame.this,
                            "Reporte generado en:\n" + ruta + "\n\nDesea abrirlo?",
                            "Exito", JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);

                    if (resp == JOptionPane.YES_OPTION)
                        Desktop.getDesktop().open(new File(ruta));

                } catch (Exception ex) {
                    lblEstado.setText("Error: " + ex.getMessage());
                    lblEstado.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(ReportesFrame.this,
                            "Error al generar el reporte: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        UIUtils.estilizarBoton(b, color);
        b.setPreferredSize(new Dimension(0, 42));
        return b;
    }
}
