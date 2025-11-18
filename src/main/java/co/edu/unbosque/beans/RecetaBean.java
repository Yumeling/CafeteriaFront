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
import co.edu.unbosque.model.RecetaDTO;
import co.edu.unbosque.model.IngredienteDTO;
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
	private RecetaDTO recetaSeleccionada = new RecetaDTO();
	private RecetaDTO nuevaReceta = new RecetaDTO();
	private List<IngredienteDTO> ingredientesDisponibles = new ArrayList<>();
	private Integer selectedIngredienteCodigo;
	private Integer selectedIngredienteCantidad;
	private final String BASE_URL = "http://localhost:8083/api/recetas";
	private final String ING_URL = "http://localhost:8083/api/ingredientes";
	private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
	private final Gson gson = new GsonBuilder().registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
			.create();

	@PostConstruct
	public void init() {
		prepararNuevaReceta();
		cargarIngredientesDisponibles();
		cargarRecetas();
	}

	public void prepararNuevaReceta() {
		nuevaReceta = new RecetaDTO();
		nuevaReceta.setIngredientes(new ArrayList<>());
	}

	public void cargarRecetas() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(BASE_URL))
					.header("Content-Type", "application/json").build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			String json = resp.body() == null ? "[]" : resp.body().trim();

			if (json.startsWith("[")) {
				java.lang.reflect.Type t = new TypeToken<List<RecetaDTO>>() {
				}.getType();
				List<RecetaDTO> list = gson.fromJson(json, t);
				recetas = list != null ? list : new ArrayList<>();
			} else if (json.startsWith("{")) {
				JsonElement je = JsonParser.parseString(json);
				if (je.getAsJsonObject().has("data")) {
					String inner = je.getAsJsonObject().get("data").toString();
					java.lang.reflect.Type t = new TypeToken<List<RecetaDTO>>() {
					}.getType();
					recetas = gson.fromJson(inner, t);
				} else {
					RecetaDTO single = gson.fromJson(json, RecetaDTO.class);
					recetas = new ArrayList<>();
					recetas.add(single);
				}
			} else {
				recetas = new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			recetas = new ArrayList<>();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar las recetas."));
		}
	}

	public void cargarIngredientesDisponibles() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(ING_URL))
					.header("Content-Type", "application/json").build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			String json = resp.body() == null ? "[]" : resp.body().trim();

			if (json.startsWith("[")) {
				java.lang.reflect.Type t = new TypeToken<List<IngredienteDTO>>() {
				}.getType();
				List<IngredienteDTO> list = gson.fromJson(json, t);
				ingredientesDisponibles = list != null ? list : new ArrayList<>();
			} else if (json.startsWith("{")) {
				JsonElement je = JsonParser.parseString(json);
				if (je.getAsJsonObject().has("data")) {
					String inner = je.getAsJsonObject().get("data").toString();
					java.lang.reflect.Type t = new TypeToken<List<IngredienteDTO>>() {
					}.getType();
					ingredientesDisponibles = gson.fromJson(inner, t);
				} else {
					IngredienteDTO single = gson.fromJson(json, IngredienteDTO.class);
					ingredientesDisponibles = new ArrayList<>();
					ingredientesDisponibles.add(single);
				}
			} else {
				ingredientesDisponibles = new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			ingredientesDisponibles = new ArrayList<>();
		}
	}

	public void agregarIngredienteANuevaReceta() {
		try {
			if (selectedIngredienteCodigo == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Seleccione un ingrediente."));
				return;
			}
			if (selectedIngredienteCantidad == null || selectedIngredienteCantidad <= 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ingrese una cantidad válida (>0)."));
				return;
			}

			IngredienteDTO encontrado = null;
			for (IngredienteDTO it : ingredientesDisponibles) {
				if (selectedIngredienteCodigo.equals(it.getCodigo())) {
					encontrado = it;
					break;
				}
			}
			if (encontrado == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ingrediente no encontrado."));
				return;
			}

			if (nuevaReceta.getIngredientes() == null)
				nuevaReceta.setIngredientes(new ArrayList<>());
			boolean existe = false;
			for (IngredienteDTO it : nuevaReceta.getIngredientes()) {
				if (selectedIngredienteCodigo.equals(it.getCodigo())) {
					it.setCantidad((it.getCantidad() == null ? 0 : it.getCantidad()) + selectedIngredienteCantidad);
					existe = true;
					break;
				}
			}
			if (!existe) {
				IngredienteDTO toAdd = new IngredienteDTO();
				toAdd.setCodigo(encontrado.getCodigo());
				toAdd.setNombre(encontrado.getNombre());
				toAdd.setCantidad(selectedIngredienteCantidad);
				toAdd.setEstado(encontrado.getEstado());
				toAdd.setCostoUnitario(encontrado.getCostoUnitario());
				nuevaReceta.getIngredientes().add(toAdd);
			}

			selectedIngredienteCodigo = null;
			selectedIngredienteCantidad = null;

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Agregado", "Ingrediente agregado a la receta."));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
		}
	}

	public void removerIngredienteDeNuevaReceta(Integer codigo) {
		if (codigo == null || nuevaReceta.getIngredientes() == null)
			return;
		nuevaReceta.getIngredientes().removeIf(i -> codigo.equals(i.getCodigo()));
	}

	public String verDetalles(RecetaDTO r) {
		recetaSeleccionada = r;
		return null;
	}

	public void crearReceta() {
		try {
			if (nuevaReceta.getNombre() == null || nuevaReceta.getNombre().isBlank()) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Nombre es requerido."));
				return;
			}
			if (nuevaReceta.getIngredientes() == null || nuevaReceta.getIngredientes().isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Agrega al menos un ingrediente."));
				return;
			}

			if (nuevaReceta.getTiempoPreparacion() == null)
				nuevaReceta.setTiempoPreparacion(0);

			String json = gson.toJson(nuevaReceta);
			System.out.println("[RecetaBean] POST json: " + json);

			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Receta creada"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "No se pudo crear la receta. Código: " + resp.statusCode()));
			}

			prepararNuevaReceta();
			cargarRecetas();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
		}
	}

	public void eliminarReceta(Integer id) {
		try {
			if (id == null)
				return;

			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/" + id)).DELETE().build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Receta eliminada"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "No se pudo eliminar la receta. Código: " + resp.statusCode()));
			}
			cargarRecetas();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
		}
	}

	public List<RecetaDTO> getRecetas() {
		return recetas;
	}

	public void setRecetas(List<RecetaDTO> recetas) {
		this.recetas = recetas;
	}

	public RecetaDTO getRecetaSeleccionada() {
		return recetaSeleccionada;
	}

	public void setRecetaSeleccionada(RecetaDTO recetaSeleccionada) {
		this.recetaSeleccionada = recetaSeleccionada;
	}

	public RecetaDTO getNuevaReceta() {
		return nuevaReceta;
	}

	public void setNuevaReceta(RecetaDTO nuevaReceta) {
		this.nuevaReceta = nuevaReceta;
	}

	public List<IngredienteDTO> getIngredientesDisponibles() {
		return ingredientesDisponibles;
	}

	public void setIngredientesDisponibles(List<IngredienteDTO> ingredientesDisponibles) {
		this.ingredientesDisponibles = ingredientesDisponibles;
	}

	public Integer getSelectedIngredienteCodigo() {
		return selectedIngredienteCodigo;
	}

	public void setSelectedIngredienteCodigo(Integer selectedIngredienteCodigo) {
		this.selectedIngredienteCodigo = selectedIngredienteCodigo;
	}

	public Integer getSelectedIngredienteCantidad() {
		return selectedIngredienteCantidad;
	}

	public void setSelectedIngredienteCantidad(Integer selectedIngredienteCantidad) {
		this.selectedIngredienteCantidad = selectedIngredienteCantidad;
	}
}
