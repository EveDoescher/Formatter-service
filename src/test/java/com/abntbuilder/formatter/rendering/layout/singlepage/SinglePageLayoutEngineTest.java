package com.abntbuilder.formatter.rendering.layout.singlepage;

import com.abntbuilder.formatter.profile.model.PageOrientation;
import com.abntbuilder.formatter.profile.model.PageRule;
import com.abntbuilder.formatter.profile.model.StyleRule;
import com.abntbuilder.formatter.profile.model.StyleType;
import com.abntbuilder.formatter.profile.model.TextAlignment;
import com.abntbuilder.formatter.shared.exception.SinglePageLayoutOverflowException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SinglePageLayoutEngineTest {

    private final SinglePageLayoutEngine engine = new SinglePageLayoutEngine();

    @Test
    void shouldLayoutGroupsDistributingAvailableSpace() {
        SinglePageLayoutResult result = engine.layout(
                validPageRule(),
                List.of(
                        group("top", "UNIVERSIDADE PAULISTA"),
                        group("middle", "TÍTULO DO TRABALHO"),
                        group("bottom", "Limeira")
                ),
                BigDecimal.valueOf(0.2)
        );

        assertEquals(3, result.groups().size());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.groups().getFirst().yStartCm()));

        PositionedLayoutGroup bottomGroup = result.groups().getLast();

        assertEquals(0, result.usableHeightCm().compareTo(bottomGroup.yEndCm()));
        assertTrue(result.gapBetweenGroupsCm().compareTo(BigDecimal.valueOf(0.2)) >= 0);
    }

    @Test
    void shouldLayoutSingleGroupAtTop() {
        SinglePageLayoutResult result = engine.layout(
                validPageRule(),
                List.of(group("only", "TEXTO ÚNICO")),
                BigDecimal.ZERO
        );

        assertEquals(1, result.groups().size());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.groups().getFirst().yStartCm()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.gapBetweenGroupsCm()));
    }

    @Test
    void shouldRejectEmptyGroups() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> engine.layout(validPageRule(), List.of(), BigDecimal.ZERO)
        );

        assertEquals("groups must not be empty.", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeMinimumGap() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> engine.layout(
                        validPageRule(),
                        List.of(group("top", "Texto")),
                        BigDecimal.valueOf(-1)
                )
        );

        assertEquals("minimumGapBetweenGroupsCm must not be negative.", exception.getMessage());
    }

    @Test
    void shouldThrowOverflowWhenContentDoesNotFit() {
        PageRule tinyPage = new PageRule(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(0.5),
                BigDecimal.valueOf(1),
                PageOrientation.PORTRAIT
        );

        assertThrows(
                SinglePageLayoutOverflowException.class,
                () -> engine.layout(
                        tinyPage,
                        List.of(group("huge", "TEXTO GRANDE", hugeStyle())),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldRejectBlankGroupId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SinglePageLayoutGroup(
                        " ",
                        List.of(new SinglePageLayoutTextLine("Texto", validStyle()))
                )
        );

        assertEquals("id must not be blank.", exception.getMessage());
    }

    @Test
    void shouldRejectBlankTextLine() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SinglePageLayoutTextLine(" ", validStyle())
        );

        assertEquals("text must not be blank.", exception.getMessage());
    }

    private static SinglePageLayoutGroup group(String id, String text) {
        return group(id, text, validStyle());
    }

    private static SinglePageLayoutGroup group(String id, String text, StyleRule styleRule) {
        return new SinglePageLayoutGroup(
                id,
                List.of(new SinglePageLayoutTextLine(text, styleRule))
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

    private static StyleRule validStyle() {
        return new StyleRule(
                "cover.default",
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(12),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }

    private static StyleRule hugeStyle() {
        return new StyleRule(
                "huge",
                StyleType.PARAGRAPH,
                "Times New Roman",
                BigDecimal.valueOf(72),
                TextAlignment.CENTER,
                BigDecimal.valueOf(1.5),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                false,
                false
        );
    }
}