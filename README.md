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
├── db/                  ConfiguracionApp, ConexionDB (igual arquitectura que supermercado)
├── modelo/              Repuesto, Cliente, Cajero, Factura, ItemFactura, EstadoFactura
├── dao/                 RepuestoDAO (sin schema hardcodeado - usa currentSchema de la conexion)
├── servicio/            Inventario
└── Main.java            Prueba de conexion
```

## Pendiente (fase 2)

- ClienteDAO, CajeroDAO, FacturaDAO (calcados del supermercado, renombrando producto -> repuesto)
- SistemaFacturacion (servicio Singleton)
- UI Swing: LoginFrame, MenuPrincipalFrame, InventarioFrame, NuevaVentaFrame, ReportesFrame, BuscarFacturaFrame
- GeneradorReportePDF y GeneradorReporteExcel con branding de Caliche Motos
- Decidir si "modelo_compatible" se queda como texto libre o se separa en tabla propia

## Diferencias intencionales respecto al repo Supermercado

1. Las queries SQL de los DAO no llevan el nombre del schema escrito
   a mano (bug detectado en el repo original). El schema se resuelve
   una sola vez via `currentSchema` en la URL JDBC.
2. Se agrego `ruta_imagen` directamente en el script SQL desde el
   inicio (en el repo original faltaba esa columna en el DDL).
3. `Producto` -> `Repuesto`, con `referencia_oem`, `marca` y
   `modelo_compatible` como campos propios del catalogo de motos.
