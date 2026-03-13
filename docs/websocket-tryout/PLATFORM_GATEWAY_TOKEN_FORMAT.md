# Platform gateway registration token format

## Aligned with API Platform (no salt, direct lookup)

Token verification uses **deterministic hash** only: `SHA-256(plainToken)` stored in `TOKEN_HASH`. No per-token salt. This allows a single indexed DB lookup by `token_hash` instead of loading all tokens (same behaviour as API Platform, adopted for performance).

---

## Combined format: token ID + plain token (for direct lookup by ID)

To allow **direct lookup by token row ID** (e.g. UUID or future UUID7), the control plane can return a **combined** value that the gateway sends back in the `api-key` header. The server then parses it and can look up by ID first (one row), then verify the token hash.

### Recommended format: **dot separator**

- **Format:** `{tokenId}.{plainToken}`
- **Example:** `550e8400-e29b-41d4-a716-446655440000.abc123base64url...`
- **Parsing:** Split on the **first** `.` → `tokenId = part[0]`, `plainToken = part[1]`.
- **Why:**  
  - UUID (with hyphens) and base64url token do not contain `.`, so the split is unambiguous.  
  - Simple, no extra encoding; URL- and header-safe.  
  - Easy to log or redact by showing only `tokenId` and hiding the token part.

### Alternative: JSON + base64url

- **Format:** `base64url(JSON.stringify({id: tokenId, token: plainToken}))`
- **Pros:** Extensible (more fields later), single opaque string.
- **Cons:** Slightly larger, encode/decode overhead.

**Recommendation:** Use the **dot separator** unless you need to add more fields to the payload later; then consider JSON + base64url.

---

## Verification flow

1. **Combined value** (contains `.`): parse `tokenId` and `plainToken`; look up row by `ID = tokenId`; verify `SHA-256(plainToken)` equals stored `TOKEN_HASH`; resolve gateway from that row.
2. **Plain token only** (no `.`): compute `tokenHash = SHA-256(plainToken)`; look up row by `TOKEN_HASH = tokenHash`; resolve gateway from that row.

Both paths use a **single row lookup** (by primary key or indexed `TOKEN_HASH`).
