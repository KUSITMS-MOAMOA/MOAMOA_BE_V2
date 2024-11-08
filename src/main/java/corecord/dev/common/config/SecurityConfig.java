package corecord.dev.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import corecord.dev.common.response.ApiResponse;
import corecord.dev.common.status.ErrorStatus;
import corecord.dev.common.util.CookieUtil;
import corecord.dev.common.util.JwtFilter;
import corecord.dev.common.util.JwtUtil;
import corecord.dev.domain.auth.application.OAuthLoginFailureHandler;
import corecord.dev.domain.auth.application.OAuthLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;

    private final String[] swaggerUrls = {"/swagger-ui/**", "/v3/**"};
    private final String[] authUrls = {"/", "/api/users/register", "/oauth2/authorization/kakao", "/actuator/health", "/api/token/**", "/api/token"};
    private final String[] allowedUrls = Stream.concat(Arrays.stream(swaggerUrls), Arrays.stream(authUrls))
            .toArray(String[]::new);

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173", "https://localhost:5173",
                "https://corecord.site",
                "https://www.corecord.site",
                "https://corecord.vercel.app"
        ));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            ObjectMapper mapper = new ObjectMapper();
            ErrorStatus errorStatus = ErrorStatus.UNAUTHORIZED;
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(errorStatus.getHttpStatus().value());
            ApiResponse<Object> errorResponse = ApiResponse.error(ErrorStatus.UNAUTHORIZED).getBody();
            response.getWriter().write(mapper.writeValueAsString(errorResponse));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        httpSecurity
                .httpBasic(HttpBasicConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
//                .exceptionHandling(exceptionHandlingConfigurer ->
//                        exceptionHandlingConfigurer
//                                .authenticationEntryPoint(authenticationEntryPoint)
//                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(allowedUrls).permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth ->
                        oauth
                                .successHandler(oAuthLoginSuccessHandler)
                                .failureHandler(oAuthLoginFailureHandler)
                )
                .addFilterBefore(new JwtFilter(jwtUtil, cookieUtil), UsernamePasswordAuthenticationFilter.class)
        ;

        return httpSecurity.build();
    }

}
