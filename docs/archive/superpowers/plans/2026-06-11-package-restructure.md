# Package Restructure — Vertical Slices Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the package structure from layered-by-technical-concern to vertical slices by academic component, with a shared `engine/` for reusable processing engines and `infrastructure/` for Spring adapters.

**Architecture:** Each academic component (`cover`, `titlepage`, `approvalsheet`, `bodycontent`) owns its `api/`, `domain/`, `profile/`, and `rendering/` sub-packages. Shared processing engines live in `engine/singlepage/` and `engine/text/`. All Spring Boot wiring (controller, service, config) lives in `infrastructure/`.

**Tech Stack:** Java 21, Spring Boot 4, Maven (`./mvnw`)

---

## Packages that do NOT move

These packages are already in their target location per the spec — do not touch them:

- `profile/model/` (DocumentProfile, PageRule, StyleRule, etc.)
- `profile/model/layout/singlepage/` (profile-level layout rule *models* — HorizontalPlacementRule, SinglePageGroupRule, etc.)
- `profile/loading/` (ProfileDefinition)
- `profile/resolution/` (ProfileProvider, ClasspathJsonProfileProvider, etc.)
- `output/docx/api/` and `output/docx/docx4j/`
- `shared/exception/` and `shared/measurement/`

> Note: `profile/model/layout/singlepage/` holds **profile rule models** (data shapes).
> `engine/singlepage/` (Task 4) holds the **engine implementation** (algorithms). They are different.

---

## Package mapping reference

```
OLD                                          → NEW
document.component.DocumentComponent        → shared
document.component.ComponentType            → shared
document.component.cover                    → cover.domain
document.component.titlepage                → titlepage.domain
document.component.approvalsheet            → approvalsheet.domain
document.component.bodycontent (root)       → bodycontent.domain
document.component.bodycontent (citation)   → bodycontent.domain.citation
document.component.bodycontent (inline)     → bodycontent.domain.inline
document.component.bodycontent (display)    → bodycontent.domain.display
profile.model.component.ComponentRule       → profile.model
profile.model.component.ComponentContentBindings → profile.model
profile.model.component.cover               → cover.profile
profile.model.component.titlepage           → titlepage.profile
profile.model.component.approvalsheet       → approvalsheet.profile
profile.model.component.bodycontent         → bodycontent.profile
rendering.layout.singlepage                 → engine.singlepage
rendering.layout.text                       → engine.text
rendering.component.cover (+ cover/layout/) → cover.rendering
rendering.component.titlepage               → titlepage.rendering
rendering.component.approvalsheet           → approvalsheet.rendering
rendering.component.bodycontent             → bodycontent.rendering
rendering.component.ComponentRenderer       → infrastructure.rendering
rendering.component.ComponentRendererRegistry → infrastructure.rendering
rendering.document                          → infrastructure.rendering
api.export.dto.request (cover DTOs)         → cover.api
api.export.dto.request (titlepage DTOs)     → titlepage.api
api.export.dto.request (approvalsheet DTOs) → approvalsheet.api
api.export.dto.request (bodycontent DTOs)   → bodycontent.api
api.export.dto.request (shared DTOs)        → infrastructure.api
api.export.controller                       → infrastructure.api
api.error                                   → infrastructure.api
api.export.dto.response                     → infrastructure.api
application.export                          → infrastructure.application
config                                      → infrastructure.config
```

---

## Conventions used throughout this plan

```
BASE=src/main/java/com/abntbuilder/formatter
TEST=src/test/java/com/abntbuilder/formatter
PKG=com.abntbuilder.formatter
```

For every file move the steps are always: (1) create dir, (2) update `package` declaration in the file, (3) update all `import` statements across the whole src tree, (4) verify build, (5) commit. Steps for imports use:

```bash
find src -name "*.java" -exec sed -i \
  's/import PKG\.OLD\./import PKG.NEW./g' {} \;
```

---

### Task 1: Move shared base types — `DocumentComponent` and `ComponentType`

These are imported by nearly every layer. Moving them first gives all later tasks a stable target.

**Files:**
- Move: `$BASE/document/component/DocumentComponent.java` → `$BASE/shared/DocumentComponent.java`
- Move: `$BASE/document/component/ComponentType.java` → `$BASE/shared/ComponentType.java`
- Move (test): none — no dedicated tests for these two files

- [ ] **Step 1: Update package declarations**

```bash
sed -i 's/^package com\.abntbuilder\.formatter\.document\.component;$/package com.abntbuilder.formatter.shared;/' \
  src/main/java/com/abntbuilder/formatter/document/component/DocumentComponent.java

sed -i 's/^package com\.abntbuilder\.formatter\.document\.component;$/package com.abntbuilder.formatter.shared;/' \
  src/main/java/com/abntbuilder/formatter/document/component/ComponentType.java
```

- [ ] **Step 2: Move the files**

```bash
mv src/main/java/com/abntbuilder/formatter/document/component/DocumentComponent.java \
   src/main/java/com/abntbuilder/formatter/shared/DocumentComponent.java

mv src/main/java/com/abntbuilder/formatter/document/component/ComponentType.java \
   src/main/java/com/abntbuilder/formatter/shared/ComponentType.java
```

- [ ] **Step 3: Update imports across the entire source tree**

```bash
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.document\.component\.DocumentComponent;/import com.abntbuilder.formatter.shared.DocumentComponent;/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.document\.component\.ComponentType;/import com.abntbuilder.formatter.shared.ComponentType;/g' {} \;
```

