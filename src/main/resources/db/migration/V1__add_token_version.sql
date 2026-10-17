-- Migración V1: Invalidación de sesiones (jti + ver)
-- Se ejecuta si spring.jpa.hibernate.ddl-auto != update (o en entornos con Flyway/Liquibase).
-- Con ddl-auto=update (valor por defecto del proyecto) Hibernate crea estas columnas automáticamente.

ALTER TABLE usuarios
    ADD COLUMN token_version INT NOT NULL DEFAULT 0;

ALTER TABLE usuarios
    ADD COLUMN last_logout_at DATETIME NULL;

-- Índice opcional para consultas de auditoría por última fecha de logout
CREATE INDEX idx_usuarios_last_logout_at ON usuarios (last_logout_at);
