package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.PlatoDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
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
    private PlatoDTO nuevoPlato = new PlatoDTO();

    private final String BASE_URL = "http://localhost:8083/api/platos";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarPlatos();
    }

    public void cargarPlatos() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<PlatoDTO>>() {}.getType();
            platos = gson.fromJson(body, t);
            if (platos == null) platos = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            platos = new ArrayList<>();
        }
    }

    public void crearPlato() {
        try {
            JsonObject j = new JsonObject();
            j.addProperty("nombre", nuevoPlato.getNombre());
            j.addProperty("imagen", nuevoPlato.getImagen());
            if (nuevoPlato.getPrecioVenta() != null) j.addProperty("precioVenta", nuevoPlato.getPrecioVenta());
            if (nuevoPlato.getCosto() != null) j.addProperty("costo", nuevoPlato.getCosto());
            if (nuevoPlato.getMargenGanancia() != null) j.addProperty("margenGanancia", nuevoPlato.getMargenGanancia());

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            nuevoPlato = new PlatoDTO();
            cargarPlatos();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Plato creado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el plato"));
        }
    }

    public void eliminarPlato(Integer id) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + id);
            cargarPlatos();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Plato eliminado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el plato"));
        }
    }

	public List<PlatoDTO> getPlatos() {
		return platos;
	}

	public void setPlatos(List<PlatoDTO> platos) {
		this.platos = platos;
	}

	public PlatoDTO getNuevoPlato() {
		return nuevoPlato;
	}

	public void setNuevoPlato(PlatoDTO nuevoPlato) {
		this.nuevoPlato = nuevoPlato;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public Gson getGson() {
		return gson;
	}

   
}
