package com.eatcloud.adminservice.ports;

public interface ManagerAdminPort {
    UUID upsert(ManagerUpsertCommand cmd);
}