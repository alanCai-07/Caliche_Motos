package calichemotos.reporte;

import java.io.File;
import java.time.format.DateTimeFormatter;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import calichemotos.dao.CajeroDAO;
import calichemotos.db.ConfiguracionApp;
import calichemotos.modelo.Cajero;
import calichemotos.modelo.Factura;
import calichemotos.modelo.ItemFactura;

public class GeneradorReportePDF {

    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DeviceRgb COLOR_HEADER  = new DeviceRgb(180, 45, 25);   // rojo Caliche Motos
    private static final DeviceRgb COLOR_ROW_PAR = new DeviceRgb(250, 238, 233);
    private static final DeviceRgb COLOR_TOTAL   = new DeviceRgb(255, 225, 210);
    private static final DeviceRgb COLOR_TEXT_H  = new DeviceRgb(255, 255, 255);

    private static void crearDirectorio(String ruta) {
        File dir = new File(ruta).getParentFile();
        if (dir != null && !dir.exists())
            dir.mkdirs();
    }

    /** Genera la factura individual en PDF (A4) y devuelve la ruta del archivo. */
    public static String generarFacturaPDF(Factura factura) throws Exception {
        ConfiguracionApp cfg = ConfiguracionApp.getInstance();
        String rutaBase = cfg.getRutaFacturas(); // por defecto: reportes/facturas
        String ruta = rutaBase + "/" + factura.getNumero() + ".pdf";
        crearDirectorio(ruta);

        PdfWriter writer = new PdfWriter(ruta);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(30, 36, 30, 36);

        String empresaNombre    = cfg.getEmpresaNombre();
        String empresaNit       = "NIT: " + cfg.getEmpresaNit();
        String empresaDireccion = cfg.getEmpresaDireccion();
        String empresaTelefono  = "Tel: " + cfg.getEmpresaTelefono();

        // ---- ENCABEZADO EMPRESA ----
        Table cabecera = new Table(new float[] { 3, 2 })
                .setWidth(UnitValue.createPercentValue(100));

        Cell celdaEmpresa = new Cell()
                .add(new Paragraph(empresaNombre).setFontSize(14).setBold().setFontColor(COLOR_HEADER))
                .add(new Paragraph(empresaNit).setFontSize(9))
                .add(new Paragraph(empresaDireccion).setFontSize(9))
                .add(new Paragraph(empresaTelefono).setFontSize(9))
                .setBorder(Border.NO_BORDER);
        cabecera.addCell(celdaEmpresa);

        Cell celdaFact = new Cell()
                .add(new Paragraph("FACTURA DE VENTA").setFontSize(12).setBold()
                        .setFontColor(COLOR_HEADER).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph("N: " + factura.getNumero()).setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT).setBold())
                .add(new Paragraph("Fecha: " + factura.getFecha().format(FMT_HORA))
                        .setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph("Estado: " + factura.getEstado())
                        .setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER);
        cabecera.addCell(celdaFact);
        doc.add(cabecera);

        doc.add(lineaSeparadora());

        // ---- DATOS CLIENTE / VENDEDOR ----
        Table datosGrid = new Table(new float[] { 1, 1 })
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(6).setMarginBottom(6);

        CajeroDAO cajeroDAO = new CajeroDAO();
        String tecnicoNombre = "Sin tecnico";
        String tecnicoTurno = "-";
        if (factura.getTecnicoAsignado() != null && !factura.getTecnicoAsignado().isBlank()) {
            Cajero tecnico = cajeroDAO.buscar(factura.getTecnicoAsignado());
            if (tecnico != null) {
                tecnicoNombre = tecnico.getNombre();
                tecnicoTurno = tecnico.getTurno();
            }
        }

