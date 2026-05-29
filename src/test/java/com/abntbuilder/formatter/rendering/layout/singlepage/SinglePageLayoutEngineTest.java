package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageAnchorStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLineHeightStrategy;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageSafetyPolicyId;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SpacerStylePolicy;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageStyleException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinglePageLayoutEngineTest {

    @Test
    void shouldCalculatePlanAndDiagnosticWithoutReturningDocxBlocks() {
        SinglePageLayoutEngine engine = engineWithSafeCapacity(6);

        SinglePageLayoutPlan plan = engine.calculate(input(
                List.of(group("a", item("one", style("a"), "A")),
                        group("b", item("two", style("b"), "B1", "B2")),
                        group("c", item("three", style("c"), "C"))),
                List.of(resolvedGap("a", "b", 1), resolvedGap("b", "c", 1)),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        ));

        assertEquals(6, plan.totalLines());
        assertEquals(6, plan.pageCapacityLines());
        assertEquals(4, plan.diagnostic().contentLineCount());
        assertEquals(2, plan.diagnostic().availableGapLines());
        assertEquals(4, sum(plan.diagnostic().groupLineCounts().values().stream().toList()));
        assertEquals(4, sum(plan.diagnostic().itemLineCounts().values().stream().toList()));
        assertEquals(2, sum(plan.diagnostic().gapLineCounts().values().stream().toList()));
        assertFalse(plan.elements().stream().anyMatch(element ->
                element instanceof SinglePageSpacerLines spacer && spacer.lineCount() <= 0
        ));
    }

    @Test
    void shouldFailWithDiagnosticWhenContentExceedsSafeCapacity() {
        SinglePageLayoutEngine engine = engineWithSafeCapacity(3);

        SinglePageLayoutOverflowException exception = assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> engine.calculate(input(
                        List.of(group("a", item("one", style("a"), "A1", "A2")),
                                group("b", item("two", style("b"), "B1", "B2"))),
                        List.of(resolvedGap("a", "b", 1)),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                ))
        );

        assertTrue(exception.singlePageDiagnostic().isPresent());
        assertEquals(4, exception.singlePageDiagnostic().orElseThrow().contentLineCount());
        assertEquals(1, exception.singlePageDiagnostic().orElseThrow().overflowLineCount());
    }

    @Test
    void shouldUseNextGroupStyleForSpacerByDefault() {
        SinglePageLayoutPlan plan = engineWithSafeCapacity(3).calculate(input(
                List.of(group("a", item("one", style("previous"), "A")),
                        group("b", item("two", style("next"), "B"))),
                List.of(resolvedGap("a", "b", 1)),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        ));

        SinglePageSpacerLines spacer = plan.elements().stream()
                .filter(SinglePageSpacerLines.class::isInstance)
                .map(SinglePageSpacerLines.class::cast)
                .findFirst()
                .orElseThrow();

        assertEquals("next", spacer.styleRule().id());
    }

    @Test
    void shouldUsePreviousGroupStyleWhenPolicyRequestsIt() {
        SinglePageLayoutPlan plan = engineWithSafeCapacity(3).calculate(input(
                List.of(group("a", item("one", style("previous"), "A")),
                        group("b", item("two", style("next"), "B"))),
                List.of(resolvedGap("a", "b", 1)),
                new SinglePageLayoutPolicy(
                        SinglePageAnchorStrategy.LAST_GROUP_AT_SAFE_AREA_END,
                        SinglePageLineHeightStrategy.MAX_EXACT_LINE_HEIGHT,
                        SpacerStylePolicy.PREVIOUS_GROUP_STYLE,
                        SinglePageSafetyPolicyId.MARGIN_BASED
                )
        ));

        SinglePageSpacerLines spacer = plan.elements().stream()
                .filter(SinglePageSpacerLines.class::isInstance)
                .map(SinglePageSpacerLines.class::cast)
                .findFirst()
                .orElseThrow();

        assertEquals("previous", spacer.styleRule().id());
    }

    @Test
    void shouldRejectSinglePageStyleWithSpacingBeforeOrAfter() {
        SinglePageLayoutEngine engine = engineWithSafeCapacity(3);

        assertInstanceOf(InvalidSinglePageStyleException.class, assertThrows(
                InvalidSinglePageStyleException.class,
                () -> engine.calculate(input(
                        List.of(group("a", item("one", styleWithSpacing("a", BigDecimal.ONE, BigDecimal.ZERO), "A"))),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                ))
        ));

        assertInstanceOf(InvalidSinglePageStyleException.class, assertThrows(
                InvalidSinglePageStyleException.class,
                () -> engine.calculate(input(
                        List.of(group("a", item("one", styleWithSpacing("a", BigDecimal.ZERO, BigDecimal.ONE), "A"))),
                        List.of(),
                        SinglePageLayoutPolicy.defaultSinglePagePolicy()
                ))
        ));
    }

    private static SinglePageLayoutEngine engineWithSafeCapacity(int safeCapacity) {
        return new SinglePageLayoutEngine(
                new SinglePageLayoutLineMetrics(),
                (pageRule, lineHeightTwips) -> new SinglePageRenderableArea(safeCapacity, 0, safeCapacity),
                new SinglePageGapDistributor()
        );
    }

    private static SinglePageLayoutInput input(
            List<SinglePageLayoutGroup> groups,
            List<ResolvedLayoutGap> gaps,
            SinglePageLayoutPolicy policy
    ) {
        return new SinglePageLayoutInput(pageRule(), groups, gaps, policy);
    }

    private static SinglePageLayoutGroup group(String id, SinglePageLayoutItem... items) {
        return new SinglePageLayoutGroup(id, List.of(items));
    }

    private static SinglePageLayoutItem item(String id, StyleRule styleRule, String... visualLines) {
        return new SinglePageLayoutItem(id, styleRule, List.of(visualLines));
    }

    private static ResolvedLayoutGap resolvedGap(String from, String to, int weight) {
        LayoutGapRule source = new LayoutGapRule(from, to, BigDecimal.valueOf(weight));
        return new ResolvedLayoutGap(from, to, BigDecimal.valueOf(weight), List.of(source));
    }

    private static PageRule pageRule() {
        return new PageRule(
                BigDecimal.valueOf(21),
                BigDecimal.valueOf(29.7),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                PageOrientation.PORTRAIT
        );
    }

    private static StyleRule style(String id) {
        return styleWithSpacing(id, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StyleRule styleWithSpacing(
            String id,
            BigDecimal spacingBeforePt,
            BigDecimal spacingAfterPt
    ) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                spacingBeforePt,
                spacingAfterPt,
                false,
                false,
                false
        );
    }

    private static int sum(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).sum();
    }
}
