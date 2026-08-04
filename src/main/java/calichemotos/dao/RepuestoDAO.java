package calichemotos.dao;

import calichemotos.db.ConexionDB;
import calichemotos.modelo.Repuesto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Nota: las queries NO llevan el nombre del schema escrito a mano
 * (a diferencia del repo original del supermercado). El schema se
 * resuelve una sola vez via "currentSchema" en la URL JDBC
 * (ver ConfiguracionApp.buildJdbcUrl). Esto evita tener que editar
 * decenas de queries si el schema cambia de nombre.
 */
public class RepuestoDAO {

    public List<Repuesto> listarActivos() throws SQLException {
        List<Repuesto> lista = new ArrayList<>();
        String sql = """
                SELECT r.id_repuesto, r.nombre, r.referencia_oem, r.marca,
                       r.modelo_compatible, r.precio, r.stock, r.ruta_imagen,
                       c.nombre AS categoria, c.impuesto
                FROM repuestos r
                JOIN categorias c ON r.id_categoria = c.id_categoria
                WHERE r.activo = TRUE
                ORDER BY r.nombre
                """;
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                lista.add(mapear(rs));
        }
        return lista;
    }

    public Repuesto buscar(String id) throws SQLException {
        String sql = """
                SELECT r.id_repuesto, r.nombre, r.referencia_oem, r.marca,
                       r.modelo_compatible, r.precio, r.stock, r.ruta_imagen,
                       c.nombre AS categoria, c.impuesto
                FROM repuestos r
                JOIN categorias c ON r.id_categoria = c.id_categoria
                WHERE r.id_repuesto = ?
                """;
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapear(rs);
        }
        return null;
    }

    public List<Repuesto> buscarPorNombreOMarca(String texto) throws SQLException {
        List<Repuesto> lista = new ArrayList<>();
        String sql = """
                SELECT r.id_repuesto, r.nombre, r.referencia_oem, r.marca,
                       r.modelo_compatible, r.precio, r.stock, r.ruta_imagen,
                       c.nombre AS categoria, c.impuesto
                FROM repuestos r
                JOIN categorias c ON r.id_categoria = c.id_categoria
                WHERE r.activo = TRUE
                  AND (LOWER(r.nombre) LIKE LOWER(?)
                       OR LOWER(r.marca) LIKE LOWER(?)
                       OR LOWER(r.referencia_oem) LIKE LOWER(?)
                       OR LOWER(r.modelo_compatible) LIKE LOWER(?))
                ORDER BY r.nombre
                """;
        String like = "%" + texto + "%";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(mapear(rs));
        }
        return lista;
    }

    public void insertar(Repuesto r, int idCategoria) throws SQLException {
        String sql = """
                INSERT INTO repuestos
                  (id_repuesto, nombre, referencia_oem, marca, modelo_compatible,
                   precio, stock, id_categoria, ruta_imagen, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
                """;
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getNombre());
            ps.setString(3, r.getReferenciaOem());
            ps.setString(4, r.getMarca());
            ps.setString(5, r.getModeloCompatible());
            ps.setDouble(6, r.getPrecio());
            ps.setInt(7, r.getStock());
            ps.setInt(8, idCategoria);
            ps.setString(9, r.getRutaImagen());
            ps.executeUpdate();
        }
    }

    public void actualizar(Repuesto r, int idCategoria) throws SQLException {
        String sql = """
                UPDATE repuestos
                   SET nombre = ?, referencia_oem = ?, marca = ?, modelo_compatible = ?,
                       precio = ?, stock = ?, id_categoria = ?, ruta_imagen = ?
                 WHERE id_repuesto = ?
                """;
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, r.getNombre());
            ps.setString(2, r.getReferenciaOem());
            ps.setString(3, r.getMarca());
            ps.setString(4, r.getModeloCompatible());
            ps.setDouble(5, r.getPrecio());
            ps.setInt(6, r.getStock());
            ps.setInt(7, idCategoria);
            ps.setString(8, r.getRutaImagen());
            ps.setString(9, r.getId());
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(String id, boolean activo) throws SQLException {
        String sql = "UPDATE repuestos SET activo = ? WHERE id_repuesto = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    public boolean existe(String id) throws SQLException {
        String sql = "SELECT 1 FROM repuestos WHERE id_repuesto = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /** Devuelve id_categoria -> nombre, para poblar combos en la UI. */
    public java.util.Map<Integer, String> listarCategorias() throws SQLException {
        java.util.Map<Integer, String> cats = new java.util.LinkedHashMap<>();
        String sql = "SELECT id_categoria, nombre FROM categorias ORDER BY nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                cats.put(rs.getInt("id_categoria"), rs.getString("nombre"));
        }
        return cats;
    }

    public void insertarCategoria(String nombre, double impuesto) throws SQLException {
        String sql = "INSERT INTO categorias (nombre, impuesto) VALUES (?, ?)";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setDouble(2, impuesto);
            ps.executeUpdate();
        }
    }

    public void actualizarStock(String id, int nuevoStock) throws SQLException {
        String sql = "UPDATE repuestos SET stock = ? WHERE id_repuesto = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    public void descontarStock(String id, int cantidad) throws SQLException {
        String sql = "UPDATE repuestos SET stock = stock - ? WHERE id_repuesto = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    private Repuesto mapear(ResultSet rs) throws SQLException {
        Repuesto r = new Repuesto(
                rs.getString("id_repuesto"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getString("categoria"),
                rs.getDouble("impuesto"),
                rs.getInt("stock"));
        r.setReferenciaOem(rs.getString("referencia_oem"));
        r.setMarca(rs.getString("marca"));
        r.setModeloCompatible(rs.getString("modelo_compatible"));
        r.setRutaImagen(rs.getString("ruta_imagen"));
        return r;
    }
}
