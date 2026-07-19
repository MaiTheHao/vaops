# Design Specification: Centralized Dialogs Framework (Factory + Strategy + Wrapper Pattern)

## Context & Goal
VAOPS requires a clean, reusable dialog architecture to handle common user flows such as user confirmations, informative alerts, and system/API error messages.
To separate UI details, third-party libraries, and business logic, we use a hybrid design combining the **Strategy Pattern** (for formatting/parameters) with a **Factory Pattern** and a **Wrapper Pattern** (for library isolation).
This design fully encapsulates `@angular/cdk/dialog`. Components in the codebase will only consume a single `DialogFactoryService`.

## Style Integration
* **Theme**: Strict Industrial Theme. Matches the existing login card.
* **Borders/Corners**: Square corners (`rounded-none`), thin outlines (`border border-outline-variant`), and flat styling.
* **Typography**: Outfit (`font-body-md`, `font-headline-md`, `font-button`).
* **Icons**: Use `@lucide/angular` icons tailored to each strategy type:
  * Info: `LucideInfo`
  * Confirm: `LucideCircleHelp`
  * Error: `LucideCircleAlert`
* **Backdrop**: Smooth dimming backdrop (`rgba(27, 28, 30, 0.4)`) with minor blur.

---

## Proposed Changes

### File Layout

All files will be created under `frontend/src/app/shared/component/dialogs/`:

```text
frontend/src/app/shared/component/dialogs/
├── core/
│   └── cdk-dialog.wrapper.ts         # Wrapper hiding CDK dialog package
├── strategy/
│   ├── dialog.strategy.ts            # Strategy Interface
│   ├── confirm.strategy.ts           # Confirm Strategy
│   ├── info.strategy.ts              # Info Strategy
│   └── error.strategy.ts             # Error Strategy
├── component/
│   └── generic-dialog.component.ts   # Standalone UI Component with Tailwind CSS v4
└── dialog-factory.service.ts         # Central Factory Service
```

### Components & Services Specifications

#### [NEW] [cdk-dialog.wrapper.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/core/cdk-dialog.wrapper.ts)
* Wraps the opening of `@angular/cdk/dialog` Dialog ref.
* Configures default modal options (backdrop, dimensions, ESC/Backdrop close behavior).

#### [NEW] [dialog.strategy.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/strategy/dialog.strategy.ts)
* Common interface:
  ```typescript
  import { Observable } from 'rxjs';
  import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';

  export interface DialogStrategy<T = any> {
    execute(wrapper: CdkDialogWrapper, title: string, message: string, extra?: any): Observable<T>;
  }
  ```

#### [NEW] [confirm.strategy.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/strategy/confirm.strategy.ts)
* Handles confirmation dialog setup. Maps dialog closed stream to `boolean`.

#### [NEW] [info.strategy.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/strategy/info.strategy.ts)
* Handles standard announcements/information alerts. Maps stream to `void`.

#### [NEW] [error.strategy.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/strategy/error.strategy.ts)
* Handles warning/API error popups. Maps stream to `void`.

#### [NEW] [generic-dialog.component.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/component/generic-dialog.component.ts)
* A standalone UI component displaying the dialog modal.
* Layout: Styled container, header title, type icon, scrollable body description, and context-dependent buttons.
* Integrates `lucide-angular` icons for visual indication.

#### [NEW] [dialog-factory.service.ts](file:///home/maithehao/Workspace/projects/vaops/frontend/src/app/shared/component/dialogs/dialog-factory.service.ts)
* Single entry point to invoke all dialogs.
* Strategy map resolves dynamically based on requested type: `confirm`, `info`, `error`.

#### [MODIFY] [styles.css](file:///home/maithehao/Workspace/projects/vaops/frontend/src/styles.css)
* Append global classes for dialog backdrop and custom configuration.

---

## Verification Plan

### Manual Verification
* Create temporary triggers in the application to render each Dialog strategy type.
* Verify keyboard accessibility (Enter/Esc behavior), background scrolling locking, and backdrop blur.

### Automated Verification
* Verify building via `npm run build` or `pnpm build` completes cleanly.
