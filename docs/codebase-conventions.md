# Codebase Conventions

## Package Structure

Use shared layer packages under `com.amigos.courtpulse`:

- `config`: application configuration classes and bean definitions
- `controller`: REST controllers
- `dto`: request and response DTOs grouped by feature when useful, for example `dto.player` and `dto.club`
- `dto.common`: shared API response DTOs
- `entity`: JPA entities mapped to database tables
- `enums`: shared enum types; enum class names should end with `Enum`
- `exception`: application exceptions and global exception handling
- `mapper`: mapping code between entities, DTOs, and external models
- `repository`: Spring Data repositories
- `security`: security configuration, filters, authentication, and authorization support
- `service`: service interfaces
- `service.impl`: service implementations
- `util`: small shared utilities
- `validation`: custom validators and validation annotations

Do not create feature-root packages such as `com.amigos.courtpulse.club.*`. Keep classes in the shared layer folders above.

## Naming

- Entities use singular names, for example `Player`.
- Enums end with `Enum`, for example `PlayerStatusEnum`.
- Controllers end with `Controller`.
- Services end with `Service`.
- Repositories end with `Repository`.
- DTOs should describe their API role, for example `CreatePlayerRequest` or `PlayerResponse`.
- Controller responses should use `ApiResponse<T>` through `ResponseUtil`.
- Shared null checks and object comparisons should use `ObjectUtil`.
- Generated business codes should be deterministic when possible; prefer database sequences over random generation.

## Layer Direction

- Controllers call services.
- Controllers return explicit HTTP status codes through `ResponseUtil`.
- Services use repositories and mappers.
- Repositories access entities.
- DTOs stay at API boundaries and should not be persisted directly.

## Security

- Use stateless JWT authentication through Spring Security OAuth2 Resource Server.
- `POST /api/v1/auth/login` is public.
- `POST /api/v1/players` is public while player registration is part of the Player API.
- All other application endpoints require a bearer token unless explicitly documented otherwise.
- JWTs use HS256 and must include `sub` as username plus `playerCode` as a claim.
- Protected APIs should infer the current player from the JWT. Do not require clients to send `playerCode`, `ownerPlayerCode`, or `reviewerPlayerCode` when it represents the authenticated user.
- Store the JWT secret in `JWT_SECRET`; production must not rely on the local default.
- Do not add refresh tokens until a persistence and revocation model is designed.

## Club API Sync

- Mobile should use `GET /api/v1/clubs/my` as the source of truth for clubs the current player belongs to.
- Mobile should use `GET /api/v1/clubs/requests/my` as the source of truth for join request status badges.
- Club membership actions must infer the current player from JWT.
- Owners cannot leave their own club until an ownership transfer or close-club workflow exists.
