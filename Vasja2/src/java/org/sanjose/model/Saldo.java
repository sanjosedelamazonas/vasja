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

import org.sanjose.model.Operacion.Tipo;

/**
 *
 * @author pol
 */
@Entity
@Table(name="saldo")
@NamedQueries({
@NamedQuery(name="Saldo.getSaldoPorFecha", query="SELECT s FROM Saldo s WHERE s.categoriaCuenta.id = ?1 AND s.fecha < ?2 ORDER BY s.fecha DESC, s.id DESC"),
@NamedQuery(name="Saldo.getLast", query="SELECT s FROM Saldo s WHERE s.categoriaCuenta.id = ?1 ORDER BY s.id DESC"),
@NamedQuery(name="Saldo.getSaldosNewerForCategoria", query="SELECT s FROM Saldo s WHERE s.categoriaCuenta.id = ?1 AND s.fecha >= ?2 ORDER BY s.fecha ASC, s.id ASC"),
@NamedQuery(name="Saldo.getSaldoLastForCategoria", query="SELECT s FROM Saldo s WHERE s.categoriaCuenta.id = ?1 AND s.fecha < ?2 ORDER BY s.fecha DESC, s.id DESC")
})
public class Saldo implements Serializable {
    private static final long serialVersionUID = 1452462376251L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private CategoriaCuenta categoriaCuenta;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha = new Date(System.currentTimeMillis());
    
    @Column(name="PEN",columnDefinition="DECIMAL(20,2)")
    private BigDecimal pen = new BigDecimal(0.00);
    
    @Column(name="USD",columnDefinition="DECIMAL(20,2)")
    private BigDecimal usd = new BigDecimal(0.00);

    @Column(name="EFPEN",columnDefinition="DECIMAL(20,2)")
    private BigDecimal efPen = new BigDecimal(0.00);
    
    @Column(name="EFUSD",columnDefinition="DECIMAL(20,2)")
    private BigDecimal efUsd = new BigDecimal(0.00);

    @ManyToOne
    private Operacion operacion;
    
    public Saldo() {    	
    }
    
    // Create Saldo from lastSaldo and Operacion
    public Saldo(Saldo oldSaldo, Operacion oper) {
    	this.pen = oldSaldo.getPen().add(oper.getPen());
    	this.usd = oldSaldo.getUsd().add(oper.getUsd());
    	if (oper.getTipo().toString().equals(Tipo.EFECTIVO.toString())) {
	    	this.efPen = oldSaldo.getEfPen().add(oper.getPen());
	    	this.efUsd = oldSaldo.getEfUsd().add(oper.getUsd());
    	} else {
    		this.efPen = oldSaldo.getEfPen();
	    	this.efUsd = oldSaldo.getEfUsd();
    	}
    	this.fecha = oper.getFecha();
    	if (oldSaldo.getCategoriaCuenta()==null)
    		this.categoriaCuenta = oper.getCuenta().getCategoriaCuenta();
    	else
    		this.categoriaCuenta = oldSaldo.getCategoriaCuenta();
    	this.operacion = oper;
    }
    // Create correction saldo from Operacion and old Operacion
    public Saldo(Saldo oldSaldo, Operacion oper, Operacion oldOper) {
    	this.pen = oldSaldo.getPen().add(oper.getPen());
    	this.usd = oldSaldo.getUsd().add(oper.getUsd());
    	if (oldOper.getTipo().toString().equals(Tipo.EFECTIVO.toString())) {
	    	this.efPen = oldSaldo.getEfPen().add(oper.getPen());
	    	this.efUsd = oldSaldo.getEfUsd().add(oper.getUsd());
    	} else {
    		this.efPen = oldSaldo.getEfPen();
	    	this.efUsd = oldSaldo.getEfUsd();
    	}
/*    	if (oper.getTipo().toString().equals(Tipo.EFECTIVO.toString())) {
	    	this.efPen = this.efPen.add(oper.getPen());
	    	this.efUsd = this.efUsd.add(oper.getUsd());
    	}*/
    	this.fecha = oldOper.getFecha();
    	this.categoriaCuenta = oldSaldo.getCategoriaCuenta();
    	this.operacion = oper;
    }
    
    
    
    
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

	public Operacion getOperacion() {
		return operacion;
	}

	public void setOperacion(Operacion operacion) {
		this.operacion = operacion;
	}

	@Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Saldo)) {
            return false;
        }
        Saldo other = (Saldo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    public CategoriaCuenta getCategoriaCuenta() {
		return categoriaCuenta;
	}

	public void setCategoriaCuenta(CategoriaCuenta categoriaCuenta) {
		this.categoriaCuenta = categoriaCuenta;
	}

	public BigDecimal getEfPen() {
		return efPen;
	}

	public void setEfPen(BigDecimal efPen) {
		this.efPen = efPen;
	}

	public BigDecimal getEfUsd() {
		return efUsd;
	}

	public void setEfUsd(BigDecimal efUsd) {
		this.efUsd = efUsd;
	}

	@Override
	public String toString() {
		return "Saldo [id=" + id + ", categoriaCuenta=" + categoriaCuenta
				+ ", fecha=" + fecha + ", pen=" + pen + ", usd=" + usd
				+ ", efPen=" + efPen + ", efUsd=" + efUsd + ", operacion="
				+ operacion + "]";
	}
    
}
