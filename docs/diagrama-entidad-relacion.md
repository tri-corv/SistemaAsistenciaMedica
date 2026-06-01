# Diagrama Entidad-Relacion

```mermaid
erDiagram
    PACIENTES ||--o{ ASISTENCIAS_MEDICAS : solicita
    PROFESIONALES ||--o{ ASISTENCIAS_MEDICAS : atiende

    PACIENTES {
        int id PK
        varchar nombre
        varchar dni UK
        varchar cobertura
    }

    PROFESIONALES {
        int id PK
        varchar nombre
        varchar especialidad
        varchar matricula UK
    }

    ASISTENCIAS_MEDICAS {
        int id PK
        int paciente_id FK
        int profesional_id FK
        enum tipo
        datetime fecha_hora
        enum estado
        varchar consultorio
        enum nivel_urgencia
        varchar enlace_videollamada
    }
```
