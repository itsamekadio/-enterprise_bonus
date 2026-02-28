# Enterprise Bonus: Spring Scope Showcase

This repository showcases the internal mechanics of Spring's bean scoping system. It specifically focuses on the necessity of Scoped Proxies, the role of ThreadLocal in context management, and the challenges of context propagation in asynchronous execution.

## The Scope Rulebook

When Spring performs DI (dependency injection), it has to answer three lifecycle questions for every dependency:

1. Which instance do I inject? (Selection)
   Spring first resolves what bean definition matches. Then scope determines which concrete instance is appropriate right now. For Request/Session, it means the one associated with the current context.
   
2. When do I create it? (Creation time)
   - Singleton: Eagerly at startup (context refresh).
   - Request/Session: Lazily when first needed within that request/session.
   - Prototype: Each time Spring resolves it.

3. How long do I keep it? (Lifetime + Storage)
   - Singleton: Container's singleton cache; lives until the context is closed.
   - Request: Request attributes; lives until the HTTP request completes.
   - Session: Session attributes; lives until session invalidation/timeout.
   - Prototype: Spring typically does not keep it; it hands it to you and stops tracking it.

---

## Case 1: @RequestScope bean injected into a @Singleton

### What happens
A Singleton is created once at application startup. A request-scoped bean can only exist during an active HTTP request. If Spring tries to inject the real request bean into the singleton at startup:
- There is no HTTP request
- There is no request scope active
- Injection fails

### Typical error (without proxy)
- `ScopeNotActiveException`: Scope 'request' is not active for the current thread
- `IllegalStateException`: No thread-bound request found

### Why this happens (internal flow)
1. Application starts.
2. Spring creates all singleton beans.
3. While creating a service, it tries to inject the request-scoped data.
4. Spring checks `RequestContextHolder` (which stores request attributes in a `ThreadLocal`).
5. At startup, there is no request thread -> `ThreadLocal` is empty -> Scope not active.

### Solution: Scoped Proxy
Using `@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)` creates a CGLIB proxy.
- **Startup**: The proxy is injected. No real request bean is created yet, so startup succeeds.
- **Request Phase**: When the proxy is called, it looks into `RequestContextHolder`, gets the current request from `ThreadLocal`, and delegates to the real instance stored in request attributes.

---

## Case 2: @SessionScope bean injected into a @Singleton

### What happens
Session resolution also requires `RequestContextHolder` to find the `HttpServletRequest` and subsequently the `HttpSession`. Without a proxy, injection fails at startup because no session context exists.

### Full internal flow with proxy
1. Request hits `DispatcherServlet`.
2. Request bound to `ThreadLocal` via `RequestContextHolder`.
3. Proxy intercepts calls.
4. Session scope implementation reads current request from `ThreadLocal`, calls `request.getSession()`, and retrieves or creates the bean inside the session.

---

## Case 3: @Scope("prototype") injected into @Singleton

### The Trap (Without Proxy)
The Singleton is created once. During creation, Spring creates ONE prototype instance, injects it, and reuses it forever. There is no exception, but the logic is broken because you don't get new instances.

### The Fix with Proxy
The proxy intercepts every method call and asks the container for a fresh prototype instance every time.

---

## Case 4: @Async and ThreadLocal Boundaries

### The Problem
Request/Session scopes depend on `RequestContextHolder` (`ThreadLocal`). Since `@Async` runs in a different thread, and `ThreadLocal` values are NOT automatically copied, the context is lost.
- Inside an `@Async` method, `RequestContextHolder.getRequestAttributes()` returns null.
- Result: `IllegalStateException: No thread-bound request found`.

### Proper Async Handling
1. **Manual Context Propagation**: Capture `RequestAttributes` in the main thread, pass them to the async method, and set them manually in the child thread via `RequestContextHolder`.
2. **TaskDecorator**: Configure a `TaskDecorator` in the `TaskExecutor` to automatically bridge the `ThreadLocal` gap.

---

## Mental Model Summary

- Singleton holds a proxy.
- Proxy asks: "Who is my real target right now?"
- For Request/Session: Looks inside `ThreadLocal`-bound request context.
- For Prototype: Just asks container for a new instance.
- For Async: `ThreadLocal` is empty unless context is propagated.

| Scope | Needs ThreadLocal? | Error without proxy | Works with proxy? | Async safe? |
| :--- | :--- | :--- | :--- | :--- |
| Request | Yes | ScopeNotActiveException | Yes | No (needs propagation) |
| Session | Yes | ScopeNotActiveException | Yes | No (needs propagation) |
| Prototype | No | No error (Logic Trap) | Yes | Yes |

---

## Relationship to Enterprise Features

Scope is Spring's core tool to make guarantees practical in senior-level enterprise applications.

### Concurrency + Safety (Multi-user systems)
Enterprise apps handle many users at once. Scope prevents accidental shared state. stateless services are singletons (fast/scalable), while user-specific state is in request/session scope to ensure isolation under concurrency.

### Web Context Features
Needs like security principal/user identity, tenant ID (multi-tenancy), locale, and request correlation IDs for tracing are naturally request-bound. Scopes allow components to "just inject" a context object that resolves the correct value per request.

### Cross-Cutting Concerns (Transactions, Security, Observability)
Spring's AOP-style features depend on context active "around" a call:
- `@Transactional` binds transaction resources.
- Spring Security binds authentication.
- MDC logging binds correlation info.
These boundaries match scoping: a bounded lifetime where context must be consistent.

### Performance + Resource Management
- Singletons reduce object churn for stateless components.
- Request/session scopes confine stateful objects to the smallest necessary lifetime.
- Prototype supports "fresh object per use" patterns (e.g., builders, per-task state).

### Async/Distributed Reality
In enterprise apps, async execution is common. The reality check is that the "current request" doesn't exist on worker threads, requiring the context propagation strategies mentioned above to maintain correctness across the thread boundary.

---

## Demonstration Note: Using @Lazy
In this project, "Broken" scenarios use the **@Lazy** annotation. This allows the application to start (bypassing the startup `BeanCreationException`) so that you can trigger and observe the **Runtime Crash** inside the browser UI, demonstrating the internal flow and failure points.
