# Ledger Architecture

## Purpose

The customer ledger is the authoritative record of customer debt and business financial activity.
Customer balances are derived from immutable business events and are never edited directly.

## Core Model

Every significant business action produces a `BusinessEvent` row.

Examples:

- `PURCHASE_RECORDED`
- `PURCHASE_CONFIRMED`
- `SALE_RECORDED`
- `PAYMENT_RECEIVED`
- `PURCHASE_REVERSED`
- `SALE_REVERSED`
- `PAYMENT_REVERSED`

The event stream is the source of truth. Entities such as `Sale`, `Payment`, and `Purchase` remain useful operational records, but the ledger is what drives financial reporting.

## Data Flow

1. A receipt, sale, or payment is captured.
2. The service validates the request and persists the business record.
3. The service writes an immutable `BusinessEvent`.
4. The customer ledger service reads ordered events for a customer.
5. Running balance is calculated from the event stream.
6. Customer balances, statements, and dashboard debt figures are derived from that ledger.

## Statement Generation

The statement endpoint supports:

- `startDate`
- `endDate`
- `transactionType`
- server-side pagination for the ledger view

The controller does not compute balances. It delegates to the ledger service, which reads the event stream, batches related sale/payment lookups, and returns a derived statement.

## Balance Calculation Rules

- `SALE_RECORDED` increases the running balance by the sale amount owing.
- `PAYMENT_RECEIVED` decreases the running balance by the payment amount.
- `SALE_REVERSED` reverses a sale by crediting the sale amount.
- `PAYMENT_REVERSED` reverses a payment by debiting the payment amount.
- The final running balance is the current outstanding amount.

## Reversal Rules

Reversals never edit history.

Instead:

- the original business record remains intact
- a compensating `BusinessEvent` is created
- inventory is restored or reduced only when the business rule requires it
- the ledger remains auditable because the original and reversal are both visible

## Purchase Confirmation Workflow

Purchases are captured first and confirmed later.

1. Upload receipt or enter manually.
2. Store OCR text and confidence when available.
3. Keep the purchase in `PENDING_REVIEW`.
4. Confirm it explicitly.
5. Increase stock only after confirmation.

This prevents inventory from changing when a receipt is merely uploaded.

## Migration Notes

- `V2__immutable_ledger_and_purchase_confirmation.sql` introduces the event table and purchase confirmation fields.
- `V3__ledger_indexes_and_drop_customer_balance.sql` removes `customers.outstanding_balance` and adds the event indexes required for statement queries.
- New ledger features must add migrations rather than mutating historical schema assumptions in code.

## Contributor Rules

- Never update historical financial events.
- Never delete financial history.
- Never store customer debt as the source of truth on `Customer`.
- Never let the frontend be the only enforcement layer for financial rules.
- Any correction must be a compensating event.
- Any report must read from the ledger or a derived projection.

## Integration Rules For Future Features

- Any new financial workflow must write a `BusinessEvent`.
- Any correction must be represented by a compensating event.
- Any report must read from the ledger or from totals derived from the ledger.
- No feature should persist a manual customer balance update.