- [ ] **Step 4: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: move DocumentComponent and ComponentType to shared package"
```

---

### Task 2: Move component domain models

Move the four component semantic models to their own `domain/` slices. The `bodycontent` domain gets split into sub-packages.

**Files:**
- `$BASE/document/component/cover/CoverComponent.java` → `$BASE/cover/domain/`
- `$BASE/document/component/titlepage/{TitlePageComponent,TitlePageNature,AcademicPerson}.java` → `$BASE/titlepage/domain/`
- `$BASE/document/component/approvalsheet/{ApprovalSheetComponent,ApprovalCommitteeMember,ApprovalEvent,ApprovalSheetNature}.java` → `$BASE/approvalsheet/domain/`
- `$BASE/document/component/bodycontent/{BodyBlock,BodySection,BodyContentComponent}.java` → `$BASE/bodycontent/domain/`
- `$BASE/document/component/bodycontent/{BodyCitation,BodyCitationCall,BodyCitationType,BodyCitationMode,CitationSource,CitationAuthor,CitationAuthorType}.java` → `$BASE/bodycontent/domain/citation/`
- `$BASE/document/component/bodycontent/{BodyParagraph,BodyInline,BodyText,BodyQuoteText,BodyQuoteType}.java` → `$BASE/bodycontent/domain/inline/`
- `$BASE/document/component/bodycontent/{BodyFigure,BodyTable,BodyTableColumn,BodyTableRow,BodyImageSource,ImageSourceType,NumberedDisplayObject}.java` → `$BASE/bodycontent/domain/display/`
- Tests: mirror the same moves in `$TEST/document/component/`

- [ ] **Step 1: Create target directories**

```bash
mkdir -p src/main/java/com/abntbuilder/formatter/cover/domain
mkdir -p src/main/java/com/abntbuilder/formatter/titlepage/domain
mkdir -p src/main/java/com/abntbuilder/formatter/approvalsheet/domain
mkdir -p src/main/java/com/abntbuilder/formatter/bodycontent/domain/citation
mkdir -p src/main/java/com/abntbuilder/formatter/bodycontent/domain/inline
mkdir -p src/main/java/com/abntbuilder/formatter/bodycontent/domain/display
mkdir -p src/test/java/com/abntbuilder/formatter/cover/domain
mkdir -p src/test/java/com/abntbuilder/formatter/titlepage/domain
mkdir -p src/test/java/com/abntbuilder/formatter/bodycontent/domain
```

- [ ] **Step 2: Update package declarations — cover**

```bash
sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.cover;$/package com.abntbuilder.formatter.cover.domain;/' \
  src/main/java/com/abntbuilder/formatter/document/component/cover/CoverComponent.java
```

- [ ] **Step 3: Update package declarations — titlepage**

```bash
for f in TitlePageComponent TitlePageNature AcademicPerson; do
  sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.domain;/' \
    src/main/java/com/abntbuilder/formatter/document/component/titlepage/${f}.java
done
```

- [ ] **Step 4: Update package declarations — approvalsheet**

```bash
for f in ApprovalSheetComponent ApprovalCommitteeMember ApprovalEvent ApprovalSheetNature; do
  sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.approvalsheet;$/package com.abntbuilder.formatter.approvalsheet.domain;/' \
    src/main/java/com/abntbuilder/formatter/document/component/approvalsheet/${f}.java
done
```

- [ ] **Step 5: Update package declarations — bodycontent root**

```bash
for f in BodyBlock BodySection BodyContentComponent; do
  sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.domain;/' \
    src/main/java/com/abntbuilder/formatter/document/component/bodycontent/${f}.java
done
```

- [ ] **Step 6: Update package declarations — bodycontent.domain.citation**

```bash
for f in BodyCitation BodyCitationCall BodyCitationType BodyCitationMode CitationSource CitationAuthor CitationAuthorType; do
  sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.domain.citation;/' \
    src/main/java/com/abntbuilder/formatter/document/component/bodycontent/${f}.java
done
```

- [ ] **Step 7: Update package declarations — bodycontent.domain.inline**

```bash
for f in BodyParagraph BodyInline BodyText BodyQuoteText BodyQuoteType; do
  sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.domain.inline;/' \
    src/main/java/com/abntbuilder/formatter/document/component/bodycontent/${f}.java
done
```

- [ ] **Step 8: Update package declarations — bodycontent.domain.display**

```bash
for f in BodyFigure BodyTable BodyTableColumn BodyTableRow BodyImageSource ImageSourceType NumberedDisplayObject; do
  sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.domain.display;/' \
    src/main/java/com/abntbuilder/formatter/document/component/bodycontent/${f}.java
done
```

- [ ] **Step 9: Move all files to their new homes**

```bash
# cover
mv src/main/java/com/abntbuilder/formatter/document/component/cover/CoverComponent.java \
   src/main/java/com/abntbuilder/formatter/cover/domain/

# titlepage
mv src/main/java/com/abntbuilder/formatter/document/component/titlepage/{TitlePageComponent.java,TitlePageNature.java,AcademicPerson.java} \
   src/main/java/com/abntbuilder/formatter/titlepage/domain/

# approvalsheet
mv src/main/java/com/abntbuilder/formatter/document/component/approvalsheet/{ApprovalSheetComponent.java,ApprovalCommitteeMember.java,ApprovalEvent.java,ApprovalSheetNature.java} \
   src/main/java/com/abntbuilder/formatter/approvalsheet/domain/

# bodycontent root
mv src/main/java/com/abntbuilder/formatter/document/component/bodycontent/{BodyBlock.java,BodySection.java,BodyContentComponent.java} \
   src/main/java/com/abntbuilder/formatter/bodycontent/domain/

# bodycontent citation
mv src/main/java/com/abntbuilder/formatter/document/component/bodycontent/{BodyCitation.java,BodyCitationCall.java,BodyCitationType.java,BodyCitationMode.java,CitationSource.java,CitationAuthor.java,CitationAuthorType.java} \
   src/main/java/com/abntbuilder/formatter/bodycontent/domain/citation/

# bodycontent inline
mv src/main/java/com/abntbuilder/formatter/document/component/bodycontent/{BodyParagraph.java,BodyInline.java,BodyText.java,BodyQuoteText.java,BodyQuoteType.java} \
   src/main/java/com/abntbuilder/formatter/bodycontent/domain/inline/

# bodycontent display
mv src/main/java/com/abntbuilder/formatter/document/component/bodycontent/{BodyFigure.java,BodyTable.java,BodyTableColumn.java,BodyTableRow.java,BodyImageSource.java,ImageSourceType.java,NumberedDisplayObject.java} \
   src/main/java/com/abntbuilder/formatter/bodycontent/domain/display/
```

- [ ] **Step 10: Move test files**

```bash
mv src/test/java/com/abntbuilder/formatter/document/component/cover/CoverComponentTest.java \
   src/test/java/com/abntbuilder/formatter/cover/domain/

mv src/test/java/com/abntbuilder/formatter/document/component/titlepage/{TitlePageComponentTest.java,TitlePageNatureTest.java,AcademicPersonTest.java} \
   src/test/java/com/abntbuilder/formatter/titlepage/domain/

mv src/test/java/com/abntbuilder/formatter/document/component/bodycontent/{BodyContentComponentTest.java,BodyCitationTest.java} \
   src/test/java/com/abntbuilder/formatter/bodycontent/domain/
```

- [ ] **Step 11: Update package declarations in test files**

```bash
sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.cover;$/package com.abntbuilder.formatter.cover.domain;/' \
  src/test/java/com/abntbuilder/formatter/cover/domain/CoverComponentTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.domain;/' \
  src/test/java/com/abntbuilder/formatter/titlepage/domain/TitlePageComponentTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.domain;/' \
  src/test/java/com/abntbuilder/formatter/titlepage/domain/TitlePageNatureTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.domain;/' \
  src/test/java/com/abntbuilder/formatter/titlepage/domain/AcademicPersonTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.domain;/' \
  src/test/java/com/abntbuilder/formatter/bodycontent/domain/BodyContentComponentTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.document\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.domain;/' \
  src/test/java/com/abntbuilder/formatter/bodycontent/domain/BodyCitationTest.java
