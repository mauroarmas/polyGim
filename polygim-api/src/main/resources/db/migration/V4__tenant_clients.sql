CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    dni VARCHAR(8) NOT NULL CHECK (dni ~ '^[0-9]{7,8}$'),
    nombre VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    apellido VARCHAR(100) NOT NULL CHECK (length(trim(apellido)) > 0),
    telefono VARCHAR(40),
    canal_llegada_id UUID,
    calle VARCHAR(160),
    altura VARCHAR(20),
    localidad_id UUID,
    fecha_ingreso DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id),
    UNIQUE (gimnasio_id, dni),
    FOREIGN KEY (gimnasio_id, canal_llegada_id) REFERENCES canal_llegada(gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, localidad_id) REFERENCES localidad(gimnasio_id, id)
);
CREATE INDEX cliente_listado ON cliente (gimnasio_id, apellido, id);
