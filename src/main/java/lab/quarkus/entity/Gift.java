package lab.quarkus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Cacheable;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "gifts")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Gift extends PanacheEntity {
    
    @Column(nullable = false)
    public String name;
    
    @Column(nullable = false)
    public String description;
    
    @Column(nullable = false)
    public Double price;
    
    @Column(nullable = false)
    public String category;
    
    public Gift() {}
    
    public Gift(String name, String description, Double price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }
}