package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
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
    private IngredienteDTO nuevoIngrediente = new IngredienteDTO();

    private final String BASE_URL = "http://localhost:8083/api/ingredientes";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarIngredientes();
    }

    public void cargarIngredientes() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<IngredienteDTO>>() {}.getType();
            ingredientes = gson.fromJson(body, t);
            if (ingredientes == null) ingredientes = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            ingredientes = new ArrayList<>();
        }
    }

    public void crearIngrediente() {
        try {
            String json = gson.toJson(nuevoIngrediente);
            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", json);
            nuevoIngrediente = new IngredienteDTO();
            cargarIngredientes();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ingrediente creado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el ingrediente"));
        }
    }

    public void eliminarIngrediente(Integer codigo) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + codigo);
            cargarIngredientes();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ingrediente eliminado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el ingrediente"));
        }
    }

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public IngredienteDTO getNuevoIngrediente() {
		return nuevoIngrediente;
	}

	public void setNuevoIngrediente(IngredienteDTO nuevoIngrediente) {
		this.nuevoIngrediente = nuevoIngrediente;
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
