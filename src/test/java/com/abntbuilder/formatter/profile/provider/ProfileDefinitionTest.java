package com.abntbuilder.formatter.profile.provider;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.shared.exception.InvalidProfileStructureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileDefinitionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldLoadMinimalExplicitParagraphsProfile() throws IOException {
        DocumentProfile profile = readProfile("""
                {
                  "id": "test-profile",
                  "displayName": "Test Profile",
                  "componentOrder": ["paragraphs"],
                  "pageRule": {
                    "widthCm": 21,
                    "heightCm": 29.7,
                    "marginTopCm": 3,
                    "marginRightCm": 2,
                    "marginBottomCm": 2,
                    "marginLeftCm": 3,
                    "orientation": "PORTRAIT"
                  },
                  "styleRules": [
                    {
                      "id": "body",
                      "type": "PARAGRAPH",
                      "fontFamily": "Times New Roman",
                      "fontSizePt": 12,
                      "alignment": "JUSTIFIED",
                      "lineSpacing": 1.5,
                      "firstLineIndentCm": 1.25,
                      "leftIndentCm": 0,
                      "rightIndentCm": 0,
                      "spacingBeforePt": 0,
                      "spacingAfterPt": 0,
                      "bold": false,
                      "italic": false,
                      "uppercase": false
                    }
                  ],
                  "componentRules": {}
                }
                """);

        assertEquals(List.of("paragraphs"), profile.componentOrder());
    }

    @Test
    void shouldRejectProfileDefinitionWithoutComponentOrder() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> readProfile("""
                        {
                          "id": "test-profile",
                          "displayName": "Test Profile",
                          "pageRule": {
                            "widthCm": 21,
                            "heightCm": 29.7,
                            "marginTopCm": 3,
                            "marginRightCm": 2,
                            "marginBottomCm": 2,
                            "marginLeftCm": 3,
                            "orientation": "PORTRAIT"
                          },
                          "styleRules": [
                            {
                              "id": "body",
                              "type": "PARAGRAPH",
                              "fontFamily": "Times New Roman",
                              "fontSizePt": 12,
                              "alignment": "JUSTIFIED",
                              "lineSpacing": 1.5,
                              "firstLineIndentCm": 1.25,
                              "leftIndentCm": 0,
                              "rightIndentCm": 0,
                              "spacingBeforePt": 0,
                              "spacingAfterPt": 0,
                              "bold": false,
                              "italic": false,
                              "uppercase": false
                            }
                          ],
                          "componentRules": {}
                        }
                        """)
        );

        assertEquals("componentOrder must be provided.", exception.getMessage());
    }

    @Test
    void shouldRejectStyleRuleWithoutRequiredBooleanFlags() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> readProfile("""
                        {
                          "id": "test-profile",
                          "displayName": "Test Profile",
                          "componentOrder": ["paragraphs"],
                          "pageRule": {
                            "widthCm": 21,
                            "heightCm": 29.7,
                            "marginTopCm": 3,
                            "marginRightCm": 2,
                            "marginBottomCm": 2,
                            "marginLeftCm": 3,
                            "orientation": "PORTRAIT"
                          },
                          "styleRules": [
                            {
                              "id": "body",
                              "type": "PARAGRAPH",
                              "fontFamily": "Times New Roman",
                              "fontSizePt": 12,
                              "alignment": "JUSTIFIED",
                              "lineSpacing": 1.5,
                              "firstLineIndentCm": 1.25,
                              "leftIndentCm": 0,
                              "rightIndentCm": 0,
                              "spacingBeforePt": 0,
                              "spacingAfterPt": 0,
                              "italic": false,
                              "uppercase": false
                            }
                          ],
                          "componentRules": {}
                        }
                        """)
        );

        assertEquals("style.bold must be provided.", exception.getMessage());
    }

    @Test
    void shouldRejectSinglePageItemWithoutHorizontalPlacement() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> readProfile(coverProfileWithItem("""
                        {
                          "id": "institutionalLines",
                          "required": true
                        }
                        """))
        );

        assertEquals("item.horizontalPlacement must be provided.", exception.getMessage());
    }

    @Test
    void shouldRejectSinglePageGroupWithoutRequiredFlag() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> readProfile(coverProfileWithGroupRequired(""))
        );

        assertEquals("group.required must be provided.", exception.getMessage());
    }

    @Test
    void shouldRejectSinglePageLayoutWithoutPolicy() {
        InvalidProfileStructureException exception = assertThrows(
                InvalidProfileStructureException.class,
                () -> readProfile(coverProfileWithoutPolicy())
        );

        assertEquals("cover.layoutRule.policy must be provided.", exception.getMessage());
    }

    private DocumentProfile readProfile(String json) throws IOException {
        return objectMapper.readValue(json, ProfileDefinition.class).toDomain();
    }

    private static String coverProfileWithItem(String itemJson) {
        return coverProfile("""
                "groups": [
                  {
                    "id": "cover.institution",
                    "required": true,
                    "items": [
                      %s
                    ]
                  }
                ],
                "gapRules": [],
                "policy": {
                  "anchorStrategy": "LAST_GROUP_AT_SAFE_AREA_END",
                  "lineHeightStrategy": "MAX_EXACT_LINE_HEIGHT",
                  "spacerStylePolicy": "NEXT_GROUP_STYLE",
                  "safetyPolicy": "MARGIN_BASED"
                }
                """.formatted(itemJson));
    }

    private static String coverProfileWithGroupRequired(String requiredProperty) {
        return coverProfile("""
                "groups": [
                  {
                    "id": "cover.institution",
                    %s
                    "items": [
                      {
                        "id": "institutionalLines",
                        "required": true,
                        "horizontalPlacement": {
                          "strategy": "FULL_CONTENT_WIDTH"
                        }
                      }
                    ]
                  }
                ],
                "gapRules": [],
                "policy": {
                  "anchorStrategy": "LAST_GROUP_AT_SAFE_AREA_END",
                  "lineHeightStrategy": "MAX_EXACT_LINE_HEIGHT",
                  "spacerStylePolicy": "NEXT_GROUP_STYLE",
                  "safetyPolicy": "MARGIN_BASED"
                }
                """.formatted(requiredProperty));
    }

    private static String coverProfileWithoutPolicy() {
        return coverProfile("""
                "groups": [
                  {
                    "id": "cover.institution",
                    "required": true,
                    "items": [
                      {
                        "id": "institutionalLines",
                        "required": true,
                        "horizontalPlacement": {
                          "strategy": "FULL_CONTENT_WIDTH"
                        }
                      }
                    ]
                  }
                ],
                "gapRules": []
                """);
    }

    private static String coverProfile(String layoutRuleJson) {
        return """
                {
                  "id": "test-profile",
                  "displayName": "Test Profile",
                  "componentOrder": ["cover"],
                  "pageRule": {
                    "widthCm": 21,
                    "heightCm": 29.7,
                    "marginTopCm": 3,
                    "marginRightCm": 2,
                    "marginBottomCm": 2,
                    "marginLeftCm": 3,
                    "orientation": "PORTRAIT"
                  },
                  "styleRules": [
                    {
                      "id": "body",
                      "type": "PARAGRAPH",
                      "fontFamily": "Times New Roman",
                      "fontSizePt": 12,
                      "alignment": "CENTER",
                      "lineSpacing": 1.5,
                      "firstLineIndentCm": 0,
                      "leftIndentCm": 0,
                      "rightIndentCm": 0,
                      "spacingBeforePt": 0,
                      "spacingAfterPt": 0,
                      "bold": false,
                      "italic": false,
                      "uppercase": false
                    }
                  ],
                  "componentRules": {
                    "cover": {
                      "componentId": "cover",
                      "styleMapping": {
                        "institutionalLinesStyleId": "body",
                        "authorsStyleId": "body",
                        "titleStyleId": "body",
                        "subtitleStyleId": "body",
                        "cityStyleId": "body",
                        "yearStyleId": "body"
                      },
                      "layoutRule": {
                        %s
                      }
                    }
                  }
                }
                """.formatted(layoutRuleJson);
    }
}
