/*
3 * To change this template, choose Tools | Templates
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * @author pol
 */
@Entity
@Table(name="operacion")
@NamedQueries({
@NamedQuery(name="Operacion.getNewerForCuenta", query="SELECT o FROM Operacion o WHERE o.cuenta.id= ?1 AND o.fecha >= ?2 ORDER BY o.fecha ASC, o.id ASC"),
@NamedQuery(name="Operacion.getLastForCuenta", query="SELECT o FROM Operacion o WHERE o.cuenta.id= ?1 AND o.fecha < ?2 ORDER BY o.fecha DESC, o.id DESC"),
@NamedQuery(name="Operacion.getByOperacionDetalle", query="SELECT o FROM Operacion o WHERE o.operacionDetalle.id= ?1")
})
public class Operacion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Usuario usuario;
    
    @ManyToOne
    public Cuenta cuenta;
    
    @ManyToOne
    private Banco banco;
    
	private String descripcion = "";
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fecha = new Date(System.currentTimeMillis());
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fechaDeCobro = null;
    
    @Column(name="PEN",columnDefinition="DECIMAL(38,2)")
    private BigDecimal pen = new BigDecimal(0.00);
    
    @Column(name="USD",columnDefinition="DECIMAL(38,2)")
    private BigDecimal usd = new BigDecimal(0.00);

    @Column(name="SALDOPEN",columnDefinition="DECIMAL(38,2)")
    private BigDecimal saldoPen = new BigDecimal(0.00);
    
    @Column(name="SALDOUSD",columnDefinition="DECIMAL(38,2)")
    private BigDecimal saldoUsd = new BigDecimal(0.00);    
    
    private Boolean isCargo = true;
    
    private Boolean isPen = true;
    
    public enum Tipo{
        EFECTIVO,
    	TRANSFERENCIA,
        CHEQUE,
        NOTA_CARGO,
        NOTA_ABONO,
        REGULARIZACION,
        ENTR_EFECTIVO,
        SALDO_AL_2010_01_01,
        SALDO_INICIAL,
        INTERNA;
    }
    
    //@Enumerated(value = EnumType.STRING)
    private String tipo = Tipo.EFECTIVO.toString();
     
    private String firma = "";

    @ManyToOne
    private Beneficiario beneficiario;
    
    @ManyToOne
    private TipoDocumento tipoDocumento;
    
    private String docNumero = "";
    @ManyToOne
    private CentroCosto centroCosto;
    
    @ManyToOne
    private LugarGasto lugarGasto;

    @ManyToOne
    private CuentaContable cuentaContable;
    
    @ManyToOne
    private RubroInstitucional rubroInstitucional;
    
    @ManyToOne
    private RubroProyecto rubroProyecto;
    
    private String dni = "";
    
	private String chequeNumero = "";
    
    private Boolean isCorrection = false;
    
    @OneToOne
    private Operacion operacionCorrected;
    
    @OneToOne
    private Operacion operacionDetalle;
    
    public Operacion clone() {
    	Operacion op = new Operacion();
    	op.setBanco(this.getBanco());
    	op.setChequeNumero(this.getChequeNumero());
    	op.setCuenta(this.getCuenta());
    	op.setDescripcion(this.getDescripcion());
    	op.setDni(this.getDni());
    	op.setFecha(this.getFecha());
    	op.setFechaDeCobro(this.getFechaDeCobro());
    	op.setFirma(this.getFirma());
    	op.setIsCargo(this.getIsCargo());
    	op.setIsCorrection(this.getIsCorrection());
    	op.setIsPen(this.getIsPen());
    	op.setPen(this.getPen());
    	op.setOperacionCorrected(this.getOperacionCorrected());
    	op.setOperacionDetalle(this.getOperacionDetalle());
    	op.setSaldoPen(this.getSaldoPen());
    	op.setSaldoUsd(this.getSaldoUsd());
    	op.setTipo(this.getTipo());
    	op.setUsd(this.getUsd());
    	op.setUsuario(this.getUsuario());
    	op.setBeneficiario(this.getBeneficiario());
    	op.setTipoDocumento(this.getTipoDocumento());
    	op.setDocNumero(this.getDocNumero());
    	op.setCentroCosto(this.getCentroCosto());
    	op.setLugarGasto(this.getLugarGasto());
    	return op;
    }

    public Operacion giveCajaOperacion(Cuenta cajaCuenta) {
    	Operacion cajaOper = this.clone();
    	cajaOper.setOperacionDetalle(this);
    	cajaOper.setCuenta(cajaCuenta);
    	cajaOper.setSaldoPen(cajaCuenta.getPen());
    	cajaOper.setSaldoUsd(cajaCuenta.getUsd());
    	return cajaOper;
    }
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Cuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(Cuenta cuenta) {
		this.cuenta = cuenta;
	}

    public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

    public Date getFechaDeCobro() {
		return fechaDeCobro;
	}

	public void setFechaDeCobro(Date fechaDeCobro) {
		this.fechaDeCobro = fechaDeCobro;
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

	public BigDecimal getSaldoPen() {
		return saldoPen;
	}

	public void setSaldoPen(BigDecimal saldoPen) {
		this.saldoPen = saldoPen;
	}

	public BigDecimal getSaldoUsd() {
		return saldoUsd;
	}

	public void setSaldoUsd(BigDecimal saldoUsd) {
		this.saldoUsd = saldoUsd;
	}

	public String getTipo() {
		return tipo;
	}
	
	/*public Tipo giveTipoTipo() {
		if (tipo.equals("TRANSFERENCIA")) return Tipo.TRANSFERENCIA;
		else if (tipo.equals("CHEQUE")) return Tipo.CHEQUE;
		else if (tipo.equals("SALDO_INICIAL")) return Tipo.SALDO_INICIAL;
		else return Tipo.EFECTIVO;
	}*/

	public void setTipo(Tipo tipo) {
		this.tipo = tipo.toString();
	}

	public void setTipo(String sTipo) {
		if (sTipo.equals("TRANSFERENCIA")) this.tipo = Tipo.TRANSFERENCIA.toString();
		else if (sTipo.equals("CHEQUE")) this.tipo = Tipo.CHEQUE.toString();
		else if (sTipo.equals("SALDO_INICIAL")) this.tipo = Tipo.SALDO_INICIAL.toString();
		else this.tipo = Tipo.EFECTIVO.toString();
	}
	
	public String getFirma() {
		return firma;
	}

	public void setFirma(String firma) {
		this.firma = firma;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}
	
	public Boolean getIsCargo() {
		return isCargo;
	}

	public void setIsCargo(Boolean isCargo) {
		this.isCargo = isCargo;
	}

	public Boolean getIsPen() {
		return isPen;
	}

	public void setIsPen(Boolean isPen) {
		this.isPen = isPen;
	}

	public String getChequeNumero() {
		return chequeNumero;
	}

	public void setChequeNumero(String chequeNumero) {
		this.chequeNumero = chequeNumero;
	}

	public Boolean getIsCorrection() {
		return isCorrection;
	}

	public void setIsCorrection(Boolean isCorrection) {
		this.isCorrection = isCorrection;
	}

	public Operacion getOperacionCorrected() {
		return operacionCorrected;
	}

	public void setOperacionCorrected(Operacion operacionCorrected) {
		this.operacionCorrected = operacionCorrected;
	}

	public Operacion getOperacionDetalle() {
		return operacionDetalle;
	}

	public void setOperacionDetalle(Operacion operacionDetalle) {
		this.operacionDetalle = operacionDetalle;
	}

	public Beneficiario getBeneficiario() {
		return beneficiario;
	}


	public void setBeneficiario(Beneficiario beneficiario) {
		this.beneficiario = beneficiario;
	}


	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}


	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}


	public CentroCosto getCentroCosto() {
		return centroCosto;
	}


	public CuentaContable getCuentaContable() {
		return cuentaContable;
	}

	public void setCuentaContable(CuentaContable cuentaContable) {
		this.cuentaContable = cuentaContable;
	}

	public RubroInstitucional getRubroInstitucional() {
		return rubroInstitucional;
	}

	public void setRubroInstitucional(RubroInstitucional rubroInstitucional) {
		this.rubroInstitucional = rubroInstitucional;
	}

	public RubroProyecto getRubroProyecto() {
		return rubroProyecto;
	}

	public void setRubroProyecto(RubroProyecto rubroProyecto) {
		this.rubroProyecto = rubroProyecto;
	}

	public void setCentroCosto(CentroCosto centroCosto) {
		this.centroCosto = centroCosto;
	}


	public String getDocNumero() {
		return docNumero;
	}


	public void setDocNumero(String docNumero) {
		this.docNumero = docNumero;
	}

	public LugarGasto getLugarGasto() {
		return lugarGasto;
	}

	public void setLugarGasto(LugarGasto lugarGasto) {
		this.lugarGasto = lugarGasto;
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
        if (!(object instanceof Operacion)) {
            return false;
        }
        Operacion other = (Operacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
	public String toString() {
		return "Operacion ["
				+ (id != null ? "id=" + id + ", " : "")
				+ (usuario != null ? "usuario=" + usuario + ", " : "")
				+ (cuenta != null ? "cuenta=" + cuenta + ", " : "")
				+ (banco != null ? "banco=" + banco + ", " : "")
				+ (descripcion != null ? "descripcion=" + descripcion + ", "
						: "")
				+ (fecha != null ? "fecha=" + fecha + ", " : "")
				+ (fechaDeCobro != null ? "fechaDeCobro=" + fechaDeCobro + ", "
						: "")
				+ (pen != null ? "pen=" + pen + ", " : "")
				+ (usd != null ? "usd=" + usd + ", " : "")
				+ (saldoPen != null ? "saldoPen=" + saldoPen + ", " : "")
				+ (saldoUsd != null ? "saldoUsd=" + saldoUsd + ", " : "")
				+ (isCargo != null ? "isCargo=" + isCargo + ", " : "")
				+ (isPen != null ? "isPen=" + isPen + ", " : "")
				+ (tipo != null ? "tipo=" + tipo + ", " : "")
				+ (firma != null ? "firma=" + firma + ", " : "")
				+ (beneficiario != null ? "beneficiario=" + beneficiario + ", "
						: "")
				+ (tipoDocumento != null ? "tipoDocumento=" + tipoDocumento
						+ ", " : "")
				+ (docNumero != null ? "docNumero=" + docNumero + ", " : "")
				+ (centroCosto != null ? "centroCosto=" + centroCosto + ", "
						: "")
				+ (lugarGasto != null ? "lugarGasto=" + lugarGasto + ", " : "")
				+ (cuentaContable != null ? "cuentaContable=" + cuentaContable + ", " : "")
				+ (rubroProyecto!= null ? "rubroProyecto=" + rubroProyecto+ ", " : "")
				+ (rubroInstitucional != null ? "rubroInstitucional=" + rubroInstitucional+ ", " : "")
				+ (dni != null ? "dni=" + dni + ", " : "")
				+ (chequeNumero != null ? "chequeNumero=" + chequeNumero + ", "
						: "")
				+ (isCorrection != null ? "isCorrection=" + isCorrection + ", "
						: "")
				+ (operacionCorrected != null ? "operacionCorrected="
						+ operacionCorrected + ", " : "")
				+ (operacionDetalle != null ? "operacionDetalle="
						+ operacionDetalle : "") + "]";
	}
}
