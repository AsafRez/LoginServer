package org.example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@EnableScheduling
public class Main {
    public static boolean applicationStarted = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static long startTime;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        LOGGER.info("Application started.");
        applicationStarted = true;
        startTime = System.currentTimeMillis();

    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // הכתובת של ה-React
        config.addAllowedHeader("*");
        config.addAllowedMethod("*"); // מאפשר GET, POST, OPTIONS וכו'

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and() // מאפשר להגדרות ה-CORS שלך לעבוד
                .csrf().disable() // חובה כדי לאפשר בקשות POST ו-GET מ-React
                .authorizeRequests()
                .antMatchers("/**").permitAll() // מאשר גישה לכל הנתיבים ללא צורך ב-Login של Spring
                .and()
                .formLogin().disable() // מבטל את דף הלוגין האוטומטי שגורם ל-302
                .httpBasic().disable();

        return http.build();
    }
}
