package calichemotos.servicio;

import calichemotos.dao.RepuestoDAO;
import calichemotos.modelo.Repuesto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventario {

    private final RepuestoDAO dao = new RepuestoDAO();
    private Map<String, Repuesto> repuestos = new HashMap<>();

    public void cargarDesdeDB() throws SQLException {
        repuestos.clear();
        for (Repuesto r : dao.listarActivos())
            repuestos.put(r.getId(), r);
        System.out.println("[Inventario] " + repuestos.size() + " repuestos cargados.");
    }

    public Repuesto buscarRepuesto(String id) {
        return repuestos.get(id);
    }

    public List<Repuesto> buscarPorNombreOMarca(String texto) {
        List<Repuesto> resultado = new ArrayList<>();
        String lower = texto.toLowerCase();
        for (Repuesto r : repuestos.values())
            if (r.getNombre().toLowerCase().contains(lower)
                    || r.getMarca().toLowerCase().contains(lower)
                    || r.getModeloCompatible().toLowerCase().contains(lower))
                resultado.add(r);
        return resultado;
    }

    public boolean hayDisponible(String idRepuesto, int cantidad) {
        Repuesto r = repuestos.get(idRepuesto);
        return r != null && r.getStock() >= cantidad;
    }

    public int getStock(String idRepuesto) {
        Repuesto r = repuestos.get(idRepuesto);
        return r == null ? 0 : r.getStock();
    }

    public Map<String, Repuesto> getTodos() {
        return new HashMap<>(repuestos);
    }

    public void descontarStock(String idRepuesto, int cantidad) {
        Repuesto r = repuestos.get(idRepuesto);
        if (r != null)
            r.setStock(r.getStock() - cantidad);
    }
}
