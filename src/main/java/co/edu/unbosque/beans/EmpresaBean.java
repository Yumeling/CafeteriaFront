package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.EmpresaDTO;
import co.edu.unbosque.model.DireccionEmpresaDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("empresaBean")
@SessionScoped
public class EmpresaBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EmpresaDTO> empresas = new ArrayList<>();
    private EmpresaDTO nuevaEmpresa = new EmpresaDTO();
    private List<DireccionEmpresaDTO> direcciones = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8083/api/empresas";
    private final String DIR_URL = "http://localhost:8083/api/direccion_empresa";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarDirecciones();
        cargarEmpresas();
    }

    public void cargarEmpresas() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<EmpresaDTO>>() {}.getType();
            empresas = gson.fromJson(body, t);
            if (empresas == null) empresas = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            empresas = new ArrayList<>();
        }
    }

    public void cargarDirecciones() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(DIR_URL + "/listar");
            Type t = new TypeToken<List<DireccionEmpresaDTO>>() {}.getType();
            direcciones = gson.fromJson(body, t);
            if (direcciones == null) direcciones = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            direcciones = new ArrayList<>();
        }
    }

    public void crearEmpresa() {
        try {
            // Evitar enviar objetos anidados; enviar direccionId en su lugar
            JsonObject j = new JsonObject();
            j.addProperty("nit", nuevaEmpresa.getNit());
            j.addProperty("nombre", nuevaEmpresa.getNombre());
            j.addProperty("email", nuevaEmpresa.getEmail());
            if (nuevaEmpresa.getTelefono() != null) j.addProperty("telefono", nuevaEmpresa.getTelefono());
            if (nuevaEmpresa.getDireccionId() != null) j.addProperty("direccionId", nuevaEmpresa.getDireccionId());
            j.addProperty("estado", nuevaEmpresa.getEstado());

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            nuevaEmpresa = new EmpresaDTO();
            cargarEmpresas();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empresa creada"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear la empresa"));
        }
    }

    public void eliminarEmpresa(Integer nit) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + nit);
            cargarEmpresas();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Empresa eliminada"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la empresa"));
        }
    }

	public List<EmpresaDTO> getEmpresas() {
		return empresas;
	}

	public void setEmpresas(List<EmpresaDTO> empresas) {
		this.empresas = empresas;
	}

	public EmpresaDTO getNuevaEmpresa() {
		return nuevaEmpresa;
	}

	public void setNuevaEmpresa(EmpresaDTO nuevaEmpresa) {
		this.nuevaEmpresa = nuevaEmpresa;
	}

	public List<DireccionEmpresaDTO> getDirecciones() {
		return direcciones;
	}

	public void setDirecciones(List<DireccionEmpresaDTO> direcciones) {
		this.direcciones = direcciones;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getDIR_URL() {
		return DIR_URL;
	}

	public Gson getGson() {
		return gson;
	}

  
}
