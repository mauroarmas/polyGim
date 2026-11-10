CREATE TABLE pago (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    pedido_id UUID NOT NULL,
    monto_aplicado NUMERIC(19,2) NOT NULL CHECK (monto_aplicado > 0),
    registrado_por_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, pedido_id) REFERENCES pedido(gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, registrado_por_id) REFERENCES usuario_gimnasio(gimnasio_id, id)
);
CREATE INDEX pago_pedido_historial ON pago (gimnasio_id, pedido_id, created_at, id);
CREATE TABLE anulacion_pago (
    id UUID PRIMARY KEY,
    gimnasio_id UUID NOT NULL REFERENCES gimnasio(id),
    pago_id UUID NOT NULL,
    anulado_por_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (gimnasio_id, id),
    UNIQUE (gimnasio_id, pago_id),
    FOREIGN KEY (gimnasio_id, pago_id) REFERENCES pago(gimnasio_id, id),
    FOREIGN KEY (gimnasio_id, anulado_por_id) REFERENCES usuario_gimnasio(gimnasio_id, id)
);
