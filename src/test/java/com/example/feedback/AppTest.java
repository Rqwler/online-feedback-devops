package com.example.feedback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void thankYouMessageContainsName() {
        assertTrue(App.thankYouMessage("Ansh").contains("Ansh"));
    }

    @Test
    void thankYouMessageHandlesBlankName() {
        assertTrue(App.thankYouMessage("  ").contains("friend"));
    }

    @Test
    void thankYouMessageStripsMarkup() {
        assertFalse(App.thankYouMessage("<script>").contains("<script>"));
    }

    @Test
    void feedbackFormIsBundled() {
        assertTrue(App.class.getResource("/feedback.html") != null);
    }

    @Test
    void parseFormDecodesAllFields() {
        Map<String, String> fields = App.parseForm(
                "name=Ansh+Gupta&email=ansh%40example.com&category=Bug+Report&rating=5&comments=Great+work%21");

        assertEquals("Ansh Gupta", fields.get("name"));
        assertEquals("ansh@example.com", fields.get("email"));
        assertEquals("Bug Report", fields.get("category"));
        assertEquals("5", fields.get("rating"));
        assertEquals("Great work!", fields.get("comments"));
    }

    @Test
    void thankYouPageShowsFeedbackSummary() {
        Map<String, String> fields = App.parseForm(
                "name=Ansh&email=ansh%40example.com&category=Praise&rating=5&comments=Keep+it+up");

        String page = App.thankYouPage(fields);

        assertTrue(page.contains("Thank you, Ansh!"));
        assertTrue(page.contains("ansh@example.com"));
        assertTrue(page.contains("Praise"));
        assertTrue(page.contains("5 / 5"));
        assertTrue(page.contains("Keep it up"));
    }

    @Test
    void thankYouPageEscapesUserInput() {
        Map<String, String> fields = App.parseForm(
                "name=%3Cscript%3Ealert(1)%3C%2Fscript%3E&comments=%3Cb%3Ebold%3C%2Fb%3E");

        String page = App.thankYouPage(fields);

        assertFalse(page.contains("<script>"));
        assertFalse(page.contains("<b>bold</b>"));
        assertTrue(page.contains("&lt;script&gt;"));
    }
}
