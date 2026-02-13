package com.deepseek.apiplatform.dto;

import java.util.List;

public class ModelListResponse {
    private String object = "list";
    private List<ModelResponse> data;

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    
    public List<ModelResponse> getData() { return data; }
    public void setData(List<ModelResponse> data) { this.data = data; }
}
