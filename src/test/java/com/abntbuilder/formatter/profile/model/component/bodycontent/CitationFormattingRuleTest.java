package com.abntbuilder.formatter.profile.model.component.bodycontent;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

class CitationFormattingRuleTest {

    @Test
    void shouldCreateWithValidValues() {
        CitationFormattingRule rule = new CitationFormattingRule("p. ", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal");
        assertThat(rule.pagePrefix()).isEqualTo("p. ");
        assertThat(rule.multiAuthorJoiner()).isEqualTo("; ");
        assertThat(rule.etAl()).isEqualTo("et al.");
        assertThat(rule.apudConnector()).isEqualTo(" apud ");
    }

    @Test
    void shouldRejectBlankPagePrefix() {
        assertThatThrownBy(() -> new CitationFormattingRule("", "; ", "et al.", " apud ", "[...]", "grifo nosso", "grifo do autor", "informação verbal"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
