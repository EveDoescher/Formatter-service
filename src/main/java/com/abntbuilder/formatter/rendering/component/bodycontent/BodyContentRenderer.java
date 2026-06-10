package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.BodyContentComponent;
import com.abntbuilder.formatter.document.component.bodycontent.BodyBlock;
import com.abntbuilder.formatter.document.component.bodycontent.BodyCitation;
import com.abntbuilder.formatter.document.component.bodycontent.BodyParagraph;
import com.abntbuilder.formatter.document.component.bodycontent.BodySection;
import com.abntbuilder.formatter.output.docx.api.DocxBlankLine;
import com.abntbuilder.formatter.output.docx.api.DocxBlock;
import com.abntbuilder.formatter.output.docx.api.DocxPageBreak;
import com.abntbuilder.formatter.output.docx.api.DocxParagraph;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentComponentRule;
import com.abntbuilder.formatter.profile.model.component.bodycontent.BodyContentNumberingRule;
import com.abntbuilder.formatter.profile.resolution.ComponentRuleResolver;
import com.abntbuilder.formatter.profile.resolution.StyleResolver;
import com.abntbuilder.formatter.rendering.component.ComponentRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BodyContentRenderer implements ComponentRenderer<BodyContentComponent> {

    public static final String COMPONENT_ID = "bodyContent";

    @Override
    public String componentId() {
        return COMPONENT_ID;
    }

    @Override
    public Class<BodyContentComponent> componentType() {
        return BodyContentComponent.class;
    }

    @Override
    public List<DocxBlock> render(BodyContentComponent component, DocumentProfile profile) {
        Objects.requireNonNull(component, "component must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        BodyContentComponentRule rule = bodyContentRule(profile);
        StyleResolver styleResolver = new StyleResolver(profile);
        List<DocxBlock> blocks = new ArrayList<>();
        SectionNumberingState numberingState = new SectionNumberingState(rule.numbering());
        StyleRule blankLineStyle = styleResolver.resolve(rule.layout().blankLineStyleId());
        boolean previousRenderedTextWasBodyParagraph = false;

        for (BodySection section : component.sections()) {
            if (section.title().isPresent()) {
                if (rule.layout().pageBreakBeforePrimarySection() && section.level() == 1 && !blocks.isEmpty()) {
                    blocks.add(new DocxPageBreak());
                } else if (previousRenderedTextWasBodyParagraph) {
                    addBlankLines(
                            blocks,
                            blankLineStyle,
                            rule.layout().blankLinesBeforeSectionTitleWhenPrecededByContent()
                    );
                }

                blocks.add(new DocxParagraph(
                        numberingState.resolveTitle(section.level(), section.title().orElseThrow()),
                        styleResolver.resolve(rule.styleMapping().sectionTitleStyleIdForLevel(section.level()))
                ));
                previousRenderedTextWasBodyParagraph = false;

                addBlankLines(blocks, blankLineStyle, rule.layout().blankLinesAfterSectionTitle());
            }

            for (BodyBlock contentBlock : section.blocks()) {
                blocks.add(renderContentBlock(contentBlock, rule, styleResolver));
                previousRenderedTextWasBodyParagraph = true;
            }
        }

        return List.copyOf(blocks);
    }

    private static BodyContentComponentRule bodyContentRule(DocumentProfile profile) {
        return new ComponentRuleResolver(profile).resolve(COMPONENT_ID, BodyContentComponentRule.class);
    }

    private static void addBlankLines(List<DocxBlock> blocks, StyleRule styleRule, int count) {
        for (int index = 0; index < count; index++) {
            blocks.add(new DocxBlankLine(styleRule));
        }
    }

    private static DocxParagraph renderContentBlock(
            BodyBlock contentBlock,
            BodyContentComponentRule rule,
            StyleResolver styleResolver
    ) {
        return switch (contentBlock) {
            case BodyParagraph paragraph -> new DocxParagraph(
                    paragraph.text(),
                    styleResolver.resolve(rule.styleMapping().paragraphStyleId())
            );
            case BodyCitation citation -> new DocxParagraph(
                    citation.renderedText(),
                    styleResolver.resolve(rule.styleMapping().styleIdForCitation(citation.type()))
            );
        };
    }

    private static final class SectionNumberingState {

        private static final int MAX_LEVEL = 6;

        private final BodyContentNumberingRule numberingRule;
        private final int[] counters = new int[MAX_LEVEL];

        private SectionNumberingState(BodyContentNumberingRule numberingRule) {
            this.numberingRule = Objects.requireNonNull(numberingRule, "numberingRule must not be null");
        }

        private String resolveTitle(int level, String title) {
            if (!numberingRule.enabled()) {
                return title;
            }

            increment(level);

            return sectionNumber(level) + " " + title;
        }

        private void increment(int level) {
            int index = level - 1;

            for (int currentIndex = 0; currentIndex < index; currentIndex++) {
                if (counters[currentIndex] == 0) {
                    counters[currentIndex] = 1;
                }
            }

            counters[index]++;

            for (int currentIndex = index + 1; currentIndex < counters.length; currentIndex++) {
                counters[currentIndex] = 0;
            }
        }

        private String sectionNumber(int level) {
            if (level == 1) {
                return counters[0] + numberingRule.primarySuffix();
            }

            List<String> parts = new ArrayList<>();

            for (int index = 0; index < level; index++) {
                parts.add(String.valueOf(counters[index]));
            }

            return String.join(numberingRule.separator(), parts);
        }
    }
}
