package co.edu.unbosque.model;

import java.math.BigDecimal;

public class InventarioDTO {
    private Integer inventarioId;
    private Integer cantidad;
    private Integer administradorId;
    private Integer ingredienteId;
    private BigDecimal stockMinimo;
	public InventarioDTO() {
		super();
	}
	
	
	
	public InventarioDTO(Integer inventarioId, Integer cantidad, Integer administradorId, Integer ingredienteId,
			BigDecimal stockMinimo) {
		super();
		this.inventarioId = inventarioId;
		this.cantidad = cantidad;
		this.administradorId = administradorId;
		this.ingredienteId = ingredienteId;
		this.stockMinimo = stockMinimo;
	}



	public Integer getAdministradorId() {
		return administradorId;
	}

	public void setAdministradorId(Integer administradorId) {
		this.administradorId = administradorId;
	}


	public Integer getInventarioId() {
		return inventarioId;
	}

	public void setInventarioId(Integer inventarioId) {
		this.inventarioId = inventarioId;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Integer getIngredienteId() {
		return ingredienteId;
	}

	public void setIngredienteId(Integer ingredienteId) {
		this.ingredienteId = ingredienteId;
	}

	public BigDecimal getStockMinimo() {
		return stockMinimo;
	}

	public void setStockMinimo(BigDecimal stockMinimo) {
		this.stockMinimo = stockMinimo;
	}

	@Override
	public String toString() {
		return "InventarioDTO [inventarioId=" + inventarioId + ", cantidad=" + cantidad + ", administradorId="
				+ administradorId + ", ingredienteId=" + ingredienteId + ", stockMinimo=" + stockMinimo + "]";
	}
	
    
}