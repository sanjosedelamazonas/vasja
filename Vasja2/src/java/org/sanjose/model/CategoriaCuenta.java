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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * 
 * @author pol
 */
@Entity
@Table(name = "categoriacuenta", uniqueConstraints = { @UniqueConstraint(columnNames = { "nombre" }) })
@NamedQueries({
@NamedQuery(name="CategoriaCuenta.getCategoriaCuentaPorNombre", query="SELECT c FROM CategoriaCuenta c WHERE c.nombre = ?1")
})
public class CategoriaCuenta implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String codigo;

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	private String nombre = "";

	private String descripcion = "" ;
	
	@OneToOne
	private Cuenta cajaCuenta;  

	@ManyToOne
	private Banco bancoPen;  

	@ManyToOne
	private Banco bancoUsd;  

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

	public Cuenta getCajaCuenta() {
		return cajaCuenta;
	}

	public void setCajaCuenta(Cuenta cajaCuenta) {
		this.cajaCuenta = cajaCuenta;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Banco getBancoPen() {
		return bancoPen;
	}

	public void setBancoPen(Banco bancoPen) {
		this.bancoPen = bancoPen;
	}

	public Banco getBancoUsd() {
		return bancoUsd;
	}

	public void setBancoUsd(Banco bancoUsd) {
		this.bancoUsd = bancoUsd;
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
		if (!(object instanceof CategoriaCuenta)) {
			return false;
		}
		CategoriaCuenta other = (CategoriaCuenta) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CategoriaCuenta [id=" + id + ", nombre=" + nombre
				+ ", descripcion=" + descripcion + ", cajaCuenta=" + (cajaCuenta!=null ? cajaCuenta.getNumero() : "")
				+ ", bancoPen=" + (bancoPen!=null ? bancoPen.getNombre() : "") + ", bancoUsd=" + (bancoUsd!=null ? bancoUsd.getNombre() : "") + "]";
	}

}
