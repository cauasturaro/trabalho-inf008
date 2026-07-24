# INF008 — E-commerce Order Processing (Microkernel + JavaFX)

Order processing system built as a plugin on top of the provided microkernel
architecture, using JavaFX for the UI and MariaDB for persistence.

## Requirements

- JDK 11+ (tested with newer JDKs targeting 11)
- Maven 3.x
- Docker + Docker Compose (for the MariaDB database)

## Running

```bash
# 1. Start the database (first run also loads the seed data)
docker compose up -d

# 2. Build everything (also runs the tests)
cd microkernel
mvn install

# 3. Run the application
mvn exec:java -pl app
```

The e-commerce plugin is packaged as a fat jar (`EcommercePlugin.jar`) into
`microkernel/plugins/`, where the kernel's `PluginController` discovers and
loads it at startup.

## Architecture

The microkernel (`interfaces` + `app`) is unchanged. All functionality lives in
the plugin module `microkernel/plugins/ecommerce`:

| Layer | Package | Contents |
|-------|---------|----------|
| Integration | `br.edu.ifba.inf008.plugins` | `EcommercePlugin` (implements `IPlugin`, wires layers, registers tabs/menus via `IUIController`) |
| Domain | `...plugins.ecommerce.domain` | `Product`, `Customer`, `Cart`/`CartItem`, `Order`/`OrderItem` (composition), `Payable`, `DiscountPolicy`, `ShippingPolicy` + concrete strategies, domain exceptions |
| Services | `...plugins.ecommerce.service` | `ProductService`, `CartService`, `CheckoutService`, `OrderService`, `PolicyFactory` |
| Persistence | `...plugins.ecommerce.persistence` | `Database` + repository interfaces and JDBC implementations |
| UI | `...plugins.ecommerce.ui` | JavaFX views (`ProductsView`, `CartView`, `CheckoutView`) with thin event handlers |

Order total: `grand total = items subtotal - discounts + shipping`, where each
part is computed by polymorphic strategy objects (`DiscountPolicy`,
`ShippingPolicy`) and payment is processed through `Payable` implementations
(`CreditCardPayment`, `PixPayment`, `BoletoPayment`).

Stock rules: available stock is derived from `stock_movements`
(`INBOUND - OUTBOUND - RESERVED`). A confirmed (PAID) order writes OUTBOUND
movements; a PENDING order (boleto) writes RESERVED; cancelled or invalid
orders do not touch stock.

## Tests

```bash
cd microkernel
mvn test
```

Unit tests cover the domain and services (with in-memory fakes). JDBC
integration tests run automatically when the database is reachable and are
skipped otherwise.
