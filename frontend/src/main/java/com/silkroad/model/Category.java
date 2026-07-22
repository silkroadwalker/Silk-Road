package com.silkroad.model;

public class Category {
    private Long id;
    private String name;
    private Long parentId;
    private boolean hasChildren;

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getParentId() { return parentId; }
    public boolean isHasChildren() { return hasChildren; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }

    @Override
    public String toString() { return name; }
}