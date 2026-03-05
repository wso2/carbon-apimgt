# Environment Tables and Gateway Types

## How other gateways (Synapse, APK, etc.) manage environments

- **Storage:** Environments are **stored** in `AM_GATEWAY_ENVIRONMENT` (and related `AM_GW_VHOST`, gateway visibility permissions).
- **Registration:** Admin creates an environment via **Admin REST API** `POST /environments` → `EnvironmentsApiServiceImpl.environmentsPost` → `APIAdmin.addEnvironment(organization, env)` → `ApiMgtDAO.addEnvironment(tenantDomain, environment)`. The DAO generates a new UUID and inserts a row.
- **Retrieval:** `APIUtil.getEnvironments(organization)` builds the map from:
  1. `getReadOnlyEnvironments()` (api-manager.xml)
  2. `ApiMgtDAO.getInstance().getAllEnvironments(organization)` — reads from `AM_GATEWAY_ENVIRONMENT` by ORGANIZATION
  3. `addPlatformGatewaysToEnvironmentsMap(...)` — merges platform gateways from a different source (see below).

So for Synapse/APK, **add = insert into AM_GATEWAY_ENVIRONMENT**; **get = read from AM_GATEWAY_ENVIRONMENT** (plus read-only). Lookup by UUID (e.g. `getEnvironmentWithoutPropertyMasking`) uses `AM_GATEWAY_ENVIRONMENT` (and read-only).

---

## How API-platform gateways are currently managed

- **Storage:** Platform gateways use **separate tables**: `AM_PLATFORM_GATEWAY`, `AM_PLATFORM_GATEWAY_TOKEN`, and `AM_GW_INSTANCES` (for instance/label registration). They are **not** stored in `AM_GATEWAY_ENVIRONMENT`.
- **Registration:** Create via **PlatformGatewayService** (e.g. admin flow) → `PlatformGatewayServiceImpl.createGateway` → `PlatformGatewayDAO.createGatewayWithTokenAndGatewayInstance`. Only the platform gateway tables are written; no row is inserted into `AM_GATEWAY_ENVIRONMENT`.
- **Retrieval:** Platform gateways are **resolved on-the-fly** when getting environments:
  - `APIUtil.getEnvironments(organization)` calls `addPlatformGatewaysToEnvironmentsMap(allEnvironments, organization)`, which uses `PlatformGatewayService.listGatewaysByOrganization(organization)` and for each platform gateway builds an `Environment` and puts it into the map by name. So they appear in the same “environments” map but come from **PlatformGatewayDAO** / `AM_PLATFORM_GATEWAY`, not from `AM_GATEWAY_ENVIRONMENT`.
- **Consequence:** Any code that looks up an environment **by UUID** in `AM_GATEWAY_ENVIRONMENT` (e.g. `apiMgtDAO.getEnvironment(org, uuid)`, `getEnvironmentWithoutPropertyMasking`) does **not** find platform gateways. A fallback was added to resolve by UUID from the in-memory map returned by `getEnvironments(organization)` so that deployment/Store flows work.

---

## Requirement: same approach for API-platform gateways

Use the **same approach as other gateways**: **store** platform gateway environments in **AM_GATEWAY_ENVIRONMENT** when they are created/registered, so that:

- One source of truth for “environments” (including platform gateways).
- Lookup by UUID, deployment, and Store all use the same tables and code paths.
- No need for a special fallback that resolves platform gateways from a different source.

### Implementation outline

1. **On platform gateway create**  
   When a platform gateway is created (`PlatformGatewayServiceImpl.createGateway` / `PlatformGatewayDAO.createGatewayWithTokenAndGatewayInstance`), **also** insert a corresponding row into `AM_GATEWAY_ENVIRONMENT` (and `AM_GW_VHOST`) with:
   - **UUID** = platform gateway id (so existing references, e.g. revision deployments, keep working).
   - NAME, DISPLAY_NAME, DESCRIPTION, ORGANIZATION, GATEWAY_TYPE = `api-platform`, PROVIDER, CONFIGURATION (e.g. {}), ENV_MODE, vhosts derived from the platform gateway.

2. **Use existing add path**  
   Reuse the same insert path as other gateways: build an `Environment` from the platform gateway, set `environment.setUuid(gatewayId)` before calling the existing environment add logic. The DAO must **use the provided UUID** when it is already set (instead of always generating a new one) so the platform gateway id is stored as the environment UUID.

3. **On platform gateway delete**  
   When a platform gateway is deleted, **also** delete the corresponding row from `AM_GATEWAY_ENVIRONMENT` (by UUID = gateway id).

4. **Getting environments**  
   `APIUtil.getEnvironments(organization)` already includes `getAllEnvironments(organization)` from the DB; once platform gateways are in `AM_GATEWAY_ENVIRONMENT`, they will appear there. `addPlatformGatewaysToEnvironmentsMap` can remain for backward compatibility (e.g. to avoid duplicates by name) or be simplified so it does not add entries that already exist in the map from the DB.

5. **Fallback in getEnvironmentWithoutPropertyMasking**  
   The fallback that resolves by UUID from the in-memory map can remain as a safety net for **existing** platform gateways (created before this change) that are not yet in `AM_GATEWAY_ENVIRONMENT`. New platform gateways created after this change will be in the DB and resolved normally.

6. **On platform gateway delete**  
   When a platform gateway is deleted (e.g. from admin or a future delete API), the corresponding row in `AM_GATEWAY_ENVIRONMENT` should also be removed (e.g. call `APIAdmin.deleteEnvironment(organization, gatewayId)`) so the two stores stay in sync. If no delete flow exists yet, add it when the platform gateway delete feature is implemented.

---

## Summary table

| Aspect              | Other gateways (Synapse, APK)     | API-platform (current)              | API-platform (target)                    |
|---------------------|------------------------------------|-------------------------------------|------------------------------------------|
| Tables              | AM_GATEWAY_ENVIRONMENT, AM_GW_VHOST | AM_PLATFORM_GATEWAY, etc.           | **Both**: platform tables + AM_GATEWAY_ENVIRONMENT |
| When stored         | On add via Admin API               | Not in AM_GATEWAY_ENVIRONMENT       | **On platform gateway create**           |
| When retrieved      | From DB in getEnvironments         | On-the-fly merge in getEnvironments | **From DB** in getEnvironments (same as others) |
| Lookup by UUID      | DAO getEnvironment(org, uuid)       | Fallback from in-memory map         | **DAO getEnvironment(org, uuid)**        |
