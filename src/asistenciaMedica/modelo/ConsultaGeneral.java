package asistenciaMedica.modelo;

import java.time.LocalDateTime;

public class ConsultaGeneral extends AsistenciaMedica {
    private static final double VALOR_CONSULTA = 12000;

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
    public double obtenerValorConsulta() {
        return VALOR_CONSULTA;
    }

    @Override
    protected String obtenerIndicacion() {
        return "Presentarse en consultorio " + consultorio + " para control clinico.";
    }
}
