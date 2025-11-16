package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("administradorBean")
@SessionScoped
public class AdministradorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<AdministradorDTO> administradores = new ArrayList<>();
    private AdministradorDTO nuevoAdministrador = new AdministradorDTO();
    private AdministradorDTO seleccionado;

    private final String BASE_URL = "http://localhost:8083/api/administradores";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarAdministradores();
    }

    public void cargarAdministradores() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<AdministradorDTO>>() {}.getType();
            administradores = gson.fromJson(body, t);
            if (administradores == null) administradores = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            administradores = new ArrayList<>();
        }
    }

    public void crearAdministrador() {
        try {
            String json = gson.toJson(nuevoAdministrador);
            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", json);
            nuevoAdministrador = new AdministradorDTO();
            cargarAdministradores();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Administrador creado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el administrador"));
        }
    }

    public void eliminarAdministrador(Integer id) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + id);
            cargarAdministradores();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Administrador eliminado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar"));
        }
    }

	public List<AdministradorDTO> getAdministradores() {
		return administradores;
	}

	public void setAdministradores(List<AdministradorDTO> administradores) {
		this.administradores = administradores;
	}

	public AdministradorDTO getNuevoAdministrador() {
		return nuevoAdministrador;
	}

	public void setNuevoAdministrador(AdministradorDTO nuevoAdministrador) {
		this.nuevoAdministrador = nuevoAdministrador;
	}

	public AdministradorDTO getSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(AdministradorDTO seleccionado) {
		this.seleccionado = seleccionado;
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
