package com.example.BirdApp.config;

import org.springframework.context.annotation.Bean;
import com.example.BirdApp.security.GoogleOAuth2UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final GoogleOAuth2UserService googleService;

    public SecurityConfig(GoogleOAuth2UserService googleService) {
        this.googleService = googleService;
    }
    // private final GoogleOidcUserService googleOidcService;

    // public SecurityConfig(GoogleOidcUserService googleOidcService) {
    //     this.googleOidcService = googleOidcService;
    // }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/signup").authenticated()
                .requestMatchers("/", "/api/public/**","/oauth2/**","/login/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> {
                oauth.userInfoEndpoint(userInfo ->
                    userInfo.userService(googleService)
                );

                oauth.successHandler((request, response, authentication) -> {
                    // TODO: replace with real JWT later if you want token-based auth
                    String token = "TEMP_TOKEN";

                    // If the login was started from the mobile app, we pass mobile=true
                    String mobileFlag = request.getParameter("mobile");
                    boolean fromMobileApp = "true".equalsIgnoreCase(mobileFlag);

                    if (fromMobileApp) {
                    //     // 🔹 Mobile: send deep-link back to Expo app
                        String redirectUrl = "exp://10.11.116.151:8081?token=" + token;
                        response.sendRedirect(redirectUrl);
                    } else {
                    //     // 🔹 Browser: just go somewhere normal (e.g. / or /api/users/me)
                        response.sendRedirect("/api/auth/signup");
                    }
                });

                // (Optional) you can plug your failureHandler here again if you want
            })
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
