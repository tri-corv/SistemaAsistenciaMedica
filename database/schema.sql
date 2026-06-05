CREATE DATABASE IF NOT EXISTS asistencia_medica;
USE asistencia_medica;

CREATE TABLE obras_sociales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    porcentaje_cobertura DECIMAL(5,2) NOT NULL,
    CONSTRAINT chk_porcentaje_cobertura
        CHECK (porcentaje_cobertura >= 0 AND porcentaje_cobertura <= 100)
);

CREATE TABLE pacientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    obra_social_id INT NOT NULL,
    CONSTRAINT fk_paciente_obra_social
        FOREIGN KEY (obra_social_id) REFERENCES obras_sociales(id)
);

CREATE TABLE profesionales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especialidad VARCHAR(80) NOT NULL,
    matricula VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE asistencias_medicas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paciente_id INT NOT NULL,
    profesional_id INT NOT NULL,
    tipo ENUM('CONSULTA_GENERAL', 'EMERGENCIA', 'TELEMEDICINA') NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado ENUM('PENDIENTE', 'ATENDIDA') NOT NULL DEFAULT 'PENDIENTE',
    consultorio VARCHAR(20),
    nivel_urgencia ENUM('BAJA', 'MEDIA', 'ALTA'),
    enlace_videollamada VARCHAR(255),
    CONSTRAINT fk_asistencia_paciente
        FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_asistencia_profesional
        FOREIGN KEY (profesional_id) REFERENCES profesionales(id)
);

INSERT INTO obras_sociales (nombre, porcentaje_cobertura) VALUES
('OSDE', 70.00),
('Swiss Medical', 60.00),
('Particular', 0.00);

INSERT INTO pacientes (nombre, dni, obra_social_id) VALUES
('Ana Gomez', '35123456', 1),
('Luis Perez', '28777888', 2),
('Marta Ruiz', '40999111', 3);

INSERT INTO profesionales (nombre, especialidad, matricula) VALUES
('Dra. Laura Medina', 'Clinica medica', 'MP-1020'),
('Dr. Pablo Torres', 'Emergentologia', 'MP-7781'),
('Dra. Sofia Ramos', 'Medicina familiar', 'MP-4403');

INSERT INTO asistencias_medicas
(paciente_id, profesional_id, tipo, fecha_hora, estado, consultorio, nivel_urgencia, enlace_videollamada)
VALUES
(1, 1, 'CONSULTA_GENERAL', '2026-06-03 10:30:00', 'PENDIENTE', 'A12', NULL, NULL),
(2, 2, 'EMERGENCIA', '2026-06-01 14:00:00', 'PENDIENTE', NULL, 'ALTA', NULL),
(3, 3, 'TELEMEDICINA', '2026-06-04 18:15:00', 'PENDIENTE', NULL, NULL, 'https://meet.salud.local/consulta-3');
