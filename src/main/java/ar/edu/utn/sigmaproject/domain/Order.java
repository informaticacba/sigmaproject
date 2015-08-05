package ar.edu.utn.sigmaproject.domain;

import java.io.Serializable;
import java.util.Date;

public class Order implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    
    Integer id;
    Integer idClient;
    String name;
    Date date;

    public Order(Integer id, Integer idClient, String name, Date date) {
        this.id = id;
        this.idClient = idClient;
        this.name = name;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getIdClient() {
        return idClient;
    }
    
    public void setIdClient(Integer idClient) {
        this.idClient = idClient;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Order other = (Order) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
    public static Order clone(Order order){
        try {
            return (Order)order.clone();
        } catch (CloneNotSupportedException e) {
            //not possible
        }
        return null;
    }
}