package com.abntbuilder.formatter.rendering.component.flowtextual;

import com.abntbuilder.formatter.document.component.flowtextual.FlowTextualContent;
import com.abntbuilder.formatter.document.component.singlepage.ContentValue;
import com.abntbuilder.formatter.document.component.singlepage.TableValue;
import com.abntbuilder.formatter.document.component.singlepage.TextListValue;
import com.abntbuilder.formatter.document.component.singlepage.TextValue;
import com.abntbuilder.formatter.document.component.bodycontent.InlineFormatting;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.output.docx.api.DocxRun;
import com.abntbuilder.formatter.output.docx.api.DocxTableBlock;
import com.abntbuilder.formatter.output.docx.api.DocxTableCell;
import com.abntbuilder.formatter.output.docx.api.TableBorderStyle;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.flowtextual.FlowItem;
import com.abntbuilder.formatter.profile.model.component.flowtextual.FlowTextualComponentRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.MetadataConsumingRenderer;
import com.abntbuilder.formatter.rendering.component.bodycontent.BodyAbbreviationMetadata;
import com.abntbuilder.formatter.rendering.phase0.Phase0Index;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class FlowTextualRenderer implements MetadataConsumingRenderer<FlowTextualContent> {

    private final String componentId;

    public FlowTextualRenderer(String componentId) {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("FlowTextualRenderer.componentId must not be blank.");
        }
        this.componentId = componentId;
    }

    @Override
    public String componentId() {
        return componentId;
    }

    @Override
    public Class<FlowTextualContent> componentType() {
        return FlowTextualContent.class;
    }

    @Override
    public List<DocxBlock> renderWithMetadata(
            FlowTextualContent component, DocumentProfile profile, Phase0Index phase0Index) {
        FlowTextualComponentRule rule = new ComponentRuleResolver(profile)
                .resolve(componentId, FlowTextualComponentRule.class);
        StyleResolver styleResolver = new StyleResolver(profile);

        List<DocxBlock> blocks = new ArrayList<>();

        for (FlowItem item : rule.items()) {
            switch (item) {
                case FlowItem.HeadingItem h -> {
                    StyleRule style = styleResolver.resolve(h.styleId());
                    blocks.add(new DocxParagraph(List.of(DocxRun.of(h.text(), style)), style));
                }
                case FlowItem.BlankLinesItem b -> {
                    StyleRule style = styleResolver.resolve(b.styleId());
                    for (int i = 0; i < b.count(); i++) {
                        blocks.add(new DocxBlankLine(style));
                    }
                }
                case FlowItem.PlainTextItem p -> {
                    StyleRule style = styleResolver.resolve(p.styleId());
                    String text = requireTextValue(component, p.slotName()).text();
                    blocks.add(new DocxParagraph(List.of(DocxRun.of(text, style)), style));
                }
                case FlowItem.TemplatedTextItem t -> {
                    StyleRule style = styleResolver.resolve(t.styleId());
                    String text = t.template();
                    for (String fieldName : t.fieldNames()) {
                        ContentValue value = component.slots().get(fieldName);
                        String replacement = (value instanceof TextValue tv) ? tv.text() : "";
                        text = text.replace("{" + fieldName + "}", replacement);
                    }
                    blocks.add(new DocxParagraph(List.of(DocxRun.of(text, style)), style));
                }
                case FlowItem.BoldLabeledKeywordsItem k -> {
                    StyleRule style = styleResolver.resolve(k.styleId());
                    String label = requireTextValue(component, k.labelSlotName()).text();
                    List<String> keywords = requireTextListValue(component, k.keywordsSlotName()).items();
                    InlineFormatting bold = new InlineFormatting(
                            Optional.of(true), Optional.empty(), Optional.empty(),
                            Optional.empty(), Optional.empty());
                    DocxRun labelRun = new DocxRun(label, style, bold);
                    String keywordsBody = " " + String.join(k.separator(), keywords) + k.terminator();
                    DocxRun keywordsRun = DocxRun.of(keywordsBody, style);
                    blocks.add(new DocxParagraph(List.of(labelRun, keywordsRun), style));
                }
                case FlowItem.PairListItem p -> {
                    StyleRule style = styleResolver.resolve(p.styleId());
                    List<String> terms;
                    List<String> definitions;
                    if (p.termsSlotName().startsWith("$")) {
                        // Phase0-sourced data (e.g. "$abbreviations")
                        List<BodyAbbreviationMetadata> abbrs = resolvePhase0PairList(
                                p.termsSlotName(), phase0Index, p);
                        for (BodyAbbreviationMetadata abbr : abbrs) {
                            String text = abbr.abbreviation() + p.separator() + abbr.expansion();
                            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, style)), style));
                        }
                    } else {
                        terms = requireTextListValue(component, p.termsSlotName()).items();
                        definitions = requireTextListValue(component, p.definitionsSlotName()).items();
                        if (terms.size() != definitions.size()) {
                            throw new IllegalArgumentException(
                                    "FlowTextualContent[" + componentId + "]: slots '"
                                    + p.termsSlotName() + "' and '" + p.definitionsSlotName()
                                    + "' must have the same number of entries.");
                        }
                        for (int i = 0; i < terms.size(); i++) {
                            String text = terms.get(i) + p.separator() + definitions.get(i);
                            blocks.add(new DocxParagraph(List.of(DocxRun.of(text, style)), style));
                        }
                    }
                }
                case FlowItem.TableBlockItem t -> {
                    StyleRule headerStyle = styleResolver.resolve(t.headerStyleId());
                    StyleRule cellStyle = styleResolver.resolve(t.cellStyleId());
                    TableValue tableValue = requireTableValue(component, t.rowsSlotName());
                    List<List<DocxTableCell>> rows = tableValue.rows().stream()
                            .map(row -> row.stream().map(DocxTableCell::new).toList())
                            .toList();
                    blocks.add(new DocxTableBlock(
                            t.headers(), rows,
                            headerStyle, cellStyle,
                            BigDecimal.valueOf(100), TextAlignment.CENTER,
                            false, false, false, TableBorderStyle.CLOSED
                    ));
                }
            }
        }

        return List.copyOf(blocks);
    }

    private TextValue requireTextValue(FlowTextualContent component, String slotName) {
        ContentValue value = component.slots().get(slotName);
        if (!(value instanceof TextValue tv)) {
            throw new IllegalArgumentException(
                    "FlowTextualContent[" + componentId + "]: slot '" + slotName
                    + "' must be a TextValue (found: "
                    + (value == null ? "null" : value.getClass().getSimpleName()) + ").");
        }
        return tv;
    }

    private TextListValue requireTextListValue(FlowTextualContent component, String slotName) {
        ContentValue value = component.slots().get(slotName);
        if (!(value instanceof TextListValue tlv)) {
            throw new IllegalArgumentException(
                    "FlowTextualContent[" + componentId + "]: slot '" + slotName
                    + "' must be a TextListValue (found: "
                    + (value == null ? "null" : value.getClass().getSimpleName()) + ").");
        }
        return tlv;
    }

    private TableValue requireTableValue(FlowTextualContent component, String slotName) {
        ContentValue value = component.slots().get(slotName);
        if (!(value instanceof TableValue tv)) {
            throw new IllegalArgumentException(
                    "FlowTextualContent[" + componentId + "]: slot '" + slotName
                    + "' must be a TableValue (found: "
                    + (value == null ? "null" : value.getClass().getSimpleName()) + ").");
        }
        return tv;
    }

    private List<BodyAbbreviationMetadata> resolvePhase0PairList(
            String sourceKey, Phase0Index phase0Index, FlowItem.PairListItem item) {
        if ("$abbreviations".equals(sourceKey)) {
            List<BodyAbbreviationMetadata> abbrs = new ArrayList<>(phase0Index.abbreviations());
            // sortAlphabetically is encoded by a non-blank definitionsSlotName = "$sort"
            if ("$sort".equals(item.definitionsSlotName())) {
                abbrs.sort(Comparator.comparing(BodyAbbreviationMetadata::abbreviation));
            }
            return List.copyOf(abbrs);
        }
        throw new IllegalArgumentException(
                "Unknown Phase0 source key: '" + sourceKey + "' in component '" + componentId + "'.");
    }
}
