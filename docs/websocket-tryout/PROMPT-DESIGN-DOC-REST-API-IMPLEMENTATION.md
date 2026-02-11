# Prompt: Generate Design Documentation — REST API Implementation (Platform Gateway Registration)

Use this prompt in a **new chat** to generate the **REST API implementation** section of a design/contribution document for the **Platform Gateway Registration** feature in on-prem WSO2 API Manager (carbon-apimgt). The document should be suitable for a contribution (e.g. PR description, design doc, or wiki).

---

## Instructions for the doc author (new chat)

**Goal:** Write the **REST API implementation** part of a design document that describes how Platform Gateway Registration is implemented in on-prem APIM. Focus on:

1. **Overview** — What the feature does: register API Platform / self-hosted gateways via Admin REST API; generate a registration token (returned once); gateway uses the token to connect to the control plane (WebSocket). One short paragraph.
2. **Admin REST API contract**
   - Base path: `/api/am/admin/v4`
   - **POST /gateways** — Create gateway. Request body: name (required), displayName (required), description (optional), vhost (required), isCritical (optional), functionalityType (required). Response 201: gateway object plus **registrationToken** (returned only once). Errors: 400 validation, 404 org not found, 409 gateway name already exists in org.
   - **GET /gateways** — List gateways for the organization. Response 200: count + list of gateways (no tokens). Security: `apim:admin` scope.
3. **Implementation components** (reference these paths in carbon-apimgt repo):
   - **OpenAPI:** `components/apimgt/org.wso2.carbon.apimgt.rest.api.admin.v1/src/main/resources/admin-api.yaml` — paths `/gateways` (GET, POST), schemas `CreatePlatformGatewayRequest`, `PlatformGateway`, `PlatformGatewayList`.
   - **Generated API & DTOs:** `GatewaysApi.java`, `GatewaysApiService.java`, `CreatePlatformGatewayRequestDTO`, `PlatformGatewayDTO`, `PlatformGatewayListDTO` under `org.wso2.carbon.apimgt.rest.api.admin.v1` and `.dto`.
   - **Service implementation:** `org.wso2.carbon.apimgt.rest.api.admin.v1.impl.GatewaysApiServiceImpl` — validates org via `RestApiUtil.getValidatedOrganization(messageContext)`, validates body, checks name uniqueness, generates token/salt/hash, persists gateway and token in one transaction, returns gateway + registrationToken on create.
   - **Persistence:** `PlatformGatewayDAO` (org.wso2.carbon.apimgt.impl.dao), tables `AM_PLATFORM_GATEWAY` and `AM_PLATFORM_GATEWAY_TOKEN`. SQL constants in `SQLConstants.PlatformGatewaySQLConstants`. DDL in `features/apimgt/org.wso2.carbon.apimgt.core.feature/src/main/resources/sql/` (mysql.sql, postgresql.sql, h2.sql).
   - **Token handling:** `PlatformGatewayTokenUtil` (org.wso2.carbon.apimgt.impl.utils) — generateToken (32 bytes SecureRandom, Base64 URL no padding), generateSalt, hashToken (SHA-256(plain+salt), hex), verifyToken(plainToken) for WebSocket auth. Token stored hashed only; plain token returned only in POST response.
4. **Security** — Admin API protected by OAuth2; scope `apim:admin`. Organization resolved from request context; gateways scoped by organization.
5. **Error handling** — Validation errors 400 (e.g. missing required fields); 409 for duplicate gateway name (ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS); 404 if org not found.
6. **Database design: relations and why not existing gateway tables**
   - **New tables and relations:** Describe the two tables and their relationship.
     - **AM_PLATFORM_GATEWAY:** Primary table for a *logical* platform gateway registration. Columns: ID (UUID, PK), ORGANIZATION_ID, NAME, DISPLAY_NAME, DESCRIPTION, VHOST, IS_CRITICAL, FUNCTIONALITY_TYPE, IS_ACTIVE, CREATED_AT, UPDATED_AT. Unique constraint on (NAME, ORGANIZATION_ID). One row per registered gateway (long-lived identity).
     - **AM_PLATFORM_GATEWAY_TOKEN:** Stores registration tokens (hashed). Columns: ID (UUID, PK), GATEWAY_ID (FK → AM_PLATFORM_GATEWAY.ID), TOKEN_HASH, SALT, STATUS ('active'/'revoked'), CREATED_AT, REVOKED_AT. Relation: one gateway can have one or more tokens (e.g. after rotation); verification joins token table with gateway table to resolve gateway by token.
   - **Why not reuse existing gateway tables:** Explain clearly:
     - **Existing tables (AM_GW_INSTANCES, AM_GW_INSTANCE_ENV_MAPPING, AM_GW_REVISION_DEPLOYMENT)** serve a *different purpose*: they track *runtime gateway instances* (heartbeats, LAST_UPDATED, GW_PROPERTIES) and *which API revisions are deployed to which instance*. They are used for deployment topology and instance liveness, not for "register a gateway and issue a registration token."
     - **No place for token storage:** Existing schema has no columns for registration token hash, salt, or status; adding them would mix two concerns (instance heartbeat vs. registration identity) and complicate existing GatewayManagementDAO and deployment logic.
     - **Different lifecycle:** Platform gateway registration is a long-lived *identity* (name, vhost, org); AM_GW_INSTANCES rows are tied to *ephemeral* runtime instances (heartbeat-based, retention policies). Reusing would blur identity vs. instance.
     - **Different access patterns:** Admin API needs "list gateways by org," "create gateway with unique name per org," "verify token and return gateway"; existing tables are queried by GATEWAY_UUID + ORGANIZATION for deployment and heartbeat updates. Keeping them separate avoids breaking existing behaviour and keeps the new feature's schema clear.
   - Use a short diagram or bullet list for the relation: AM_PLATFORM_GATEWAY 1 — * AM_PLATFORM_GATEWAY_TOKEN (FK GATEWAY_ID).
