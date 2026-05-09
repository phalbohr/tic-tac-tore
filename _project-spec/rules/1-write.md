# AI Code Conventions Guide

## Table of Contents

1. [Technical Text Language](#2-technical-text-language)
2. [Optimistic Locking](#4-optimistic-locking)
3. [@Transactional, @Retryable, and @Idempotent](#5-transactional-retryable-and-idempotent)
4. [Fail-Fast Principle](#6-fail-fast-principle)
5. [Tell, Don't Ask Principle](#7-tell-dont-ask-principle)
6. [Strict Layering: Reads/Writes via Service](#8-strict-layering-readswrites-via-service)
7. [API Boundaries & Object Passing](#9-api-boundaries--object-passing)
8. [Single Responsibility Principle (SRP)](#10-single-responsibility-principle-srp)
9. [JPA Persistence Best Practices](#11-jpa-persistence-best-practices)
10. [Single Level of Abstraction](#12-single-level-of-abstraction)
11. [Layer Responsibilities](#13-layer-responsibilities)
12. [Layer Dependency Rules](#15-layer-dependency-rules)
13. [Backend vs Frontend Boundary](#18-backend-vs-frontend-boundary)

## 1. Technical Text Language

All developer-facing text (comments, logs, exceptions, APIs, tests) MUST be in English.

## 2. Optimistic Locking

Mutable `@Entity` classes must declare `@Version`. Use the object wrapper `Long` or `Integer` (not primitive `long`) to ensure JPA `save()` handles the "new entity" lifecycle correctly via `null` checks. Do NOT map the column explicitly (`@Column` is forbidden).

## 3. @Transactional, @Retryable, and @Idempotent

**STRICT LAYERING REQUIRED:**

- **Inner Operation:** `@Idempotent` + `@Transactional` (atomic logic).
- **Outer Service:** `@Retryable` ONLY. Must call the inner operation so each retry opens a fresh transaction.
- **Read-Only Service:** `@Transactional(readOnly = true)`.
  **CRITICAL:** NEVER combine `@Retryable` and `@Transactional` on the same method.

## 4. Fail-Fast Principle

Place ALL guard clauses and parameter validations at the absolute top of the method before business logic.

## 5. Tell, Don't Ask Principle

Business logic mutates state INSIDE domain objects. Call `stock.decreaseQuantity(x)` on entities. Do not extract state into the service to make decisions externally.

## 6. Strict Layering: Reads/Writes via Service

ALL database access (reads and writes) MUST go through the Service (or UseCase/Query) layer to encapsulate business constraints, security, and filtering in one place. Controllers or other presentation components MUST NEVER access Repositories directly.

## 7. API Boundaries & Object Passing

You MAY pass whole objects (DTOs, Entities) intra-service to avoid the "Long Parameter List" anti-pattern. However, you MUST NEVER pass complete entity/domain objects across inter-service API boundaries (use strict integration DTOs instead).

## 8. Single Responsibility Principle (SRP)

Classes/methods must have exactly ONE reason to change.

## 9. JPA Persistence Best Practices

When saving entities, ALWAYS capture and use the instance returned by `repository.save()`. Discard the original object reference to avoid detached entity bugs.

## 10. Single Level of Abstraction

High-level methods must read like a narrative. Bury implementation details inside small, descriptively-named private methods.

## 11. Layer Responsibilities

- **Controller:** Validates input (`@Valid`), delegates to Service, builds DTO HTTP response. NO logic, NO repos.
- **Service:** Orchestration (when Operations are used). Throws domain exceptions directly (e.g. `EntityNotFoundException`).
- **Operation:** Atomic business logic. Annotated `@Idempotent` + `@Transactional` (see rule 3).
- **Mapper:** Isolated conversion logic only. No business rules, no side effects.
- **CRITICAL:** Controllers MUST NOT perform `null` checks on service responses to determine HTTP 404s. `GlobalApiExceptionHandler` must handle domain exceptions and map to status codes.

## 12. Layer Dependency Rules

Dependencies flow INWARD. The domain layer must be framework-free.

- NO `org.springframework.web.*` in the domain.
- Domain exceptions are plain `RuntimeException` subclasses without HTTP mapping annotations.
- HTTP mapping belongs to `GlobalApiExceptionHandler`.

## 13. Backend vs Frontend Boundary

**Strict Layer SRP:** Backend = business logic, auth, domain data. Frontend = UI, formatting (dates/i18n), presentation. NEVER leak frontend presentation concerns into backend APIs. NEVER leak backend business rules into frontend execution. Solve problems ONLY in their native domain.
