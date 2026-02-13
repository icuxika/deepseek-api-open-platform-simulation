package com.deepseek.apiplatform.dto;

import java.util.List;

public class ModelResponse {
    private String id;
    private String object = "model";
    private Long created;
    private String ownedBy;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    
    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }
    
    public String getOwnedBy() { return ownedBy; }
    public void setOwnedBy(String ownedBy) { this.ownedBy = ownedBy; }
}
