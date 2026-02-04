package org.example.Utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. הגדרת CORS בתוך ה-Security
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    // הכתובת המדויקת של ה-Frontend שלך ב-Render
                    config.setAllowedOrigins(Arrays.asList("https://stock-scanner-user.onrender.com"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                // 2. ביטול CSRF (נחוץ כשעובדים עם JWT/Stateless)
                .csrf(csrf -> csrf.disable())
                // 3. פתיחת הגישה לנתיבים הספציפיים
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/Register", "/Login", "/check-session").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}