7. **Optional short subsection:** How the registration token is used after create — gateway sends it as `api-key` header when connecting to the control plane WebSocket (`/internal/data/v1/ws/gateways/connect`); control plane verifies via `PlatformGatewayTokenUtil.verifyToken` and associates the connection with the gateway. (Do not expand the WebSocket implementation; one short paragraph is enough.)
8. **Tryout** — Example curl commands: (1) Obtain admin token; (2) POST create gateway and show sample request/response with registrationToken; (3) GET list gateways. Base URL example: `https://localhost:9443`.

**Format:** Clear headings, short paragraphs, bullet lists where helpful. Use code blocks for API examples and curl. Do not invent file paths; use the ones listed above. Write in a neutral, documentation style suitable for a contribution or design doc.

---

## Reference: Key file paths (carbon-apimgt)

| Component | Path |
|-----------|------|
| Admin API OpenAPI | `components/apimgt/org.wso2.carbon.apimgt.rest.api.admin.v1/src/main/resources/admin-api.yaml` |
| GatewaysApi (JAX-RS) | `components/apimgt/org.wso2.carbon.apimgt.rest.api.admin.v1/src/gen/java/.../GatewaysApi.java` |
| GatewaysApiServiceImpl | `components/apimgt/org.wso2.carbon.apimgt.rest.api.admin.v1/src/main/java/.../impl/GatewaysApiServiceImpl.java` |
| PlatformGatewayDAO | `components/apimgt/org.wso2.carbon.apimgt.impl/src/main/java/.../dao/PlatformGatewayDAO.java` |
| PlatformGatewayTokenUtil | `components/apimgt/org.wso2.carbon.apimgt.impl/src/main/java/.../utils/PlatformGatewayTokenUtil.java` |
| SQL constants | `components/apimgt/org.wso2.carbon.apimgt.impl/src/main/java/.../dao/constants/SQLConstants.java` (inner class PlatformGatewaySQLConstants) |
| DDL (MySQL) | `features/apimgt/org.wso2.carbon.apimgt.core.feature/src/main/resources/sql/mysql.sql` (AM_PLATFORM_GATEWAY, AM_PLATFORM_GATEWAY_TOKEN) |
| Exception code | `components/apimgt/org.wso2.carbon.apimgt.api/.../ExceptionCodes.java` — PLATFORM_GATEWAY_NAME_ALREADY_EXISTS (409) |
| RestApiConstants | `components/apimgt/org.wso2.carbon.apimgt.rest.api.common/.../RestApiConstants.java` — RESOURCE_PATH_PLATFORM_GATEWAYS = "/gateways" |

---

## Reference: Existing vs new gateway tables

| Purpose | Existing tables | New tables |
|--------|------------------|------------|
| What they represent | Runtime gateway *instances* (heartbeat, env label, deployment state) | Logical platform gateway *registration* (name, vhost, org) + registration *tokens* (hashed) |
| Tables | AM_GW_INSTANCES (GATEWAY_ID, GATEWAY_UUID, ORGANIZATION, LAST_UPDATED, GW_PROPERTIES), AM_GW_INSTANCE_ENV_MAPPING, AM_GW_REVISION_DEPLOYMENT | AM_PLATFORM_GATEWAY (ID, ORGANIZATION_ID, NAME, DISPLAY_NAME, VHOST, …), AM_PLATFORM_GATEWAY_TOKEN (ID, GATEWAY_ID FK, TOKEN_HASH, SALT, STATUS) |
| DAO | GatewayManagementDAO (instance insert/update, deployment, heartbeat) | PlatformGatewayDAO (gateway CRUD, token insert, verify token) |
| Relation | Instance → env mapping; instance + API → deployment | Gateway 1 — * Token (FK GATEWAY_ID → AM_PLATFORM_GATEWAY.ID) |

---

## Suggested section title for the generated content

**"REST API implementation"** or **"Admin REST API for platform gateway registration"**.

End of prompt.
