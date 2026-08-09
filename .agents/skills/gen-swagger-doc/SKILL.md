---
name: gen-swagger-doc
description: Generate clean Spring OpenAPI (Swagger 3.0) documentation annotations on Spring Boot web controllers and DTOs according to project style guidelines.
---

# Mission

Generate standard, clean, and consistent Spring OpenAPI (Swagger 3.0) annotations across Spring Web Controllers and Request/Response DTOs in VAOPS backend.

---

# Priority Rules & Constraints

## P0 Mandatory Constraints

1. **`@Operation` Summary Only:**
   - NEVER define `description = "..."` on `@Operation`. Only use `summary = "..."`.
   - Correct: `@Operation(summary = "Get user details by ID")`
   - Incorrect: `@Operation(summary = "...", description = "...")`

2. **No Validation Conditions in `@Schema(description)`:**
   - NEVER put validation rules or length/range constraints in `@Schema(description = "...")` (e.g., DO NOT write "at least 8 characters", "must not be empty", "true to enable", "max 256 chars").
   - SpringDoc automatically merges Bean Validation annotations (`@NotBlank`, `@NotNull`, `@NotEmpty`, `@Size`, `@Min`, `@Max`) into schema attributes (`minLength`, `maxLength`, `required`).
   - `@Schema(description = "...")` MUST ONLY describe the semantic purpose of the field.

3. **Controller `@Tag`:**
   - Every `@RestController` must have `@Tag(name = "...", description = "...")`.

4. **Public Endpoint Security:**
   - Use `@SecurityRequirements({})` on public endpoints to mark them as unauthenticated in OpenAPI UI.

5. **DTO Schema Annotations:**
   - Annotate DTO fields with `@Schema(description = "...", example = "...")`.

---

# Execution Workflow

1. **Identify Target Controllers & DTOs:**
   - Inspect controller endpoints, path parameters, query parameters, request bodies, and return types.
   - Trace associated Web Request and Response DTO records/classes.

2. **Annotate Controllers:**
   - Add `@Tag` to controller class.
   - Add `@Operation(summary = "...")` to each mapping method.
   - Add `@ApiResponses` / `@ApiResponse` for expected status codes (200, 201, 204, 400, 401, 403, 404).
   - Add `@Parameter` for `@PathVariable` and `@RequestParam` elements where helpful.
   - Add `@SecurityRequirements({})` for unauthenticated endpoints (`/login`, `/register`, `/refresh`, `/hello`).

3. **Annotate DTOs:**
   - Add `@Schema(description = "...", example = "...")` to field components strictly describing semantic intent (without duplicating validation constraints).

4. **Verify & Validate:**
   - Compile and test with `mvn test-compile` / `mvn test`.
