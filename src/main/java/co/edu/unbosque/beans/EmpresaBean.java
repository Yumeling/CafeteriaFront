package co.edu.unbosque.beans;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import co.edu.unbosque.model.EmpresaDTO;
import co.edu.unbosque.model.DireccionEmpresaDTO;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("empresaBean")
@SessionScoped
public class EmpresaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<EmpresaDTO> empresas = new ArrayList<>();
    private EmpresaDTO empresaSeleccionada = new EmpresaDTO();
    private EmpresaDTO nuevaEmpresa = new EmpresaDTO();

    // para direcciones
    private List<DireccionEmpresaDTO> direccionesEmpresaSeleccionada = new ArrayList<>();
    private DireccionEmpresaDTO nuevaDireccionParaEmpresa = new DireccionEmpresaDTO();
    private Integer eliminarNit;
    private Integer mostrarDireccionesNit; // nit para el dialog de mostrar direcciones

    private final String BASE_URL = "http://localhost:8083/api/empresas";
    private final String DIR_BY_EMPRESA = "http://localhost:8083/api/direcciones/empresa";
    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter()).create();

    @PostConstruct
    public void init() {
        cargarEmpresas();
    }

    public void cargarEmpresas() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();

            if (json.startsWith("[")) {
                EmpresaDTO[] arr = gson.fromJson(json, EmpresaDTO[].class);
                empresas = Arrays.asList(arr);
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    empresas = Arrays.asList(gson.fromJson(je.getAsJsonObject().get("data"), EmpresaDTO[].class));
                } else {
                    EmpresaDTO single = gson.fromJson(json, EmpresaDTO.class);
                    empresas = new ArrayList<>();
                    empresas.add(single);
                }
            } else {
                empresas = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            empresas = new ArrayList<>();
            FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las empresas."));
        }
    }

    public String seleccionarParaEditar(EmpresaDTO e) {
        this.empresaSeleccionada = e;
        return null;
    }

    // ---------- crear ----------
    public void crearEmpresa() {
        try {
            // force estado activo and no direccionId on create (as requested)
            nuevaEmpresa.setEstado("activo");
            nuevaEmpresa.setDireccionId(null);

            String json = gson.toJson(nuevaEmpresa);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empresa creada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la empresa. Código: " + resp.statusCode()));
            }
            nuevaEmpresa = new EmpresaDTO();
            cargarEmpresas();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // ---------- actualizar ----------
    public void actualizarEmpresa() {
        try {
            if (empresaSeleccionada == null || empresaSeleccionada.getNit() == null) return;

            // According to request: do not send direccionId in update (keep it null)
            empresaSeleccionada.setDireccionId(null);

            String json = gson.toJson(empresaSeleccionada);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + empresaSeleccionada.getNit()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empresa actualizada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar. Código: " + resp.statusCode()));
            }
            cargarEmpresas();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // ---------- preparar mostrar direcciones ----------
    public void prepararMostrarDirecciones(Integer nit) {
        this.mostrarDireccionesNit = nit;
        cargarDireccionesPorEmpresa(nit);
    }

    public void cargarDireccionesPorEmpresa(Integer nit) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(DIR_BY_EMPRESA + "/" + nit))
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body() == null ? "[]" : resp.body().trim();
            if (body.startsWith("[")) {
                DireccionEmpresaDTO[] arr = gson.fromJson(body, DireccionEmpresaDTO[].class);
                direccionesEmpresaSeleccionada = Arrays.asList(arr);
            } else {
                direccionesEmpresaSeleccionada = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            direccionesEmpresaSeleccionada = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las direcciones de la empresa."));
        }
    }

    // ---------- preparar crear direccion para empresa (abre dialog en UI) ----------
    public void prepararCrearDireccionParaEmpresa(Integer nit) {
        nuevaDireccionParaEmpresa = new DireccionEmpresaDTO();
        nuevaDireccionParaEmpresa.setEmpresaNit(nit);
        // el show del dialog lo hace el oncomplete en el xhtml
    }

    // ---------- crear direccion para empresa ----------
    public void crearDireccionParaEmpresa() {
        try {
            String json = gson.toJson(nuevaDireccionParaEmpresa);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8083/api/direcciones"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Dirección creada y asociada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la dirección. Código: " + resp.statusCode()));
            }
            nuevaDireccionParaEmpresa = new DireccionEmpresaDTO();
            // refrescar la vista de empresa y direcciones si corresponde
            cargarEmpresas();
            if (mostrarDireccionesNit != null) cargarDireccionesPorEmpresa(mostrarDireccionesNit);
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // ---------- marcar inactivo (no borrar) ----------
    public void prepararEliminar(Integer nit) {
        this.eliminarNit = nit;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Preparado", "Listo para poner inactivo NIT: " + nit));
    }

    public void eliminarEmpresa() {
        try {
            if (eliminarNit == null) return;
            // hacemos PUT parcial cambiando estado -> inactivo
            EmpresaDTO dto = new EmpresaDTO();
            dto.setEstado("inactivo");
            String json = gson.toJson(dto);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + eliminarNit))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Hecho", "Empresa marcada como inactiva"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo marcar inactiva. Código: " + resp.statusCode()));
            }
            cargarEmpresas();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // getters & setters

    public List<EmpresaDTO> getEmpresas() { return empresas; }
    public void setEmpresas(List<EmpresaDTO> empresas) { this.empresas = empresas; }

    public EmpresaDTO getEmpresaSeleccionada() { return empresaSeleccionada; }
    public void setEmpresaSeleccionada(EmpresaDTO empresaSeleccionada) { this.empresaSeleccionada = empresaSeleccionada; }

    public EmpresaDTO getNuevaEmpresa() { return nuevaEmpresa; }
    public void setNuevaEmpresa(EmpresaDTO nuevaEmpresa) { this.nuevaEmpresa = nuevaEmpresa; }

    public List<DireccionEmpresaDTO> getDireccionesEmpresaSeleccionada() { return direccionesEmpresaSeleccionada; }
    public void setDireccionesEmpresaSeleccionada(List<DireccionEmpresaDTO> direccionesEmpresaSeleccionada) { this.direccionesEmpresaSeleccionada = direccionesEmpresaSeleccionada; }

    public DireccionEmpresaDTO getNuevaDireccionParaEmpresa() { return nuevaDireccionParaEmpresa; }
    public void setNuevaDireccionParaEmpresa(DireccionEmpresaDTO nuevaDireccionParaEmpresa) { this.nuevaDireccionParaEmpresa = nuevaDireccionParaEmpresa; }

    public Integer getEliminarNit() { return eliminarNit; }
    public void setEliminarNit(Integer eliminarNit) { this.eliminarNit = eliminarNit; }

    public Integer getMostrarDireccionesNit() { return mostrarDireccionesNit; }
    public void setMostrarDireccionesNit(Integer mostrarDireccionesNit) { this.mostrarDireccionesNit = mostrarDireccionesNit; }
}
