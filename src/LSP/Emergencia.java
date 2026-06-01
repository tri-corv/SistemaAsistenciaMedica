package LSP;

import java.time.LocalDateTime;

public class Emergencia extends AsistenciaMedica {
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
    protected String obtenerIndicacion() {
        return "Derivar a guardia y priorizar triaje nivel " + nivelUrgencia + ".";
    }
}
