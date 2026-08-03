package calichemotos.pago;

public class PagoTarjeta implements MetodoPago {

    private String numeroTarjeta;
    private String tipoTarjeta;
    private double monto;
    private boolean aprobado;

    public PagoTarjeta(String ultimosCuatroDigitos, String tipoTarjeta) {
        this.numeroTarjeta = ultimosCuatroDigitos;
        this.tipoTarjeta   = tipoTarjeta.toUpperCase();
    }

    @Override
    public boolean pagar(double monto) {
        this.monto = monto;
        aprobado = numeroTarjeta != null && numeroTarjeta.matches("\\d{4}");
        if (aprobado)
            System.out.printf("[TARJETA] Pago aprobado por $%,.0f con tarjeta ***%s%n", monto, numeroTarjeta);
        else
            System.out.println("[TARJETA] Pago rechazado.");
        return aprobado;
    }

    @Override public String getTipo()  { return "TARJETA_" + tipoTarjeta; }
    @Override public double getMonto() { return monto; }

    @Override
    public void generarRecibo() {
        System.out.printf("[RECIBO TARJETA] Tipo: %s  Tarjeta: ***%s  Monto: $%,.0f  Estado: %s%n",
                tipoTarjeta, numeroTarjeta, monto, aprobado ? "APROBADO" : "RECHAZADO");
    }
}
