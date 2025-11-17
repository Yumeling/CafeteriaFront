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

import co.edu.unbosque.model.InventarioDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("inventarioBean")
@SessionScoped
public class InventarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<InventarioDTO> inventarios = new ArrayList<>();
    private InventarioDTO inventarioSeleccionado = new InventarioDTO();
    private InventarioDTO nuevoInventario = new InventarioDTO();
    private Integer eliminarId;

    private List<AdministradorDTO> administradores = new ArrayList<>();
    private List<IngredienteDTO> ingredientes = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8083/api/inventarios";
    private final String ADMIN_URL = "http://localhost:8083/api/administradores";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter()).create();

    @PostConstruct
    public void init() {
        cargarAdministradores();
        cargarIngredientes();
        cargarInventarios();
    }

    // --- Cargar inventarios ---
    public void cargarInventarios() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();

            if (json.startsWith("[")) {
                InventarioDTO[] arr = gson.fromJson(json, InventarioDTO[].class);
                inventarios = Arrays.asList(arr);
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    inventarios = Arrays.asList(gson.fromJson(je.getAsJsonObject().get("data"), InventarioDTO[].class));
                } else {
                    InventarioDTO single = gson.fromJson(json, InventarioDTO.class);
                    inventarios = new ArrayList<>();
                    inventarios.add(single);
                }
            } else {
                inventarios = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            inventarios = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los inventarios."));
        }
    }

    // --- Cargar administradores ---
    public void cargarAdministradores() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(ADMIN_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();
            if (json.startsWith("[")) {
                AdministradorDTO[] arr = gson.fromJson(json, AdministradorDTO[].class);
                administradores = Arrays.asList(arr);
            } else {
                administradores = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            administradores = new ArrayList<>();
        }
    }

    // --- Cargar ingredientes (para select) ---
    public void cargarIngredientes() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(ING_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();
            if (json.startsWith("[")) {
                IngredienteDTO[] arr = gson.fromJson(json, IngredienteDTO[].class);
                ingredientes = Arrays.asList(arr);
            } else {
                ingredientes = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ingredientes = new ArrayList<>();
        }
    }

    // --- Crear (POST) ---
    public void crearInventario() {
        try {
            // validaciones
            if (nuevoInventario.getCantidad() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad es obligatoria."));
                return;
            }
            // dejar id nulo para que el backend lo genere
            nuevoInventario.setInventarioId(null);

            String json = gson.toJson(nuevoInventario);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Inventario creado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear (código: " + resp.statusCode() + ")."));
            }

            nuevoInventario = new InventarioDTO();
            cargarInventarios();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // --- Preparar para editar ---
    public void prepararEditar(InventarioDTO inv) {
        this.inventarioSeleccionado = inv;
    }

    // --- Actualizar (PUT) ---
    public void actualizarInventario() {
        try {
            if (inventarioSeleccionado == null || inventarioSeleccionado.getInventarioId() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Seleccione un inventario para editar."));
                return;
            }

            String json = gson.toJson(inventarioSeleccionado);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + inventarioSeleccionado.getInventarioId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Inventario actualizado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar (código: " + resp.statusCode() + ")."));
            }

            cargarInventarios();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // --- Preparar eliminar ---
    public void prepararEliminar(Integer id) {
        this.eliminarId = id;
        if (id != null && inventarios != null) {
            for (InventarioDTO it : inventarios) {
                if (id.equals(it.getInventarioId())) {
                    this.inventarioSeleccionado = it;
                    break;
                }
            }
        }
    }

    // --- Eliminar (DELETE físico) ---
    public void eliminarInventario() {
        try {
            if (eliminarId == null) return;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + eliminarId))
                    .DELETE()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Inventario eliminado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar (código: " + resp.statusCode() + ")."));
            }
            eliminarId = null;
            cargarInventarios();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // getters/setters
    public List<InventarioDTO> getInventarios() { return inventarios; }
    public void setInventarios(List<InventarioDTO> inventarios) { this.inventarios = inventarios; }

    public InventarioDTO getInventarioSeleccionado() { return inventarioSeleccionado; }
    public void setInventarioSeleccionado(InventarioDTO inventarioSeleccionado) { this.inventarioSeleccionado = inventarioSeleccionado; }

    public InventarioDTO getNuevoInventario() { return nuevoInventario; }
    public void setNuevoInventario(InventarioDTO nuevoInventario) { this.nuevoInventario = nuevoInventario; }

    public Integer getEliminarId() { return eliminarId; }
    public void setEliminarId(Integer eliminarId) { this.eliminarId = eliminarId; }

    public List<AdministradorDTO> getAdministradores() { return administradores; }
    public List<IngredienteDTO> getIngredientes() { return ingredientes; }

    public String getBASE_URL() { return BASE_URL; }
    public String getADMIN_URL() { return ADMIN_URL; }
    public String getING_URL() { return ING_URL; }
}
