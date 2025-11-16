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

    private final String BASE_URL = "http://localhost:8083/api/ordenes";
    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

    @PostConstruct
    public void init() {
        cargarOrdenes();
    }

    public void cargarOrdenes() {
        try {
            HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(BASE_URL)).header("Content-Type", "application/json").build();
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las órdenes."));
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

    public void crearOrden() {
        try {
            String json = gson.toJson(nuevaOrden);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden creada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la orden"));
            }
            nuevaOrden = new OrdenCompraDTO();
            cargarOrdenes();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    public void actualizarOrden() {
        try {
            if (ordenSeleccionada == null || ordenSeleccionada.getOrdenId() == null) return;

            String json = gson.toJson(ordenSeleccionada);
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + ordenSeleccionada.getOrdenId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Orden actualizada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo actualizar"));
            }
            cargarOrdenes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void eliminarOrden(Integer id) {
        try {
            if (id == null) return;
            // usar ExternalHTTPRequestHandler.doDelete si lo tienes; aquí uso HttpURLConnection simple:
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Eliminado", "Orden eliminada"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar"));
            }
            cargarOrdenes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // getters & setters

    public List<OrdenCompraDTO> getOrdenes() {
        return ordenes;
    }

    public void setOrdenes(List<OrdenCompraDTO> ordenes) {
        this.ordenes = ordenes;
    }

    public OrdenCompraDTO getOrdenSeleccionada() {
        return ordenSeleccionada;
    }

    public void setOrdenSeleccionada(OrdenCompraDTO ordenSeleccionada) {
        this.ordenSeleccionada = ordenSeleccionada;
    }

    public OrdenCompraDTO getNuevaOrden() {
        return nuevaOrden;
    }

    public void setNuevaOrden(OrdenCompraDTO nuevaOrden) {
        this.nuevaOrden = nuevaOrden;
    }

    public Integer getEliminarId() {
        return eliminarId;
    }

    public void setEliminarId(Integer eliminarId) {
        this.eliminarId = eliminarId;
    }
}
