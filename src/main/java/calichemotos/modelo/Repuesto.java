package calichemotos.modelo;

public class Repuesto {

    private String  id;
    private String  nombre;
    private String  referenciaOem;
    private String  marca;
    private String  modeloCompatible;
    private double  precio;
    private String  categoria;
    private double  impuesto;
    private int     stock;
    private boolean activo;
    private String  rutaImagen;

    public Repuesto(String id, String nombre, double precio,
                     String categoria, double impuesto, int stock) {
        this.id        = id;
        this.nombre    = nombre;
        this.precio    = precio;
        this.categoria = categoria;
        this.impuesto  = impuesto;
        this.stock     = stock;
        this.activo    = true;
        this.referenciaOem    = "";
        this.marca             = "";
        this.modeloCompatible  = "";
    }

    public double getPrecioConIva() {
        return precio * (1 + impuesto);
    }

    // ---- Getters ----
    public String  getId()               { return id; }
    public String  getNombre()           { return nombre; }
    public String  getReferenciaOem()    { return referenciaOem; }
    public String  getMarca()            { return marca; }
    public String  getModeloCompatible() { return modeloCompatible; }
    public double  getPrecio()           { return precio; }
    public String  getCategoria()        { return categoria; }
    public double  getImpuesto()         { return impuesto; }
    public int     getStock()            { return stock; }
    public boolean isActivo()            { return activo; }
    public String  getRutaImagen()       { return rutaImagen; }

    // ---- Setters ----
    public void setStock(int stock)                    { this.stock = stock; }
    public void setActivo(boolean a)                   { this.activo = a; }
    public void setPrecio(double p)                     { this.precio = p; }
    public void setRutaImagen(String ruta)              { this.rutaImagen = ruta; }
    public void setReferenciaOem(String r)              { this.referenciaOem = r; }
    public void setMarca(String m)                      { this.marca = m; }
    public void setModeloCompatible(String m)           { this.modeloCompatible = m; }

    @Override
    public String toString() {
        return String.format("[%s] %-30s %s  $%,.0f  (IVA %.0f%%)  Stock: %d",
                id, nombre, marca, precio, impuesto * 100, stock);
    }
}
