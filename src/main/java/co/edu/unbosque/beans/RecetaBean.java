package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.RecetaDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("recetaBean")
@SessionScoped
public class RecetaBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<RecetaDTO> recetas = new ArrayList<>();
    private RecetaDTO nuevaReceta = new RecetaDTO();

    private List<IngredienteDTO> ingredientes = new ArrayList<>();
    private List<Integer> ingredientesSeleccionados = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8083/api/recetas";
    private final String ING_URL = "http://localhost:8083/api/ingredientes";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarIngredientes();
        cargarRecetas();
    }

    public void cargarRecetas() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<RecetaDTO>>() {}.getType();
            recetas = gson.fromJson(body, t);
            if (recetas == null) recetas = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            recetas = new ArrayList<>();
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

    public void crearReceta() {
        try {
            JsonObject j = new JsonObject();
            j.addProperty("nombre", nuevaReceta.getNombre());
            j.addProperty("descripcion", nuevaReceta.getDescripcion());
            j.addProperty("tiempoPreparacion", nuevaReceta.getTiempoPreparacion());
            j.addProperty("imagen", nuevaReceta.getImagen());

            JsonArray arr = new JsonArray();
            for (Integer id : ingredientesSeleccionados) {
                arr.add(id);
            }
            j.add("ingredientes", arr);

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            nuevaReceta = new RecetaDTO();
            ingredientesSeleccionados = new ArrayList<>();
            cargarRecetas();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Receta creada"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la receta"));
        }
    }

    public void eliminarReceta(Integer id) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + id);
            cargarRecetas();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Receta eliminada"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la receta"));
        }
    }

	public List<RecetaDTO> getRecetas() {
		return recetas;
	}

	public void setRecetas(List<RecetaDTO> recetas) {
		this.recetas = recetas;
	}

	public RecetaDTO getNuevaReceta() {
		return nuevaReceta;
	}

	public void setNuevaReceta(RecetaDTO nuevaReceta) {
		this.nuevaReceta = nuevaReceta;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public List<Integer> getIngredientesSeleccionados() {
		return ingredientesSeleccionados;
	}

	public void setIngredientesSeleccionados(List<Integer> ingredientesSeleccionados) {
		this.ingredientesSeleccionados = ingredientesSeleccionados;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getING_URL() {
		return ING_URL;
	}

	public Gson getGson() {
		return gson;
	}

 
}
