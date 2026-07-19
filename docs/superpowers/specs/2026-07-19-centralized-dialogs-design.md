# Design Specification: Centralized Dialogs Framework

## Context & Goal
VAOPS requires a clean, reusable dialog architecture to handle common user flows such as user confirmations, informative alerts, and system/API error messages.
To prevent duplicate boilerplate across modules, this design introduces a **Wrapper Component + Service Singleton** pattern using `@angular/cdk/dialog`.
Calling dialogs from business logic components will require a single line of code (e.g., `this.dialogService.confirm(...)`).

## Style Integration
* **Theme**: Strict Industrial Theme. Matches the existing login card.
* **Borders/Corners**: Square corners (`rounded-none`), thin outlines (`border border-outline-variant`), and flat styling.
* **Typography**: Outfit (`font-body-md`, `font-headline-md`, `font-button`).
* **Backdrop**: Smooth dimming backdrop (`rgba(27, 28, 30, 0.4)`) with minor blur.

---

## Proposed Changes

### Components & Services

#### [NEW] [dialog.service.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/dialog.service.ts)
A singleton service that manages the opening and configuration of dialogs.
* Methods:
  * `confirm(title: string, message: string): Observable<boolean>`
  * `info(title: string, message: string): Observable<void>`
  * `error(title: string, message: string): Observable<void>`

#### [NEW] [base-dialog.component.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/base-dialog/base-dialog.component.ts)
A generic layout component that serves as the wrapper for all modal dialogs.
* Input Signals:
  * `title: InputSignal<string>`
* Output Signals:
  * `close: OutputEmitterRef<void>`
* Templates: Contains layout for Dialog Header, Body, and Footer (Actions select area).

#### [NEW] [confirm-dialog.component.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/confirm-dialog/confirm-dialog.component.ts)
Content component for confirm dialogs. Yields a Boolean result (`true` for Confirm, `false` for Cancel).

#### [NEW] [info-dialog.component.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/info-dialog/info-dialog.component.ts)
Content component for informative dialogs. Yields no result (`void`).

#### [NEW] [error-dialog.component.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/error-dialog/error-dialog.component.ts)
Content component for warning/error dialogs. Styled with error colors and icon.

#### [MODIFY] [styles.css](file:///home/maithehao/Workspace/projects/vaops/frontend/src/styles.css)
Add custom backdrop rules for the dialog modal.

---

## Verification Plan
* Manual Verification:
  * Create a temporary verification route/button in `auth.component.html` to trigger the different dialogs.
  * Verify UI alignment, border-radius (square), colors, and hover transitions.
  * Verify the overlay block and clicking behavior (clicking backdrop does not close).
* Automated Verification:
  * Run build checks using `npm run build` or `pnpm build` to verify there are no TypeScript or bundling compilation errors.
