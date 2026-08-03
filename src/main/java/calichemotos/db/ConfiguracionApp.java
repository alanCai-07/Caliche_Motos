package calichemotos.db;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Lee la configuracion del archivo config.properties.
 *
 * Orden de busqueda:
 * 1. ./config.properties (junto al JAR o raiz del proyecto)
 * 2. ~/calichemotos/config.properties
 * 3. Classpath (src/main/resources) - solo desarrollo en VS Code
 */
public class ConfiguracionApp {

    private static final String ARCHIVO = "config.properties";
    private static ConfiguracionApp instancia;
    private final Properties props = new Properties();

    private static final Path[] RUTAS_BUSQUEDA = {
            Paths.get(ARCHIVO),
            Paths.get(System.getProperty("user.home"), "calichemotos", ARCHIVO),
            Paths.get("src", "main", "resources", ARCHIVO)
    };

    private ConfiguracionApp() {
        cargar();
    }

    public static ConfiguracionApp getInstance() {
        if (instancia == null)
            instancia = new ConfiguracionApp();
        return instancia;
    }

    private void cargar() {
        for (Path ruta : RUTAS_BUSQUEDA) {
            if (Files.exists(ruta)) {
                try (InputStream is = Files.newInputStream(ruta)) {
                    props.load(is);
                    System.out.println("[Config] Configuracion cargada desde: " + ruta.toAbsolutePath());
                    return;
                } catch (IOException e) {
                    System.err.println("[Config] Error leyendo " + ruta + ": " + e.getMessage());
                }
            }
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (is != null) {
                props.load(is);
                System.out.println("[Config] Configuracion cargada desde classpath (modo desarrollo).");
                return;
            }
        } catch (IOException e) {
            System.err.println("[Config] Error leyendo classpath: " + e.getMessage());
        }

        throw new RuntimeException(
                "\nARCHIVO DE CONFIGURACION NO ENCONTRADO.\n" +
                "Crea config.properties a partir de config.properties.template\n" +
                "y coloca tus credenciales de Neon.");
    }

    public String get(String clave) {
        String val = props.getProperty(clave);
        if (val == null)
            throw new RuntimeException("[Config] Clave no encontrada: " + clave);
        return val.trim();
    }

    public String get(String clave, String valorPorDefecto) {
        return props.getProperty(clave, valorPorDefecto).trim();
    }

    public int getInt(String clave, int defecto) {
        try {
            return Integer.parseInt(get(clave, String.valueOf(defecto)));
        } catch (NumberFormatException e) {
            return defecto;
        }
    }

    public boolean getBoolean(String clave, boolean defecto) {
        return Boolean.parseBoolean(get(clave, String.valueOf(defecto)));
    }

    public String getMotor()        { return get("db.motor", "postgresql"); }
    public String getHost()         { return get("db.host"); }
    public int    getPuerto()       { return getInt("db.puerto", 5432); }
    public String getNombreDB()     { return get("db.nombre", "neondb"); }
    public String getUsuario()      { return get("db.usuario"); }
    public String getPassword()     { return get("db.password"); }
    public String getZonaHoraria()  { return get("db.zona_horaria", "America/Bogota"); }
    public boolean isSsl()          { return getBoolean("db.ssl", true); }
    public int    getTimeoutConex() { return getInt("db.timeout_conexion", 30); }
    public int    getConexMax()     { return getInt("db.conexiones_max", 10); }

    /** Nombre EXACTO del schema en Neon. Se usa como search_path, nunca hardcodeado en SQL. */
    public String getSchema()       { return get("db.schema", "public"); }

    public String getEmpresaNombre()    { return get("empresa.nombre", "CALICHE MOTOS"); }
    public String getEmpresaNit()       { return get("empresa.nit", ""); }
    public String getEmpresaDireccion() { return get("empresa.direccion", ""); }
    public String getEmpresaTelefono()  { return get("empresa.telefono", ""); }
    public String getEmpresaEmail()     { return get("empresa.email", ""); }
    public String getEmpresaRegimen()   { return get("empresa.regimen", "Regimen Simplificado"); }

    public String getRutaFacturas() { return get("rutas.facturas", "reportes/facturas"); }
    public String getRutaReportes() { return get("rutas.reportes", "reportes"); }

    /**
     * Construye la URL JDBC. El schema se pasa via currentSchema,
     * asi las queries en los DAO NUNCA llevan el nombre del schema escrito a mano.
     */
    public String buildJdbcUrl() {
        String motor = getMotor().toLowerCase();
        return switch (motor) {
            case "postgresql", "postgres" -> String.format(
                    "jdbc:postgresql://%s:%d/%s?sslmode=%s&currentSchema=%s&TimeZone=%s&connectTimeout=%d",
                    getHost(), getPuerto(), getNombreDB(),
                    isSsl() ? "require" : "disable",
                    getSchema(),
                    getZonaHoraria(), getTimeoutConex());
            default -> throw new RuntimeException(
                    "[Config] Motor no soportado: " + motor + ". Use 'postgresql'.");
        };
    }

    public String getDriverClass() {
        return switch (getMotor().toLowerCase()) {
            case "postgresql", "postgres" -> "org.postgresql.Driver";
            default -> throw new RuntimeException("[Config] Driver desconocido para motor: " + getMotor());
        };
    }
}
