package calichemotos.dao;

import calichemotos.db.ConexionDB;
import calichemotos.modelo.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public void guardar(Cliente c) throws SQLException {
        String sql = """
                INSERT INTO clientes (nit, nombre, telefono, email, direccion)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (nit) DO UPDATE
                  SET nombre=EXCLUDED.nombre, telefono=EXCLUDED.telefono,
                      email=EXCLUDED.email,   direccion=EXCLUDED.direccion
                """;
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, c.getNit());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDireccion());
            ps.executeUpdate();
        }
    }

    public Cliente buscar(String nit) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE nit = ?";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, nit);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapear(rs);
        }
        return null;
    }

    public List<Cliente> buscarPorNombre(String texto) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE LOWER(nombre) LIKE LOWER(?) " +
                "AND nit != '222222222' ORDER BY nombre LIMIT 20";
        try (PreparedStatement ps = ConexionDB.getConexion().prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Cliente> buscarInteligente(String texto) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        if (texto == null || texto.isBlank())
            return lista;

        if (texto.matches("\\d+")) {
            Cliente c = buscar(texto);
            if (c != null)
                lista.add(c);
        } else {
            lista = buscarPorNombre(texto);
        }
        return lista;
    }

    public Cliente consumidorFinal() throws SQLException {
        Cliente c = buscar("222222222");
        if (c == null) {
            c = new Cliente("222222222", "Consumidor Final", "", "", "");
            guardar(c);
        }
        return c;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getString("nit"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("direccion"));
    }
}
