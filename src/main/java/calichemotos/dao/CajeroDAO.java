package calichemotos.dao;

import calichemotos.db.ConexionDB;
import calichemotos.modelo.Cajero;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CajeroDAO {

    public Cajero buscar(String id) throws SQLException {
        String sql = "SELECT * FROM cajeros WHERE id_cajero = ? AND activo = TRUE";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapear(rs);
        }
        return null;
    }

    public Cajero buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM cajeros WHERE LOWER(nombre) = LOWER(?) AND activo = TRUE";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapear(rs);
        }
        return null;
    }

    public Cajero autenticar(String nombre, String contrasena) throws SQLException {
        Cajero cajero = buscarPorNombre(nombre);
        if (cajero != null && cajero.autenticar(contrasena))
            return cajero;
        return null;
    }

    public List<String> listarNombres() throws SQLException {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM cajeros WHERE activo = TRUE ORDER BY nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                nombres.add(rs.getString("nombre"));
        }
        return nombres;
    }

    public List<Cajero> listarPorRol(String rol) throws SQLException {
        List<Cajero> lista = new ArrayList<>();
        String sql = "SELECT * FROM cajeros WHERE activo = TRUE AND LOWER(rol::text) = LOWER(?) ORDER BY nombre";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, rol);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(mapear(rs));
        }
        return lista;
    }

    public List<String[]> listarTecnicosCombo() throws SQLException {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_cajero, nombre FROM cajeros WHERE activo = TRUE AND LOWER(rol::text) = 'tecnico' ORDER BY nombre";
        try (Statement st = ConexionDB.getConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                lista.add(new String[] { rs.getString("id_cajero"), rs.getString("nombre") });
        }
        return lista;
    }

    public void guardar(Cajero c) throws SQLException {
        String sql = """
                INSERT INTO cajeros (id_cajero, nombre, turno, contrasena_hash, rol, activo)
                VALUES (?, ?, ?::turno_enum, ?, ?::rol_cajero, ?)
                ON CONFLICT (id_cajero) DO UPDATE
                  SET nombre = EXCLUDED.nombre,
                      turno  = EXCLUDED.turno,
                      contrasena_hash = EXCLUDED.contrasena_hash,
                      rol = EXCLUDED.rol,
                      activo = EXCLUDED.activo
                """;
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getId());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTurno());
            ps.setString(4, c.getContrasenaHash());
            ps.setString(5, c.getRol());
            ps.setBoolean(6, c.isActivo());
            ps.executeUpdate();
        }
    }

    private Cajero mapear(ResultSet rs) throws SQLException {
        return new Cajero(
                rs.getString("id_cajero"),
                rs.getString("nombre"),
                rs.getString("turno"),
                rs.getString("contrasena_hash"),
                rs.getString("rol"));
    }
}