```

- [ ] **Step 12: Update all imports across the source tree**

```bash
# cover domain
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.document\.component\.cover\./import com.abntbuilder.formatter.cover.domain./g' {} \;

# titlepage domain
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.document\.component\.titlepage\./import com.abntbuilder.formatter.titlepage.domain./g' {} \;

# approvalsheet domain
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.document\.component\.approvalsheet\./import com.abntbuilder.formatter.approvalsheet.domain./g' {} \;

# bodycontent — update the wildcard import pattern first, then specific classes
# root bodycontent classes
for cls in BodyBlock BodySection BodyContentComponent; do
  find src -name "*.java" -exec sed -i \
    "s/import com\.abntbuilder\.formatter\.document\.component\.bodycontent\.${cls};/import com.abntbuilder.formatter.bodycontent.domain.${cls};/g" {} \;
done

# citation sub-package
for cls in BodyCitation BodyCitationCall BodyCitationType BodyCitationMode CitationSource CitationAuthor CitationAuthorType; do
  find src -name "*.java" -exec sed -i \
    "s/import com\.abntbuilder\.formatter\.document\.component\.bodycontent\.${cls};/import com.abntbuilder.formatter.bodycontent.domain.citation.${cls};/g" {} \;
done

# inline sub-package
for cls in BodyParagraph BodyInline BodyText BodyQuoteText BodyQuoteType; do
  find src -name "*.java" -exec sed -i \
    "s/import com\.abntbuilder\.formatter\.document\.component\.bodycontent\.${cls};/import com.abntbuilder.formatter.bodycontent.domain.inline.${cls};/g" {} \;
done

# display sub-package
for cls in BodyFigure BodyTable BodyTableColumn BodyTableRow BodyImageSource ImageSourceType NumberedDisplayObject; do
  find src -name "*.java" -exec sed -i \
    "s/import com\.abntbuilder\.formatter\.document\.component\.bodycontent\.${cls};/import com.abntbuilder.formatter.bodycontent.domain.display.${cls};/g" {} \;
