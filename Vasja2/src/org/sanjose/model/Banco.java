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
@Table(name="banco")
@NamedQueries({
@NamedQuery(name="Banco.getBancoPorNombre", query="SELECT b FROM Banco b WHERE b.nombre = ?1"),
@NamedQuery(name="Banco.getBancoPorCuenta", query="SELECT b FROM Banco b WHERE b.bancoCuenta.id= ?1")
})
public class Banco implements Serializable {
    private static final long serialVersionUID = 1452462376251L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String banco = "";

    private String nombre = "";
    
    private String numero = "";
    
    private String descripcion = "";

    public enum Moneda{
        PEN,
        USD;
    }
    
    private String moneda = Moneda.PEN.toString();
    
    @ManyToOne
	private Cuenta bancoCuenta;  
    
    public String getBanco() {
		return banco;
	}

	public void setBanco(String banco) {
		this.banco = banco;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
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

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMoneda() {
		return moneda;
	}
    
	public void setMoneda(Moneda monedaM) {
		this.moneda = monedaM.toString();
	}

	public void setMoneda(String strMoneda) {
		this.moneda = strMoneda;
	}

	public Cuenta getBancoCuenta() {
		return bancoCuenta;
	}

	public void setBancoCuenta(Cuenta bancoCuenta) {
		this.bancoCuenta = bancoCuenta;
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
        if (!(object instanceof Banco)) {
            return false;
        }
        Banco other = (Banco) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    
    @Override
	public String toString() {
		return "Banco [id=" + id + ", banco=" + banco + ", nombre=" + nombre
				+ ", numero=" + numero + ", descripcion=" + descripcion
				+ ", moneda=" + moneda + ", bancoCuenta=" + bancoCuenta + "]";
	}
    
}
