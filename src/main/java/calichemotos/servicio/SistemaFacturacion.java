package calichemotos.servicio;

import calichemotos.dao.CajeroDAO;
import calichemotos.dao.ClienteDAO;
import calichemotos.dao.FacturaDAO;
import calichemotos.modelo.*;
import calichemotos.pago.MetodoPago;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SistemaFacturacion {

    private static SistemaFacturacion instancia;

    private Inventario  inventario;
    private Cajero      cajeroActivo;
    private FacturaDAO  facturaDAO;
    private ClienteDAO  clienteDAO;
    private CajeroDAO   cajeroDAO;

    private List<Factura> facturasSesion = new ArrayList<>();

    private SistemaFacturacion() {
        inventario  = new Inventario();
        facturaDAO  = new FacturaDAO();
        clienteDAO  = new ClienteDAO();
        cajeroDAO   = new CajeroDAO();
    }

    public static SistemaFacturacion getInstance() {
        if (instancia == null) instancia = new SistemaFacturacion();
        return instancia;
    }

    public boolean login(String nombreCajero, String contrasena) throws SQLException {
        cajeroActivo = cajeroDAO.autenticar(nombreCajero, contrasena);
        if (cajeroActivo != null) {
            System.out.println("[SISTEMA] Sesion iniciada: " + cajeroActivo.getNombre());
            inventario.cargarDesdeDB();
            return true;
        }
        System.out.println("[SISTEMA] Credenciales incorrectas.");
        return false;
    }

    public void logout() {
        System.out.println("[SISTEMA] Sesion cerrada: " +
                (cajeroActivo != null ? cajeroActivo.getNombre() : ""));
        cajeroActivo = null;
    }

    // ---- Crear nueva factura ----
    public Factura crearFactura(Cliente cliente) throws SQLException {
        if (cajeroActivo == null) throw new IllegalStateException("No hay usuario activo.");
        String numero = facturaDAO.siguienteNumero();
        Factura f = new Factura(numero, cliente, cajeroActivo);
        facturasSesion.add(f);
        return f;
    }

    // ---- Completar pago y guardar en BD ----
    public boolean procesarPago(Factura factura, MetodoPago metodo) throws SQLException {
        if (metodo.pagar(factura.calcularTotal())) {
            factura.marcarPagada();
            factura.setMetodoPago(metodo.getTipo());
            // facturaDAO.guardar ya descuenta el stock en BD (UPDATE stock = stock - cantidad)
            // NO llamar a inventario.descontarStock() aqui para evitar doble descuento
            facturaDAO.guardar(factura);
            inventario.cargarDesdeDB();
            return true;
        }
        return false;
    }

    // ---- Anular factura ----
    public void anularFactura(String numero) throws SQLException {
        facturaDAO.actualizarEstado(numero, EstadoFactura.ANULADA);
        facturasSesion.stream()
                .filter(f -> f.getNumero().equals(numero))
                .findFirst()
                .ifPresent(Factura::anular);
        System.out.println("[SISTEMA] Factura " + numero + " anulada.");
    }

    public Optional<Factura> buscarFactura(String numero) throws SQLException {
        return facturaDAO.buscarPorNumero(numero, clienteDAO, cajeroDAO);
    }

    public Inventario  getInventario()   { return inventario; }
    public Cajero      getCajeroActivo() { return cajeroActivo; }
    public FacturaDAO  getFacturaDAO()   { return facturaDAO; }
    public ClienteDAO  getClienteDAO()   { return clienteDAO; }
    public CajeroDAO   getCajeroDAO()    { return cajeroDAO; }
}
