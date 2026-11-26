package com.example.feedback;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(!App.thankYouMessage("<script>").contains("<script>"));
    }

    @Test
    void feedbackFormIsBundled() {
        assertTrue(App.class.getResource("/feedback.html") != null);
    }
}
