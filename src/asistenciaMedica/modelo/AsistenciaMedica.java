package asistenciaMedica.modelo;

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
        marcarAtendida();
        System.out.println(resumen());
        System.out.println(detallePago());
        System.out.println("Indicacion: " + obtenerIndicacion());
        System.out.println();
    }

    public final void marcarAtendida() {
        estado = EstadoAsistencia.ATENDIDA;
    }

    public final String resumen() {
        return "[" + id + "] " + obtenerTipo() + " | Paciente: " + paciente.getNombre()
                + " | Profesional: " + profesional.getNombre()
                + " | Fecha: " + fechaHora.format(FORMATO_FECHA)
                + " | Estado: " + estado
                + " | Total: $" + formatearImporte(obtenerValorConsulta())
                + " | Paciente paga: $" + formatearImporte(calcularMontoPaciente())
                + " | Obra social cubre: $" + formatearImporte(calcularMontoObraSocial());
    }

    public final String detallePago() {
        ObraSocial obraSocial = paciente.getObraSocial();
        return "Pago: " + obraSocial.getNombre() + " cubre " + obraSocial.getPorcentajeCobertura()
                + "% ($" + formatearImporte(calcularMontoObraSocial()) + ")"
                + " y el paciente paga $" + formatearImporte(calcularMontoPaciente()) + ".";
    }

    public final double calcularMontoObraSocial() {
        return obtenerValorConsulta() * paciente.getObraSocial().getPorcentajeCobertura() / 100;
    }

    public final double calcularMontoPaciente() {
        return obtenerValorConsulta() - calcularMontoObraSocial();
    }

    private String formatearImporte(double importe) {
        return String.format("%.2f", importe);
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

    public abstract double obtenerValorConsulta();

    protected abstract String obtenerIndicacion();
}
