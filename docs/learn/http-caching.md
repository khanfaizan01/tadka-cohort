# HTTP Caching (ADR-048)

## Overview

Conditional GET via `ETag` and `If-None-Match` headers. On a match, the server returns `304 Not Modified` with an empty body instead of re-sending the full JSON.

## How it works

1. Server computes SHA-256 hash of the response body
2. Sets `ETag` header to the hash
3. Client sends `If-None-Match: "<etag>"` on subsequent requests
4. If hash matches → return `304` with empty body
5. If no match → return full response with new `ETag`

## Why `public` not `private`

`Cache-Control: public` allows shared caches (proxies, edge CDNs) to store the response. This is correct for restaurant/menu listings which are not personalized. `private` would mean shared caches must not store the response, which defeats the purpose of an edge cache.

## Application

Only apply to reads that are safe to cache (menu, restaurant GETs). Never on writes or customer-specific resources like orders.

## Key files

- `src/main/java/com/tadka/filters/ETagFilterAttribute.java` — ETag computation and 304 response
- `src/main/java/com/tadka/controller/RestaurantsController.java` — Apply `@ETagFilter` to GET endpoints

## Demo

```bash
# First request
curl -v http://localhost:5224/api/v1/restaurants/{id} -o /dev/null -D -
# → 200 OK, ETag header present

# Second request with If-None-Match
curl -v http://localhost:5224/api/v1/restaurants/{id} \
  -H 'If-None-Match: "<etag>"' -o /dev/null -D -
# → 304 Not Modified, empty body
```
