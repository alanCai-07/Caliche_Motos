package calichemotos.modelo;

public class ItemFactura {

    private Repuesto repuesto;
    private int      cantidad;
    private double   precioUnitario;

    public ItemFactura(Repuesto repuesto, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        this.repuesto       = repuesto;
        this.cantidad       = cantidad;
        this.precioUnitario = repuesto.getPrecio();
    }

    public double getSubtotal()   { return precioUnitario * cantidad; }
    public double getImpuesto()   { return getSubtotal() * repuesto.getImpuesto(); }
    public double getTotal()      { return getSubtotal() + getImpuesto(); }

    public Repuesto getRepuesto()       { return repuesto; }
    public int      getCantidad()       { return cantidad; }
    public double   getPrecioUnitario() { return precioUnitario; }

    @Override
    public String toString() {
        return String.format("%-30s x%3d  $%,10.0f  IVA: $%,8.0f  Total: $%,10.0f",
                repuesto.getNombre(), cantidad,
                getSubtotal(), getImpuesto(), getTotal());
    }
}
