package com.silkroad.market.dto.category;

/**
 * Response shape for category endpoints. hasChildren is computed
 * per-request (rather than stored) so it always reflects the current
 * state of the category tree.
 */
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final Long parentId;
    private final boolean hasChildren;

    public CategoryResponse(Long id, String name, Long parentId, boolean hasChildren) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.hasChildren = hasChildren;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }
}
