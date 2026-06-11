package asistenciaMedica.modelo;

public class Profesional {
    private final int id;
    private final String nombre;
    private final String especialidad;
    private final String matricula;

    public Profesional(int id, String nombre, String especialidad, String matricula) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.matricula = matricula;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getMatricula() {
        return matricula;
    }

    @Override
    public String toString() {
        return nombre + " | " + especialidad;
    }
}
