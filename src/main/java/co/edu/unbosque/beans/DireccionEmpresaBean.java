package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.DireccionEmpresaDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("direccionEmpresaBean")
@SessionScoped
public class DireccionEmpresaBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<DireccionEmpresaDTO> direcciones = new ArrayList<>();
    private DireccionEmpresaDTO nuevaDireccion = new DireccionEmpresaDTO();

    private final String BASE_URL = "http://localhost:8083/api/direccion_empresa";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarDirecciones();
    }

    public void cargarDirecciones() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/listar");
            Type t = new TypeToken<List<DireccionEmpresaDTO>>() {}.getType();
            direcciones = gson.fromJson(body, t);
            if (direcciones == null) direcciones = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            direcciones = new ArrayList<>();
        }
    }

    public void crearDireccion() {
        try {
            String json = gson.toJson(nuevaDireccion);
            ExternalHTTPRequestHandler.doPost(BASE_URL + "/crear", json);
            nuevaDireccion = new DireccionEmpresaDTO();
            cargarDirecciones();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Dirección creada"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la dirección"));
        }
    }

    public void eliminarDireccion(Integer id) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/eliminar/" + id);
            cargarDirecciones();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Dirección eliminada"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la dirección"));
        }
    }

	public List<DireccionEmpresaDTO> getDirecciones() {
		return direcciones;
	}

	public void setDirecciones(List<DireccionEmpresaDTO> direcciones) {
		this.direcciones = direcciones;
	}

	public DireccionEmpresaDTO getNuevaDireccion() {
		return nuevaDireccion;
	}

	public void setNuevaDireccion(DireccionEmpresaDTO nuevaDireccion) {
		this.nuevaDireccion = nuevaDireccion;
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
