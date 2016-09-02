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
@Table(name="RubroProyecto")
@NamedQueries({
@NamedQuery(name="RubroProyecto.getRubroProyectoPorNombre", query="SELECT b FROM RubroProyecto b WHERE b.nombre = ?1"),
@NamedQuery(name="RubroProyecto.getRubroProyectoPorCodigo", query="SELECT b FROM RubroProyecto b WHERE b.codigo = ?1")
})
public class RubroProyecto implements Serializable {
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
	private RubroProyecto categoriaRubroProyecto;  

	// we need two types of grouping - one RubroProyecto general for all of the accounts
	// and second specific for each account 
	// (rubros presupuestales segun lo que define cada proyecto)
	@ManyToOne
	private Cuenta cuentaRubroProyecto;  
	
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Cuenta getCuentaRubroProyecto() {
		return cuentaRubroProyecto;
	}

	public void setCuentaRubroProyecto(Cuenta cuentaRubroProyecto) {
		this.cuentaRubroProyecto = cuentaRubroProyecto;
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

	public RubroProyecto getCategoriaRubroProyecto() {
		return categoriaRubroProyecto;
	}

	public void setCategoriaRubroProyecto(RubroProyecto categoriaRubroProyecto) {
		this.categoriaRubroProyecto = categoriaRubroProyecto;
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
		if (!(object instanceof RubroProyecto)) {
			return false;
		}
		RubroProyecto other = (RubroProyecto) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RubroProyecto [id=" + id + ", isLeaf=" + isLeaf + ", codigo "+codigo+ ", nombre="
				+ nombre + ", descripcion=" + descripcion
				+ ", categoriaRubroProyecto=" + categoriaRubroProyecto +", cuenta "+cuentaRubroProyecto+ "]";
	}

}
