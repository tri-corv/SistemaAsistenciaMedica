package LSP;

import java.time.LocalDateTime;

public class ConsultaGeneral extends AsistenciaMedica {
    private final String consultorio;

    public ConsultaGeneral(int id, Paciente paciente, Profesional profesional, LocalDateTime fechaHora, String consultorio) {
        super(id, paciente, profesional, fechaHora);
        this.consultorio = consultorio;
    }

    @Override
    public String obtenerTipo() {
        return "Consulta general";
    }

    @Override
    protected String obtenerIndicacion() {
        return "Presentarse en consultorio " + consultorio + " para control clinico.";
    }
}
