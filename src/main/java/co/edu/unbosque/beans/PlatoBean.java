package co.edu.unbosque.beans;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.PlatoDTO;
import co.edu.unbosque.model.RecetaDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("platoBean")
@SessionScoped
public class PlatoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PlatoDTO> platos = new ArrayList<>();
    private PlatoDTO nuevoPlato = new PlatoDTO(); // contenedor temporal para crear desde receta
    private PlatoDTO platoSeleccionado = new PlatoDTO(); // para mostrar detalles

    private List<RecetaDTO> recetasDisponibles = new ArrayList<>();
    private Integer selectedRecetaId;

    private final String BASE_URL = "http://localhost:8083/api/platos";
    private final String RECETAS_URL = "http://localhost:8083/api/recetas";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter()).create();

    @PostConstruct
    public void init() {
        prepararNuevoPlato();
        cargarRecetasDisponibles();
        cargarPlatos();
    }

    public void prepararNuevoPlato() {
        nuevoPlato = new PlatoDTO();
        nuevoPlato.setMargenGanancia(35); // fijo
        selectedRecetaId = null;
    }

    public void cargarPlatos() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();

            if (json.startsWith("[")) {
                java.lang.reflect.Type t = new TypeToken<List<PlatoDTO>>() {}.getType();
                List<PlatoDTO> list = gson.fromJson(json, t);
                platos = list != null ? list : new ArrayList<>();
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    String inner = je.getAsJsonObject().get("data").toString();
                    java.lang.reflect.Type t = new TypeToken<List<PlatoDTO>>() {}.getType();
                    platos = gson.fromJson(inner, t);
                } else {
                    PlatoDTO single = gson.fromJson(json, PlatoDTO.class);
                    platos = new ArrayList<>();
                    platos.add(single);
                }
            } else {
                platos = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            platos = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los platos."));
        }
    }

    public void cargarRecetasDisponibles() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(RECETAS_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();

            if (json.startsWith("[")) {
                java.lang.reflect.Type t = new TypeToken<List<RecetaDTO>>() {}.getType();
                List<RecetaDTO> list = gson.fromJson(json, t);
                recetasDisponibles = list != null ? list : new ArrayList<>();
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    String inner = je.getAsJsonObject().get("data").toString();
                    java.lang.reflect.Type t = new TypeToken<List<RecetaDTO>>() {}.getType();
                    recetasDisponibles = gson.fromJson(inner, t);
                } else {
                    RecetaDTO single = gson.fromJson(json, RecetaDTO.class);
                    recetasDisponibles = new ArrayList<>();
                    recetasDisponibles.add(single);
                }
            } else {
                recetasDisponibles = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            recetasDisponibles = new ArrayList<>();
        }
    }

    private IngredienteDTO fetchIngredienteByCodigo(Integer codigo) {
        if (codigo == null) return null;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(ING_URL + "/" + codigo))
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300 && resp.body() != null && !resp.body().isBlank()) {
                return gson.fromJson(resp.body(), IngredienteDTO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onRecetaChange() {
        try {
            if (selectedRecetaId == null) {
                nuevoPlato.setCosto(null);
                nuevoPlato.setPrecioVenta(null);
                nuevoPlato.setRecetaId(null);
                return;
            }

            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(RECETAS_URL + "/" + selectedRecetaId))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (!(resp.statusCode() >= 200 && resp.statusCode() < 300)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "No se pudo recuperar la receta (status " + resp.statusCode() + ")."));
                return;
            }

            String json = resp.body();
            if (json == null || json.isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Receta vacía."));
                return;
            }

            RecetaDTO receta = gson.fromJson(json, RecetaDTO.class);
            if (receta == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Receta no encontrada."));
                return;
            }

            int total = 0;
            if (receta.getIngredientes() != null) {
                for (IngredienteDTO ingInReceta : receta.getIngredientes()) {
                    if (ingInReceta == null) continue;
                    Integer cantidad = ingInReceta.getCantidad() != null ? ingInReceta.getCantidad() : 0;
                    Integer costoUnit = ingInReceta.getCostoUnitario();

                    if (costoUnit == null && ingInReceta.getCodigo() != null) {
                        IngredienteDTO servidor = fetchIngredienteByCodigo(ingInReceta.getCodigo());
                        if (servidor != null && servidor.getCostoUnitario() != null) {
                            costoUnit = servidor.getCostoUnitario();
                        } else {
                            costoUnit = 0; // fallback
                        }
                    } else if (costoUnit == null) {
                        costoUnit = 0;
                    }

                    total += cantidad * costoUnit;
                }
            }

            nuevoPlato.setCosto(total);
            nuevoPlato.setRecetaId(selectedRecetaId);
            nuevoPlato.setMargenGanancia(35);
            nuevoPlato.setPrecioVenta((int) Math.round(total * 1.35));

            String sugerido = "Plato - " + (receta.getNombre() != null ? receta.getNombre() : receta.getRecetaId());
            nuevoPlato.setNombre(sugerido);
            nuevoPlato.setImagen(receta.getImagen());

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al leer receta o ingredientes: " + e.getMessage()));
            nuevoPlato.setCosto(null);
            nuevoPlato.setPrecioVenta(null);
            nuevoPlato.setRecetaId(selectedRecetaId);
        }
    }

    public void crearPlatoDesdeReceta() {
        try {
            if (selectedRecetaId == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selecciona primero una receta."));
                return;
            }

            onRecetaChange();

            if (nuevoPlato.getNombre() == null || nuevoPlato.getNombre().isBlank()) {
                nuevoPlato.setNombre("Plato - receta " + selectedRecetaId);
            }

            String json = gson.toJson(nuevoPlato);
            System.out.println("[PlatoBean] POST -> " + BASE_URL);
            System.out.println("[PlatoBean] Request JSON: " + json);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("[PlatoBean] Response status: " + resp.statusCode() + " body: " + (resp.body() == null ? "" : resp.body()));

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Plato creado desde receta."));
                prepararNuevoPlato();
                cargarPlatos();
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el plato. HTTP: " + resp.statusCode()));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void generarPlatosDesdeRecetas() {
        try {
            cargarPlatos();

            if (recetasDisponibles == null || recetasDisponibles.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "No hay recetas disponibles."));
                return;
            }

            List<Integer> conPlato = new ArrayList<>();
            for (PlatoDTO p : platos) if (p.getRecetaId() != null) conPlato.add(p.getRecetaId());

            int creados = 0;
            int saltados = 0;

            for (RecetaDTO r : recetasDisponibles) {
                if (r.getRecetaId() == null) continue;
                if (conPlato.contains(r.getRecetaId())) {
                    saltados++;
                    continue;
                }

                int total = 0;
                if (r.getIngredientes() != null) {
                    for (IngredienteDTO ingInReceta : r.getIngredientes()) {
                        if (ingInReceta == null) continue;
                        Integer cantidad = ingInReceta.getCantidad() != null ? ingInReceta.getCantidad() : 0;
                        Integer costoUnit = ingInReceta.getCostoUnitario();

                        if (costoUnit == null && ingInReceta.getCodigo() != null) {
                            IngredienteDTO serv = fetchIngredienteByCodigo(ingInReceta.getCodigo());
                            if (serv != null && serv.getCostoUnitario() != null) {
                                costoUnit = serv.getCostoUnitario();
                            } else {
                                costoUnit = 0;
                            }
                        } else if (costoUnit == null) {
                            costoUnit = 0;
                        }
                        total += cantidad * costoUnit;
                    }
                }

                PlatoDTO dto = new PlatoDTO();
                dto.setNombre("Plato - " + (r.getNombre() != null ? r.getNombre() : r.getRecetaId()));
                dto.setRecetaId(r.getRecetaId());
                dto.setCosto(total);
                dto.setMargenGanancia(35);
                dto.setPrecioVenta((int) Math.round(total * 1.35));
                dto.setImagen(r.getImagen());

                String json = gson.toJson(dto);
                System.out.println("[PlatoBean BATCH] POST json: " + json);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    creados++;
                } else {
                    System.out.println("[PlatoBean BATCH] No se creó plato para receta " + r.getRecetaId() + " status: " + resp.statusCode());
                }
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Batch terminado", "Creados: " + creados + " | Saltados: " + saltados));
            prepararNuevoPlato();
            cargarPlatos();

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void eliminarPlato(Integer id) {
        try {
            if (id == null) return;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Plato eliminado."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar. Código: " + resp.statusCode()));
            }
            cargarPlatos();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public String verDetalles(PlatoDTO p) {
        this.platoSeleccionado = p;
        return null;
    }

    // getters & setters
    public List<PlatoDTO> getPlatos() { return platos; }
    public void setPlatos(List<PlatoDTO> platos) { this.platos = platos; }

    public PlatoDTO getNuevoPlato() { return nuevoPlato; }
    public void setNuevoPlato(PlatoDTO nuevoPlato) { this.nuevoPlato = nuevoPlato; }

    public PlatoDTO getPlatoSeleccionado() { return platoSeleccionado; }
    public void setPlatoSeleccionado(PlatoDTO platoSeleccionado) { this.platoSeleccionado = platoSeleccionado; }

    public List<RecetaDTO> getRecetasDisponibles() { return recetasDisponibles; }
    public void setRecetasDisponibles(List<RecetaDTO> recetasDisponibles) { this.recetasDisponibles = recetasDisponibles; }

    public Integer getSelectedRecetaId() { return selectedRecetaId; }
    public void setSelectedRecetaId(Integer selectedRecetaId) { this.selectedRecetaId = selectedRecetaId; }
}
