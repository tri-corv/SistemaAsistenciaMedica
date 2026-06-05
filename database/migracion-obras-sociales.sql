USE asistencia_medica;

CREATE TABLE IF NOT EXISTS obras_sociales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    porcentaje_cobertura DECIMAL(5,2) NOT NULL,
    CONSTRAINT chk_porcentaje_cobertura
        CHECK (porcentaje_cobertura >= 0 AND porcentaje_cobertura <= 100)
);

INSERT IGNORE INTO obras_sociales (nombre, porcentaje_cobertura) VALUES
('OSDE', 70.00),
('Swiss Medical', 60.00),
('Particular', 0.00);

ALTER TABLE pacientes
    ADD COLUMN obra_social_id INT NULL;

UPDATE pacientes p
INNER JOIN obras_sociales os ON os.nombre = p.cobertura
SET p.obra_social_id = os.id
WHERE p.obra_social_id IS NULL;

UPDATE pacientes
SET obra_social_id = (SELECT id FROM obras_sociales WHERE nombre = 'Particular')
WHERE obra_social_id IS NULL;

ALTER TABLE pacientes
    MODIFY obra_social_id INT NOT NULL;

ALTER TABLE pacientes
    ADD CONSTRAINT fk_paciente_obra_social
        FOREIGN KEY (obra_social_id) REFERENCES obras_sociales(id);

ALTER TABLE pacientes
    DROP COLUMN cobertura;