        datosGrid.addCell(bloqueInfo("DATOS DEL CLIENTE",
                "Nombre : " + factura.getCliente().getNombre(),
                "NIT/CC : " + factura.getCliente().getNit(),
                "Tel    : " + factura.getCliente().getTelefono()));
        datosGrid.addCell(bloqueInfo("ATENDIDO POR",
                "Cajero : " + factura.getCajero().getNombre(),
                "Tecnico: " + tecnicoNombre,
                "Turno  : " + tecnicoTurno,
                "Pago   : " + factura.getMetodoPago()));
        doc.add(datosGrid);

        // ---- TABLA DE ITEMS ----
        Table tabla = new Table(new float[] { 4, 1, 2, 2, 2 })
                .setWidth(UnitValue.createPercentValue(100));

        for (String h : new String[] { "Repuesto", "Cant.", "Precio unit.", "IVA", "Total" })
            tabla.addHeaderCell(celdaHeader(h));

        boolean par = false;
        for (ItemFactura item : factura.getItems()) {
            DeviceRgb bg = par ? COLOR_ROW_PAR : null;
            tabla.addCell(celdaFila(item.getRepuesto().getNombre(), TextAlignment.LEFT, bg));
            tabla.addCell(celdaFila(String.valueOf(item.getCantidad()), TextAlignment.CENTER, bg));
            tabla.addCell(celdaFila(fmt(item.getPrecioUnitario()), TextAlignment.RIGHT, bg));
            tabla.addCell(celdaFila(fmt(item.getImpuesto()), TextAlignment.RIGHT, bg));
            tabla.addCell(celdaFila(fmt(item.getTotal()), TextAlignment.RIGHT, bg));
            par = !par;
        }
        doc.add(tabla);

        // ---- TOTALES ----
        Table totales = new Table(new float[] { 4, 2 })
                .setWidth(UnitValue.createPercentValue(55))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                .setMarginTop(6);

        agregarFilaTotales(totales, "Subtotal:", fmt(factura.calcularSubtotal()), null, false);
        agregarFilaTotales(totales, "IVA:", fmt(factura.calcularIva()), null, false);
        agregarFilaTotales(totales, "TOTAL A PAGAR:", fmt(factura.calcularTotal()), COLOR_TOTAL, true);
        doc.add(totales);

