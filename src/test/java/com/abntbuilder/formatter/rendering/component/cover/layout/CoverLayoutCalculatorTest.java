package com.abntbuilder.formatter.rendering.component.cover.layout;

import com.abntbuilder.formatter.document.component.cover.CoverComponent;
import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.profile.model.component.cover.CoverComponentRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverLayoutRule;
import com.abntbuilder.formatter.profile.model.component.cover.CoverStyleMapping;
import com.abntbuilder.formatter.profile.model.layout.singlepage.LayoutGapRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageGroupRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageItemRule;
import com.abntbuilder.formatter.profile.model.layout.singlepage.SinglePageLayoutPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.MarginBasedSinglePageSafetyPolicy;
import com.abntbuilder.formatter.rendering.layout.singlepage.OrderedLayoutGapResolver;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageGapDistributor;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutEngine;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutElement;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageLayoutLineMetrics;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageSpacerLines;
import com.abntbuilder.formatter.rendering.layout.singlepage.SinglePageTextLines;
import com.abntbuilder.formatter.rendering.layout.text.FontMetricsTextMeasurer;
import com.abntbuilder.formatter.shared.exception.InvalidCoverContentException;
import com.abntbuilder.formatter.shared.exception.InvalidSinglePageStyleException;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import com.abntbuilder.formatter.shared.exception.TextMeasurementException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CoverLayoutCalculatorTest {

    private final CoverLayoutCalculator calculator = coverLayoutCalculator();

    @Test
    void shouldCreateValidatedPlanThatFillsEffectiveSinglePageCapacity() {
        CoverLayoutPlan plan = calculator.calculate(validCover(), validProfile());
        CoverLayoutDiagnostic diagnostic = plan.diagnostic();

        assertFalse(plan.elements().isEmpty());
        assertTrue(plan.elements().stream().anyMatch(SinglePageSpacerLines.class::isInstance));
        assertTrue(plan.elements().stream().anyMatch(SinglePageTextLines.class::isInstance));
        assertTrue(plan.exactLineHeightPt().compareTo(BigDecimal.ZERO) > 0);

        int elementLineCount = plan.elements().stream()
                .mapToInt(SinglePageLayoutElement::lineCount)
                .sum();
        int blockLineCount = diagnostic.blockLineCounts().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int gapLineCount = diagnostic.gapLineCounts().values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(plan.totalLines(), elementLineCount);
        assertEquals(plan.pageCapacityLines(), diagnostic.renderableArea().safeLineCapacity());
        assertEquals(diagnostic.contentLineCount(), blockLineCount);
        assertEquals(diagnostic.availableGapLines(), gapLineCount);
        assertEquals(plan.totalLines(), diagnostic.contentLineCount() + diagnostic.availableGapLines());
        assertEquals(
                plan.layoutPlan().diagnostic().renderableArea().safeHeightTwips(),
                plan.layoutPlan().diagnostic().contentHeightTwips()
                        + plan.layoutPlan().diagnostic().allocatedGapHeightTwips()
        );
        assertEquals(plan.exactLineHeightPt(), diagnostic.exactLineHeightPt());
        assertEquals(2, diagnostic.blockLineCounts().get("cover.bottom"));
    }

    @Test
    void shouldFailBeforeRenderingWhenCoverContentDoesNotFit() {
        List<String> authors = new ArrayList<>();

        for (int index = 0; index < 40; index++) {
            authors.add("NOME COMPLETO DO ALUNO " + index);
        }

        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                authors,
                "TITULO DO TRABALHO",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        CoverLayoutOverflowException exception = assertThrows(
                CoverLayoutOverflowException.class,
                () -> calculator.calculate(cover, validProfile())
        );

        CoverLayoutFailureDiagnostic diagnostic = exception.diagnostic();

        assertInstanceOf(SinglePageLayoutOverflowException.class, exception);
        assertTrue(diagnostic.contentLineCount() > diagnostic.renderableArea().safeLineCapacity());
        assertEquals(
                diagnostic.contentLineCount() - diagnostic.renderableArea().safeLineCapacity(),
                diagnostic.overflowLineCount()
        );
        assertTrue(diagnostic.blockLineCounts().containsKey("cover.authors"));
        assertTrue(diagnostic.exactLineHeightPt().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void shouldFailWhenBottomBlockWrapsToMoreThanCityAndYearLines() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "TITULO DO TRABALHO",
                Optional.empty(),
                "Cidade Brasileira Com Nome Propositalmente Extenso Para Forcar Quebra Em Mais De Uma Linha",
                "2026"
        );

        InvalidCoverContentException exception = assertThrows(
                InvalidCoverContentException.class,
                () -> calculator.calculate(cover, validProfile())
        );

        assertEquals("cover city and year must each fit in exactly one visual line.", exception.getMessage());
    }

    @Test
    void shouldFailWhenBottomItemContainsExplicitLineBreak() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "TITULO DO TRABALHO",
                Optional.empty(),
                "Limeira\n2026",
                "2026"
        );

        InvalidCoverContentException exception = assertThrows(
                InvalidCoverContentException.class,
                () -> calculator.calculate(cover, validProfile())
        );

        assertEquals("cover city and year must each fit in exactly one visual line.", exception.getMessage());
    }

    @Test
    void shouldDeriveTopToTitleGapWeightWhenAuthorsAreAbsent() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of(),
                "TITULO DO TRABALHO",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        CoverLayoutPlan plan = calculator.calculate(cover, validProfile());

        assertTrue(plan.diagnostic().gapLineCounts().get("cover.institution->cover.titleBlock") > 1);
        assertTrue(plan.diagnostic().gapLineCounts().containsKey("cover.titleBlock->cover.bottom"));
    }

    @Test
    void shouldFailWhenSinglePageStyleHasSpacingBefore() {
        InvalidSinglePageStyleException exception = assertThrows(
                InvalidSinglePageStyleException.class,
                () -> calculator.calculate(
                        validCover(),
                        profileWithTitleStyle(style(
                                "cover.title",
                                true,
                                true,
                                BigDecimal.ONE,
                                BigDecimal.ZERO
                        ))
                )
        );

        assertEquals("single-page layout styles must have spacingBeforePt equal to zero.", exception.getMessage());
    }

    @Test
    void shouldFailWhenSinglePageStyleHasSpacingAfter() {
        InvalidSinglePageStyleException exception = assertThrows(
                InvalidSinglePageStyleException.class,
                () -> calculator.calculate(
                        validCover(),
                        profileWithTitleStyle(style(
                                "cover.title",
                                true,
                                true,
                                BigDecimal.ZERO,
                                BigDecimal.ONE
                        ))
                )
        );

        assertEquals("single-page layout styles must have spacingAfterPt equal to zero.", exception.getMessage());
    }

    @Test
    void shouldFailBeforeRenderingWhenWordExceedsAvailableWidth() {
        CoverComponent cover = new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "PALAVRAEXTREMAMENTELONGAQUEULTRAPASSAALARGURADISPONIVELSEMESPACOS",
                Optional.empty(),
                "Limeira",
                "2026"
        );

        TextMeasurementException exception = assertThrows(
                TextMeasurementException.class,
                () -> calculator.calculate(cover, validProfile())
        );

        assertEquals("word width exceeds available text width.", exception.getMessage());
    }

    private static CoverComponent validCover() {
        return new CoverComponent(
                List.of("UNIVERSIDADE PAULISTA"),
                List.of("NOME COMPLETO DO ALUNO"),
                "TITULO DO TRABALHO",
                Optional.of("Subtitulo do trabalho"),
                "Limeira",
                "2026"
        );
    }

    private static DocumentProfile validProfile() {
        return profileWithTitleStyle(style("cover.title", true, true));
    }

    private static DocumentProfile profileWithTitleStyle(StyleRule titleStyle) {
        return new DocumentProfile(
                "abnt-unip-profile",
                "ABNT UNIP Profile",
                validPageRule(),
                List.of(
                        style("cover.top", true, true),
                        style("cover.author", false, true),
                        titleStyle,
                        style("cover.subtitle", false, false),
                        style("cover.bottom", false, false)
                ),
                List.of(new CoverComponentRule(
                        "cover",
                        new CoverStyleMapping(
                                "cover.top",
                                "cover.author",
                                "cover.title",
                                "cover.subtitle",
                                "cover.bottom",
                                "cover.bottom"
                        ),
                        validCoverLayoutRule()
                ))
        );
    }

    private static CoverLayoutRule validCoverLayoutRule() {
        return new CoverLayoutRule(
                List.of(
                        new SinglePageGroupRule(
                                CoverLayoutRule.INSTITUTION_GROUP_ID,
                                true,
                                List.of(new SinglePageItemRule("institutionalLines", true, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.AUTHORS_GROUP_ID,
                                false,
                                List.of(new SinglePageItemRule("authors", false, Optional.empty()))
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.TITLE_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("title", true, Optional.empty()),
                                        new SinglePageItemRule("subtitle", false, Optional.empty())
                                )
                        ),
                        new SinglePageGroupRule(
                                CoverLayoutRule.BOTTOM_GROUP_ID,
                                true,
                                List.of(
                                        new SinglePageItemRule("city", true, Optional.of(1)),
                                        new SinglePageItemRule("year", true, Optional.of(1))
                                )
                        )
                ),
                List.of(
                        new LayoutGapRule(CoverLayoutRule.INSTITUTION_GROUP_ID, CoverLayoutRule.AUTHORS_GROUP_ID, BigDecimal.valueOf(30)),
                        new LayoutGapRule(CoverLayoutRule.AUTHORS_GROUP_ID, CoverLayoutRule.TITLE_GROUP_ID, BigDecimal.valueOf(10)),
                        new LayoutGapRule(CoverLayoutRule.TITLE_GROUP_ID, CoverLayoutRule.BOTTOM_GROUP_ID, BigDecimal.valueOf(60))
                ),
                SinglePageLayoutPolicy.defaultSinglePagePolicy()
        );
    }

    private static PageRule validPageRule() {
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

    private static StyleRule style(String id, boolean bold, boolean uppercase) {
        return style(id, bold, uppercase, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StyleRule style(
            String id,
            boolean bold,
            boolean uppercase,
            BigDecimal spacingBeforePt,
            BigDecimal spacingAfterPt
    ) {
        return new StyleRule(
                id,
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                spacingBeforePt,
                spacingAfterPt,
                bold,
                false,
                uppercase
        );
    }

    private static CoverLayoutCalculator coverLayoutCalculator() {
        return new CoverLayoutCalculator(
                new CoverLayoutAssembler(
                        new FontMetricsTextMeasurer(),
                        new OrderedLayoutGapResolver(),
                        new CoverProfileContentValidator()
                ),
                new SinglePageLayoutEngine(
                        new SinglePageLayoutLineMetrics(),
                        new MarginBasedSinglePageSafetyPolicy(),
                        new SinglePageGapDistributor()
                )
        );
    }
}
