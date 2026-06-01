package LSP;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AsistenciaMedica {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int id;
    private final Paciente paciente;
    private final Profesional profesional;
    private final LocalDateTime fechaHora;
    private EstadoAsistencia estado;

    protected AsistenciaMedica(int id, Paciente paciente, Profesional profesional, LocalDateTime fechaHora) {
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente es obligatorio.");
        }
        if (profesional == null) {
            throw new IllegalArgumentException("El profesional es obligatorio.");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha y hora son obligatorias.");
        }

        this.id = id;
        this.paciente = paciente;
        this.profesional = profesional;
        this.fechaHora = fechaHora;
        this.estado = EstadoAsistencia.PENDIENTE;
    }

    public final void atender() {
        estado = EstadoAsistencia.ATENDIDA;
        System.out.println(resumen());
        System.out.println("Indicacion: " + obtenerIndicacion());
        System.out.println();
    }

    public final String resumen() {
        return "[" + id + "] " + obtenerTipo() + " | Paciente: " + paciente.getNombre()
                + " | Profesional: " + profesional.getNombre()
                + " | Fecha: " + fechaHora.format(FORMATO_FECHA)
                + " | Estado: " + estado;
    }

    public final int getId() {
        return id;
    }

    public final Paciente getPaciente() {
        return paciente;
    }

    public final Profesional getProfesional() {
        return profesional;
    }

    public final LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public final EstadoAsistencia getEstado() {
        return estado;
    }

    public abstract String obtenerTipo();

    protected abstract String obtenerIndicacion();
}
