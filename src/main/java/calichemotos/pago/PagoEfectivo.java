package calichemotos.pago;

public class PagoEfectivo implements MetodoPago {

    private double montoRecibido;
    private double cambio;

    public PagoEfectivo(double montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    @Override
    public boolean pagar(double monto) {
        if (montoRecibido >= monto) {
            cambio = montoRecibido - monto;
            return true;
        }
        System.out.printf("[PAGO] Monto insuficiente. Falta: $%,.0f%n", monto - montoRecibido);
        return false;
    }

    @Override public String getTipo()  { return "EFECTIVO"; }
    @Override public double getMonto() { return montoRecibido; }
    public    double getCambio()       { return cambio; }

    @Override
    public void generarRecibo() {
        System.out.printf("[RECIBO EFECTIVO] Recibido: $%,.0f  Cambio: $%,.0f%n", montoRecibido, cambio);
    }
}
