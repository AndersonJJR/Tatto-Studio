package com.studio.tattoo.model.user;

public enum Role {
    ROLE_CLIENT,
    ROLE_ARTIST;

    public String getRoleNameWithoutPrefix() {
        return this.name().replace("ROLE_", "");
    }
}
