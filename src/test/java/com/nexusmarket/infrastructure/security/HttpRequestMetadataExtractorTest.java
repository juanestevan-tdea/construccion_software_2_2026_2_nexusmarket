package com.nexusmarket.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestMetadataExtractorTest {

    @Test
    @DisplayName("prioriza la primera IP de X-Forwarded-For")
    void extractIp_usesFirstForwardedAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");

        assertThat(HttpRequestMetadataExtractor.extractIp(request)).isEqualTo("203.0.113.7");
    }

    @Test
    @DisplayName("usa remoteAddr cuando no hay X-Forwarded-For")
    void extractIp_fallsBackToRemoteAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");

        assertThat(HttpRequestMetadataExtractor.extractIp(request)).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("extrae User-Agent y usa unknown cuando falta")
    void extractUserAgent_handlesPresentAndMissingHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0 Test");
        assertThat(HttpRequestMetadataExtractor.extractUserAgent(request))
                .isEqualTo("Mozilla/5.0 Test");

        MockHttpServletRequest missing = new MockHttpServletRequest();
        assertThat(HttpRequestMetadataExtractor.extractUserAgent(missing)).isEqualTo("unknown");
    }

    @Test
    @DisplayName("una petición nula no provoca NPE")
    void nullRequest_returnsUnknown() {
        assertThat(HttpRequestMetadataExtractor.extractIp(null)).isEqualTo("unknown");
        assertThat(HttpRequestMetadataExtractor.extractUserAgent(null)).isEqualTo("unknown");
    }
}
