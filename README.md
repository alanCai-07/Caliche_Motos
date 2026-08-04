# Sistema de Facturacion - Caliche Motos

Base inicial migrada desde el proyecto Supermercado, adaptada a
repuestos de moto y conectada a Neon (PostgreSQL).

## Paso 1 - Confirmar/crear el schema en Neon

Verifica que el schema se llame EXACTO `caliche_motos` (en minusculas).
Si en tu Neon quedo como `Calice_Motos`, renombralo o edita
`db.schema` en tu `config.properties` para que coincida caracter por
caracter (Postgres es sensible a mayusculas si el nombre no esta en
minusculas puras).

## Paso 2 - Ejecutar el script SQL

Abre el SQL Editor de Neon, selecciona el schema `caliche_motos` y
ejecuta el contenido de `sql/caliche_motos_postgresql.sql`.

Esto crea: categorias, cajeros, clientes, repuestos, facturas,
items_factura, y datos de prueba (5 repuestos, 1 admin, 1 cliente).

Cajero de prueba: usuario "Administrador", contrasena "admin123".

## Paso 3 - Configurar la conexion

```bash
cp src/main/resources/config.properties.template src/main/resources/config.properties
```

Edita `config.properties` con el host, usuario y password que te
da Neon en el boton "Connect" (arriba a la izquierda del dashboard).

## Paso 4 - Probar la conexion

```bash
mvn compile exec:java -Dexec.mainClass="calichemotos.Main"
```

Si todo esta bien, deberia imprimir "Conexion a Neon exitosa." y
listar los 5 repuestos de prueba.

## Estructura actual

```
calichemotos/
├── db/                  ConfiguracionApp, ConexionDB (SET search_path explicito)
├── modelo/              Repuesto, Cliente, Cajero, Factura, ItemFactura, EstadoFactura
├── dao/                 RepuestoDAO, CajeroDAO, ClienteDAO, FacturaDAO (sin schema hardcodeado)
├── pago/                MetodoPago, PagoEfectivo, PagoTarjeta
├── reporte/             GeneradorReportePDF (factura + 3 reportes de ventas)
├── servicio/            Inventario, SistemaFacturacion (login, crear factura, cobrar, anular)
├── util/                GestorImagenes (guardar/resolver fotos de repuestos)
├── ui/                  AppIcon, UIUtils, LoginFrame, MenuPrincipalFrame,
│                        InventarioFrame (CRUD completo modo admin), ClienteFrame,
│                        NuevaVentaFrame, ReportesFrame, BuscarFacturaFrame
└── Main.java            Lanza LoginFrame (flujo completo de punta a punta)
```

## Como ejecutar

Desde VS Code: abre `Main.java` y usa el boton Run (usa `.vscode/launch.json`).

Usuario de prueba (del script SQL): "Administrador", contrasena "admin123" (rol ADMIN).

Modulos funcionales:
- **Inventario**: en modo administrador permite agregar, editar,
  ajustar stock, activar/desactivar repuestos, y subir una imagen
  (se guarda en `imagenes/repuestos/` con el codigo del repuesto como
  nombre de archivo, y la ruta relativa queda en `ruta_imagen`).
  En modo cajero (rol CAJERO) queda en solo lectura.
- **Nueva Venta**: punto de venta completo, genera PDF de la factura.
- **Clientes**: registrar y buscar clientes por NIT.
- **Reportes**: ventas del dia, top 20 repuestos vendidos, ventas por
  usuario, en PDF.
- **Buscar Factura**: historial completo, filtro, detalle, PDF, anular
  y cambiar estado (solo admin).

## Pendiente (fase 6 - opcional)

- Importador de repuestos desde Excel (equivalente a
  `ImportadorProductosExcel` del supermercado).
- Reportes tambien en Excel (`GeneradorReporteExcel`, Apache POI ya
  esta en el pom.xml).
- Dashboard con graficos (JFreeChart ya esta en el pom.xml).
- Ticket termico 80mm (opcional, si se va a imprimir en impresora POS).
- Mostrar la foto del repuesto en la tabla de Nueva Venta (columna con
  miniatura), igual que en el supermercado.
- Decidir si "modelo_compatible" se queda como texto libre o se separa
  en tabla propia (`repuesto_modelos`).

## Diferencias intencionales respecto al repo Supermercado

1. Las queries SQL de los DAO no llevan el nombre del schema escrito
   a mano (bug detectado en el repo original). El schema se resuelve
   una sola vez via `currentSchema` en la URL JDBC.
2. Se agrego `ruta_imagen` directamente en el script SQL desde el
   inicio (en el repo original faltaba esa columna en el DDL).
3. `Producto` -> `Repuesto`, con `referencia_oem`, `marca` y
   `modelo_compatible` como campos propios del catalogo de motos.
