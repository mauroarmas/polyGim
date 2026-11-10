CREATE TABLE gimnasio (
    id UUID PRIMARY KEY CHECK (id <> '00000000-0000-0000-0000-000000000000'::uuid),
    nombre VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    logo_url VARCHAR(2048) NOT NULL,
    color_primario CHAR(7) NOT NULL CHECK (color_primario ~ '^#[0-9A-Fa-f]{6}$'),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);
CREATE TABLE cuenta_acceso (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('SUPERADMIN', 'ADMIN', 'RECEPCION')),
    habilitada BOOLEAN NOT NULL,
    gimnasio_id UUID REFERENCES gimnasio(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id),
    CHECK ((rol = 'SUPERADMIN' AND gimnasio_id IS NULL) OR
           (rol IN ('ADMIN', 'RECEPCION') AND gimnasio_id IS NOT NULL))
);
CREATE TABLE usuario_gimnasio (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, id) REFERENCES cuenta_acceso(gimnasio_id, id)
);
CREATE TABLE configuracion_gimnasio (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL UNIQUE REFERENCES gimnasio(id),
    cantidad_maxima_compra INTEGER NOT NULL CHECK (cantidad_maxima_compra > 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id)
);
