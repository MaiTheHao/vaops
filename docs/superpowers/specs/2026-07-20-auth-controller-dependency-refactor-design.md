# Refactor AuthenticationController Dependencies

Refactor the `AuthenticationController` to remove its dependency on the public API package (`c4f.vannang.vaops.modules.authentication.api`), restricting its dependencies solely to the `internal` package and infrastructure/web packages of the authentication module.

## Context & Motivation

In the modular monolith architecture of VAOPS, the API package (`api`) of a module acts as its public contract for other modules. The implementation details and controller/web entry points reside in the implementation/infrastructure layers. 

Currently, `AuthenticationController` calls the authentication module's services through `AuthenticationModuleApi` (which is part of the public `api` package). This introduces a circular-like dependency style where a module's internal controller acts as an external consumer of the same module's API. Instead, the controller should directly interact with the module's internal use cases, bypassing the public API layer.

## Design Details

### 1. Dependency Modifications

The `AuthenticationController` will directly inject:
- `LoginUseCase`
- `RegisterUseCase`
- `RefreshTokenUseCase`
- `LogoutUseCase`

It will no longer inject `AuthenticationModuleApi` or import anything from `c4f.vannang.vaops.modules.authentication.api.*`.

### 2. DTO and Command Mapping

Request bodies received by the controller will continue to use the infrastructure web DTOs:
- `LoginWebRequestDto`
- `RegisterWebRequestDto`

The controller will map these web DTOs directly to the internal command records:
- `LoginCommand`
- `RegisterCommand`
- `RefreshTokenCommand`
- `LogoutCommand`

Responses will map internal command results back to web-specific formats or cookies:
- `LoginCommandResult` (used to generate `access_token` and `refresh_token` cookies)
- `RegisterCommandResult` (mapped to `RegisterWebResponseDto`)
- `RefreshTokenCommandResult` (used to generate new cookies)

### 3. Proposed Changes

#### [AuthenticationController.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationController.java)

Update the controller fields, constructor, imports, and endpoints to use the internal UseCases and Commands.

## Verification Plan

### Automated Tests
- Run `mvn clean compile` to ensure the project compiles successfully without any dependency errors.
- Run `mvn test` to verify that existing unit tests continue to pass.
