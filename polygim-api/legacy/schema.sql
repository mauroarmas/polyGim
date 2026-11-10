-- schema.sql — Esquema LEGACY de polyGim (MariaDB/MySQL)
-- Reconstruido a partir del modelo de MySQL Workbench (original/src/documentacion/DiseñoDeSistema/POLLYGYM.mwb).
-- Es solo REFERENCIA de dominio: la versión nueva NO lo usa tal cual (ver legacy/DOMINIO.md).
-- Problemas conocidos de este esquema: dinero en DOUBLE, DNI como INT, estado como TINYINT,
-- duración de la membresía implícita en el nombre, sin multi-tenancy, sin timestamps de auditoría.

CREATE SCHEMA IF NOT EXISTS `POLLYGYM` DEFAULT CHARACTER SET utf8mb4;
USE `POLLYGYM`;

CREATE TABLE `Localidad` (
  `idLocalidad` INT NOT NULL AUTO_INCREMENT,
  `localidad`   VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idLocalidad`)
);

CREATE TABLE `Direccion` (
  `idDireccion` INT NOT NULL AUTO_INCREMENT,
  `calle`       VARCHAR(45) NOT NULL,
  `altura`      INT NULL,
  `idLocalidad` INT NOT NULL,
  PRIMARY KEY (`idDireccion`),
  CONSTRAINT `fk_Direccion_Localidad` FOREIGN KEY (`idLocalidad`) REFERENCES `Localidad` (`idLocalidad`)
);

CREATE TABLE `Cliente` (
  `dni`           INT NOT NULL,
  `apellido`      VARCHAR(45) NOT NULL,
  `nombre`        VARCHAR(45) NOT NULL,
  `telefono`      VARCHAR(45) NOT NULL,
  `fecha_ingreso` DATE NOT NULL,
  `como_llego`    VARCHAR(45) NOT NULL,
  `idDireccion`   INT NOT NULL,
  PRIMARY KEY (`dni`),
  CONSTRAINT `fk_Cliente_Direccion1` FOREIGN KEY (`idDireccion`) REFERENCES `Direccion` (`idDireccion`)
);

CREATE TABLE `Servicio` (
  `idServicio` INT NOT NULL AUTO_INCREMENT,
  `servicio`   VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idServicio`)
);

CREATE TABLE `Membresia` (
  `idMembresia` INT NOT NULL AUTO_INCREMENT,
  `membresia`   VARCHAR(45) NOT NULL,   -- el nombre ("Mensual", otro) determina la unidad de duración
  `precio`      DOUBLE NOT NULL,
  `idServicio`  INT NOT NULL,
  PRIMARY KEY (`idMembresia`),
  CONSTRAINT `fk_Membresia_Servicio1` FOREIGN KEY (`idServicio`) REFERENCES `Servicio` (`idServicio`)
);

CREATE TABLE `Pedido` (
  `idPedido`          INT NOT NULL AUTO_INCREMENT,
  `fecha_pedido`      DATE NOT NULL,
  `estado`            TINYINT NOT NULL,  -- 0 = IMPAGO, 1 = PAGADO
  `fecha_vencimiento` DATE NULL,
  `clienteDni`        INT NOT NULL,
  `total`             DOUBLE NULL,
  PRIMARY KEY (`idPedido`),
  CONSTRAINT `fk_Pedido_Cliente1` FOREIGN KEY (`clienteDni`) REFERENCES `Cliente` (`dni`)
);

CREATE TABLE `Detalle` (
  `idDetalle`   INT NOT NULL AUTO_INCREMENT,
  `idPedido`    INT NOT NULL,
  `cantidad`    INT NOT NULL,
  `idMembresia` INT NOT NULL,
  `subtotal`    DOUBLE NOT NULL,         -- cantidad * precio de la membresía
  PRIMARY KEY (`idDetalle`),
  CONSTRAINT `fk_Detalle_Pedido1`   FOREIGN KEY (`idPedido`)    REFERENCES `Pedido` (`idPedido`),
  CONSTRAINT `fk_Detalle_Servicio1` FOREIGN KEY (`idMembresia`) REFERENCES `Membresia` (`idMembresia`)
);

CREATE TABLE `Pago` (
  `idPago`    INT NOT NULL AUTO_INCREMENT,
  `montoPago` DOUBLE NOT NULL,
  `idPedido`  INT NOT NULL,
  PRIMARY KEY (`idPago`),
  CONSTRAINT `fk_Pago_Pedido1` FOREIGN KEY (`idPedido`) REFERENCES `Pedido` (`idPedido`)
);
