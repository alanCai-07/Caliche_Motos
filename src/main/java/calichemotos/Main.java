package calichemotos;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import calichemotos.db.ConexionDB;
import calichemotos.ui.LoginFrame;

/**
 * Punto de entrada principal del sistema de facturacion Caliche Motos.
 * Ejecutar con el boton Run de VS Code, o:
 * mvn compile exec:java -Dexec.mainClass="calichemotos.Main"
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                ConexionDB.getConexion();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a Neon (PostgreSQL).\n\n" +
                        "Verifique que:\n" +
                        "  1. config.properties exista y tenga las credenciales correctas\n" +
                        "  2. El host, usuario y password sean los que entrega Neon\n" +
                        "  3. El schema configurado sea 'caliche_motos'\n\n" +
                        "Error: " + e.getMessage(),
                        "Error de conexion", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(ConexionDB::cerrar));

            new LoginFrame().setVisible(true);
        });
    }
}
