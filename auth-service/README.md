# auth-service

Spring Authorization Server + MongoDB user store (`AuthAccount`) with:

- `POST /auth/signup` - create user (BCrypt passwordHash)
- `POST /auth/login`  - verify credentials
- `POST /auth/token`  - **dev/test** email+password -> JWT access token (signed with same JWK as auth server)

Also includes standard OAuth2/OIDC endpoints (Authorization Server):
- `/.well-known/openid-configuration`
- `/oauth2/token` (client_credentials supported via client `service-client/service-secret`)
- `/oauth2/jwks`

## Run
- Start MongoDB: `mongodb://localhost:27017/algoverse`
- `mvn spring-boot:run`

## Postman quick test
1) Signup:
   POST http://localhost:9000/auth/signup
   { "email":"user@demo.com", "password":"Password123!" }

2) Dev token:
   POST http://localhost:9000/auth/token
   { "email":"user@demo.com", "password":"Password123!", "scope":"read" }

Use the returned token to call api-service endpoints.