done
```

- [ ] **Step 13: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 14: Commit**

```bash
git add -A
git commit -m "refactor: move component domain models to vertical slice domain packages"
```

---

### Task 3: Move profile component rules

Move `ComponentRule`, `ComponentContentBindings` up one level in `profile.model`, and move component-specific rules to each slice's `profile/`.

**Files:**
- `$BASE/profile/model/component/ComponentRule.java` → `$BASE/profile/model/`
- `$BASE/profile/model/component/ComponentContentBindings.java` → `$BASE/profile/model/`
- `$BASE/profile/model/component/cover/*.java` → `$BASE/cover/profile/`
- `$BASE/profile/model/component/titlepage/*.java` → `$BASE/titlepage/profile/`
- `$BASE/profile/model/component/approvalsheet/*.java` → `$BASE/approvalsheet/profile/`
- `$BASE/profile/model/component/bodycontent/*.java` → `$BASE/bodycontent/profile/`
- Tests: mirror in test tree

- [ ] **Step 1: Create target directories**

```bash
mkdir -p src/main/java/com/abntbuilder/formatter/cover/profile
mkdir -p src/main/java/com/abntbuilder/formatter/titlepage/profile
mkdir -p src/main/java/com/abntbuilder/formatter/approvalsheet/profile
mkdir -p src/main/java/com/abntbuilder/formatter/bodycontent/profile
mkdir -p src/test/java/com/abntbuilder/formatter/cover/profile
mkdir -p src/test/java/com/abntbuilder/formatter/titlepage/profile
mkdir -p src/test/java/com/abntbuilder/formatter/approvalsheet/profile
```

- [ ] **Step 2: Update package declarations — ComponentRule and ComponentContentBindings**

```bash
for f in ComponentRule ComponentContentBindings; do
  sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component;$/package com.abntbuilder.formatter.profile.model;/' \
    src/main/java/com/abntbuilder/formatter/profile/model/component/${f}.java
done
```

- [ ] **Step 3: Update package declarations — cover profile**

```bash
for f in CoverComponentRule CoverLayoutRule CoverStyleMapping; do
  sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.cover;$/package com.abntbuilder.formatter.cover.profile;/' \
    src/main/java/com/abntbuilder/formatter/profile/model/component/cover/${f}.java
done
```

- [ ] **Step 4: Update package declarations — titlepage profile**

```bash
for f in TitlePageComponentRule TitlePageStyleMapping TitlePageTextTemplateRule; do
  sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.profile;/' \
    src/main/java/com/abntbuilder/formatter/profile/model/component/titlepage/${f}.java
done
```

- [ ] **Step 5: Update package declarations — approvalsheet profile**

```bash
for f in ApprovalSheetComponentRule ApprovalSheetStyleMapping ApprovalSheetTextTemplateRule ApprovalSheetCommitteeMemberRule ApprovalSheetSignatureLineRule; do
  sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.approvalsheet;$/package com.abntbuilder.formatter.approvalsheet.profile;/' \
    src/main/java/com/abntbuilder/formatter/profile/model/component/approvalsheet/${f}.java
done
```

- [ ] **Step 6: Update package declarations — bodycontent profile**

```bash
for f in BodyContentComponentRule BodyContentLayoutRule BodyContentNumberingRule BodyContentStyleMapping FigureRule TableRule ImageFitPolicy DisplayObjectContinuationLabels DisplayObjectSourcePlacement; do
  sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.profile;/' \
    src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/${f}.java
done
```

- [ ] **Step 7: Move files**

```bash
# base profile model
mv src/main/java/com/abntbuilder/formatter/profile/model/component/ComponentRule.java \
   src/main/java/com/abntbuilder/formatter/profile/model/
mv src/main/java/com/abntbuilder/formatter/profile/model/component/ComponentContentBindings.java \
   src/main/java/com/abntbuilder/formatter/profile/model/

# cover profile
mv src/main/java/com/abntbuilder/formatter/profile/model/component/cover/{CoverComponentRule.java,CoverLayoutRule.java,CoverStyleMapping.java} \
   src/main/java/com/abntbuilder/formatter/cover/profile/

# titlepage profile
mv src/main/java/com/abntbuilder/formatter/profile/model/component/titlepage/{TitlePageComponentRule.java,TitlePageStyleMapping.java,TitlePageTextTemplateRule.java} \
   src/main/java/com/abntbuilder/formatter/titlepage/profile/

# approvalsheet profile
mv src/main/java/com/abntbuilder/formatter/profile/model/component/approvalsheet/{ApprovalSheetComponentRule.java,ApprovalSheetStyleMapping.java,ApprovalSheetTextTemplateRule.java,ApprovalSheetCommitteeMemberRule.java,ApprovalSheetSignatureLineRule.java} \
   src/main/java/com/abntbuilder/formatter/approvalsheet/profile/

# bodycontent profile
mv src/main/java/com/abntbuilder/formatter/profile/model/component/bodycontent/{BodyContentComponentRule.java,BodyContentLayoutRule.java,BodyContentNumberingRule.java,BodyContentStyleMapping.java,FigureRule.java,TableRule.java,ImageFitPolicy.java,DisplayObjectContinuationLabels.java,DisplayObjectSourcePlacement.java} \
   src/main/java/com/abntbuilder/formatter/bodycontent/profile/
```

- [ ] **Step 8: Move test files**

```bash
mv src/test/java/com/abntbuilder/formatter/profile/model/component/ComponentContentBindingsTest.java \
   src/test/java/com/abntbuilder/formatter/profile/model/

mv src/test/java/com/abntbuilder/formatter/profile/model/component/cover/CoverComponentRuleTest.java \
   src/test/java/com/abntbuilder/formatter/cover/profile/
mv src/test/java/com/abntbuilder/formatter/profile/model/component/cover/CoverLayoutRuleStructureTest.java \
   src/test/java/com/abntbuilder/formatter/cover/profile/

mv src/test/java/com/abntbuilder/formatter/profile/model/component/titlepage/TitlePageComponentRuleTest.java \
   src/test/java/com/abntbuilder/formatter/titlepage/profile/
```

- [ ] **Step 9: Update package declarations in test files**

```bash
sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component;$/package com.abntbuilder.formatter.profile.model;/' \
  src/test/java/com/abntbuilder/formatter/profile/model/ComponentContentBindingsTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.cover;$/package com.abntbuilder.formatter.cover.profile;/' \
  src/test/java/com/abntbuilder/formatter/cover/profile/CoverComponentRuleTest.java
sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.cover;$/package com.abntbuilder.formatter.cover.profile;/' \
  src/test/java/com/abntbuilder/formatter/cover/profile/CoverLayoutRuleStructureTest.java

sed -i 's/^package com\.abntbuilder\.formatter\.profile\.model\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.profile;/' \
  src/test/java/com/abntbuilder/formatter/titlepage/profile/TitlePageComponentRuleTest.java
```

- [ ] **Step 10: Update all imports across the source tree**

```bash
# ComponentRule and ComponentContentBindings (moved up one level)
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.profile\.model\.component\.ComponentRule;/import com.abntbuilder.formatter.profile.model.ComponentRule;/g' {} \;
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.profile\.model\.component\.ComponentContentBindings;/import com.abntbuilder.formatter.profile.model.ComponentContentBindings;/g' {} \;

# cover profile
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.profile\.model\.component\.cover\./import com.abntbuilder.formatter.cover.profile./g' {} \;

# titlepage profile
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.profile\.model\.component\.titlepage\./import com.abntbuilder.formatter.titlepage.profile./g' {} \;

# approvalsheet profile
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.profile\.model\.component\.approvalsheet\./import com.abntbuilder.formatter.approvalsheet.profile./g' {} \;

# bodycontent profile
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.profile\.model\.component\.bodycontent\./import com.abntbuilder.formatter.bodycontent.profile./g' {} \;
```

- [ ] **Step 11: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 12: Commit**

```bash
git add -A
git commit -m "refactor: move profile component rules to vertical slice profile packages"
```

---

### Task 4: Move engine packages (singlepage + text)

Move `rendering/layout/singlepage/` and `rendering/layout/text/` to `engine/`.

**Files:**
- All files in `$BASE/rendering/layout/singlepage/` → `$BASE/engine/singlepage/`
- All files in `$BASE/rendering/layout/text/` → `$BASE/engine/text/`
- Tests: mirror in test tree

- [ ] **Step 1: Create target directories**

```bash
mkdir -p src/main/java/com/abntbuilder/formatter/engine/singlepage
mkdir -p src/main/java/com/abntbuilder/formatter/engine/text
mkdir -p src/test/java/com/abntbuilder/formatter/engine/singlepage
mkdir -p src/test/java/com/abntbuilder/formatter/engine/text
```

- [ ] **Step 2: Update package declarations — singlepage engine**

```bash
find src/main/java/com/abntbuilder/formatter/rendering/layout/singlepage -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.layout\.singlepage;$/package com.abntbuilder.formatter.engine.singlepage;/' {} \;
```

- [ ] **Step 3: Update package declarations — text engine**

```bash
find src/main/java/com/abntbuilder/formatter/rendering/layout/text -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.layout\.text;$/package com.abntbuilder.formatter.engine.text;/' {} \;
```

- [ ] **Step 4: Move main source files**

```bash
mv src/main/java/com/abntbuilder/formatter/rendering/layout/singlepage/*.java \
   src/main/java/com/abntbuilder/formatter/engine/singlepage/

mv src/main/java/com/abntbuilder/formatter/rendering/layout/text/*.java \
   src/main/java/com/abntbuilder/formatter/engine/text/
```

- [ ] **Step 5: Update package declarations in test files**

```bash
find src/test/java/com/abntbuilder/formatter/rendering/layout/singlepage -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.layout\.singlepage;$/package com.abntbuilder.formatter.engine.singlepage;/' {} \;

find src/test/java/com/abntbuilder/formatter/rendering/layout/text -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.layout\.text;$/package com.abntbuilder.formatter.engine.text;/' {} \;
```

- [ ] **Step 6: Move test files**

```bash
mv src/test/java/com/abntbuilder/formatter/rendering/layout/singlepage/*.java \
   src/test/java/com/abntbuilder/formatter/engine/singlepage/

mv src/test/java/com/abntbuilder/formatter/rendering/layout/text/*.java \
   src/test/java/com/abntbuilder/formatter/engine/text/
```

- [ ] **Step 7: Update all imports across the source tree**

```bash
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.layout\.singlepage\./import com.abntbuilder.formatter.engine.singlepage./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.layout\.text\./import com.abntbuilder.formatter.engine.text./g' {} \;
```

- [ ] **Step 8: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "refactor: move singlepage and text layout engines to engine package"
```

---

### Task 5: Move component renderers

Move each component's rendering classes to its `rendering/` slice. Note: `cover` currently has a `layout/` sub-package that gets **flattened** — all classes land directly in `cover/rendering/`.

**Files:**
- `$BASE/rendering/component/cover/CoverRenderer.java` → `$BASE/cover/rendering/`
- `$BASE/rendering/component/cover/layout/*.java` → `$BASE/cover/rendering/` (flatten — no sub-package)
- `$BASE/rendering/component/titlepage/*.java` → `$BASE/titlepage/rendering/`
- `$BASE/rendering/component/approvalsheet/*.java` → `$BASE/approvalsheet/rendering/`
- `$BASE/rendering/component/bodycontent/*.java` → `$BASE/bodycontent/rendering/`
- Tests: mirror in test tree

- [ ] **Step 1: Create target directories**

```bash
mkdir -p src/main/java/com/abntbuilder/formatter/cover/rendering
mkdir -p src/main/java/com/abntbuilder/formatter/titlepage/rendering
mkdir -p src/main/java/com/abntbuilder/formatter/approvalsheet/rendering
mkdir -p src/main/java/com/abntbuilder/formatter/bodycontent/rendering
mkdir -p src/test/java/com/abntbuilder/formatter/cover/rendering
mkdir -p src/test/java/com/abntbuilder/formatter/titlepage/rendering
mkdir -p src/test/java/com/abntbuilder/formatter/approvalsheet/rendering
mkdir -p src/test/java/com/abntbuilder/formatter/bodycontent/rendering
```

- [ ] **Step 2: Update package declarations — cover rendering (including layout/ flatten)**

```bash
sed -i 's/^package com\.abntbuilder\.formatter\.rendering\.component\.cover;$/package com.abntbuilder.formatter.cover.rendering;/' \
  src/main/java/com/abntbuilder/formatter/rendering/component/cover/CoverRenderer.java

find src/main/java/com/abntbuilder/formatter/rendering/component/cover/layout -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.cover\.layout;$/package com.abntbuilder.formatter.cover.rendering;/' {} \;
```

- [ ] **Step 3: Update package declarations — titlepage rendering**

```bash
find src/main/java/com/abntbuilder/formatter/rendering/component/titlepage -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.rendering;/' {} \;
```

- [ ] **Step 4: Update package declarations — approvalsheet rendering**

```bash
find src/main/java/com/abntbuilder/formatter/rendering/component/approvalsheet -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.approvalsheet;$/package com.abntbuilder.formatter.approvalsheet.rendering;/' {} \;
```

- [ ] **Step 5: Update package declarations — bodycontent rendering**

```bash
find src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.rendering;/' {} \;
```

- [ ] **Step 6: Move main source files**

```bash
mv src/main/java/com/abntbuilder/formatter/rendering/component/cover/CoverRenderer.java \
   src/main/java/com/abntbuilder/formatter/cover/rendering/
mv src/main/java/com/abntbuilder/formatter/rendering/component/cover/layout/*.java \
   src/main/java/com/abntbuilder/formatter/cover/rendering/

mv src/main/java/com/abntbuilder/formatter/rendering/component/titlepage/*.java \
   src/main/java/com/abntbuilder/formatter/titlepage/rendering/

mv src/main/java/com/abntbuilder/formatter/rendering/component/approvalsheet/*.java \
   src/main/java/com/abntbuilder/formatter/approvalsheet/rendering/

mv src/main/java/com/abntbuilder/formatter/rendering/component/bodycontent/*.java \
   src/main/java/com/abntbuilder/formatter/bodycontent/rendering/
```

- [ ] **Step 7: Update package declarations in test files and move them**

```bash
# cover tests — flatten layout/ sub-tests too
find src/test/java/com/abntbuilder/formatter/rendering/component/cover -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.cover\.layout;$/package com.abntbuilder.formatter.cover.rendering;/' {} \;
find src/test/java/com/abntbuilder/formatter/rendering/component/cover -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.cover;$/package com.abntbuilder.formatter.cover.rendering;/' {} \;

find src/test/java/com/abntbuilder/formatter/rendering/component/titlepage -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.titlepage;$/package com.abntbuilder.formatter.titlepage.rendering;/' {} \;

find src/test/java/com/abntbuilder/formatter/rendering/component/approvalsheet -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.approvalsheet;$/package com.abntbuilder.formatter.approvalsheet.rendering;/' {} \;

find src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent -name "*.java" -exec sed -i \
  's/^package com\.abntbuilder\.formatter\.rendering\.component\.bodycontent;$/package com.abntbuilder.formatter.bodycontent.rendering;/' {} \;

# Move test files
find src/test/java/com/abntbuilder/formatter/rendering/component/cover -name "*.java" \
  -exec mv {} src/test/java/com/abntbuilder/formatter/cover/rendering/ \;

mv src/test/java/com/abntbuilder/formatter/rendering/component/titlepage/*.java \
   src/test/java/com/abntbuilder/formatter/titlepage/rendering/

mv src/test/java/com/abntbuilder/formatter/rendering/component/approvalsheet/*.java \
   src/test/java/com/abntbuilder/formatter/approvalsheet/rendering/

mv src/test/java/com/abntbuilder/formatter/rendering/component/bodycontent/*.java \
   src/test/java/com/abntbuilder/formatter/bodycontent/rendering/
```

- [ ] **Step 8: Update all imports across the source tree**

```bash
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.cover\.layout\./import com.abntbuilder.formatter.cover.rendering./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.cover\./import com.abntbuilder.formatter.cover.rendering./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.titlepage\./import com.abntbuilder.formatter.titlepage.rendering./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.approvalsheet\./import com.abntbuilder.formatter.approvalsheet.rendering./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.bodycontent\./import com.abntbuilder.formatter.bodycontent.rendering./g' {} \;
```

- [ ] **Step 9: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "refactor: move component renderers to vertical slice rendering packages"
```

---

### Task 6: Move component API DTOs

Move component-specific request DTOs from the flat `api/export/dto/request/` to each component's `api/` sub-package.

**Files to `cover/api/`:** `CoverRequest`, `CoverLayoutRuleRequest`, `CoverStyleMappingRequest`, `CoverComponentRuleRequest`

**Files to `titlepage/api/`:** `TitlePageRequest`, `TitlePageNatureRequest`, `TitlePageStyleMappingRequest`, `TitlePageTextTemplateRuleRequest`, `TitlePageComponentRuleRequest`

**Files to `approvalsheet/api/`:** `ApprovalSheetRequest`, `ApprovalCommitteeMemberRequest`, `ApprovalEventRequest`, `ApprovalSheetNatureRequest`, `ApprovalSheetComponentRuleRequest`, `ApprovalSheetSignatureLineRuleRequest`, `ApprovalSheetStyleMappingRequest`, `ApprovalSheetTextTemplateRuleRequest`, `ApprovalSheetCommitteeMemberRuleRequest`

**Files to `bodycontent/api/`:** `BodyContentRequest`, `BodySectionRequest`, `BodyBlockRequest`, `BodyBlockType`, `BodyInlineRequest`, `BodyInlineType`, `BodyFigureRequest`, `BodyTableRequest`, `BodyTableColumnRequest`, `BodyTableRowRequest`, `CitationAuthorRequest`, `CitationSourceRequest`, `ImageSourceRequest`, `BodyContentComponentRuleRequest`, `BodyContentLayoutRuleRequest`, `BodyContentNumberingRuleRequest`, `BodyContentStyleMappingRequest`, `FigureRuleRequest`, `TableRuleRequest`, `DisplayObjectContinuationLabelsRequest`

- [ ] **Step 1: Create target directories**

```bash
mkdir -p src/main/java/com/abntbuilder/formatter/cover/api
mkdir -p src/main/java/com/abntbuilder/formatter/titlepage/api
mkdir -p src/main/java/com/abntbuilder/formatter/approvalsheet/api
mkdir -p src/main/java/com/abntbuilder/formatter/bodycontent/api
mkdir -p src/test/java/com/abntbuilder/formatter/cover/api
mkdir -p src/test/java/com/abntbuilder/formatter/titlepage/api
mkdir -p src/test/java/com/abntbuilder/formatter/approvalsheet/api
mkdir -p src/test/java/com/abntbuilder/formatter/bodycontent/api
```

- [ ] **Step 2: Update package declarations — cover api**

```bash
for f in CoverRequest CoverLayoutRuleRequest CoverStyleMappingRequest CoverComponentRuleRequest; do
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.cover.api;/' \
    src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java
done
```

- [ ] **Step 3: Update package declarations — titlepage api**

```bash
for f in TitlePageRequest TitlePageNatureRequest TitlePageStyleMappingRequest TitlePageTextTemplateRuleRequest TitlePageComponentRuleRequest; do
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.titlepage.api;/' \
    src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java
done
```

- [ ] **Step 4: Update package declarations — approvalsheet api**

```bash
for f in ApprovalSheetRequest ApprovalCommitteeMemberRequest ApprovalEventRequest ApprovalSheetNatureRequest ApprovalSheetComponentRuleRequest ApprovalSheetSignatureLineRuleRequest ApprovalSheetStyleMappingRequest ApprovalSheetTextTemplateRuleRequest ApprovalSheetCommitteeMemberRuleRequest; do
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.approvalsheet.api;/' \
    src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java
done
```

- [ ] **Step 5: Update package declarations — bodycontent api**

```bash
for f in BodyContentRequest BodySectionRequest BodyBlockRequest BodyBlockType BodyInlineRequest BodyInlineType BodyFigureRequest BodyTableRequest BodyTableColumnRequest BodyTableRowRequest CitationAuthorRequest CitationSourceRequest ImageSourceRequest BodyContentComponentRuleRequest BodyContentLayoutRuleRequest BodyContentNumberingRuleRequest BodyContentStyleMappingRequest FigureRuleRequest TableRuleRequest DisplayObjectContinuationLabelsRequest; do
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.bodycontent.api;/' \
    src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java
done
```

- [ ] **Step 6: Move source files**

```bash
# cover
for f in CoverRequest CoverLayoutRuleRequest CoverStyleMappingRequest CoverComponentRuleRequest; do
  mv src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java \
     src/main/java/com/abntbuilder/formatter/cover/api/
done

# titlepage
for f in TitlePageRequest TitlePageNatureRequest TitlePageStyleMappingRequest TitlePageTextTemplateRuleRequest TitlePageComponentRuleRequest; do
  mv src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java \
     src/main/java/com/abntbuilder/formatter/titlepage/api/
done

# approvalsheet
for f in ApprovalSheetRequest ApprovalCommitteeMemberRequest ApprovalEventRequest ApprovalSheetNatureRequest ApprovalSheetComponentRuleRequest ApprovalSheetSignatureLineRuleRequest ApprovalSheetStyleMappingRequest ApprovalSheetTextTemplateRuleRequest ApprovalSheetCommitteeMemberRuleRequest; do
  mv src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java \
     src/main/java/com/abntbuilder/formatter/approvalsheet/api/
done

# bodycontent
for f in BodyContentRequest BodySectionRequest BodyBlockRequest BodyBlockType BodyInlineRequest BodyInlineType BodyFigureRequest BodyTableRequest BodyTableColumnRequest BodyTableRowRequest CitationAuthorRequest CitationSourceRequest ImageSourceRequest BodyContentComponentRuleRequest BodyContentLayoutRuleRequest BodyContentNumberingRuleRequest BodyContentStyleMappingRequest FigureRuleRequest TableRuleRequest DisplayObjectContinuationLabelsRequest; do
  mv src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java \
     src/main/java/com/abntbuilder/formatter/bodycontent/api/
done
```

- [ ] **Step 7: Move and update test files**

```bash
# cover
mv src/test/java/com/abntbuilder/formatter/api/export/dto/request/CoverRequestTest.java \
   src/test/java/com/abntbuilder/formatter/cover/api/
sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.cover.api;/' \
  src/test/java/com/abntbuilder/formatter/cover/api/CoverRequestTest.java

# titlepage
for f in TitlePageRequestTest TitlePageComponentRuleRequestTest; do
  mv src/test/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java \
     src/test/java/com/abntbuilder/formatter/titlepage/api/
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.titlepage.api;/' \
    src/test/java/com/abntbuilder/formatter/titlepage/api/${f}.java
done

# approvalsheet
mv src/test/java/com/abntbuilder/formatter/api/export/dto/request/ApprovalSheetRequestTest.java \
   src/test/java/com/abntbuilder/formatter/approvalsheet/api/
sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.approvalsheet.api;/' \
  src/test/java/com/abntbuilder/formatter/approvalsheet/api/ApprovalSheetRequestTest.java

# bodycontent
mv src/test/java/com/abntbuilder/formatter/api/export/dto/request/BodySectionRequestTest.java \
   src/test/java/com/abntbuilder/formatter/bodycontent/api/
sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.bodycontent.api;/' \
  src/test/java/com/abntbuilder/formatter/bodycontent/api/BodySectionRequestTest.java
```

- [ ] **Step 8: Update all imports across the source tree**

```bash
# The files that still remain in api.export.dto.request import the ones we moved.
# Since many classes in the same package don't use explicit imports for each other,
# we target the classes that are referenced by name in other packages:

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.Cover/import com.abntbuilder.formatter.cover.api.Cover/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.TitlePage/import com.abntbuilder.formatter.titlepage.api.TitlePage/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.ApprovalSheet/import com.abntbuilder.formatter.approvalsheet.api.ApprovalSheet/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.ApprovalCommitteeMember/import com.abntbuilder.formatter.approvalsheet.api.ApprovalCommitteeMember/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.ApprovalEvent/import com.abntbuilder.formatter.approvalsheet.api.ApprovalEvent/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.Body/import com.abntbuilder.formatter.bodycontent.api.Body/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.CitationAuthorRequest/import com.abntbuilder.formatter.bodycontent.api.CitationAuthorRequest/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.CitationSourceRequest/import com.abntbuilder.formatter.bodycontent.api.CitationSourceRequest/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.ImageSourceRequest/import com.abntbuilder.formatter.bodycontent.api.ImageSourceRequest/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.FigureRuleRequest/import com.abntbuilder.formatter.bodycontent.api.FigureRuleRequest/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.TableRuleRequest/import com.abntbuilder.formatter.bodycontent.api.TableRuleRequest/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\.DisplayObjectContinuationLabelsRequest/import com.abntbuilder.formatter.bodycontent.api.DisplayObjectContinuationLabelsRequest/g' {} \;
```

- [ ] **Step 9: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "refactor: move component API DTOs to vertical slice api packages"
```

---

### Task 7: Move infrastructure (rendering orchestration, api, application, config)

Move all Spring-specific wiring: rendering orchestration classes, controller, error handling, service, config.

**Files to `infrastructure/rendering/`:** `ComponentRenderer`, `ComponentRendererRegistry`, `DocumentRenderer`, `ComponentSelectionResolver`

**Files to `infrastructure/api/`:** `DocxExportController`, `ApiExceptionHandler`, `ApiErrorResponse`, `GenerateDocxResponse`, plus the remaining shared request objects: `ExportDocxRequest`, `DocumentContentRequest`, `ProfileRequest`, `ComponentRulesRequest`, `ExportOptionsRequest`, `AcademicWorkRequest`, `AcademicPersonRequest`, `AcademicWorkNatureRequest`, `PageRuleRequest`, `StyleRuleRequest`, `PageNumberingRuleRequest`, `LayoutGapRuleRequest`, `SinglePageGroupRuleRequest`, `SinglePageItemRuleRequest`, `SinglePageLayoutPolicyRequest`, `SinglePageLayoutRuleRequest`, `ParagraphRequest`, `WorkContentBindingResolver`

**Files to `infrastructure/application/`:** `DocxExportService`, `ExportDocxCommand`, `GeneratedDocxExport`, `GeneratedDocxExportStore`, `InMemoryGeneratedDocxExportStore`

**Files to `infrastructure/config/`:** `RenderingConfig`, `DocxWriterConfig`, `TextMeasurementProperties`

- [ ] **Step 1: Create target directories**

```bash
mkdir -p src/main/java/com/abntbuilder/formatter/infrastructure/rendering
mkdir -p src/main/java/com/abntbuilder/formatter/infrastructure/api
mkdir -p src/main/java/com/abntbuilder/formatter/infrastructure/application
mkdir -p src/main/java/com/abntbuilder/formatter/infrastructure/config
mkdir -p src/test/java/com/abntbuilder/formatter/infrastructure/rendering
mkdir -p src/test/java/com/abntbuilder/formatter/infrastructure/api
mkdir -p src/test/java/com/abntbuilder/formatter/infrastructure/application
```

- [ ] **Step 2: Update package declarations — infrastructure/rendering**

```bash
for f in ComponentRenderer ComponentRendererRegistry; do
  sed -i 's/^package com\.abntbuilder\.formatter\.rendering\.component;$/package com.abntbuilder.formatter.infrastructure.rendering;/' \
    src/main/java/com/abntbuilder/formatter/rendering/component/${f}.java
done

for f in DocumentRenderer ComponentSelectionResolver; do
  sed -i 's/^package com\.abntbuilder\.formatter\.rendering\.document;$/package com.abntbuilder.formatter.infrastructure.rendering;/' \
    src/main/java/com/abntbuilder/formatter/rendering/document/${f}.java
done
```

- [ ] **Step 3: Update package declarations — infrastructure/api (controller and error)**

```bash
sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.controller;$/package com.abntbuilder.formatter.infrastructure.api;/' \
  src/main/java/com/abntbuilder/formatter/api/export/controller/DocxExportController.java

for f in ApiErrorResponse ApiExceptionHandler; do
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.error;$/package com.abntbuilder.formatter.infrastructure.api;/' \
    src/main/java/com/abntbuilder/formatter/api/error/${f}.java
done

sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.response;$/package com.abntbuilder.formatter.infrastructure.api;/' \
  src/main/java/com/abntbuilder/formatter/api/export/dto/response/GenerateDocxResponse.java
```

- [ ] **Step 4: Update package declarations — infrastructure/api (shared request objects)**

```bash
for f in ExportDocxRequest DocumentContentRequest ProfileRequest ComponentRulesRequest ExportOptionsRequest \
         AcademicWorkRequest AcademicPersonRequest AcademicWorkNatureRequest \
         PageRuleRequest StyleRuleRequest PageNumberingRuleRequest LayoutGapRuleRequest \
         SinglePageGroupRuleRequest SinglePageItemRuleRequest SinglePageLayoutPolicyRequest SinglePageLayoutRuleRequest \
         ParagraphRequest WorkContentBindingResolver; do
  sed -i 's/^package com\.abntbuilder\.formatter\.api\.export\.dto\.request;$/package com.abntbuilder.formatter.infrastructure.api;/' \
    src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java
done
```

- [ ] **Step 5: Update package declarations — infrastructure/application**

```bash
for f in DocxExportService ExportDocxCommand GeneratedDocxExport GeneratedDocxExportStore InMemoryGeneratedDocxExportStore; do
  sed -i 's/^package com\.abntbuilder\.formatter\.application\.export;$/package com.abntbuilder.formatter.infrastructure.application;/' \
    src/main/java/com/abntbuilder/formatter/application/export/${f}.java
done
```

- [ ] **Step 6: Update package declarations — infrastructure/config**

```bash
for f in RenderingConfig DocxWriterConfig TextMeasurementProperties; do
  sed -i 's/^package com\.abntbuilder\.formatter\.config;$/package com.abntbuilder.formatter.infrastructure.config;/' \
    src/main/java/com/abntbuilder/formatter/config/${f}.java
done
```

- [ ] **Step 7: Move all infrastructure source files**

```bash
# rendering
for f in ComponentRenderer ComponentRendererRegistry; do
  mv src/main/java/com/abntbuilder/formatter/rendering/component/${f}.java \
     src/main/java/com/abntbuilder/formatter/infrastructure/rendering/
done
mv src/main/java/com/abntbuilder/formatter/rendering/document/{DocumentRenderer.java,ComponentSelectionResolver.java} \
   src/main/java/com/abntbuilder/formatter/infrastructure/rendering/

# api — controller
mv src/main/java/com/abntbuilder/formatter/api/export/controller/DocxExportController.java \
   src/main/java/com/abntbuilder/formatter/infrastructure/api/

# api — error
mv src/main/java/com/abntbuilder/formatter/api/error/{ApiErrorResponse.java,ApiExceptionHandler.java} \
   src/main/java/com/abntbuilder/formatter/infrastructure/api/

# api — response
mv src/main/java/com/abntbuilder/formatter/api/export/dto/response/GenerateDocxResponse.java \
   src/main/java/com/abntbuilder/formatter/infrastructure/api/

# api — shared request objects
for f in ExportDocxRequest DocumentContentRequest ProfileRequest ComponentRulesRequest ExportOptionsRequest \
         AcademicWorkRequest AcademicPersonRequest AcademicWorkNatureRequest \
         PageRuleRequest StyleRuleRequest PageNumberingRuleRequest LayoutGapRuleRequest \
         SinglePageGroupRuleRequest SinglePageItemRuleRequest SinglePageLayoutPolicyRequest SinglePageLayoutRuleRequest \
         ParagraphRequest WorkContentBindingResolver; do
  mv src/main/java/com/abntbuilder/formatter/api/export/dto/request/${f}.java \
     src/main/java/com/abntbuilder/formatter/infrastructure/api/
done

# application
mv src/main/java/com/abntbuilder/formatter/application/export/{DocxExportService.java,ExportDocxCommand.java,GeneratedDocxExport.java,GeneratedDocxExportStore.java,InMemoryGeneratedDocxExportStore.java} \
   src/main/java/com/abntbuilder/formatter/infrastructure/application/

# config
mv src/main/java/com/abntbuilder/formatter/config/{RenderingConfig.java,DocxWriterConfig.java,TextMeasurementProperties.java} \
   src/main/java/com/abntbuilder/formatter/infrastructure/config/
```

- [ ] **Step 8: Move and update test files**

```bash
# rendering tests
for f in ComponentRendererRegistryTest ComponentSelectionResolverTest DocumentRendererComponentSelectionTest; do
  src_file=$(find src/test -name "${f}.java")
  if [ -n "$src_file" ]; then
    sed -i 's/^package com\.abntbuilder\.formatter\.rendering\.component;$/package com.abntbuilder.formatter.infrastructure.rendering;/' "$src_file"
    sed -i 's/^package com\.abntbuilder\.formatter\.rendering\.document;$/package com.abntbuilder.formatter.infrastructure.rendering;/' "$src_file"
    mv "$src_file" src/test/java/com/abntbuilder/formatter/infrastructure/rendering/
  fi
done

# api tests
for f in DocxExportControllerIntegrationTest ExportDocxRequestTest SinglePageItemRuleRequestTest ComponentRulesRequestTest; do
  src_file=$(find src/test -name "${f}.java")
  if [ -n "$src_file" ]; then
    sed -i 's/^package com\.abntbuilder\.formatter\.api\.[a-z.]*;$/package com.abntbuilder.formatter.infrastructure.api;/' "$src_file"
    mv "$src_file" src/test/java/com/abntbuilder/formatter/infrastructure/api/
  fi
done
```

- [ ] **Step 9: Update all imports across the source tree**

```bash
find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.ComponentRenderer;/import com.abntbuilder.formatter.infrastructure.rendering.ComponentRenderer;/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.component\.ComponentRendererRegistry;/import com.abntbuilder.formatter.infrastructure.rendering.ComponentRendererRegistry;/g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.rendering\.document\./import com.abntbuilder.formatter.infrastructure.rendering./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.controller\./import com.abntbuilder.formatter.infrastructure.api./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.error\./import com.abntbuilder.formatter.infrastructure.api./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.response\./import com.abntbuilder.formatter.infrastructure.api./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.api\.export\.dto\.request\./import com.abntbuilder.formatter.infrastructure.api./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.application\.export\./import com.abntbuilder.formatter.infrastructure.application./g' {} \;

find src -name "*.java" -exec sed -i \
  's/import com\.abntbuilder\.formatter\.config\./import com.abntbuilder.formatter.infrastructure.config./g' {} \;
```

- [ ] **Step 10: Verify build passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "refactor: move infrastructure classes (rendering orchestration, api, application, config)"
```

---

### Task 8: Final cleanup and full verification

Remove now-empty old package directories, run the full test suite, and verify no orphaned files remain.

- [ ] **Step 1: Check for any remaining files in old package locations**

```bash
find src/main/java/com/abntbuilder/formatter/document -name "*.java" 2>/dev/null
find src/main/java/com/abntbuilder/formatter/rendering -name "*.java" 2>/dev/null
find src/main/java/com/abntbuilder/formatter/api -name "*.java" 2>/dev/null
find src/main/java/com/abntbuilder/formatter/application -name "*.java" 2>/dev/null
find src/main/java/com/abntbuilder/formatter/config -name "*.java" 2>/dev/null
```

Expected: no output (all files moved). If any files remain, their package declarations and imports need manual inspection before removal.

- [ ] **Step 2: Remove empty old directories**

```bash
rm -rf src/main/java/com/abntbuilder/formatter/document
rm -rf src/main/java/com/abntbuilder/formatter/rendering
rm -rf src/main/java/com/abntbuilder/formatter/api
rm -rf src/main/java/com/abntbuilder/formatter/application
rm -rf src/main/java/com/abntbuilder/formatter/config

rm -rf src/test/java/com/abntbuilder/formatter/document
rm -rf src/test/java/com/abntbuilder/formatter/rendering/component
rm -rf src/test/java/com/abntbuilder/formatter/rendering/layout
rm -rf src/test/java/com/abntbuilder/formatter/rendering/document
rm -rf src/test/java/com/abntbuilder/formatter/api
```

- [ ] **Step 3: Verify build still passes**

```bash
./mvnw package -DskipTests -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Run full test suite**

```bash
./mvnw test
```

Expected: BUILD SUCCESS, all tests pass. If any test fails due to a package import issue (not a logic issue), fix the import and re-run.

- [ ] **Step 5: Verify test mirror matches source structure**

```bash
# Both should list the same set of component slice names
echo "=== source ==="
ls src/main/java/com/abntbuilder/formatter/

echo "=== test ==="
ls src/test/java/com/abntbuilder/formatter/
```

Expected: both sides show `cover`, `titlepage`, `approvalsheet`, `bodycontent`, `engine`, `output`, `profile`, `shared`, `infrastructure`.

- [ ] **Step 6: Final commit**

```bash
git add -A
git commit -m "refactor: remove empty legacy package directories after restructure

Package restructure complete. Structure migrated from layered-by-concern
to vertical slices. See docs/superpowers/specs/2026-06-11-package-restructure-design.md"
```
