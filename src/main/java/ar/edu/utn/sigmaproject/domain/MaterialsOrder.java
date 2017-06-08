package ar.edu.utn.sigmaproject.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class MaterialsOrder implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "materialsOrder", targetEntity = MaterialsOrderDetail.class)
	private List<MaterialsOrderDetail> details = new ArrayList<>();
	
	@ManyToOne
	private ProductionPlan productionPlan = null;
	
	private Integer number = 0;
	private Date date = null;
	private Date dateReception = null;
	private String receiptNumber = "";
	
	public MaterialsOrder() {
		
	}
	
	public MaterialsOrder(Integer number, Date date) {
		this.number = number;
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<MaterialsOrderDetail> getDetails() {
		return details;
	}

	public void setDetails(List<MaterialsOrderDetail> details) {
		this.details = details;
	}

	public Date getDateReception() {
		return dateReception;
	}

	public void setDateReception(Date dateReception) {
		this.dateReception = dateReception;
	}

	public ProductionPlan getProductionPlan() {
		return productionPlan;
	}

	public void setProductionPlan(ProductionPlan productionPlan) {
		this.productionPlan = productionPlan;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}
	
//	public String isTotallyReceived() {
//		for(MaterialsOrderDetail each : details) {
//			if(!each.getQuantity().equals(each.getQuantityReceived())) {
//				return "NO";
//			}
//		}
//		return "SI";
//	}
	
	public boolean isTotallyReceived() {
		for(MaterialsOrderDetail each : details) {
			if(each.getQuantity().compareTo(each.getQuantityReceived()) > 0) {// cantidad necesaria mayor que cantidad recibida
				return false;
			}
		}
		return true;
	}
}
