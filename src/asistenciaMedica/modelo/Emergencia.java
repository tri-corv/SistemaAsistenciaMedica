package asistenciaMedica.modelo;

import java.time.LocalDateTime;

public class Emergencia extends AsistenciaMedica {
    private static final double VALOR_CONSULTA = 30000;

    private final NivelUrgencia nivelUrgencia;

    public Emergencia(int id, Paciente paciente, Profesional profesional, LocalDateTime fechaHora, NivelUrgencia nivelUrgencia) {
        super(id, paciente, profesional, fechaHora);
        this.nivelUrgencia = nivelUrgencia;
    }

    @Override
    public String obtenerTipo() {
        return "Emergencia " + nivelUrgencia;
    }

    @Override
    public double obtenerValorConsulta() {
        return VALOR_CONSULTA;
    }

    @Override
    protected String obtenerIndicacion() {
        return "Derivar a guardia y priorizar triaje nivel " + nivelUrgencia + ".";
    }
}
