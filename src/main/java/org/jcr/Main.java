
package org.jcr;

import org.jcr.entidades.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("===== SISTEMA DE GESTIÓN HOSPITALARIA =====\n");

        try {
            Hospital hospital = inicializarHospital();
            List<Medico> medicos = crearMedicos(hospital);
            List<Paciente> pacientes = registrarPacientes(hospital);
            CitaManager citaManager = new CitaManager();
            programarCitas(citaManager, medicos, pacientes, hospital);
            mostrarInformacionCompleta(hospital, citaManager);
            probarPersistencia(citaManager, pacientes, medicos, hospital);
            ejecutarPruebasValidacion(citaManager, medicos, pacientes, hospital);
            mostrarEstadisticasFinales(hospital);

            System.out.println("\n===== SISTEMA EJECUTADO EXITOSAMENTE =====");

        } catch (Exception e) {
            System.err.println("Error en el sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Hospital inicializarHospital() {
        System.out.println("Inicializando hospital y departamentos...");

        Hospital hospital = new Hospital("Hospital Central", "Av. Libertador 1234", "011-4567-8901");

        Departamento cardiologia = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
        Departamento pediatria = new Departamento("Pediatría", EspecialidadMedica.PEDIATRIA);
        Departamento traumatologia = new Departamento("Traumatología", EspecialidadMedica.TRAUMATOLOGIA);

        hospital.agregarDepartamento(cardiologia);
        hospital.agregarDepartamento(pediatria);
        hospital.agregarDepartamento(traumatologia);

        crearSalasPorDepartamento(cardiologia, pediatria, traumatologia);

        System.out.println("Hospital inicializado con " + hospital.getDepartamentos().size() + " departamentos\n");
        return hospital;
    }

    private static void crearSalasPorDepartamento(Departamento cardiologia, Departamento pediatria, Departamento traumatologia) {
        cardiologia.crearSala("CARD-101", "Consultorio");
        cardiologia.crearSala("CARD-102", "Quirófano");

        pediatria.crearSala("PED-201", "Consultorio");

        traumatologia.crearSala("TRAUMA-301", "Emergencias");
    }

    private static List<Medico> crearMedicos(Hospital hospital) {
        System.out.println("Registrando médicos especialistas...");

        List<Medico> medicos = new ArrayList<>();

        Medico cardiologo = new Medico("Carlos", "González", "12345678",
                LocalDate.of(1975, 5, 15), TipoSangre.A_POSITIVO,
                "MP-12345", EspecialidadMedica.CARDIOLOGIA);

        Medico pediatra = new Medico("Ana", "Martínez", "23456789",
                LocalDate.of(1980, 8, 22), TipoSangre.O_NEGATIVO,
                "MP-23456", EspecialidadMedica.PEDIATRIA);

        Medico traumatologo = new Medico("Luis", "Rodríguez", "34567890",
                LocalDate.of(1978, 3, 10), TipoSangre.B_POSITIVO,
                "MP-34567", EspecialidadMedica.TRAUMATOLOGIA);

        for (Departamento dep : hospital.getDepartamentos()) {
            switch (dep.getEspecialidad()) {
                case CARDIOLOGIA:
                    dep.agregarMedico(cardiologo);
                    medicos.add(cardiologo);
                    break;
                case PEDIATRIA:
                    dep.agregarMedico(pediatra);
                    medicos.add(pediatra);
                    break;
                case TRAUMATOLOGIA:
                    dep.agregarMedico(traumatologo);
                    medicos.add(traumatologo);
                    break;
            }
        }

        System.out.println("Registrados " + medicos.size() + " médicos especialistas\n");
        return medicos;
    }

    private static List<Paciente> registrarPacientes(Hospital hospital) {
        System.out.println("Registrando pacientes...");

        List<Paciente> pacientes = new ArrayList<>();

        Paciente pacienteCardiaco = new Paciente("María", "López", "11111111",
                LocalDate.of(1985, 12, 5), TipoSangre.A_POSITIVO,
                "011-1111-1111", "Calle Falsa 123");

        Paciente pacientePediatrico = new Paciente("Pedro", "García", "22222222",
                LocalDate.of(2010, 6, 15), TipoSangre.O_POSITIVO,
                "011-2222-2222", "Av. Siempreviva 456");

        Paciente pacienteTraumatologico = new Paciente("Elena", "Fernández", "33333333",
                LocalDate.of(1992, 9, 28), TipoSangre.AB_NEGATIVO,
                "011-3333-3333", "Belgrano 789");

        hospital.agregarPaciente(pacienteCardiaco);
        hospital.agregarPaciente(pacientePediatrico);
        hospital.agregarPaciente(pacienteTraumatologico);

        pacientes.add(pacienteCardiaco);
        pacientes.add(pacientePediatrico);
        pacientes.add(pacienteTraumatologico);

        configurarHistoriasClinicas(pacienteCardiaco, pacientePediatrico, pacienteTraumatologico);

        System.out.println("Registrados " + pacientes.size() + " pacientes con historias clínicas\n");
        return pacientes;
    }

    private static void configurarHistoriasClinicas(Paciente pacienteCardiaco, Paciente pacientePediatrico, Paciente pacienteTraumatologico) {
        pacienteCardiaco.getHistoriaClinica().agregarDiagnostico("Hipertensión arterial");
        pacienteCardiaco.getHistoriaClinica().agregarTratamiento("Enalapril 10mg");
        pacienteCardiaco.getHistoriaClinica().agregarAlergia("Penicilina");

        pacientePediatrico.getHistoriaClinica().agregarDiagnostico("Control pediátrico rutinario");
        pacientePediatrico.getHistoriaClinica().agregarTratamiento("Vacunas al día");

        pacienteTraumatologico.getHistoriaClinica().agregarDiagnostico("Fractura de muñeca");
        pacienteTraumatologico.getHistoriaClinica().agregarTratamiento("Inmovilización y fisioterapia");
        pacienteTraumatologico.getHistoriaClinica().agregarAlergia("Ibuprofeno");
    }

    private static void programarCitas(CitaManager citaManager, List<Medico> medicos, List<Paciente> pacientes, Hospital hospital) throws CitaException {
        System.out.println("Programando citas médicas...");

        Map<EspecialidadMedica, Sala> salasPorEspecialidad = obtenerSalasPorEspecialidad(hospital);

        LocalDateTime fechaBase = LocalDateTime.now().plusDays(1);

        Cita citaCardiologica = citaManager.programarCita(
                pacientes.get(0),
                obtenerMedicoPorEspecialidad(medicos, EspecialidadMedica.CARDIOLOGIA),
                salasPorEspecialidad.get(EspecialidadMedica.CARDIOLOGIA),
                fechaBase.withHour(10).withMinute(0),
                new BigDecimal("150000.00")
        );
        citaCardiologica.setObservaciones("Paciente con antecedentes de hipertensión");
        citaCardiologica.setEstado(EstadoCita.COMPLETADA);

        Cita citaPediatrica = citaManager.programarCita(
                pacientes.get(1),
                obtenerMedicoPorEspecialidad(medicos, EspecialidadMedica.PEDIATRIA),
                salasPorEspecialidad.get(EspecialidadMedica.PEDIATRIA),
                fechaBase.plusDays(1).withHour(14).withMinute(30),
                new BigDecimal("80000.00")
        );
        citaPediatrica.setObservaciones("Control de rutina - vacunas");
        citaPediatrica.setEstado(EstadoCita.EN_CURSO);

        Cita citaTraumatologica = citaManager.programarCita(
                pacientes.get(2),
                obtenerMedicoPorEspecialidad(medicos, EspecialidadMedica.TRAUMATOLOGIA),
                salasPorEspecialidad.get(EspecialidadMedica.TRAUMATOLOGIA),
                fechaBase.plusDays(2).withHour(9).withMinute(15),
                new BigDecimal("120000.00")
        );
        citaTraumatologica.setObservaciones("Seguimiento post-fractura");

        System.out.println("Programadas 3 citas médicas exitosamente\n");
    }


    private static Map<EspecialidadMedica, Sala> obtenerSalasPorEspecialidad(Hospital hospital) {
        Map<EspecialidadMedica, Sala> salasPorEspecialidad = new HashMap<>();

        for (Departamento dep : hospital.getDepartamentos()) {
            if (!dep.getSalas().isEmpty()) {
                salasPorEspecialidad.put(dep.getEspecialidad(), dep.getSalas().get(0));
            }
        }

        return salasPorEspecialidad;
    }

    private static Medico obtenerMedicoPorEspecialidad(List<Medico> medicos, EspecialidadMedica especialidad) {
        return medicos.stream()
                .filter(medico -> medico.getEspecialidad() == especialidad)
                .findFirst()
                .orElse(null);
    }

    private static void mostrarInformacionCompleta(Hospital hospital, CitaManager citaManager) {
        mostrarInformacionHospital(hospital);
        mostrarDepartamentosYPersonal(hospital);
        mostrarPacientesEHistorias(hospital);
        mostrarCitasProgramadas(hospital, citaManager);
    }

    private static void mostrarInformacionHospital(Hospital hospital) {
        System.out.println("===== INFORMACIÓN DEL HOSPITAL =====");
        System.out.println(hospital);
        System.out.println("Departamentos: " + hospital.getDepartamentos().size());
        System.out.println("Pacientes registrados: " + hospital.getPacientes().size());
        System.out.println();
    }

    private static void mostrarDepartamentosYPersonal(Hospital hospital) {
        System.out.println("===== DEPARTAMENTOS Y PERSONAL =====");
        for (Departamento dep : hospital.getDepartamentos()) {
            System.out.println(dep);

            System.out.println("  Médicos (" + dep.getMedicos().size() + "):");
            for (Medico medico : dep.getMedicos()) {
                System.out.println("    " + medico);
            }

            System.out.println("  Salas (" + dep.getSalas().size() + "):");
            for (Sala sala : dep.getSalas()) {
                System.out.println("    " + sala);
            }
            System.out.println();
        }
    }

    private static void mostrarPacientesEHistorias(Hospital hospital) {
        System.out.println("===== PACIENTES E HISTORIAS CLÍNICAS =====");
        for (Paciente paciente : hospital.getPacientes()) {
            System.out.println(paciente);
            HistoriaClinica historia = paciente.getHistoriaClinica();
            System.out.println("  Historia: " + historia.getNumeroHistoria() + " | Edad: " + paciente.getEdad() + " años");

            if (!historia.getDiagnosticos().isEmpty()) {
                System.out.println("  Diagnósticos: " + historia.getDiagnosticos());
            }
            if (!historia.getTratamientos().isEmpty()) {
                System.out.println("  Tratamientos: " + historia.getTratamientos());
            }
            if (!historia.getAlergias().isEmpty()) {
                System.out.println("  Alergias: " + historia.getAlergias());
            }
            System.out.println();
        }
    }

    private static void mostrarCitasProgramadas(Hospital hospital, CitaManager citaManager) {
        System.out.println("===== CITAS PROGRAMADAS =====");

        // Mostrar citas por paciente
        for (Paciente paciente : hospital.getPacientes()) {
            List<Cita> citasPaciente = citaManager.getCitasPorPaciente(paciente);
            if (!citasPaciente.isEmpty()) {
                System.out.println("Citas de " + paciente.getNombreCompleto() + ":");
                for (Cita cita : citasPaciente) {
                    System.out.println("  " + cita);
                    if (!cita.getObservaciones().isEmpty()) {
                        System.out.println("    Observaciones: " + cita.getObservaciones());
                    }
                }
                System.out.println();
            }
        }
    }


    private static void probarPersistencia(CitaManager citaManager, List<Paciente> pacientes, List<Medico> medicos, Hospital hospital) {
        System.out.println("===== PRUEBA DE PERSISTENCIA =====");

        try {
            String archivo = "citas_hospital.csv";
            citaManager.guardarCitas(archivo);
            System.out.println("✓ Citas guardadas en " + archivo);
            CitaManager nuevoCitaManager = new CitaManager();
            Map<String, Paciente> pacientesMap = crearMapaPacientes(pacientes);
            Map<String, Medico> medicosMap = crearMapaMedicos(medicos);
            Map<String, Sala> salasMap = crearMapaSalas(hospital);
            nuevoCitaManager.cargarCitas(archivo, pacientesMap, medicosMap, salasMap);
            System.out.println("✓ Citas cargadas exitosamente desde archivo");

            int totalCitasCargadas = 0;
            for (Paciente paciente : pacientes) {
                totalCitasCargadas += nuevoCitaManager.getCitasPorPaciente(paciente).size();
            }
            System.out.println("✓ Total de citas cargadas: " + totalCitasCargadas);

        } catch (Exception e) {
            System.err.println("✗ Error en persistencia: " + e.getMessage());
        }

        System.out.println();
    }

    private static Map<String, Paciente> crearMapaPacientes(List<Paciente> pacientes) {
        Map<String, Paciente> mapa = new HashMap<>();
        for (Paciente p : pacientes) {
            mapa.put(p.getDni(), p);
        }
        return mapa;
    }

    private static Map<String, Medico> crearMapaMedicos(List<Medico> medicos) {
        Map<String, Medico> mapa = new HashMap<>();
        for (Medico m : medicos) {
            mapa.put(m.getDni(), m);
        }
        return mapa;
    }

    private static Map<String, Sala> crearMapaSalas(Hospital hospital) {
        Map<String, Sala> mapa = new HashMap<>();
        for (Departamento dep : hospital.getDepartamentos()) {
            for (Sala sala : dep.getSalas()) {
                mapa.put(sala.getNumero(), sala);
            }
        }
        return mapa;
    }


    private static void ejecutarPruebasValidacion(CitaManager citaManager, List<Medico> medicos, List<Paciente> pacientes, Hospital hospital) {
        System.out.println("===== PRUEBAS DE VALIDACIÓN =====");

        Paciente pacientePrueba = pacientes.get(0);
        Medico medicoPrueba = medicos.get(0);
        Sala salaPrueba = hospital.getDepartamentos().get(0).getSalas().get(0);

        probarValidacionFechaPasado(citaManager, pacientePrueba, medicoPrueba, salaPrueba);
        probarValidacionCostoNegativo(citaManager, pacientePrueba, medicoPrueba, salaPrueba);
        probarValidacionEspecialidadIncompatible(citaManager, pacientePrueba, medicos, hospital);

        System.out.println();
    }

    private static void probarValidacionFechaPasado(CitaManager citaManager, Paciente paciente, Medico medico, Sala sala) {
        try {
            citaManager.programarCita(paciente, medico, sala,
                    LocalDateTime.of(2020, 1, 1, 10, 0),
                    new BigDecimal("100000.00"));
            System.out.println("✗ ERROR: Se permitió cita en el pasado");
        } catch (CitaException e) {
            System.out.println("✓ Validación fecha pasado: " + e.getMessage());
        }
    }

    private static void probarValidacionCostoNegativo(CitaManager citaManager, Paciente paciente, Medico medico, Sala sala) {
        try {
            citaManager.programarCita(paciente, medico, sala,
                    LocalDateTime.of(2025, 3, 1, 10, 0),
                    new BigDecimal("-50000.00"));
            System.out.println("✗ ERROR: Se permitió costo negativo");
        } catch (CitaException e) {
            System.out.println("✓ Validación costo negativo: " + e.getMessage());
        }
    }

    private static void probarValidacionEspecialidadIncompatible(CitaManager citaManager, Paciente paciente, List<Medico> medicos, Hospital hospital) {
        try {
            Medico cardiologo = obtenerMedicoPorEspecialidad(medicos, EspecialidadMedica.CARDIOLOGIA);
            Sala salaPediatria = obtenerSalaPorEspecialidad(hospital, EspecialidadMedica.PEDIATRIA);

            citaManager.programarCita(paciente, cardiologo, salaPediatria,
                    LocalDateTime.of(2025, 3, 1, 10, 0),
                    new BigDecimal("100000.00"));
            System.out.println("✗ ERROR: Se permitió especialidad incompatible");
        } catch (CitaException e) {
            System.out.println("✓ Validación especialidad incompatible: " + e.getMessage());
        }
    }

    private static Sala obtenerSalaPorEspecialidad(Hospital hospital, EspecialidadMedica especialidad) {
        return hospital.getDepartamentos().stream()
                .filter(dep -> dep.getEspecialidad() == especialidad)
                .flatMap(dep -> dep.getSalas().stream())
                .findFirst()
                .orElse(null);
    }


    private static void mostrarEstadisticasFinales(Hospital hospital) {
        System.out.println("===== ESTADÍSTICAS FINALES =====");

        int totalDepartamentos = hospital.getDepartamentos().size();
        int totalPacientes = hospital.getPacientes().size();
        int totalMedicos = hospital.getDepartamentos().stream()
                .mapToInt(dep -> dep.getMedicos().size())
                .sum();
        int totalSalas = hospital.getDepartamentos().stream()
                .mapToInt(dep -> dep.getSalas().size())
                .sum();

        System.out.println("Departamentos: " + totalDepartamentos);
        System.out.println("Médicos: " + totalMedicos);
        System.out.println("Salas: " + totalSalas);
        System.out.println("Pacientes: " + totalPacientes);

        mostrarDistribucionTipoSangre(hospital);

        mostrarDistribucionEspecialidades(hospital);
    }

    private static void mostrarDistribucionTipoSangre(Hospital hospital) {
        System.out.println("\nDistribución por tipo de sangre:");
        Map<TipoSangre, Integer> distribucion = new HashMap<>();

        for (Paciente paciente : hospital.getPacientes()) {
            TipoSangre tipo = paciente.getTipoSangre();
            distribucion.put(tipo, distribucion.getOrDefault(tipo, 0) + 1);
        }

        distribucion.entrySet().stream()
                .sorted(Map.Entry.<TipoSangre, Integer>comparingByValue().reversed())
                .forEach(entry -> System.out.println("  " + entry.getKey().getDescripcion() + ": " + entry.getValue()));
    }

    private static void mostrarDistribucionEspecialidades(Hospital hospital) {
        System.out.println("\nDistribución por especialidad:");
        for (Departamento dep : hospital.getDepartamentos()) {
            System.out.println("  " + dep.getEspecialidad().getDescripcion() + ": " +
                    dep.getMedicos().size() + " médicos, " +
                    dep.getSalas().size() + " salas");
        }
    }
}