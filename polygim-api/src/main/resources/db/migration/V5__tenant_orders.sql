CREATE TABLE pedido (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    cliente_id UUID NOT NULL,
    membresia_id UUID NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(19,2) NOT NULL CHECK (precio_unitario > 0),
    duracion_membresia INTEGER NOT NULL CHECK (duracion_membresia > 0),
    unidad_duracion VARCHAR(10) NOT NULL CHECK (unidad_duracion IN ('DIAS','SEMANAS','MESES')),
    total NUMERIC(19,2) NOT NULL CHECK (total > 0 AND total = precio_unitario * cantidad),
    fecha_compra DATE NOT NULL,
    fecha_base_vencimiento DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    pedido_anterior_id UUID,
    registrado_por_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (gimnasio_id, id),
    UNIQUE (gimnasio_id, pedido_anterior_id),
    FOREIGN KEY (gimnasio_id, cliente_id) REFERENCES cliente(gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, membresia_id) REFERENCES membresia(gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, pedido_anterior_id) REFERENCES pedido(gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, registrado_por_id) REFERENCES usuario_gimnasio(gimnasio_id, id)
);
CREATE INDEX pedido_cliente_historial ON pedido (gimnasio_id, cliente_id, created_at, id);
