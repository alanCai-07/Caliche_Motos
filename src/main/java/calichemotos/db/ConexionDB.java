package calichemotos.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDB {

    private static Connection conexion;

    private ConexionDB() {}

    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conectar();
        }
        return conexion;
    }

    private static void conectar() throws SQLException {
        ConfiguracionApp cfg = ConfiguracionApp.getInstance();
        try {
            Class.forName(cfg.getDriverClass());

            String url      = cfg.buildJdbcUrl();
            String usuario  = cfg.getUsuario();
            String password = cfg.getPassword();

            conexion = DriverManager.getConnection(url, usuario, password);
            conexion.setAutoCommit(true);

            String schema = cfg.getSchema();
            try (Statement st = conexion.createStatement()) {
                st.execute("SET search_path TO \"" + schema + "\"");
            }

            try (Statement st = conexion.createStatement();
                 ResultSet rs = st.executeQuery("SHOW search_path")) {
                if (rs.next())
                    System.out.println("[DB] search_path efectivo: " + rs.getString(1));
            }

            System.out.println("[DB] Conectado a Neon (" + cfg.getNombreDB()
                    + ") schema configurado=" + schema);

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "[DB] Driver JDBC no encontrado.\nVerifica la dependencia en pom.xml.\n" + e.getMessage());
        }
    }

    public static void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("[DB] Conexion cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al cerrar conexion: " + e.getMessage());
        }
    }

    public static boolean isConectado() {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}