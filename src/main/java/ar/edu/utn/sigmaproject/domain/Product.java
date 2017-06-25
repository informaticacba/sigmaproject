package ar.edu.utn.sigmaproject.domain;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.xml.datatype.Duration;

@Entity
@Indexed
@Analyzer(definition = "edge_ngram")
public class Product extends Item implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	@OneToMany(orphanRemoval = true)
	List<Piece> pieces = new ArrayList<>();

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "product", targetEntity = ProductMaterial.class)
	List<ProductMaterial> materials = new ArrayList<>();

	@Lob
	byte[] imageData = new byte[0];

	@Field
	String name = "";

	@Field
	String details = "";

	@Field
	String code = "";

	Integer stock = 0;
	Integer stockMin = 0;
	Integer stockRepo = 0;

	@ManyToOne
	ProductCategory category;

	BigDecimal price = BigDecimal.ZERO;
	boolean isClone;

	public Product() {

	}

	public Product(String code , String name, String details, ProductCategory category, BigDecimal price) {
		this.name = name;
		this.details = details;
		this.category = category;
		this.code = code;
		this.price = price;
	}
	
	public Duration getDurationTotal() {
		Duration durationTotal = null;
		for(Piece each : pieces) {
			if(durationTotal == null) {
				durationTotal = each.getDurationTotal();
			} else {
				durationTotal = durationTotal.add(each.getDurationTotal());
			}
			
		}
		return durationTotal;
	}

	@Override
	public String getDescription() {
		return getName();
	}

	public List<Piece> getPieces() {
		return pieces;
	}

	public void setPieces(List<Piece> pieces) {
		this.pieces = pieces;
	}

	public List<ProductMaterial> getSupplies() {
		List<ProductMaterial> supplies = new ArrayList<>();
		for(ProductMaterial each : materials) {
			if(each.getType() == MaterialType.Supply) {
				supplies.add(each);
			}
		}
		return supplies;
	}

	public List<ProductMaterial> getRawMaterials() {
		List<ProductMaterial> rawMaterials = new ArrayList<>();
		for(ProductMaterial each : materials) {
			if(each.getType() == MaterialType.Wood) {
				rawMaterials.add(each);
			}
		}
		return rawMaterials;
	}

	public List<ProductMaterial> getMaterials() {
		return materials;
	}

	public void setMaterials(List<ProductMaterial> materials) {
		this.materials = materials;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Integer getStockMin() {
		return stockMin;
	}

	public void setStockMin(Integer stockMin) {
		this.stockMin = stockMin;
	}

	public Integer getStockRepo() {
		return stockRepo;
	}

	public void setStockRepo(Integer stockRepo) {
		this.stockRepo = stockRepo;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public String getName() {
		return name;
	}

	public String getDetails() {
		return details;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getCode() {
		return code;
	}

	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean isClone() {
		return isClone;
	}

	public void setClone(boolean isClone) {
		this.isClone = isClone;
	}

	@Override
	public List<MaterialReserved> getMaterialReservedList() {
		// TODO Auto-generated method stub
		return null;
	}
}
