/*
 * PATCH INSTRUCTIONS:
 * Add the @Value field and corsConfigurationSource() bean to your existing
 * SecurityConfig.java. Also wire the cors() call into the filter chain.
 *
 * 1. Add this field at the top of the class:
 */

@Value("${app.cors.allowed-origins:http://localhost:5173}")
private String allowedOrigins;

/*
 * 2. Add this bean anywhere in the class:
 */

@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}

/*
 * 3. In your securityFilterChain() method, add .cors(Customizer.withDefaults())
 *    as the FIRST call after http:
 *
 *    http
 *        .cors(Customizer.withDefaults())   // <-- ADD THIS LINE
 *        .csrf(csrf -> csrf.disable())
 *        ...
 *
 * 4. Add these imports to SecurityConfig.java:
 *
 *    import org.springframework.beans.factory.annotation.Value;
 *    import org.springframework.web.cors.CorsConfiguration;
 *    import org.springframework.web.cors.CorsConfigurationSource;
 *    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 *    import org.springframework.security.config.Customizer;
 *    import java.util.List;
 */
