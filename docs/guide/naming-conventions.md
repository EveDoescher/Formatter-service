# Naming Conventions

This project uses predictable names so components, profiles, samples, and tests
can be created by humans or AI without inventing local conventions.

## Core Rule

Java files should follow:

```text
<ComponentName><Responsibility>.java
```

Examples:

```text
ApprovalSheetComponent.java
ApprovalSheetRenderer.java
ApprovalSheetLayoutAssembler.java
ApprovalSheetTextTemplateResolver.java
ApprovalSheetProfileContentValidator.java
ApprovalSheetComponentRule.java
ApprovalSheetStyleMapping.java
ApprovalSheetRequest.java
```

Avoid vague names:

```text
Manager
Processor
Handler
Helper
Utils
Common
Base
Impl
New
Old
V2
Temp
Final
```

Use one of those only when the responsibility is genuinely established by the
surrounding code and cannot be named more precisely.

## Formats By Context

| Context | Format | Example |
| --- | --- | --- |
| Java class | PascalCase | `ApprovalSheetRenderer` |
| Java package | lowercase | `approvalsheet` |
| JSON component id | lowerCamelCase | `approvalSheet` |
| Docs and samples | kebab-case | `approval-sheet-short.json` |
| Branch | kebab-case | `feature/approval-sheet` |
| Test class | tested class + `Test` | `ApprovalSheetRendererTest` |

The same component should therefore appear as:

```text
componentId: approvalSheet
Java prefix: ApprovalSheet
package: approvalsheet
sample dir: approval-sheet
branch: feature/approval-sheet
```

## Component Structure

### Domain

```text
src/main/java/com/abntbuilder/formatter/document/component/<component-package>
```

Use semantic model names:

```text
ApprovalSheetComponent.java
ApprovalSheetNature.java
ApprovalCommitteeMember.java
ApprovalEvent.java
```

Domain classes must not contain layout, DOCX, profile loading, request, or
controller concerns.

### API Request

```text
src/main/java/com/abntbuilder/formatter/api/export/dto/request
```

Use:

```text
ApprovalSheetRequest.java
ApprovalSheetNatureRequest.java
ApprovalCommitteeMemberRequest.java
ApprovalEventRequest.java
```

Request classes convert external input to the semantic domain model. They must
not receive low-level layout values such as line capacities, physical slots, or
renderer decisions.

### Profile Model

```text
src/main/java/com/abntbuilder/formatter/profile/model/component/<component-package>
```

Use:

```text
ApprovalSheetComponentRule.java
ApprovalSheetStyleMapping.java
ApprovalSheetTextTemplateRule.java
ApprovalSheetCommitteeMemberRule.java
```

Component-specific profile rules stay in the component package. Shared layout
rules stay in shared profile/layout packages.

### Rendering

```text
src/main/java/com/abntbuilder/formatter/rendering/component/<component-package>
```

Use:

```text
ApprovalSheetRenderer.java
ApprovalSheetLayoutAssembler.java
ApprovalSheetLayoutCalculator.java
ApprovalSheetProfileContentValidator.java
ApprovalSheetTextTemplateResolver.java
```

Responsibilities:

```text
Renderer: orchestrates component rendering.
Assembler: builds layout input from semantic content and profile rules.
Calculator: resolves rule and calculates the layout plan.
Validator: validates content and profile compatibility before rendering.
Resolver: resolves ids, templates, or other rule-driven values.
```

## Responsibility Suffixes

| Suffix | Use |
| --- | --- |
| `Component` | Renderable semantic model |
| `Request` | API input |
| `Response` | API output |
| `Command` | Application-layer input |
| `Rule` | Profile rule |
| `StyleMapping` | Semantic item to style id mapping |
| `TextTemplateRule` | Profile text templates |
| `Renderer` | Component to DOCX blocks |
| `Assembler` | Intermediate layout input assembly |
| `Calculator` | Layout plan calculation |
| `Resolver` | Rule, id, or template resolution |
| `Validator` | Contract validation before rendering |
| `Provider` | Configuration or data source |
| `Registry` | Implementation lookup |
| `Mapper` | Technical model conversion |
| `Writer` | Concrete output writing |
| `Exception` | Explicit semantic or technical error |

## Samples

Component samples use:

```text
docs/samples/<component-id-kebab>/<component-id-kebab>-<scenario>.json
```

Examples:

```text
docs/samples/approval-sheet/approval-sheet-short.json
docs/samples/approval-sheet/approval-sheet-long-title.json
docs/samples/approval-sheet/approval-sheet-many-committee-members.json
docs/samples/approval-sheet/approval-sheet-overflow.json
```

Composed samples use:

```text
docs/samples/composed/<components-in-document-order>.json
```

Examples:

```text
docs/samples/composed/cover-and-title-page.json
docs/samples/composed/cover-title-page-approval-sheet.json
```

Sample JSON files must be valid UTF-8, formatted with two-space indentation, and
use accented Portuguese text when the scenario represents real user content.

## Practical Rule For New Files

Before adding a file, answer:

```text
1. Which component or shared mechanism?
2. Which layer?
3. Which responsibility?
```

Then name the file from those answers.

Example:

```text
Component: ApprovalSheet
Layer: rendering/component
Responsibility: resolve templates
Name: ApprovalSheetTextTemplateResolver.java
```

Example:

```text
Mechanism: single-page
Layer: rendering/layout
Responsibility: distribute gaps
Name: SinglePageGapDistributor.java
```
