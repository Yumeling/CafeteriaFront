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
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.OrdenCompraDTO;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("ordenCompraBean")
@SessionScoped
public class OrdenCompraBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<OrdenCompraDTO> ordenes = new ArrayList<>();
    private OrdenCompraDTO ordenSeleccionada = new OrdenCompraDTO();
    private OrdenCompraDTO nuevaOrden = new OrdenCompraDTO();
    private Integer eliminarId;

    // ingredientes disponibles para seleccionar al crear
    private List<IngredienteDTO> availableIngredientes = new ArrayList<>();
    private Integer selectedIngredienteCodigo;
    private Integer selectedCantidad;
    // (no se usa costo al agregar según tu pedido)

    private final String BASE_URL = "http://localhost:8083/api/ordenes";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";
    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

    @PostConstruct
    public void init() {
        cargarOrdenes();
        cargarIngredientesDisponibles();
        prepararNuevaOrden();
    }

    public void prepararNuevaOrden() {
        nuevaOrden = new OrdenCompraDTO();
        nuevaOrden.setIngredientes(new ArrayList<>());
        // por defecto estado pendiente al crear (backend también puede asignarlo)
        nuevaOrden.setEstado("pendiente");
    }

    public void cargarOrdenes() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();

            if (json.startsWith("[")) {
                OrdenCompraDTO[] arr = gson.fromJson(json, OrdenCompraDTO[].class);
                ordenes = Arrays.asList(arr);
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    ordenes = Arrays.asList(gson.fromJson(je.getAsJsonObject().get("data"), OrdenCompraDTO[].class));
                } else {
                    OrdenCompraDTO single = gson.fromJson(json, OrdenCompraDTO.class);
                    ordenes = new ArrayList<>();
                    ordenes.add(single);
                }
            } else {
                ordenes = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            ordenes = new ArrayList<>();
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las órdenes."));
        }
    }

    public void cargarIngredientesDisponibles() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(ING_URL))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            String json = resp.body() == null ? "[]" : resp.body().trim();
            if (json.startsWith("[")) {
                java.lang.reflect.Type t = new TypeToken<List<IngredienteDTO>>() {}.getType();
                List<IngredienteDTO> list = gson.fromJson(json, t);
                availableIngredientes = list != null ? list : new ArrayList<>();
            } else if (json.startsWith("{")) {
                JsonElement je = JsonParser.parseString(json);
                if (je.getAsJsonObject().has("data")) {
                    String inner = je.getAsJsonObject().get("data").toString();
                    java.lang.reflect.Type t = new TypeToken<List<IngredienteDTO>>() {}.getType();
                    availableIngredientes = gson.fromJson(inner, t);
                } else {
                    IngredienteDTO single = gson.fromJson(json, IngredienteDTO.class);
                    availableIngredientes = new ArrayList<>();
                    availableIngredientes.add(single);
                }
            } else {
                availableIngredientes = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            availableIngredientes = new ArrayList<>();
        }
    }

    public String verDetalles(OrdenCompraDTO orden) {
        this.ordenSeleccionada = orden;
        return null;
    }

    public String seleccionarParaEditar(OrdenCompraDTO orden) {
        this.ordenSeleccionada = orden;
        return null;
    }

    public void addIngredienteToNuevaOrden() {
        try {
            if (selectedIngredienteCodigo == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Seleccione un ingrediente."));
                return;
            }
            if (selectedCantidad == null || selectedCantidad <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ingrese una cantidad válida (>0)."));
                return;
            }

            IngredienteDTO found = null;
            for (IngredienteDTO it : availableIngredientes) {
                if (selectedIngredienteCodigo.equals(it.getCodigo())) {
                    found = it;
                    break;
                }
            }
            if (found == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ingrediente no encontrado."));
                return;
            }

            if (nuevaOrden.getIngredientes() == null) nuevaOrden.setIngredientes(new ArrayList<>());
            boolean existe = false;
            for (IngredienteDTO it : nuevaOrden.getIngredientes()) {
                if (selectedIngredienteCodigo.equals(it.getCodigo())) {
                    it.setCantidad((it.getCantidad() == null ? 0 : it.getCantidad()) + selectedCantidad);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                IngredienteDTO toAdd = new IngredienteDTO();
                toAdd.setCodigo(found.getCodigo());
                toAdd.setNombre(found.getNombre());
                toAdd.setCantidad(selectedCantidad);
                toAdd.setEstado(found.getEstado());
                // no seteamos costo (según tu pedido)
                toAdd.setCostoUnitario(null);
                nuevaOrden.getIngredientes().add(toAdd);
            }

            // limpiar selects
            selectedIngredienteCodigo = null;
            selectedCantidad = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Agregado", "Ingrediente agregado a la orden."));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void removeIngredienteFromNuevaOrden(Integer codigo) {
        if (codigo == null || nuevaOrden.getIngredientes() == null) return;
        nuevaOrden.getIngredientes().removeIf(i -> codigo.equals(i.getCodigo()));
    }

    public void crearOrden() {
        try {
            // validar administrador y nitEmpresa y que haya ingredientes
            if (nuevaOrden.getAdministradorId() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Administrador es requerido."));
                return;
            }
            if (nuevaOrden.getNitEmpresa() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "NIT de la empresa es requerido."));
                return;
            }
            if (nuevaOrden.getIngredientes() == null || nuevaOrden.getIngredientes().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Agrega al menos un ingrediente a la orden."));
                return;
            }

            if (nuevaOrden.getFechaEmision() == null) {
                nuevaOrden.setFechaEmision(LocalDate.now());
            }

            // asegurarnos estado por defecto
            if (nuevaOrden.getEstado() == null) nuevaOrden.setEstado("pendiente");

            String json = gson.toJson(nuevaOrden);
            System.out.println("[OrdenCompraBean] POST json: " + json);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden creada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la orden. Código: " + resp.statusCode()));
            }

            prepararNuevaOrden();
            cargarOrdenes();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void actualizarOrden() {
        try {
            if (ordenSeleccionada == null || ordenSeleccionada.getOrdenId() == null) return;

            if (ordenSeleccionada.getEstado() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El estado es requerido."));
                return;
            }

            String json = gson.toJson(ordenSeleccionada);
            System.out.println("[OrdenCompraBean] PUT json (update): " + json);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + ordenSeleccionada.getOrdenId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden actualizada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar. Código: " + resp.statusCode()));
            }
            cargarOrdenes();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void prepararCancelar(Integer id) {
        this.eliminarId = id;
        if (id != null && ordenes != null) {
            for (OrdenCompraDTO o : ordenes) {
                if (id.equals(o.getOrdenId())) {
                    this.ordenSeleccionada = o;
                    break;
                }
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Preparado", "Listo para inactivar la orden id: " + id));
    }

    /**
     * Ahora eliminarOrden hace PUT: toma la orden actual (de la lista local),
     * le asigna estado = "inactivo" y la envía por PUT al endpoint /api/ordenes/{id}
     */
    public void eliminarOrden() {
        try {
            Integer id = this.eliminarId;
            if (id == null) {
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "ID inválido para inactivar."));
                return;
            }

            // buscar la orden en la lista local
            OrdenCompraDTO target = null;
            if (ordenes != null) {
                for (OrdenCompraDTO o : ordenes) {
                    if (id.equals(o.getOrdenId())) {
                        target = o;
                        break;
                    }
                }
            }

            // Si no está en la lista, intentar traerla del servicio (GET)
            if (target == null) {
                try {
                    HttpRequest reqGet = HttpRequest.newBuilder().GET().uri(URI.create(BASE_URL + "/" + id))
                            .header("Content-Type", "application/json").build();
                    HttpResponse<String> respGet = httpClient.send(reqGet, HttpResponse.BodyHandlers.ofString());
                    if (respGet.statusCode() >= 200 && respGet.statusCode() < 300) {
                        String body = respGet.body() == null ? "{}" : respGet.body();
                        target = gson.fromJson(body, OrdenCompraDTO.class);
                    }
                } catch (Exception ex) {
                    // ignore here, we'll handle null below
                    ex.printStackTrace();
                }
            }

            if (target == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se encontró la orden para inactivar."));
                return;
            }

            // Cambiar estado a "inactivo" (mismo objeto que se enviaría en actualizar)
            target.setEstado("inactivo");

            String json = gson.toJson(target);
            System.out.println("[OrdenCompraBean] PUT json (inactivar): " + json + " -> url: " + BASE_URL + "/" + id);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            String body = resp.body() == null ? "" : resp.body();

            if (status >= 200 && status < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Hecho", "Orden marcada como inactiva (id: " + id + ")."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "No se pudo inactivar la orden. Código: " + status + " Respuesta: " + body));
            }

            // refrescar lista local
            cargarOrdenes();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public LocalDate getToday() { return LocalDate.now(); }

    // getters & setters

    public List<OrdenCompraDTO> getOrdenes() { return ordenes; }
    public void setOrdenes(List<OrdenCompraDTO> ordenes) { this.ordenes = ordenes; }

    public OrdenCompraDTO getOrdenSeleccionada() { return ordenSeleccionada; }
    public void setOrdenSeleccionada(OrdenCompraDTO ordenSeleccionada) { this.ordenSeleccionada = ordenSeleccionada; }

    public OrdenCompraDTO getNuevaOrden() { return nuevaOrden; }
    public void setNuevaOrden(OrdenCompraDTO nuevaOrden) { this.nuevaOrden = nuevaOrden; }

    public Integer getEliminarId() { return eliminarId; }
    public void setEliminarId(Integer eliminarId) { this.eliminarId = eliminarId; }

    public List<IngredienteDTO> getAvailableIngredientes() { return availableIngredientes; }
    public void setAvailableIngredientes(List<IngredienteDTO> availableIngredientes) { this.availableIngredientes = availableIngredientes; }

    public Integer getSelectedIngredienteCodigo() { return selectedIngredienteCodigo; }
    public void setSelectedIngredienteCodigo(Integer selectedIngredienteCodigo) { this.selectedIngredienteCodigo = selectedIngredienteCodigo; }

    public Integer getSelectedCantidad() { return selectedCantidad; }
    public void setSelectedCantidad(Integer selectedCantidad) { this.selectedCantidad = selectedCantidad; }
}
