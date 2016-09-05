/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sanjose.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * 
 * @author pol
 */
@Entity
@Table(name="mescerrado")
@NamedQueries({
@NamedQuery(name="MesCerrado.getMesCerradoUltimo", query="SELECT b FROM MesCerrado b ORDER BY b.fecha DESC")
})
public class MesCerrado implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha = new Date(System.currentTimeMillis());

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fechaCerrado = new Date(System.currentTimeMillis());
    
    @ManyToOne
    private Usuario usuario;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFechaCerrado() {
		return fechaCerrado;
	}

	public void setFechaCerrado(Date fechaCerrado) {
		this.fechaCerrado = fechaCerrado;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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
		if (!(object instanceof MesCerrado)) {
			return false;
		}
		MesCerrado other = (MesCerrado) object;
		if ((this.id == null && other.id != null)
				|| (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MesCerrado [id=" + id + ", fecha=" + fecha + ", fechaCerrado="
				+ fechaCerrado + ", usuario=" + usuario + "]";
	}

}
