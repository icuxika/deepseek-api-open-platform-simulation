package com.deepseek.apiplatform.security;

import java.security.Principal;

public class UserPrincipal implements Principal {
    private Long id;
    
    public UserPrincipal() {}
    
    public UserPrincipal(Long id) {
        this.id = id;
    }
    
    @Override
    public String getName() {
        return String.valueOf(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
