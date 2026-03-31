# OpenAPI Rules

Keep controllers clean. Routing, validation, and delegation to Service only. NO business logic.

## 1. Interface-Driven Documentation (Hybrid Split)

**Rule:** No `@Operation`, `@Tag` or `@ApiResponses` in controllers.
Put them in an API interface (e.g., `MassStockApi`). The Controller implements this interface. 
Use `therapi-runtime-javadoc` to automatically pull descriptions from JavaDoc, reducing annotation noise.

**Critical Exception (Routing):**
All Spring MVC routing and security annotations **MUST** remain in the Controller class. This ensures reliable request mapping and compatibility with `@WebMvcTest`/`MockMvc`.

- Keep in **Controller**: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`, `@RequestMapping`, `@RequestParam`, `@RequestBody`, `@PathVariable`, `@Valid`, `@PreAuthorize`.
- Keep in **Interface**: `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Parameter`, `@Tag`.

## 2. Global Errors

**Rule:** Declare an error response in the API interface **only if it carries endpoint-specific information** (e.g., a 400 caused by filter-parameter validation with a distinct description). Errors that add no extra meaning beyond what `GlobalApiExceptionHandler` already documents (e.g., a generic 401 Unauthorized or 403 Forbidden) **must not** be repeated in the interface.

## 3. @ParameterObject

**Rule:** Group more than 2 query parameters into a DTO and use `@ParameterObject` in the **Interface** signature to expand fields in Swagger UI. `Pageable` is supported automatically.

## 4. JSR-303 Validation

**Rule:** Do not manually describe constraints in `@Schema`. SpringDoc automatically reads `@NotNull`, `@Min`, `@Max`, `@Size`, etc., from your DTOs.

## 5. OpenAPI Documentation Constants

**Rule:** If an `@Operation` description must explain non-trivial caller rules (filter syntax, operators, value formats), extract it to a `final` constants class in `application/controller/openapi/` and reference it from the Api interface. Simple one-line descriptions stay inline.

- Naming: `<Domain>OpenApiDocumentation` / `<OPERATION>_DESCRIPTION`
- Every such class requires a `*OpenApiDocumentationTest`: private-constructor test + not-null/blank assertion per constant. See `InventoryOpenApiDocumentation` + its test as the reference.

## 6. External YAML ($ref)

**Rule:** For very large documentation blocks, use external YAML files in `src/main/resources/static/*.yaml` and link them via `$ref`.

## 7. Generic Response Schemas (e.g. PageResponse<T>)

**Rule:** To correctly generate generic schemas (e.g. `PageResponse<T>`), omit the `content = @Content(...)` block entirely in `@ApiResponse(responseCode = "200")`. This allows Springdoc to accurately infer the generic types from the method signature.
