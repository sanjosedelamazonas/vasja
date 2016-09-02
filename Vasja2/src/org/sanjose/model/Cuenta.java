/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sanjose.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author pol
 */
@Entity
@Table(name = "cuenta", uniqueConstraints = { @UniqueConstraint(columnNames = { "numero" }) })
@NamedQueries({
@NamedQuery(name="Cuenta.getCuentaPorNumero", query="SELECT c FROM Cuenta c WHERE c.numero = ?1")
})

public class Cuenta implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long numero = new Long(0);
    
    private String descripcion = "";
   
    private String nombre = "";
    
    private Boolean isCaja = false;
    
    private Boolean isBanco = false;
    
    private Boolean closed = false;
    

	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fechaDeApertura = new Date(System.currentTimeMillis());

    @ManyToOne
    private CategoriaCuenta categoriaCuenta;
    
    public CategoriaCuenta getCategoriaCuenta() {
		return categoriaCuenta;
	}

	public void setCategoriaCuenta(CategoriaCuenta categoriaCuenta) {
		this.categoriaCuenta = categoriaCuenta;
	}

	public Propietario getPropietario() {
		return propietario;
	}

	public void setPropietario(Propietario propietario) {
		this.propietario = propietario;
	}

	@ManyToOne
    private Usuario creadoPorUsuario;
    
    @ManyToOne
    private Propietario propietario;

    @Column(name="PEN",columnDefinition="DECIMAL(38,2)")
    private BigDecimal pen = new BigDecimal(0);
    
    @Column(name="USD",columnDefinition="DECIMAL(38,2)")
    private BigDecimal usd  = new BigDecimal(0);
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Long getNumero() {
		return numero;
	}

	public void setNumero(Long numero) {
		this.numero = numero;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Date getFechaDeApertura() {
		return fechaDeApertura;
	}

	public void setFechaDeApertura(Date fechaDeApertura) {
		this.fechaDeApertura = fechaDeApertura;
	}

	public Usuario getCreadoPorUsuario() {
		return creadoPorUsuario;
	}

	public void setCreadoPorUsuario(Usuario creadoPorUsuario) {
		this.creadoPorUsuario = creadoPorUsuario;
	}

	public BigDecimal getPen() {
		return pen;
	}

	public void setPen(BigDecimal pen) {
		this.pen = pen;
	}

	public BigDecimal getUsd() {
		return usd;
	}

	public void setUsd(BigDecimal usd) {
		this.usd = usd;
	}

	public Boolean getIsBanco() {
		return isBanco;
	}

	public void setIsBanco(Boolean isBanco) {
		this.isBanco = isBanco;
	}
    public Boolean getClosed() {
		return closed;
	}

	public void setClosed(Boolean closed) {
		this.closed = closed;
	}
	@Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Cuenta)) {
            return false;
        }
        Cuenta other = (Cuenta) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public Boolean getIsCaja() {
		return isCaja;
	}

	public void setIsCaja(Boolean isCaja) {
		this.isCaja = isCaja;
	}

	@Override
	public String toString() {
		return "Cuenta [id=" + id + ", numero=" + numero + ", descripcion="
				+ descripcion + ", nombre=" + nombre + ", isCaja=" + isCaja
				+ ", isBanco=" + isBanco + ", fechaDeApertura="
				+ fechaDeApertura + ", categoriaCuenta=" + categoriaCuenta
				+ ", creadoPorUsuario=" + creadoPorUsuario + ", propietario="
				+ propietario + ", pen=" + pen + ", usd=" + usd + ", closed="+closed+ "]";
	}
    
}
