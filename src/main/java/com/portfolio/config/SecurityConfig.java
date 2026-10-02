package com.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Creating and editing published content is an admin action.
                        .requestMatchers("/blogs/new", "/blogs/save", "/admin/blogs/**").hasRole("ADMIN")

                        // Public endpoints - MUST come first
                        .requestMatchers(
                                "/", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/sitemap.xml", "/robots.txt",
                                "/resume/**", "/contact", "/meeting/request",
                                "/blogs", "/blogs/*", "/judge0",
                                // The notepad and its CRUD endpoints intentionally use shared public notes.
                                "/notes/**",
                                // Interview question submissions are public contributions.
                                "/interview-questions", "/interview-questions/**",
                                "/certifications", "/certifications/**", "/certifications/list",
                                "/api/jdoodle/**", "/api/chat/**",
                                "/error", "/actuator/health"
                        ).permitAll()

                        // Admin pages
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // Login
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        // Return to the originally requested protected page after login;
                        // use the dashboard as the fallback when login starts directly.
                        .defaultSuccessUrl("/admin/dashboard", false)
                        .permitAll()
                )
                // Logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                // CSRF configuration
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/contact", "/meeting/request",
                                "/api/chat/**", "/api/jdoodle/**"
                        )
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        if (adminPassword.isBlank()) {
            throw new IllegalStateException("Set ADMIN_PASSWORD before starting the application.");
        }
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
