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

    public String verDetalles(OrdenCompraDTO orden) {
        this.ordenSeleccionada = orden;
        return null;
    }

    public String seleccionarParaEditar(OrdenCompraDTO orden) {
        this.ordenSeleccionada = orden;
        return null;
    }

    // ---------- crear ----------
    public void crearOrden() {
        try {
            // validaciones cliente-servidor
            if (nuevaOrden.getFechaRecepcion() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La fecha de recepción es requerida."));
                return;
            }
            // asignar fechaEmision si no viene
            if (nuevaOrden.getFechaEmision() == null) {
                nuevaOrden.setFechaEmision(LocalDate.now());
            }
            // validar fechas: fechaRecepcion >= fechaEmision AND >= hoy
            if (nuevaOrden.getFechaRecepcion().isBefore(nuevaOrden.getFechaEmision())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La fecha de recepción no puede ser anterior a la fecha de emisión."));
                return;
            }
            if (nuevaOrden.getFechaRecepcion().isBefore(LocalDate.now())) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La fecha de recepción no puede ser anterior al día de hoy."));
                return;
            }
            // validar campos obligatorios del backend
            if (nuevaOrden.getEstado() == null || nuevaOrden.getTotal() == null || nuevaOrden.getNitEmpresa() == null || nuevaOrden.getAdministradorId() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Faltan datos obligatorios."));
                return;
            }

            String json = gson.toJson(nuevaOrden);
            // debug
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

            nuevaOrden = new OrdenCompraDTO();
            cargarOrdenes();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // ---------- actualizar ----------
    public void actualizarOrden() {
        try {
            if (ordenSeleccionada == null || ordenSeleccionada.getOrdenId() == null) return;

            // validaciones: si fechaEmision existe -> fechaRecepcion >= fechaEmision
            LocalDate fe = ordenSeleccionada.getFechaEmision();
            LocalDate fr = ordenSeleccionada.getFechaRecepcion();
            if (fr != null && fe != null && fr.isBefore(fe)) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La fecha de recepción no puede ser anterior a la fecha de emisión."));
                return;
            }

            // validar campos obligatorios
            if (ordenSeleccionada.getEstado() == null || ordenSeleccionada.getTotal() == null || ordenSeleccionada.getNitEmpresa() == null || ordenSeleccionada.getAdministradorId() == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Faltan datos obligatorios para actualizar."));
                return;
            }

            String json = gson.toJson(ordenSeleccionada);
            // debug
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

    // ---------- preparar cancelar (setear id antes de mostrar dialog) ----------
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
        // opcional: feedback
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Preparado", "Listo para cancelar la orden id: " + id));
    }

    // ---------- cancelar (no borrar) ----------
    public void cancelarOrden() {
        try {
            Integer id = this.eliminarId;
            if (id == null) {
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "ID inválido para cancelar."));
                return;
            }

            // construir DTO parcial con solo estado=cancelado
            OrdenCompraDTO dto = new OrdenCompraDTO();
            dto.setEstado("cancelado");

            String json = gson.toJson(dto);
            // debug
            System.out.println("[OrdenCompraBean] PUT json (cancelar): " + json + " -> url: " + BASE_URL + "/" + id);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            int status = resp.statusCode();
            String body = resp.body() == null ? "" : resp.body();

            if (status >= 200 && status < 300) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Hecho", "Orden marcada como cancelada (id: " + id + ")."));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                        "No se pudo cancelar la orden. Código: " + status + " Respuesta: " + body));
            }

            // refrescar lista local
            cargarOrdenes();
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // ---------- eliminar físico (si alguna vez lo necesitas) ----------
    public void eliminarOrden(Integer id) {
        try {
            if (id == null) return;
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }

    // util: hoy para mindate en calendarios
    public LocalDate getToday() {
        return LocalDate.now();
    }

    // util: fecha mínima permitida para fechaRecepcion al editar (fechaEmision si está, si no hoy)
    public LocalDate getMinFechaRecepcionEditable() {
        if (ordenSeleccionada != null && ordenSeleccionada.getFechaEmision() != null) {
            return ordenSeleccionada.getFechaEmision();
        }
        return LocalDate.now();
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
