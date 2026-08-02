package calichemotos.pago;

public interface MetodoPago {
    boolean pagar(double monto);
    String  getTipo();
    double  getMonto();
    void    generarRecibo();
}
