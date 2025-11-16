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

@Named("loteBean")
@SessionScoped
public class LoteBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<LoteDTO> lotes = new ArrayList<>();
    private LoteDTO nuevoLote = new LoteDTO();

    private List<OrdenCompraDTO> ordenes = new ArrayList<>();
    private List<IngredienteDTO> ingredientes = new ArrayList<>();
    private List<ProveedorDTO> proveedores = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8083/api/lotes";
    private final String ORDEN_URL = "http://localhost:8083/api/orden_compra";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";
    private final String PROV_URL = "http://localhost:8083/api/proveedores";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarRecursos();
        cargarLotes();
    }

    public void cargarRecursos() {
        try {
            String bodyO = ExternalHTTPRequestHandler.doGet(ORDEN_URL + "/all");
            Type to = new TypeToken<List<OrdenCompraDTO>>() {}.getType();
            ordenes = gson.fromJson(bodyO, to);
            if (ordenes == null) ordenes = new ArrayList<>();
        } catch (Exception e) { ordenes = new ArrayList<>(); }

        try {
            String bodyI = ExternalHTTPRequestHandler.doGet(ING_URL + "/all");
            Type ti = new TypeToken<List<IngredienteDTO>>() {}.getType();
            ingredientes = gson.fromJson(bodyI, ti);
            if (ingredientes == null) ingredientes = new ArrayList<>();
        } catch (Exception e) { ingredientes = new ArrayList<>(); }

        try {
            String bodyP = ExternalHTTPRequestHandler.doGet(PROV_URL + "/all");
            Type tp = new TypeToken<List<ProveedorDTO>>() {}.getType();
            proveedores = gson.fromJson(bodyP, tp);
            if (proveedores == null) proveedores = new ArrayList<>();
        } catch (Exception e) { proveedores = new ArrayList<>(); }
    }

    public void cargarLotes() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<LoteDTO>>() {}.getType();
            lotes = gson.fromJson(body, t);
            if (lotes == null) lotes = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            lotes = new ArrayList<>();
        }
    }

    public void crearLote() {
        try {
            JsonObject j = new JsonObject();
            j.addProperty("fechaRecepcion", nuevoLote.getFechaRecepcion() != null ? nuevoLote.getFechaRecepcion().toString() : null);
            j.addProperty("cantidad", nuevoLote.getCantidad());
            if (nuevoLote.getOrdenId() != null) j.addProperty("ordenId", nuevoLote.getOrdenId());
            if (nuevoLote.getCodigoIngrediente() != null) j.addProperty("codigoIngrediente", nuevoLote.getCodigoIngrediente());
            if (nuevoLote.getProveedorId() != null) j.addProperty("proveedorId", nuevoLote.getProveedorId());

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            nuevoLote = new LoteDTO();
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

	public List<LoteDTO> getLotes() {
		return lotes;
	}

	public void setLotes(List<LoteDTO> lotes) {
		this.lotes = lotes;
	}

	public LoteDTO getNuevoLote() {
		return nuevoLote;
	}

	public void setNuevoLote(LoteDTO nuevoLote) {
		this.nuevoLote = nuevoLote;
	}

	public List<OrdenCompraDTO> getOrdenes() {
		return ordenes;
	}

	public void setOrdenes(List<OrdenCompraDTO> ordenes) {
		this.ordenes = ordenes;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public List<ProveedorDTO> getProveedores() {
		return proveedores;
	}

	public void setProveedores(List<ProveedorDTO> proveedores) {
		this.proveedores = proveedores;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getORDEN_URL() {
		return ORDEN_URL;
	}

	public String getING_URL() {
		return ING_URL;
	}

	public String getPROV_URL() {
		return PROV_URL;
	}

	public Gson getGson() {
		return gson;
	}

  
}
