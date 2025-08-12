package com.susang.stellarVision.config.security;

import com.susang.stellarVision.config.security.filter.JWTAuthenticationFilter;
import com.susang.stellarVision.config.security.filter.JWTVerificationFilter;
import com.susang.stellarVision.config.security.filter.SecurityExceptionHandlingFilter;
import com.susang.stellarVision.config.security.authentication.CustomUserDetailsService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class APISecurityConfig {

    private final String[] PUBLIC_GET = {
            "/api/photos/{photoId}",
            "/api/photos/{photoId}/tags",
            "/api/profiles/{memberId}/photos",
            "/api/profiles/{memberId}/videos",
            "/api/videos/search",
            "/api/videos",
            "/api/videos/{videoId}",
            "/api/profiles/{memberId}/badge",
            "/api/profiles/{memberId}/collections",
            "/api/streamings",
            "/api/profiles/{memberId}/image",
            "/api/profiles/{memberId}",
            "/api/videos/{videoId}/tags",
    };

    private final String[] PUBLIC_POST = {
            "/api/auth/email/verification",
            "/api/auth/email/send",
            "/api/members",
            "/api/auth/login",
    };

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Order(1)
    @Bean
    public SecurityFilterChain oauth2SecurityFilterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler authenticationSuccessHandler,
            AuthenticationFailureHandler authenticationFailureHandler
    ) throws Exception {
        http.securityMatcher("/api/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest()
                        .permitAll())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(a -> a.baseUri("/api/oauth2/authorization"))
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler));

        return http.build();
    }

    @Order(2)
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfig,
            CustomUserDetailsService userDetailsService,
            JWTAuthenticationFilter authFilter, JWTVerificationFilter jwtVerifyFilter,
            SecurityExceptionHandlingFilter exceptionFilter) throws Exception {

        http.securityMatcher("/api/**")
                .cors(t -> t.configurationSource(corsConfig))
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                .anyRequest().authenticated());

        // SecurityFilter 앞에 API 로그 필터를 추가
        http.addFilterBefore(jwtVerifyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionFilter, JWTVerificationFilter.class);

        return http.build();
    }

    // WebMvcConfigurer는 cors 설정은 무의미
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:5173", "https://i13c106.p.ssafy.io"));
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        //“클라이언트(브라우저)가 쿠키, 인증 헤더(JWT 등), 세션 같은 자격 정보(credentials)를 서버로 보낼 수 있게 허용하겠다”
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/member/checkEmail", configuration);

        return source;
    }
}