        doc.add(new Paragraph("Gracias por su compra en " + empresaNombre + ".")
                .setFontSize(8).setItalic().setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY).setMarginTop(20));

        doc.close();
        System.out.println("[PDF] Factura generada: " + new File(ruta).getAbsolutePath());
        return ruta;
    }

    // =========================================================
    // REPORTE: Ventas del dia
    // =========================================================
    public static String reporteVentasDiarias(java.time.LocalDate fecha,
            calichemotos.dao.FacturaDAO dao) throws Exception {
        ConfiguracionApp cfg = ConfiguracionApp.getInstance();
        String ruta = cfg.getRutaReportes() + "/ventas_" + fecha + ".pdf";
        crearDirectorio(ruta);

        PdfDocument pdf = new PdfDocument(new PdfWriter(ruta));
        Document doc = new Document(pdf, PageSize.A4.rotate());
        doc.setMargins(30, 30, 30, 30);

        encabezadoReporte(doc, cfg, "REPORTE DE VENTAS DIARIAS",
                "Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        java.util.List<String[]> filas = dao.ventasDia(fecha);
        double totalDia = dao.totalDia(fecha);

        String[] cols = { "N Factura", "Hora", "Cliente", "Atendio", "Total ($)", "Metodo pago" };
        float[] widths = { 2.5f, 1f, 3f, 2.5f, 2f, 2f };
        Table tabla = tablaConEncabezado(cols, widths);

        boolean par = false;
        for (String[] f : filas) {
            DeviceRgb bg = par ? COLOR_ROW_PAR : null;
            tabla.addCell(celdaFila(f[0], TextAlignment.LEFT, bg));
            tabla.addCell(celdaFila(f[1], TextAlignment.CENTER, bg));
            tabla.addCell(celdaFila(f[2], TextAlignment.LEFT, bg));
            tabla.addCell(celdaFila(f[3], TextAlignment.LEFT, bg));
            tabla.addCell(celdaFila(f[4], TextAlignment.RIGHT, bg));
            tabla.addCell(celdaFila(f[5], TextAlignment.CENTER, bg));
            par = !par;
        }
        doc.add(tabla);
        doc.add(new Paragraph(" "));
        resumenBloque(doc,
                "Total de transacciones: " + filas.size(),
                "Total recaudado del dia: " + fmt(totalDia));

        doc.close();
        System.out.println("[PDF] Reporte diario generado: " + ruta);
        return ruta;
    }

    // =========================================================
    // REPORTE: Top repuestos vendidos
    // =========================================================
    public static String reporteTopRepuestos(java.time.LocalDate desde, java.time.LocalDate hasta,
            calichemotos.dao.FacturaDAO dao) throws Exception {
        ConfiguracionApp cfg = ConfiguracionApp.getInstance();
        String ruta = cfg.getRutaReportes() + "/top_repuestos_" + desde + "_" + hasta + ".pdf";
        crearDirectorio(ruta);

        PdfDocument pdf = new PdfDocument(new PdfWriter(ruta));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(36, 36, 36, 36);

        DateTimeFormatter fmtF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        encabezadoReporte(doc, cfg, "REPORTE: REPUESTOS MAS VENDIDOS",
                "Periodo: " + desde.format(fmtF) + " al " + hasta.format(fmtF));

        java.util.List<String[]> filas = dao.topRepuestos(desde, hasta);

        String[] cols = { "#", "Repuesto", "Unidades", "Ingresos netos ($)", "Total c/IVA ($)" };
        float[] widths = { 0.5f, 4f, 1.5f, 2.5f, 2.5f };
        Table tabla = tablaConEncabezado(cols, widths);

        boolean par = false;
        for (String[] f : filas) {
            DeviceRgb bg = par ? COLOR_ROW_PAR : null;
            tabla.addCell(celdaFila(f[0], TextAlignment.CENTER, bg));
            tabla.addCell(celdaFila(f[1], TextAlignment.LEFT, bg));
            tabla.addCell(celdaFila(f[2], TextAlignment.CENTER, bg));
            tabla.addCell(celdaFila(f[3], TextAlignment.RIGHT, bg));
            tabla.addCell(celdaFila(f[4], TextAlignment.RIGHT, bg));
            par = !par;
        }
        doc.add(tabla);

        doc.close();
        System.out.println("[PDF] Reporte top repuestos generado: " + ruta);
        return ruta;
    }

    // =========================================================
    // REPORTE: Ventas por usuario
    // =========================================================
    public static String reporteVentasPorCajero(java.time.LocalDate desde, java.time.LocalDate hasta,
            calichemotos.dao.FacturaDAO dao) throws Exception {
        ConfiguracionApp cfg = ConfiguracionApp.getInstance();
        String ruta = cfg.getRutaReportes() + "/ventas_usuario_" + desde + "_" + hasta + ".pdf";
        crearDirectorio(ruta);

        PdfDocument pdf = new PdfDocument(new PdfWriter(ruta));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(36, 36, 36, 36);

        DateTimeFormatter fmtF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        encabezadoReporte(doc, cfg, "REPORTE: VENTAS POR USUARIO",
                "Periodo: " + desde.format(fmtF) + " al " + hasta.format(fmtF));

        java.util.List<String[]> filas = dao.ventasPorCajero(desde, hasta);

        String[] cols = { "Usuario", "N Facturas", "Total vendido ($)" };
        float[] widths = { 4f, 2f, 3f };
        Table tabla = tablaConEncabezado(cols, widths);

        double granTotal = 0;
        boolean par = false;
        for (String[] f : filas) {
            DeviceRgb bg = par ? COLOR_ROW_PAR : null;
            tabla.addCell(celdaFila(f[0], TextAlignment.LEFT, bg));
            tabla.addCell(celdaFila(f[1], TextAlignment.CENTER, bg));
            tabla.addCell(celdaFila(f[2], TextAlignment.RIGHT, bg));
            granTotal += Double.parseDouble(f[2].replace(",", ""));
            par = !par;
        }
        doc.add(tabla);
        doc.add(new Paragraph(" "));
        resumenBloque(doc, "Usuarios activos: " + filas.size(),
                "Gran total del periodo: " + fmt(granTotal));

        doc.close();
        System.out.println("[PDF] Reporte por usuario generado: " + ruta);
        return ruta;
    }

    // ---- Helpers de reportes tabulares ----

    private static void encabezadoReporte(Document doc, ConfiguracionApp cfg, String titulo, String subtitulo) {
        doc.add(new Paragraph(cfg.getEmpresaNombre()).setFontSize(14).setBold()
                .setFontColor(COLOR_HEADER).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(titulo).setFontSize(13).setBold()
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(subtitulo).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));
        doc.add(lineaSeparadora());
    }

    private static Table tablaConEncabezado(String[] cols, float[] widths) {
        Table t = new Table(widths).setWidth(UnitValue.createPercentValue(100));
        for (String c : cols)
            t.addHeaderCell(celdaHeader(c));
        return t;
    }

    private static void resumenBloque(Document doc, String... lineas) {
        Cell c = new Cell().setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(COLOR_HEADER, 3))
                .setBackgroundColor(COLOR_TOTAL)
                .setPadding(8);
        for (String l : lineas)
            c.add(new Paragraph(l).setFontSize(11).setBold().setFontColor(COLOR_HEADER));
        Table t = new Table(1).setWidth(UnitValue.createPercentValue(50))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
        t.addCell(c);
        doc.add(t);
    }

    // ---- Utilidades privadas ----

    private static String fmt(double v) {
        return String.format("$%,.0f", v);
    }

    private static Table lineaSeparadora() {
        Table linea = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(6).setMarginBottom(6);
        Cell celda = new Cell()
                .setHeight(2)
                .setBackgroundColor(COLOR_HEADER)
                .setBorder(Border.NO_BORDER)
                .setPadding(0);
        linea.addCell(celda);
        return linea;
    }

    private static Cell bloqueInfo(String titulo, String... lineas) {
        Cell c = new Cell().setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(COLOR_HEADER, 2))
                .setPadding(6);
        c.add(new Paragraph(titulo).setFontSize(8).setBold().setFontColor(COLOR_HEADER));
        for (String l : lineas)
            c.add(new Paragraph(l).setFontSize(8).setFontColor(ColorConstants.DARK_GRAY).setMargin(0));
        return c;
    }

    private static Cell celdaHeader(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setFontSize(9).setBold().setFontColor(COLOR_TEXT_H))
                .setBackgroundColor(COLOR_HEADER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(4);
    }

    private static Cell celdaFila(String texto, TextAlignment alin, DeviceRgb bg) {
        Cell c = new Cell()
                .add(new Paragraph(texto == null ? "" : texto).setFontSize(9).setMargin(0))
                .setTextAlignment(alin)
                .setPadding(4)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.3f))
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER);
        if (bg != null)
            c.setBackgroundColor(bg);
        return c;
    }

    private static void agregarFilaTotales(Table t, String etiqueta, String valor, DeviceRgb bg, boolean grande) {
        Paragraph pEtiq = new Paragraph(etiqueta).setFontSize(grande ? 10 : 9).setTextAlignment(TextAlignment.RIGHT);
        Paragraph pVal  = new Paragraph(valor).setFontSize(grande ? 10 : 9).setTextAlignment(TextAlignment.RIGHT);
        if (grande) { pEtiq.setBold(); pVal.setBold(); }

        Cell cEtiq = new Cell().add(pEtiq).setBorder(Border.NO_BORDER);
        Cell cVal  = new Cell().add(pVal).setBorder(Border.NO_BORDER);
        if (bg != null) { cEtiq.setBackgroundColor(bg); cVal.setBackgroundColor(bg); }
        t.addCell(cEtiq);
        t.addCell(cVal);
    }
}
