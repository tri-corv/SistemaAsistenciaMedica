package LSP;

import java.time.LocalDateTime;

public class Telemedicina extends AsistenciaMedica {
    private final String enlaceVideollamada;

    public Telemedicina(int id, Paciente paciente, Profesional profesional, LocalDateTime fechaHora, String enlaceVideollamada) {
        super(id, paciente, profesional, fechaHora);
        this.enlaceVideollamada = enlaceVideollamada;
    }

    @Override
    public String obtenerTipo() {
        return "Telemedicina";
    }

    @Override
    protected String obtenerIndicacion() {
        return "Conectarse al enlace " + enlaceVideollamada + " para la atencion virtual.";
    }
}
