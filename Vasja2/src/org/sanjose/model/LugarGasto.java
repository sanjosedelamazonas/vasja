/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sanjose.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * 
 * @author pol
 */
@Entity
@Table(name="lugargasto")
@NamedQueries({
@NamedQuery(name="LugarGasto.getLugarGastoPorNombre", query="SELECT b FROM LugarGasto b WHERE b.nombre = ?1"),
@NamedQuery(name="LugarGasto.getLugarGastoPorCodigo", query="SELECT b FROM LugarGasto b WHERE b.codigo = ?1")
})
public class LugarGasto implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private Boolean isLeaf = true;

	private String nombre = "";

	private String descripcion = "" ;
	
	private String codigo = "" ;
	
	private Integer level = new Integer(0);
	
	@ManyToOne
	private LugarGasto categoriaLugarGasto;  

	// we need two types of grouping - one centroCosto general for all of the accounts
	// and second specific for each account 
	// (rubros presupuestales segun lo que define cada proyecto)
	//@ManyToOne
	//private Cuenta cuentaCentroCosto;  
	
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(Boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
	public LugarGasto getCategoriaLugarGasto() {
		return categoriaLugarGasto;
	}

	public void setCategoriaLugarGasto(LugarGasto categoriaLugarGasto) {
		this.categoriaLugarGasto = categoriaLugarGasto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof LugarGasto)) {
			return false;
		}
		LugarGasto other = (LugarGasto) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "LugarGasto ["
				+ (id != null ? "id=" + id + ", " : "")
				+ (isLeaf != null ? "isLeaf=" + isLeaf + ", " : "")
				+ (nombre != null ? "nombre=" + nombre + ", " : "")
				+ (descripcion != null ? "descripcion=" + descripcion + ", "
						: "")
				+ (codigo != null ? "codigo=" + codigo + ", " : "")
				+ (level != null ? "level=" + level + ", " : "")
				+ (categoriaLugarGasto != null ? "categoriaLugarGasto="
						+ categoriaLugarGasto : "") + "]";
	}

}
