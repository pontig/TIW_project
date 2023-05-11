package it.polimi.tiw.beans;

import java.util.ArrayList;
import java.util.List;


public class Category {
    private int id;
    private String name;
    private Integer parentID;
    private boolean enlighted = false;
    private List<Category> children = new ArrayList<>();
    private String hierarchy;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Category> getChildren() {
        return children;
    }
    public void setChildren(List<Category> children) {
        this.children = children;
    }
    
    public Integer getParentId() {
        return this.parentID;
    }
    public void setParentId(Integer id) {
    	this.parentID = id;
    }
    
    public boolean getEnlighted() {
    	return this.enlighted;
    }
    public void enlight() {
    	this.enlighted = true;
    }
    
    public String getHierarchy() {
    	return this.hierarchy;
    }
    public void setHierarchy(String h) {
    	this.hierarchy = h;
    }
    
}
