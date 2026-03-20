# OpenAPI Rules

Keep controllers clean. Routing, validation, and delegation to Service only. NO business logic.

## 1. Interface-Driven Documentation

**Rule:** No `@Operation`, `@Tag` or `@ApiResponses` in controllers.
Put them in an API interface (e.g., `MassStockApi`). The Controller implements this interface.

**Critical Exception (Routing):** 
All Spring MVC routing and security annotations **MUST** remain in the Controller class. This ensures reliable request mapping and compatibility with `@WebMvcTest`/`MockMvc`.
- Keep in **Controller**: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`, `@RequestMapping`, `@RequestParam`, `@RequestBody`, `@PathVariable`, `@Valid`, `@PreAuthorize`.
- Keep in **Interface**: `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Parameter`, `@Tag`.

## 2. Javadoc (therapi + springdoc)

**Rule:** Use Javadoc for textual descriptions. Do NOT duplicate text in annotations.

- **First sentence:** becomes `summary`.
- **Rest:** becomes `description` (supports Markdown).
- **`@param`:** replaces `@Parameter(description=...)`.
- **`@return`:** describes 200/201 success response.

*Example:*
```java
// In Interface
/**
 * Adjust stock quantity.
 * 
 * Creates inventory transaction. Requires ADMIN role.
 * @param request Adjustment details
 * @return Success response
 */
@Operation(operationId = "adjustStock")
@ApiResponses({ ... })
ResponseEntity<MassStockResponse> adjustQuantity(AdjustMassStockQuantityRequest request, ...);

// In Controller
@Override
@PostMapping("/adjust-quantity")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<MassStockResponse> adjustQuantity(@Valid @RequestBody AdjustMassStockQuantityRequest request, ...) {
    // ...
}
```

## 3. Global Errors

**Rule:** Common errors (400, 401, 403, 404, 409, 500) handled globally in `GlobalApiExceptionHandler` via `@ResponseStatus`/`@ApiResponse`. Do not declare them in the API interface unless you need to provide a method-specific description.

## 4. @ParameterObject

**Rule:** Group more than 2 query parameters into a DTO and use `@ParameterObject` in the **Interface** signature to expand fields in Swagger UI. `Pageable` is supported automatically.

## 5. JSR-303 Validation

**Rule:** Do not manually describe constraints in `@Schema`. SpringDoc automatically reads `@NotNull`, `@Min`, `@Max`, `@Size`, etc., from your DTOs.

## 6. External YAML ($ref)

**Rule:** For very large documentation blocks, use external YAML files in `src/main/resources/static/*.yaml` and link them via `$ref`.
