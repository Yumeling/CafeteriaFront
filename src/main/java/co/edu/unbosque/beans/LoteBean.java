package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.LoteDTO;
import co.edu.unbosque.model.OrdenCompraDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.ProveedorDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * Bean para gestión de lotes. Basado en tu versión estable, corregido para:
 *  - mostrar proveedor usando los getters reales (primerNombre, etc.)
 *  - evitar conversiones erróneas en EL al formar cadenas
 */
@Named("loteBean")
@SessionScoped
public class LoteBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<LoteDTO> lotes;
    private LoteDTO nuevoLote;

    private List<OrdenCompraDTO> ordenes;
    private List<IngredienteDTO> ingredientes;
    private List<ProveedorDTO> proveedores;

    // vistas simples para presentar en los diálogos
    private ProveedorView proveedorView;
    private IngredienteView ingredienteView;

    private final String BASE_URL = "http://localhost:8083/api/lotes";
    private final String ORDEN_URL = "http://localhost:8083/api/ordenes";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";
    private final String PROV_URL = "http://localhost:8083/api/proveedores";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        lotes = new ArrayList<>();
        nuevoLote = new LoteDTO();

        ordenes = new ArrayList<>();
        ingredientes = new ArrayList<>();
        proveedores = new ArrayList<>();

        proveedorView = new ProveedorView();
        ingredienteView = new IngredienteView();

        cargarRecursos();
        cargarLotes();
    }

    // ------------------ Carga recursos ------------------

    public void cargarRecursos() {
        ordenes = tryFetchList(ORDEN_URL, new TypeToken<List<OrdenCompraDTO>>() {}.getType(), ordenes);
        ingredientes = tryFetchList(ING_URL, new TypeToken<List<IngredienteDTO>>() {}.getType(), ingredientes);
        proveedores = tryFetchList(PROV_URL, new TypeToken<List<ProveedorDTO>>() {}.getType(), proveedores);
    }

    private <T> List<T> tryFetchList(String baseUrl, Type typeOfT, List<T> fallback) {
        String[] candidates = new String[] { baseUrl + "/all", baseUrl, baseUrl + "s", baseUrl + "es" };
        for (String url : candidates) {
            try {
                String body = ExternalHTTPRequestHandler.doGet(url);
                if (body == null) continue;
                List<T> parsed = gson.fromJson(body, typeOfT);
                if (parsed != null) return parsed;
            } catch (Exception ex) {
                System.out.println("tryFetchList: fallo en " + url + " -> " + ex.getMessage());
            }
        }
        return (fallback != null) ? fallback : new ArrayList<>();
    }

    public void cargarLotes() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<LoteDTO>>() {}.getType();
            List<LoteDTO> temp = gson.fromJson(body, t);
            lotes = temp != null ? temp : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            lotes = new ArrayList<>();
        }
    }

    // ------------------ Crear / Eliminar lote ------------------

    public void prepararNuevoLote() {
        nuevoLote = new LoteDTO();
    }

    public void crearLote() {
        try {
            if (nuevoLote.getFechaRecepcion() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La fecha de recepción es obligatoria."));
                return;
            }
            if (nuevoLote.getCantidad() == null || nuevoLote.getCantidad() <= 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad debe ser mayor que 0."));
                return;
            }
            if (nuevoLote.getCodigoIngrediente() == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar un ingrediente."));
                return;
            }
            if (nuevoLote.getPrecio() == null || nuevoLote.getPrecio() < 0) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El precio del lote es requerido."));
                return;
            }

            JsonObject j = new JsonObject();
            j.addProperty("fechaRecepcion", nuevoLote.getFechaRecepcion().toString());
            if (nuevoLote.getFechaCaducidad() != null) j.addProperty("fechaCaducidad", nuevoLote.getFechaCaducidad().toString());
            j.addProperty("cantidad", nuevoLote.getCantidad());
            j.addProperty("precio", nuevoLote.getPrecio());
            if (nuevoLote.getOrdenId() != null) j.addProperty("ordenId", nuevoLote.getOrdenId());
            j.addProperty("codigoIngrediente", nuevoLote.getCodigoIngrediente());
            if (nuevoLote.getProveedorId() != null) j.addProperty("proveedorId", nuevoLote.getProveedorId());

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            prepararNuevoLote();
            cargarLotes();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Lote creado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el lote"));
        }
    }

    public void eliminarLote(Integer id) {
        try {
            if (id == null) return;
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + id);
            cargarLotes();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Lote eliminado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el lote"));
        }
    }

    // ------------------ Preparar vistas para diálogos ------------------

    public void prepararMostrarIngrediente(Integer codigo) {
        ingredienteView = new IngredienteView();
        if (codigo == null || ingredientes == null) return;
        for (IngredienteDTO it : ingredientes) {
            if (it != null && it.getCodigo() != null && it.getCodigo().equals(codigo)) {
                ingredienteView.setCodigo(String.valueOf(it.getCodigo()));
                ingredienteView.setNombre(it.getNombre() != null ? it.getNombre() : "—");
                ingredienteView.setEstado(it.getEstado() != null ? it.getEstado() : "—");
                // asume que tu DTO tiene getCostoUnitario o similar
                try {
                    Object cu = it.getCostoUnitario();
                    ingredienteView.setCostoUnitario(cu != null ? String.valueOf(cu) : "—");
                } catch (Throwable ex) {
                    ingredienteView.setCostoUnitario("—");
                }
                break;
            }
        }
    }

    public void prepararMostrarProveedor(Integer proveedorId) {
        proveedorView = new ProveedorView();
        if (proveedorId == null || proveedores == null) return;
        for (ProveedorDTO p : proveedores) {
            if (p != null && p.getProveedorId() != null && p.getProveedorId().equals(proveedorId)) {
                // nombre completo con comprobaciones simples
                StringBuilder nombre = new StringBuilder();
                if (p.getPrimerNombre() != null) nombre.append(p.getPrimerNombre());
                if (p.getSegundoNombre() != null && !p.getSegundoNombre().isBlank()) {
                    if (nombre.length() > 0) nombre.append(" ");
                    nombre.append(p.getSegundoNombre());
                }
                if (p.getPrimerApellido() != null) {
                    if (nombre.length() > 0) nombre.append(" ");
                    nombre.append(p.getPrimerApellido());
                }
                if (p.getSegundoApellido() != null && !p.getSegundoApellido().isBlank()) {
                    if (nombre.length() > 0) nombre.append(" ");
                    nombre.append(p.getSegundoApellido());
                }
                String fullName = nombre.length() > 0 ? nombre.toString() : "(sin nombre)";

                proveedorView.setProveedorId(String.valueOf(p.getProveedorId()));
                proveedorView.setNombre(fullName);
                proveedorView.setNit(p.getNitEmpresa() != null ? String.valueOf(p.getNitEmpresa()) : "—");
                proveedorView.setTelefono(p.getTelefono() != null ? String.valueOf(p.getTelefono()) : "—");
                proveedorView.setEmail(p.getEmail() != null ? p.getEmail() : "—");
                // si tu DTO no trae dirección, lo dejamos vacío o con "—"
                proveedorView.setDireccion("—");
                break;
            }
        }
    }

    // ---------- getters / setters ----------

    public List<LoteDTO> getLotes() { return lotes; }
    public void setLotes(List<LoteDTO> lotes) { this.lotes = lotes; }

    public LoteDTO getNuevoLote() { return nuevoLote; }
    public void setNuevoLote(LoteDTO nuevoLote) { this.nuevoLote = nuevoLote; }

    public List<OrdenCompraDTO> getOrdenes() { return ordenes; }
    public void setOrdenes(List<OrdenCompraDTO> ordenes) { this.ordenes = ordenes; }

    public List<IngredienteDTO> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<IngredienteDTO> ingredientes) { this.ingredientes = ingredientes; }

    public List<ProveedorDTO> getProveedores() { return proveedores; }
    public void setProveedores(List<ProveedorDTO> proveedores) { this.proveedores = proveedores; }

    public ProveedorView getProveedorView() { return proveedorView; }
    public void setProveedorView(ProveedorView proveedorView) { this.proveedorView = proveedorView; }

    public IngredienteView getIngredienteView() { return ingredienteView; }
    public void setIngredienteView(IngredienteView ingredienteView) { this.ingredienteView = ingredienteView; }

    public String getBASE_URL() { return BASE_URL; }
    public String getORDEN_URL() { return ORDEN_URL; }
    public String getING_URL() { return ING_URL; }
    public String getPROV_URL() { return PROV_URL; }
    public Gson getGson() { return gson; }

    // ------------------ vistas internas ------------------

    public static class ProveedorView {
        private String proveedorId;
        private String nombre;
        private String nit;
        private String telefono;
        private String email;
        private String direccion;

        public String getProveedorId() { return proveedorId; }
        public void setProveedorId(String proveedorId) { this.proveedorId = proveedorId; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getNit() { return nit; }
        public void setNit(String nit) { this.nit = nit; }

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
    }

    public static class IngredienteView {
        private String codigo;
        private String nombre;
        private String estado;
        private String costoUnitario;

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getCostoUnitario() { return costoUnitario; }
        public void setCostoUnitario(String costoUnitario) { this.costoUnitario = costoUnitario; }
    }
}
