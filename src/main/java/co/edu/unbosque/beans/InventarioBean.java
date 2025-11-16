package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.InventarioDTO;
import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
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
    private InventarioDTO nuevoInventario = new InventarioDTO();

    private List<AdministradorDTO> administradores = new ArrayList<>();
    private List<IngredienteDTO> ingredientes = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8083/api/inventarios";
    private final String ADMIN_URL = "http://localhost:8083/api/administradores";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarAdministradores();
        cargarIngredientes();
        cargarInventarios();
    }

    public void cargarInventarios() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<InventarioDTO>>() {}.getType();
            inventarios = gson.fromJson(body, t);
            if (inventarios == null) inventarios = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            inventarios = new ArrayList<>();
        }
    }

    public void cargarAdministradores() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(ADMIN_URL + "/all");
            Type t = new TypeToken<List<AdministradorDTO>>() {}.getType();
            administradores = gson.fromJson(body, t);
            if (administradores == null) administradores = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            administradores = new ArrayList<>();
        }
    }

    public void cargarIngredientes() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(ING_URL + "/all");
            Type t = new TypeToken<List<IngredienteDTO>>() {}.getType();
            ingredientes = gson.fromJson(body, t);
            if (ingredientes == null) ingredientes = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            ingredientes = new ArrayList<>();
        }
    }

    public void crearInventario() {
        try {
            JsonObject j = new JsonObject();
            j.addProperty("cantidad", nuevoInventario.getCantidad());
            if (nuevoInventario.getStockMinimo() != null) j.addProperty("stockMinimo", nuevoInventario.getStockMinimo().toString());
            if (nuevoInventario.getAdministradorId() != null) j.addProperty("administradorId", nuevoInventario.getAdministradorId());
            if (nuevoInventario.getIngredienteId() != null) j.addProperty("ingredienteId", nuevoInventario.getIngredienteId());

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            nuevoInventario = new InventarioDTO();
            cargarInventarios();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Inventario creado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el inventario"));
        }
    }

    public void eliminarInventario(Integer id) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + id);
            cargarInventarios();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Inventario eliminado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el inventario"));
        }
    }

	public List<InventarioDTO> getInventarios() {
		return inventarios;
	}

	public void setInventarios(List<InventarioDTO> inventarios) {
		this.inventarios = inventarios;
	}

	public InventarioDTO getNuevoInventario() {
		return nuevoInventario;
	}

	public void setNuevoInventario(InventarioDTO nuevoInventario) {
		this.nuevoInventario = nuevoInventario;
	}

	public List<AdministradorDTO> getAdministradores() {
		return administradores;
	}

	public void setAdministradores(List<AdministradorDTO> administradores) {
		this.administradores = administradores;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getADMIN_URL() {
		return ADMIN_URL;
	}

	public String getING_URL() {
		return ING_URL;
	}

	public Gson getGson() {
		return gson;
	}

  
}
