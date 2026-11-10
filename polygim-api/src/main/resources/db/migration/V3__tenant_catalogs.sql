CREATE TABLE canal_llegada (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    nombre VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id)
);
CREATE UNIQUE INDEX canal_llegada_nombre_unico ON canal_llegada (gimnasio_id, lower(trim(nombre)));
CREATE TABLE localidad (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    nombre VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id)
);
CREATE UNIQUE INDEX localidad_nombre_unico ON localidad (gimnasio_id, lower(trim(nombre)));
CREATE TABLE disciplina (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    nombre VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id)
);
CREATE UNIQUE INDEX disciplina_nombre_unico ON disciplina (gimnasio_id, lower(trim(nombre)));
CREATE TABLE membresia (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    disciplina_id UUID NOT NULL,
    nombre VARCHAR(100) NOT NULL CHECK (length(trim(nombre)) > 0),
    precio NUMERIC(19,2) NOT NULL CHECK (precio > 0),
    duracion INTEGER NOT NULL CHECK (duracion > 0),
    unidad_duracion VARCHAR(10) NOT NULL CHECK (unidad_duracion IN ('DIAS','SEMANAS','MESES')),
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL, version BIGINT NOT NULL,
    UNIQUE (gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, disciplina_id) REFERENCES disciplina(gimnasio_id, id)
);
CREATE INDEX membresia_disciplina ON membresia (gimnasio_id, disciplina_id);
