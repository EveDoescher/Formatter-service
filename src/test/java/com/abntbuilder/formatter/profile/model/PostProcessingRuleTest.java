package com.abntbuilder.formatter.profile.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PostProcessingRuleTest {

    @Test
    void shouldCreateEmptyPostProcessingRule() {
        PostProcessingRule rule = new PostProcessingRule(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertThat(rule.tableContinuationLabels()).isEmpty();
        assertThat(rule.orphanTitleCorrection()).isEmpty();
        assertThat(rule.integrityCheck()).isEmpty();
        assertThat(rule.pdfOutput()).isEmpty();
    }

    @Test
    void shouldCreateTableContinuationLabelsWhenEnabled() {
        PostProcessingRule.TableContinuationLabelsRule labels =
                new PostProcessingRule.TableContinuationLabelsRule(
                        true, "continua", "continuação", "conclusão", "table.continuation");

        assertThat(labels.enabled()).isTrue();
        assertThat(labels.continuesLabel()).isEqualTo("continua");
        assertThat(labels.conclusionLabel()).isEqualTo("conclusão");
    }

    @Test
    void shouldCreateTableContinuationLabelsWhenDisabled() {
        PostProcessingRule.TableContinuationLabelsRule labels =
                new PostProcessingRule.TableContinuationLabelsRule(
                        false, null, null, null, null);

        assertThat(labels.enabled()).isFalse();
    }

    @Test
    void shouldRejectMissingContinuesLabelWhenEnabled() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new PostProcessingRule.TableContinuationLabelsRule(
                        true, null, "continuação", "conclusão", "style"));
    }

    @Test
    void shouldRejectMissingStyleIdWhenEnabled() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                new PostProcessingRule.TableContinuationLabelsRule(
                        true, "continua", "continuação", "conclusão", null));
    }

    @Test
    void shouldCreateIntegrityCheckRuleWithMaxPages() {
        PostProcessingRule.IntegrityCheckRule rule =
                new PostProcessingRule.IntegrityCheckRule(true, true, false, Optional.of(300));

        assertThat(rule.enabled()).isTrue();
        assertThat(rule.maxPages()).contains(300);
    }

    @Test
    void shouldRejectNullOptionalFieldsInPostProcessingRule() {
        assertThatNullPointerException().isThrownBy(() ->
                new PostProcessingRule(null, Optional.empty(), Optional.empty(), Optional.empty()));
    }
}
