package com.enablex.demoenablex.model;

public class UserModel {

    /**
     * name : moderator
     * user_ref : 2236
     * role : moderator
     * permissions : {"publish":true,"subscribe":true,"record":true,"stats":true,"controlhandlers":true}
     * clientId : bdacbfe9-0db9-4577-b0b0-d2538614a640
     */

    private String name;
    private String user_ref;
    private String role;
    private PermissionsBean permissions;
    private String clientId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_ref() {
        return user_ref;
    }

    public void setUser_ref(String user_ref) {
        this.user_ref = user_ref;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public PermissionsBean getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionsBean permissions) {
        this.permissions = permissions;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public static class PermissionsBean {
        /**
         * publish : true
         * subscribe : true
         * record : true
         * stats : true
         * controlhandlers : true
         */

        private boolean publish;
        private boolean subscribe;
        private boolean record;
        private boolean stats;
        private boolean controlhandlers;

        public boolean isPublish() {
            return publish;
        }

        public void setPublish(boolean publish) {
            this.publish = publish;
        }

        public boolean isSubscribe() {
            return subscribe;
        }

        public void setSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
        }

        public boolean isRecord() {
            return record;
        }

        public void setRecord(boolean record) {
            this.record = record;
        }

        public boolean isStats() {
            return stats;
        }

        public void setStats(boolean stats) {
            this.stats = stats;
        }

        public boolean isControlhandlers() {
            return controlhandlers;
        }

        public void setControlhandlers(boolean controlhandlers) {
            this.controlhandlers = controlhandlers;
        }
    }
}
