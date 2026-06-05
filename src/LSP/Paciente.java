package LSP;

public class Paciente {
    private final int id;
    private final String nombre;
    private final String dni;
    private final ObraSocial obraSocial;

    public Paciente(int id, String nombre, String dni, ObraSocial obraSocial) {
        this.id = id;
        this.nombre = nombre;
        this.dni = dni;
        this.obraSocial = obraSocial;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public ObraSocial getObraSocial() {
        return obraSocial;
    }
}
