-- ================================================================
--  SISTEMA DE FACTURACION - CALICHE MOTOS (Repuestos de moto)
--  Script PostgreSQL / Neon
--  Ejecutar dentro del schema "caliche_motos" (crea las tablas
--  en el schema activo de la sesion, sin prefijo hardcodeado)
-- ================================================================

-- En Neon: seleccionar el schema caliche_motos antes de correr esto,
-- o descomentar la siguiente linea:
-- SET search_path TO caliche_motos;

DROP TABLE IF EXISTS items_factura  CASCADE;
DROP TABLE IF EXISTS facturas       CASCADE;
DROP TABLE IF EXISTS repuestos      CASCADE;
DROP TABLE IF EXISTS clientes       CASCADE;
DROP TABLE IF EXISTS cajeros        CASCADE;
DROP TABLE IF EXISTS categorias     CASCADE;

DROP TYPE IF EXISTS turno_enum      CASCADE;
DROP TYPE IF EXISTS estado_factura  CASCADE;
DROP TYPE IF EXISTS metodo_pago     CASCADE;
DROP TYPE IF EXISTS rol_cajero      CASCADE;

CREATE TYPE turno_enum     AS ENUM ('MAÑANA', 'TARDE', 'NOCHE');
CREATE TYPE estado_factura AS ENUM ('PENDIENTE', 'PAGADA', 'ANULADA');
CREATE TYPE metodo_pago    AS ENUM ('EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'NEQUI', 'DAVIPLATA');
CREATE TYPE rol_cajero     AS ENUM ('ADMIN', 'CAJERO', 'TECNICO');

-- ================================================================
--  CATEGORIAS (motor, frenos, suspension, electrico, etc.)
-- ================================================================
CREATE TABLE categorias (
    id_categoria  SERIAL          PRIMARY KEY,
    nombre        VARCHAR(60)     NOT NULL,
    impuesto      NUMERIC(5,4)    NOT NULL DEFAULT 0.1900
);

-- ================================================================
--  CAJEROS / EMPLEADOS
-- ================================================================
CREATE TABLE cajeros (
    id_cajero       VARCHAR(20)   PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    turno           turno_enum    NOT NULL DEFAULT 'MAÑANA',
    contrasena_hash VARCHAR(64)   NOT NULL,
    rol             rol_cajero    NOT NULL DEFAULT 'CAJERO',
    activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- ================================================================
--  CLIENTES
-- ================================================================
CREATE TABLE clientes (
    nit           VARCHAR(20)     PRIMARY KEY,
    nombre        VARCHAR(100)    NOT NULL,
    telefono      VARCHAR(15)     DEFAULT '',
    email         VARCHAR(100)    DEFAULT '',
    direccion     VARCHAR(200)    DEFAULT '',
    creado_en     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ================================================================
--  REPUESTOS (equivalente a "productos" del supermercado)
-- ================================================================
CREATE TABLE repuestos (
    id_repuesto        VARCHAR(20)   PRIMARY KEY,
    nombre             VARCHAR(120)  NOT NULL,
    referencia_oem     VARCHAR(60)   DEFAULT '',
    marca              VARCHAR(60)   DEFAULT '',
    modelo_compatible  VARCHAR(150)  DEFAULT '',
    precio             NUMERIC(12,2) NOT NULL,
    stock              INTEGER       NOT NULL DEFAULT 0,
    id_categoria       INTEGER       NOT NULL,
    ruta_imagen        VARCHAR(255),
    activo             BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_rep_categoria
        FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ================================================================
--  FACTURAS
-- ================================================================
CREATE TABLE facturas (
    numero_factura  VARCHAR(20)     PRIMARY KEY,
    fecha           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nit_cliente     VARCHAR(20)     NOT NULL,
    id_cajero       VARCHAR(20)     NOT NULL,
    id_tecnico      VARCHAR(20)     NULL,
    estado          estado_factura  NOT NULL DEFAULT 'PENDIENTE',
    subtotal        NUMERIC(12,2)   NOT NULL DEFAULT 0.00,
    iva             NUMERIC(12,2)   NOT NULL DEFAULT 0.00,
    total           NUMERIC(12,2)   NOT NULL DEFAULT 0.00,
    metodo_pago     metodo_pago     DEFAULT 'EFECTIVO',

    CONSTRAINT fk_fact_cliente
        FOREIGN KEY (nit_cliente) REFERENCES clientes(nit)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_fact_cajero
        FOREIGN KEY (id_cajero) REFERENCES cajeros(id_cajero)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_fact_tecnico
        FOREIGN KEY (id_tecnico) REFERENCES cajeros(id_cajero)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ================================================================
--  ITEMS DE FACTURA
-- ================================================================
CREATE TABLE items_factura (
    id_item           SERIAL          PRIMARY KEY,
    numero_factura    VARCHAR(20)     NOT NULL,
    id_repuesto       VARCHAR(20)     NOT NULL,
    cantidad          INTEGER         NOT NULL,
    precio_unitario   NUMERIC(12,2)   NOT NULL,
    subtotal_item     NUMERIC(12,2)   NOT NULL,
    iva_item          NUMERIC(12,2)   NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_item_factura
        FOREIGN KEY (numero_factura) REFERENCES facturas(numero_factura)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_item_repuesto
        FOREIGN KEY (id_repuesto) REFERENCES repuestos(id_repuesto)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ================================================================
--  INDICES
-- ================================================================
CREATE INDEX idx_fact_fecha   ON facturas(fecha);
CREATE INDEX idx_fact_estado  ON facturas(estado);
CREATE INDEX idx_fact_cajero  ON facturas(id_cajero);
CREATE INDEX idx_items_prod   ON items_factura(id_repuesto);
CREATE INDEX idx_items_fact   ON items_factura(numero_factura);
CREATE INDEX idx_rep_categ    ON repuestos(id_categoria);
CREATE INDEX idx_rep_marca    ON repuestos(marca);

-- ================================================================
--  DATOS INICIALES
-- ================================================================
INSERT INTO categorias (nombre, impuesto) VALUES
  ('Motor',        0.1900),
  ('Frenos',       0.1900),
  ('Suspension',   0.1900),
  ('Electrico',    0.1900),
  ('Carroceria',   0.1900),
  ('Aceites y lubricantes', 0.1900),
  ('Llantas',      0.1900),
  ('Accesorios',   0.1900);

INSERT INTO repuestos (id_repuesto, nombre, referencia_oem, marca, modelo_compatible, precio, stock, id_categoria, activo) VALUES
  ('R001', 'Kit de arrastre', 'KA-520', 'AKT',    'AKT NKD 125 / 2020-2024', 185000, 12, 1, TRUE),
  ('R002', 'Pastillas de freno delanteras', 'PF-102', 'Yamaha', 'FZ 2.0 / FZ-S', 45000, 30, 2, TRUE),
  ('R003', 'Amortiguador trasero', 'AT-330', 'Bajaj', 'Pulsar NS 200', 220000, 8, 3, TRUE),
  ('R004', 'Bateria 12V 5Ah', 'BT-05', 'Genérico', 'Universal', 95000, 15, 4, TRUE),
  ('R005', 'Aceite 20W50 x1L', 'AC-2050', 'Motul', 'Universal', 32000, 40, 6, TRUE);

-- Cajero admin (contrasena: admin123)
-- Hash SHA-256 de "admin123": 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO cajeros (id_cajero, nombre, turno, contrasena_hash, rol, activo) VALUES
  ('E001', 'Administrador', 'MAÑANA',
   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
   'ADMIN', TRUE);

-- Cliente consumidor final
INSERT INTO clientes (nit, nombre, telefono, email, direccion) VALUES
  ('222222222', 'Consumidor Final', '', '', '');

-- ================================================================
--  VERIFICACION
-- ================================================================
SELECT 'categorias' AS tabla, COUNT(*) AS registros FROM categorias
UNION ALL SELECT 'repuestos',  COUNT(*) FROM repuestos
UNION ALL SELECT 'clientes',   COUNT(*) FROM clientes
UNION ALL SELECT 'cajeros',    COUNT(*) FROM cajeros;
