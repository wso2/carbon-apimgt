# APIM and API Platform Gateway Integration — Design Document (On-Prem)

Design for integrating the API Platform (Envoy-based) gateway with on-premises WSO2 API Manager. The document is structured with the most important topics first and matches the implementation in **carbon-apimgt** and **api-platform**.

---

## Table of contents

1. [Introduction](#1-introduction)
2. [Overview and goals](#2-overview-and-goals)
3. [Admin REST API for platform gateway registration](#3-admin-rest-api-for-platform-gateway-registration)
4. [On-prem gateway registration connection flow](#4-on-prem-gateway-registration-connection-flow)
5. [Running multiple gateways locally](#5-running-multiple-gateways-locally)
6. [Troubleshooting and behavior](#6-troubleshooting-and-behavior)
7. [Key file references](#7-key-file-references)

---

## 1. Introduction

### 1.1 Purpose

This document describes the design for integrating the API Platform (Envoy-based) gateway with on-premises WSO2 API Manager (APIM). It is intended for implementers, reviewers, and contributors working on carbon-apimgt and the API Platform gateway.

### 1.2 Problem statement

The current on-prem WSO2 API Manager 4.7 setup relies on the legacy Synapse-based API Gateway. This limits the ability to adopt the newer Envoy-based API Platform Gateway and modern features such as HTTP/2 and HTTP/3. A way is needed for APIM 4.7 to drive the API Platform Gateway—including API deployments and policies—without changing existing Publisher and Developer Portal user flows.

### 1.3 Objectives

- **Unified gateway:** Use a single API Platform Gateway codebase for both cloud (Bijira) and on-premises APIM, without separate protocols or gateway variants.
- **APIM as control plane:** Enable APIM 4.7 to act as the control plane for the API Platform Gateway (registration, configuration, API deployments, and policies).
- **Preserve user experience:** Keep existing Publisher and Developer Portal flows unchanged where possible.
- **Contract alignment:** Follow the same gateway–control plane contract as the cloud offering (registration token, WebSocket connection, Platform API subset) so the gateway remains pluggable across environments.
- **Modern gateway capabilities:** Allow on-prem deployments to benefit from Envoy-based features (e.g. HTTP/2, HTTP/3).

### 1.4 Scope (inclusions and exclusions)

**In scope**

- **Add Gateway:** Control plane–generated registration token; Admin UI providing vanilla Docker Compose (or equivalent); no admin credentials in gateway configuration.
- **Admin REST API:** Create/list gateways; one-time `registrationToken`; persistence in `AM_PLATFORM_GATEWAY` and `AM_PLATFORM_GATEWAY_TOKEN`.
- **On-prem gateway registration and connection flow:** Gateway connects via WebSocket with registration token; control plane verifies token and associates the connection with the gateway.
- **Control plane–gateway communication (bridge):** Bridge embedded in APIM; JMS subscription; WebSocket/REST to gateway using Platform API subset.
- **Publisher and policies:** API Project → platform specification at API creation; Policy Hub filtering by gateway type; api.yaml export for CI/CD.
- **Deployment:** Both **top-down** (control plane → gateway) and **bottom-up** (gateway → control plane).

**Out of scope**

- Custom policies specific to the API Platform Gateway (if defined separately).
- Standalone Java agent for JMS → Platform APIs (rejected in favour of the embedded bridge).
- Any JMS or APIM-specific logic inside the gateway binary.

### 1.5 References

- Bijira / API Platform contract (cloud control plane behaviour).
- Platform APIs and api.yaml (gateway–control plane subset).
- Admin REST API: `/api/am/admin/v4`, `/gateways`; OpenAPI in `admin-api.yaml`.
- Meeting summary: APIM and API Platform Gateway Integration (5 February 2026).

---

## 2. Overview and goals

The integration allows the **same API Platform gateway binary** (gateway-controller) to register with either **API Platform (Bijira)** or **on-prem WSO2 APIM**. In both cases the **control plane** is the system that manages the gateway (the cloud platform in Bijira; WSO2 APIM on-prem). The gateway connects over **WebSocket** to the control plane; on-prem the WebSocket endpoint is **hosted by the Internal Data Service** (a component within APIM, carbon-apimgt). The gateway sends a **registration token** (obtained once via the Admin REST API). The control plane verifies the token and treats the gateway as connected; the same handshake and keep-alive behaviour are supported on-prem so the existing gateway client works unchanged.

**High-level flow**

1. Admin creates a gateway via Admin REST API → control plane generates and returns a **registration token** (once).
2. Gateway starts with `GATEWAY_REGISTRATION_TOKEN` (required to connect; if missing, the gateway runs but does not connect to the control plane) and **either** `GATEWAY_CONTROL_PLANE_WS_URL` (on-prem) **or** `GATEWAY_CONTROLPLANE_HOST` (Bijira)—at least one of these is required by the gateway config.
3. Gateway opens a WebSocket to the control plane, sending the token in the `api-key` header.
4. Control plane verifies the token (e.g. `PlatformGatewayTokenUtil.verifyToken` on-prem), associates the connection with the gateway, sends **connection.ack**, and starts a **heartbeat** (server PING).
5. Gateway waits for connection.ack (with timeout), then runs a heartbeat monitor (e.g. 35s timeout); on timeout or disconnect it reconnects with backoff.

Goals for the connection flow: **same handshake and keep-alive as API Platform**, **no gateway code paths specific to on-prem** (only configuration: WS URL and TLS skip), and **control plane tracks gateway active status** (e.g. `AM_PLATFORM_GATEWAY.IS_ACTIVE`).

---

## 3. Admin REST API for platform gateway registration

### 3.1 Overview

The feature allows registering API Platform / self-hosted gateways via the Admin REST API. The control plane creates a gateway record and generates a **registration token**, returned **once** in the create response. The gateway uses this token to connect to the control plane over WebSocket. No admin credentials are stored in the gateway.

### 3.2 API contract

- **Base path:** `/api/am/admin/v4`
- **POST /gateways** — Create gateway. Request body: `name` (required), `displayName` (required), `description` (optional), `vhost` (required), `isCritical` (optional), `functionalityType` (required). Response 201: gateway object plus **registrationToken** (returned only once). Errors: 400 validation, 404 org not found, 409 gateway name already exists in org.
- **GET /gateways** — List gateways for the organization. Response 200: count and list of gateways (no tokens). Security: `apim:admin` scope.

### 3.3 Implementation components (carbon-apimgt)

| Component | Path / location |
|-----------|------------------|
| OpenAPI | `components/apimgt/org.wso2.carbon.apimgt.rest.api.admin.v1/src/main/resources/admin-api.yaml` — paths `/gateways` (GET, POST), schemas `CreatePlatformGatewayRequest`, `PlatformGateway`, `PlatformGatewayList`. |
| Generated API & DTOs | `GatewaysApi.java`, `GatewaysApiService.java`, `CreatePlatformGatewayRequestDTO`, `PlatformGatewayDTO`, `PlatformGatewayListDTO` under `org.wso2.carbon.apimgt.rest.api.admin.v1` and `.dto`. |
| Service implementation | `org.wso2.carbon.apimgt.rest.api.admin.v1.impl.GatewaysApiServiceImpl` — validates org via `RestApiUtil.getValidatedOrganization(messageContext)`, validates body, checks name uniqueness, generates token/salt/hash, persists gateway and token in one transaction, returns gateway + registrationToken on create. |
| Persistence | `PlatformGatewayDAO` (org.wso2.carbon.apimgt.impl.dao); tables `AM_PLATFORM_GATEWAY` and `AM_PLATFORM_GATEWAY_TOKEN`. SQL constants in `SQLConstants.PlatformGatewaySQLConstants`. DDL in `features/apimgt/org.wso2.carbon.apimgt.core.feature/src/main/resources/sql/` (mysql.sql, postgresql.sql, h2.sql). |
| Token handling | `PlatformGatewayTokenUtil` (org.wso2.carbon.apimgt.impl.utils) — generateToken (32 bytes SecureRandom, Base64 URL no padding), generateSalt, hashToken (SHA-256(plain+salt), hex), verifyToken(plainToken) for WebSocket auth. Token stored hashed only; plain token returned only in POST response. |

### 3.4 Security and errors

- **Security:** Admin API protected by OAuth2; scope `apim:admin`. Organization resolved from request context; gateways scoped by organization.
- **Errors:** 400 for validation (e.g. missing required fields); 409 for duplicate gateway name (`ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS`); 404 if org not found.

### 3.5 Database design

#### 3.5.1 Table structure (platform gateway)

**AM_PLATFORM_GATEWAY** — one row per logical platform gateway registration (long-lived identity).

| Column | Type | Constraints | Description |
|--------|------|--------------|-------------|
| ID | VARCHAR(255) | PRIMARY KEY | Gateway UUID (generated on create). |
| ORGANIZATION_ID | VARCHAR(128) | NOT NULL | Tenant/organization. |
| NAME | VARCHAR(255) | NOT NULL, UNIQUE with ORGANIZATION_ID | Logical name (unique per org). |
| DISPLAY_NAME | VARCHAR(255) | NOT NULL | Display name. |
| DESCRIPTION | VARCHAR(1023) | NULL | Optional description. |
| VHOST | VARCHAR(255) | NOT NULL | Virtual host. |
| IS_CRITICAL | TINYINT(1) | NOT NULL, DEFAULT 0 | Critical gateway flag. |
| FUNCTIONALITY_TYPE | VARCHAR(255) | NOT NULL | e.g. full, read-only. |
| IS_ACTIVE | TINYINT(1) | NOT NULL, DEFAULT 1 | True when at least one WebSocket connection is active. |
| CREATED_AT | TIMESTAMP | NOT NULL | Creation time. |
| UPDATED_AT | TIMESTAMP | NOT NULL | Last update (e.g. on active status change). |

**AM_PLATFORM_GATEWAY_TOKEN** — stores registration tokens (hashed); one gateway can have multiple tokens (e.g. after rotation).

| Column | Type | Constraints | Description |
|--------|------|--------------|-------------|
| ID | VARCHAR(255) | PRIMARY KEY | Token record UUID. |
| GATEWAY_ID | VARCHAR(255) | NOT NULL, FK → AM_PLATFORM_GATEWAY(ID) ON DELETE CASCADE | References the gateway. |
| TOKEN_HASH | VARCHAR(255) | NOT NULL | SHA-256(plainToken + salt), hex. |
| SALT | VARCHAR(255) | NOT NULL | Salt (hex) used for hashing. |
| STATUS | VARCHAR(50) | NOT NULL, DEFAULT 'active' | active \| revoked. |
| CREATED_AT | TIMESTAMP | NOT NULL | When token was created. |
| REVOKED_AT | TIMESTAMP | NULL | When token was revoked (if any). |

DDL location: `features/apimgt/org.wso2.carbon.apimgt.core.feature/src/main/resources/sql/` (mysql.sql, postgresql.sql, h2.sql).

#### 3.5.2 Relationships

```
AM_PLATFORM_GATEWAY (1) ──────────< (*) AM_PLATFORM_GATEWAY_TOKEN
        │                                    │
        │ ID (PK)                             │ GATEWAY_ID (FK)
        └────────────────────────────────────┘
```

- **Cardinality:** One gateway can have zero or more tokens. Each token belongs to exactly one gateway.
- **Verification flow:** Given a plain token, the system hashes it with each active token’s salt and compares to TOKEN_HASH; on match, joins to AM_PLATFORM_GATEWAY to get gateway id, organization, etc. (see `PlatformGatewayDAO` and `PlatformGatewayTokenUtil.verifyToken`).

#### 3.5.3 Why this structure over existing gateway tables

The existing gateway-related tables serve a **different purpose** and do not support registration tokens or long-lived gateway identity. Using them for platform gateway registration would mix concerns and complicate existing logic.

**Existing tables (runtime instances and deployment)**

| Table | Purpose | Key columns |
|-------|---------|-------------|
| **AM_GW_INSTANCES** | Runtime gateway *instances*: heartbeats, liveness. | GATEWAY_ID (AUTO_INCREMENT), GATEWAY_UUID, ORGANIZATION, LAST_UPDATED, GW_PROPERTIES (BLOB). UNIQUE (GATEWAY_UUID, ORGANIZATION). |
| **AM_GW_INSTANCE_ENV_MAPPING** | Which env labels a gateway instance is in. | GATEWAY_ID (FK → AM_GW_INSTANCES), ENV_LABEL. |
| **AM_GW_REVISION_DEPLOYMENT** | Which API revisions are deployed to which instance. | GATEWAY_ID, API_ID, ORGANIZATION, STATUS, ACTION, REVISION_UUID, LAST_UPDATED. |

These are used by `GatewayManagementDAO` for: instance upsert by GATEWAY_UUID + ORGANIZATION, heartbeat updates (LAST_UPDATED), deployment state, and cleanup of stale instances (e.g. DELETE WHERE LAST_UPDATED < ?). They do **not** store registration tokens, token hash, salt, or token status.

**Comparison: why new tables**

| Aspect | Existing tables (AM_GW_*) | New tables (AM_PLATFORM_GATEWAY*) |
|--------|---------------------------|-----------------------------------|
| **What they represent** | Ephemeral *runtime instances* (a process that sends heartbeats). | Long-lived *registration identity* (name, vhost, org) plus *tokens* (hashed) for WebSocket auth. |
| **Token storage** | No columns for TOKEN_HASH, SALT, or STATUS. Adding them would mix “instance liveness” with “registration + token verification” and complicate GatewayManagementDAO and deployment logic. | Dedicated AM_PLATFORM_GATEWAY_TOKEN table for hashed tokens and status; verification joins token → gateway. |
| **Lifecycle** | Rows are tied to heartbeats; retention/cleanup removes stale instances. | Gateway row is created by Admin API and persists; IS_ACTIVE reflects connection state; tokens can be rotated/revoked. |
| **Access patterns** | Queries by GATEWAY_UUID + ORGANIZATION for deployment and heartbeat updates. | Admin: list gateways by org, create with unique name per org. Connection: verify token → resolve gateway id and org. |
| **DAO** | GatewayManagementDAO (instance insert/update, deployment, heartbeat). | PlatformGatewayDAO (gateway CRUD, token insert, verify token, update IS_ACTIVE). |

Keeping platform gateway registration in **AM_PLATFORM_GATEWAY** and **AM_PLATFORM_GATEWAY_TOKEN** avoids changing existing instance/deployment behaviour, keeps the new feature’s schema clear, and leaves room to link runtime instances (e.g. AM_GW_INSTANCES) to a platform gateway identity later if needed.

### 3.6 Use of the registration token after create

The gateway sends the registration token as the `api-key` HTTP header when opening the control plane WebSocket (e.g. `/internal/data/v1/ws/gateways/connect` on-prem). The control plane verifies it via `PlatformGatewayTokenUtil.verifyToken` and associates the connection with the gateway. See [Section 4](#4-on-prem-gateway-registration-connection-flow) for the connection flow.

### 3.7 Tryout (example)

```bash
# 1. Obtain admin token (replace client id/secret and scope as needed)
# 2. Create gateway
curl -X POST "https://localhost:9443/api/am/admin/v4/gateways" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"my-gateway","displayName":"My Gateway","vhost":"api.example.com","functionalityType":"full"}'
# Response includes "registrationToken" — store it; it is not returned again.

# 3. List gateways
curl -H "Authorization: Bearer <admin_token>" "https://localhost:9443/api/am/admin/v4/gateways"
```

---

## 4. On-prem gateway registration connection flow

The gateway (API Platform gateway-controller) connects to the control plane over WebSocket. On-prem the control plane is **WSO2 APIM**; the WebSocket endpoint used by the gateway is **hosted by the Internal Data Service** (a component within APIM). This section describes how the on-prem flow is implemented so the same gateway binary can register with on-prem APIM via WebSocket.

### 4.1 Overview and goals (connection flow)

- Gateway connects with a **registration token**; control plane verifies it and treats the gateway as connected.
- On-prem supports the **same handshake and keep-alive behaviour** as API Platform so the existing gateway client works unchanged.
- After verification: store gateway in session, update gateway active status in DB, send **connection.ack**, then send WebSocket **PING** periodically. On close or error, update active status to false and handle "connection already closed" without noisy logs.

### 4.2 On-prem server side (carbon-apimgt)

**WebSocket endpoint**

- **Path:** `/ws/gateways/connect` (relative to Internal Data Service context, e.g. full path `/internal/data/v1/ws/gateways/connect`).
- **Component:** `org.wso2.carbon.apimgt.internal.service.websocket` — `GatewayConnectEndpoint.java`, `GatewayConnectConfigurator.java`.
- **Configurator:** `GatewayConnectConfigurator` passes the `api-key` header from the handshake request into the endpoint config so it is available in `onOpen`.

**Authentication**

- Gateway sends the registration token in the **api-key** HTTP header when opening the WebSocket.
- In `onOpen`, the endpoint reads `api-key` from config, calls `PlatformGatewayTokenUtil.verifyToken(apiKey)`.
- On failure (null, exception, or missing header): close the session with close code **4401** (Unauthorized) and a short message (e.g. "API key is required" or "Invalid or expired API key").
- On success: store the resolved `PlatformGatewayDAO.PlatformGateway` in the session user property `GATEWAY_PROPERTY` ("platformGateway").

**On successful open**

1. Update gateway active status: `PlatformGatewayDAO.getInstance().updateGatewayActiveStatus(gateway.id, true)` (sets `AM_PLATFORM_GATEWAY.IS_ACTIVE` and `UPDATED_AT`).
2. Send **connection.ack** (see below).
3. Start the **heartbeat** daemon thread (see below).

**connection.ack**

- **Format:** JSON with `type`, `gatewayId`, `connectionId`, `timestamp`. Example: `{"type":"connection.ack","gatewayId":"<uuid>","connectionId":"<uuid>","timestamp":"<ISO-8601>"}`.
- **Why required:** The gateway client waits for this message with a timeout (e.g. 5s). Without it, the client logs "failed to receive connection.ack" and closes the connection. The server sends it immediately after accepting the WebSocket and verifying the token.

**Heartbeat**

- Server sends WebSocket **PING** frames periodically (e.g. every **15 seconds**). Implementation: a daemon thread in `GatewayConnectEndpoint` that sleeps 15s, then calls `session.getBasicRemote().sendPing(ByteBuffer.allocate(0))` while the session is open. A user property `HEARTBEAT_RUNNING_PROPERTY` (AtomicBoolean) is set to false in `@OnClose` so the thread exits.
- Client expects PING within its heartbeat timeout (e.g. **35s**); if no PING is received in time, the client treats it as "Heartbeat timeout detected" and disconnects/reconnects.

**On close**

- In `@OnClose`: set the heartbeat running flag to false, then get the gateway from session and call `PlatformGatewayDAO.getInstance().updateGatewayActiveStatus(gateway.id, false)`.
- **@OnError:** When the client closes first, Tomcat may produce "Unable to wrap data, invalid status [CLOSED]" or "Unable to unwrap". The endpoint treats these as benign (connection already closed) and logs at debug only to avoid noisy logs; other errors are logged at warn.

**DAO and SQL**

- `PlatformGatewayDAO.updateGatewayActiveStatus(gatewayId, boolean)` updates `AM_PLATFORM_GATEWAY.IS_ACTIVE` and `UPDATED_AT`.

### 4.3 Gateway client side (api-platform gateway-controller)

**Configuration**

- **Optional full WebSocket URL:** `GATEWAY_CONTROL_PLANE_WS_URL` or `GATEWAY_GATEWAY__CONTROLLER_CONTROLPLANE_WS_URL`. When set, this URL is used as-is for the WebSocket connection (e.g. on-prem: `wss://host:9443/internal/data/v1/ws/gateways/connect`). When not set, the client builds the URL from the host (Bijira path: `wss://{host}/api/internal/v1/ws/gateways/connect`). Config: `config.ControlPlaneConfig.WSURL`; see `pkg/config/config.go` (e.g. `gateway_controller.controlplane.ws_url`).
- **Control plane host:** `GATEWAY_CONTROLPLANE_HOST` (or `GATEWAY_GATEWAY__CONTROLLER_CONTROLPLANE_HOST`). Used when WS URL is not set to build the WebSocket URL; also used for REST API base URL and logging.
- **TLS:** `GATEWAY_GATEWAY__CONTROLLER_CONTROLPLANE_INSECURE_SKIP_VERIFY` — skip TLS certificate verification. Applied in the WebSocket dialer (`TLSClientConfig: &tls.Config{InsecureSkipVerify: c.config.InsecureSkipVerify}`) and in the REST API client (e.g. `pkg/utils/api_utils.go`). Config mapping in `config.go` (`controlplane.insecure_skip_verify`).

**Client flow**

1. **Connect:** Dial WebSocket with `api-key` header set to the registration token. Use `getWebSocketConnectURL()`: if `config.WSURL` is set, use it; otherwise `getWebSocketURL() + "/gateways/connect"`.
2. **Wait for connection.ack:** Set read deadline (e.g. 5s), read first text message, parse JSON. Expect `type == "connection.ack"` and store `gatewayId`, `connectionId`, `timestamp`. On timeout or wrong type, close and return error ("failed to receive connection.ack").
3. **Connected:** Transition to Connected state, reset retry count, log "Control plane connection established".
4. **SetPingHandler:** On server PING, update last-heartbeat timestamp (atomic) and send PONG (gorilla/websocket does this via the handler return).
5. **Heartbeat monitor:** Goroutine that runs periodically (e.g. every 5s). If time since last heartbeat > 35s, log "Heartbeat timeout detected", close the connection, set state to Reconnecting, and return (connection loop will retry).
6. **Reconnection:** `connectionLoop` calls `Connect()`; on failure it logs "Connection failed, will retry", applies backoff, and retries. On success it runs `waitForDisconnection()` until the connection is closed, then sets state to Reconnecting and loops again.

**Key files**

- `pkg/controlplane/client.go` — Connect, waitForConnectionAck, heartbeatMonitor, connectionLoop, getWebSocketConnectURL, getWebSocketURL.
- `pkg/controlplane/events.go` — `ConnectionAckMessage` struct (`type`, `gatewayId`, `connectionId`, `timestamp`).
- `pkg/config/config.go` — ControlPlaneConfig (Host, WSURL, Token, InsecureSkipVerify, reconnect settings).

### 4.4 Running multiple gateways locally

See [Section 5](#5-running-multiple-gateways-locally) for commands, env vars, and port mapping.

---

## 5. Running multiple gateways locally

A **standalone Compose file** is used for a second gateway because merging the default compose with port override files can cause port/list conflicts; a dedicated file (e.g. `docker-compose.gateway2.yaml`) with a different project name and fixed port set avoids that.

**Commands**

- **First gateway** (default ports 9090, 18000, 8080, 8443, …):  
  `docker compose up -d`
- **Second gateway** (e.g. ports 19090, 18100, 18080, 18443, …):  
  `docker compose -p gateway2 -f docker-compose.gateway2.yaml up -d`

Set on-prem env vars for the second stack before bringing it up, e.g.:

```bash
export GATEWAY_CONTROL_PLANE_WS_URL=wss://192.168.1.3:9443/internal/data/v1/ws/gateways/connect
export GATEWAY_REGISTRATION_TOKEN=your-token
export GATEWAY_GATEWAY__CONTROLLER_CONTROLPLANE_INSECURE_SKIP_VERIFY=true
docker compose -p gateway2 -f docker-compose.gateway2.yaml up -d
```

**View logs and stop**

- `docker compose -p gateway2 -f docker-compose.gateway2.yaml logs -f`
- `docker compose -p gateway2 -f docker-compose.gateway2.yaml down`

**Port mapping (second stack)**

- Controller: 19090 (REST), 18100 (xDS), 19011 (metrics).
- Router: 18080 (HTTP), 18443 (HTTPS), 19901 (Envoy admin).
- Policy engine: 19002 (Admin), 19003 (metrics).
- Backends: 15000, 15443 (as in compose).

See `gateway/README.md` and `gateway/docker-compose.gateway2.yaml` in the api-platform repo.

---

## 6. Troubleshooting and behavior

| Symptom | Cause | What to check |
|--------|--------|----------------|
| "Connection failed, will retry" / "failed to receive connection.ack" | Server did not send connection.ack before client timeout, or connection rejected. | Ensure the server sends **connection.ack** immediately after accepting the WebSocket (after token verification). Path and WS URL must match (e.g. `/internal/data/v1/ws/gateways/connect` on-prem). |
| "Heartbeat timeout detected" | No server PING received within client timeout (e.g. 35s). | Server must send WebSocket **PING** at least every ~15s. Check that the heartbeat daemon is running and the session is still open. |
| "Unable to wrap data, invalid status [CLOSED]" or close code 1006 on server when client stops | Client closed the connection first; server may try to send and hit closed socket. | Normal when the client disconnects first. Server `@OnError` treats "CLOSED" / "Unable to wrap" / "Unable to unwrap" as benign and logs at debug only to reduce noise. |
| Idle for a long time then reconnect | Heartbeat timeout (no PINGs) or network blip. | Expected. Client detects heartbeat timeout, closes connection, sets state to Reconnecting, and retries. "Control plane connection established" after reconnect is normal. |

---

## 7. Key file references

| Repo | Component | Path / file |
|------|-----------|-------------|
| carbon-apimgt | WebSocket endpoint | `components/apimgt/org.wso2.carbon.apimgt.internal.service/src/main/java/org/wso2/carbon/apimgt/internal/service/websocket/GatewayConnectEndpoint.java` |
| carbon-apimgt | WebSocket configurator | `.../websocket/GatewayConnectConfigurator.java` |
| carbon-apimgt | Gateway/token DAO | `components/apimgt/org.wso2.carbon.apimgt.impl/src/main/java/.../dao/PlatformGatewayDAO.java` |
| carbon-apimgt | Token util | `.../impl/utils/PlatformGatewayTokenUtil.java` |
| carbon-apimgt | SQL constants | `.../dao/constants/SQLConstants.java` (PlatformGatewaySQLConstants) |
| carbon-apimgt | Admin API | `components/apimgt/org.wso2.carbon.apimgt.rest.api.admin.v1` (GatewaysApi, GatewaysApiServiceImpl, admin-api.yaml) |
| api-platform | Control plane client | `gateway/gateway-controller/pkg/controlplane/client.go` |
| api-platform | Config | `gateway/gateway-controller/pkg/config/config.go` |
| api-platform | API utils (REST) | `gateway/gateway-controller/pkg/utils/api_utils.go` |
| api-platform | Connection ack model | `gateway/gateway-controller/pkg/controlplane/events.go` |
| api-platform | Compose (second gateway) | `gateway/docker-compose.gateway2.yaml` |
| api-platform | Gateway README | `gateway/README.md` |

---

*End of design document.*
