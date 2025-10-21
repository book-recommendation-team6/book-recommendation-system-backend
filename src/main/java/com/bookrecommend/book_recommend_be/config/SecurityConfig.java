package com.bookrecommend.book_recommend_be.config;

import com.bookrecommend.book_recommend_be.security.jwt.AuthTokenFilter;
import com.bookrecommend.book_recommend_be.security.jwt.JwtEntryPoint;
import com.bookrecommend.book_recommend_be.security.oauth2.OAuth2FailureHandler;
import com.bookrecommend.book_recommend_be.security.oauth2.OAuth2SuccessHandler;
import com.bookrecommend.book_recommend_be.security.userdetails.AppUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AppUserDetailsService userDetailsService;
    private final JwtEntryPoint authEntryPoint;
    private final AuthTokenFilter authTokenFilter;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Value("${api.prefix}")
    private String API;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String adminEndpoints = API + "/admin/**";
        String authEndpoints = API + "/auth/**";
        String bookEndpoints = API + "/books/**";
        String userEndpoints = API + "/users/**";
        String authorEndpoints = API + "/authors/**";
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(authEndpoints).permitAll()
                        .requestMatchers(API + "/upload").permitAll()
                        .requestMatchers(HttpMethod.GET, authorEndpoints).permitAll()
                        .requestMatchers(HttpMethod.GET, bookEndpoints).permitAll()
                        .requestMatchers(adminEndpoints).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, bookEndpoints).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, bookEndpoints).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, bookEndpoints).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                API + "/users/*/ban",
                                API + "/users/*/unban").hasAuthority("ADMIN")
                        .requestMatchers(userEndpoints).authenticated()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
