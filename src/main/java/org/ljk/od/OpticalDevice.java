package org.ljk.od;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class OpticalDevice {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;
    private String name;
    private String type;
    private String firstCategory;
    private String secondCategory;
    private Double price;
    private String characteristics;
    private String describe;
    private String factory;

    public OpticalDevice() {
    }

    public OpticalDevice(String name, String type, String firstCategory, String secondCategory,
                         Double price, String characteristics, String describe, String factory) {
        this.name = name;
        this.type = type;
        this.firstCategory = firstCategory;
        this.secondCategory = secondCategory;
        this.price = price;
        this.characteristics = characteristics;
        this.describe = describe;
        this.factory = factory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFirstCategory() {
        return firstCategory;
    }

    public void setFirstCategory(String firstCategory) {
        this.firstCategory = firstCategory;
    }

    public String getSecondCategory() {
        return secondCategory;
    }

    public void setSecondCategory(String secondCategory) {
        this.secondCategory = secondCategory;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    @Override
    public String toString() {
        return "OpticalDevice{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", firstCategory='" + firstCategory + '\'' +
                ", secondCategory='" + secondCategory + '\'' +
                ", price=" + price +
                ", characteristics='" + characteristics + '\'' +
                ", describe='" + describe + '\'' +
                ", factory='" + factory + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpticalDevice that = (OpticalDevice) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(firstCategory, that.firstCategory) &&
                Objects.equals(secondCategory, that.secondCategory) &&
                Objects.equals(price, that.price) &&
                Objects.equals(characteristics, that.characteristics) &&
                Objects.equals(describe, that.describe) &&
                Objects.equals(factory, that.factory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, firstCategory, secondCategory, price, characteristics, describe, factory);
    }
}
