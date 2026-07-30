# Workspace Rules for VAOPS

## Domain Architecture Rules
- **JPA Entities are Domain Entities**: Entities in `domain/` use JPA annotations directly. We embrace a Rich Domain Model using JPA entities without creating a separate, duplicate domain layer or redundant mapping objects.
- **Explicit Intermediate Entities**: Intermediate join tables with extra attributes or for query performance (e.g., `UserRole`, `RolePermission`) MUST be modeled as explicit JPA entities using `@EmbeddedId` composite keys in `domain/id/`.
