# AI Code Conventions Guide

Below are the mandatory conventions for this project. They are compressed for AI reasoning. Interpret strictly.

## Table of Contents

1. [Variable Declarations](#1-variable-declarations)
2. [Technical Text Language](#2-technical-text-language)
3. [OpenAPI Annotations](#3-openapi-annotations)
4. [Optimistic Locking](#4-optimistic-locking)
5. [@Transactional, @Retryable, and @Idempotent](#5-transactional-retryable-and-idempotent)
6. [Fail-Fast Principle](#6-fail-fast-principle)
7. [Tell, Don't Ask Principle](#7-tell-dont-ask-principle)
8. [Strict Layering: Reads/Writes via Service](#8-strict-layering-readswrites-via-service)
9. [API Boundaries & Object Passing](#9-api-boundaries--object-passing)
10. [Single Responsibility Principle (SRP)](#10-single-responsibility-principle-srp)
11. [JPA Persistence Best Practices](#11-jpa-persistence-best-practices)
12. [Single Level of Abstraction](#12-single-level-of-abstraction)
13. [Controller vs. Service Responsibilities](#13-controller-vs-service-responsibilities)
14. [Class Purpose Documentation (CPUD)](#14-class-purpose-documentation-cpud)
15. [Layer Dependency Rules](#15-layer-dependency-rules)
16. [Imports vs FQN](#16-imports-vs-fqn)
17. [Nested Idempotency Keys](#17-nested-idempotency-keys)
18. [Backend vs Frontend Boundary](#18-backend-vs-frontend-boundary)

## 1. Variable Declarations

**ALWAYS** use `var` for local variable declarations when the type is obvious. Reduce visual noise. Do not use explicit types unless required for clarity. Example: `var list = new ArrayList<String>();`.

## 2. Technical Text Language

All developer-facing text (comments, logs, exceptions, APIs, tests) MUST be in English.

## 3. OpenAPI Annotations

Keep controllers clean. Use Interface-Driven Documentation (extract `@Operation` etc. to an `XxxxApi` interface). Rely on `therapi-runtime-javadoc` for descriptions. Use `@ParameterObject`. Do NOT duplicate JSR-303 constraints in API docs.

## 4. Optimistic Locking

Mutable `@Entity` classes must declare `@Version`. Use the object wrapper `Long` or `Integer` (not primitive `long`) to ensure JPA `save()` handles the "new entity" lifecycle correctly via `null` checks. Do NOT map the column explicitly (`@Column` is forbidden).

## 5. @Transactional, @Retryable, and @Idempotent

**STRICT LAYERING REQUIRED:**

- **Inner Operation:** `@Idempotent` + `@Transactional` (atomic logic).
- **Outer Service:** `@Retryable` ONLY. Must call the inner operation so each retry opens a fresh transaction.
- **Read-Only Service:** `@Transactional(readOnly = true)`.
  **CRITICAL:** NEVER combine `@Retryable` and `@Transactional` on the same method.

## 6. Fail-Fast Principle

Place ALL guard clauses and parameter validations at the absolute top of the method before business logic.

## 7. Tell, Don't Ask Principle

Business logic mutates state INSIDE domain objects. Call `stock.decreaseQuantity(x)` on entities. Do not extract state into the service to make decisions externally.

## 8. Strict Layering: Reads/Writes via Service

ALL database access (reads and writes) MUST go through the Service (or UseCase/Query) layer to encapsulate business constraints, security, and filtering in one place. Controllers or other presentation components MUST NEVER access Repositories directly.

## 9. API Boundaries & Object Passing

You MAY pass whole objects (DTOs, Entities) intra-service to avoid the "Long Parameter List" anti-pattern. However, you MUST NEVER pass complete entity/domain objects across inter-service API boundaries (use strict integration DTOs instead).

## 10. Single Responsibility Principle (SRP)

Classes/methods must have exactly ONE reason to change. Operation = atomic business logic. Service = orchestration. Mapper = isolated conversion logic.

## 11. JPA Persistence Best Practices

When saving entities, ALWAYS capture and use the instance returned by `repository.save()`. Discard the original object reference to avoid detached entity bugs.

## 12. Single Level of Abstraction

High-level methods must read like a narrative. Bury implementation details inside small, descriptively-named private methods.

## 13. Controller vs. Service Responsibilities

- **Controller:** Validates input (`@Valid`), delegates to Service, builds DTO HTTP response. NO logic, NO repos.
- **Service:** Executes business logic. Throws domain exceptions directly (e.g. `EntityNotFoundException`).
- **CRITICAL:** Controllers MUST NOT perform `null` checks on service responses to determine HTTP 404s. `GlobalApiExceptionHandler` must handle domain exceptions and map to status codes.

## 14. Class Purpose Documentation (CPUD)

Keep PRODUCTION classes extremely clean from text boilerplate. The ONLY allowed JavaDoc tag is `@purpose`. Keep it minimal: one concise sentence explaining what it does and why (e.g. `/** @purpose Orchestrates the checkout process. */`).
**CRITICAL:** This does NOT apply to test classes. Test classes must follow an absolute **Zero Comments Policy** (no JavaDoc, no class-level comments).

## 15. Layer Dependency Rules

Dependencies flow INWARD. The domain layer must be framework-free.

- NO `org.springframework.web.*` in the domain.
- Domain exceptions are plain `RuntimeException` subclasses without HTTP mapping annotations.
- HTTP mapping belongs to `GlobalApiExceptionHandler`.

## 16. Imports vs FQN

ALWAYS use `import`. Avoid fully-qualified class names (FQN) inline unless resolving a naming collision (e.g. `java.util.Date` vs `java.sql.Date`).

## 17. Nested Idempotency Keys

Never reuse an outer/parent idempotency key for nested sub-operations. Derive inner keys deterministically (e.g. `UUID.nameUUIDFromBytes((parentKey + "sub").getBytes())`).

## 18. Backend vs Frontend Boundary

**Strict Layer SRP:** Backend = business logic, auth, domain data. Frontend = UI, formatting (dates/i18n), presentation. NEVER leak frontend presentation concerns into backend APIs. NEVER leak backend business rules into frontend execution. Solve problems ONLY in their native domain.
