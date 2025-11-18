package co.edu.unbosque.beans;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("ingredienteBean")
@SessionScoped
public class IngredienteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<IngredienteDTO> ingredientes = new ArrayList<>();
    private IngredienteDTO ingredienteSeleccionado = new IngredienteDTO();
    private IngredienteDTO nuevoIngrediente = new IngredienteDTO();
    private Integer eliminarCodigo;

    private final String BASE_URL = "http://localhost:8083/api/ingredientes";
    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

    @PostConstruct
    public void init() {
        cargarIngredientes();
        prepararNuevo();
    }

    // inicializa DTO para el diálogo de creación (NO pide cantidad ni costo)
    public void prepararNuevo() {
        nuevoIngrediente = new IngredienteDTO();
        // NO seteamos cantidad aquí para que permanezca null si no se quiere enviar
    }

    public void cargarIngredientes() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();

            if (json.startsWith("[")) {
                IngredienteDTO[] arr = gson.fromJson(json, IngredienteDTO[].class);
                ingredientes = Arrays.asList(arr);
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    ingredientes = Arrays.asList(gson.fromJson(je.getAsJsonObject().get("data"), IngredienteDTO[].class));
                } else {
                    IngredienteDTO single = gson.fromJson(json, IngredienteDTO.class);
                    ingredientes = new ArrayList<>();
                    ingredientes.add(single);
                }
            } else {
                ingredientes = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ingredientes = new ArrayList<>();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los ingredientes."));
        }
    }

    public void crearIngrediente() {
        try {
            if (nuevoIngrediente.getNombre() == null || nuevoIngrediente.getNombre().isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El nombre es obligatorio."));
                return;
            }

            // Forzar estado y valores por defecto: NO enviar codigo, inicializar costoUnitario en 0
            nuevoIngrediente.setEstado("disponible");
            nuevoIngrediente.setCodigo(null);
            nuevoIngrediente.setCantidad(null);
            // **INICIALIZAMOS costoUnitario a 0 para evitar nulls**
            nuevoIngrediente.setCostoUnitario(0);

            String json = gson.toJson(nuevoIngrediente);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ingrediente creado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear (código: " + resp.statusCode() + ")."));
            }

            prepararNuevo();
            cargarIngredientes();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void prepararEditar(IngredienteDTO i) {
        // asignamos la referencia que vino del listado para mantener costoUnitario si existe
        this.ingredienteSeleccionado = i;
    }

    public void actualizarIngrediente() {
        try {
            if (ingredienteSeleccionado == null || ingredienteSeleccionado.getCodigo() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Seleccione un ingrediente para editar."));
                return;
            }

            // No tocamos costoUnitario en la UI, por tanto ingredienteSeleccionado.costoUnitario conserva su valor
            String json = gson.toJson(ingredienteSeleccionado);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + ingredienteSeleccionado.getCodigo()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ingrediente actualizado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar (código: " + resp.statusCode() + ")."));
            }

            cargarIngredientes();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void prepararEliminar(Integer codigo) {
        this.eliminarCodigo = codigo;
        if (codigo != null && ingredientes != null) {
            for (IngredienteDTO it : ingredientes) {
                if (codigo.equals(it.getCodigo())) {
                    this.ingredienteSeleccionado = it;
                    break;
                }
            }
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Preparado", "Listo para marcar como agotado (código: " + codigo + ")."));
    }

    // marcar como agotado -> PUT parcial con { "estado": "agotado" }
    public void eliminarIngrediente() {
        try {
            if (eliminarCodigo == null) return;

            IngredienteDTO dto = new IngredienteDTO();
            dto.setEstado("agotado");

            String json = gson.toJson(dto);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + eliminarCodigo))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Hecho", "Ingrediente marcado como 'agotado'."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo marcar como agotado (código: " + resp.statusCode() + ")."));
            }

            eliminarCodigo = null;
            cargarIngredientes();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public LocalDate getToday() { return LocalDate.now(); }

    // getters / setters
    public List<IngredienteDTO> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<IngredienteDTO> ingredientes) { this.ingredientes = ingredientes; }

    public IngredienteDTO getIngredienteSeleccionado() { return ingredienteSeleccionado; }
    public void setIngredienteSeleccionado(IngredienteDTO ingredienteSeleccionado) { this.ingredienteSeleccionado = ingredienteSeleccionado; }

    public IngredienteDTO getNuevoIngrediente() { return nuevoIngrediente; }
    public void setNuevoIngrediente(IngredienteDTO nuevoIngrediente) { this.nuevoIngrediente = nuevoIngrediente; }

    public Integer getEliminarCodigo() { return eliminarCodigo; }
    public void setEliminarCodigo(Integer eliminarCodigo) { this.eliminarCodigo = eliminarCodigo; }

    public String getBASE_URL() { return BASE_URL; }
}
