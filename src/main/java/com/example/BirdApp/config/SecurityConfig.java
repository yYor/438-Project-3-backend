package com.example.BirdApp.config;

import org.springframework.context.annotation.Bean;

import com.example.BirdApp.domain.User;
import com.example.BirdApp.repository.UserRepository;
import com.example.BirdApp.security.GoogleOAuth2UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final GoogleOAuth2UserService googleService;
    private final UserRepository userRepository;

    public SecurityConfig(GoogleOAuth2UserService googleService, UserRepository userRepository) {
        this.googleService = googleService;
        this.userRepository = userRepository;
    }

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
                    var oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();

                  String email   = oauthUser.getAttribute("email");
                  String name    = oauthUser.getAttribute("name");
                  String picture = oauthUser.getAttribute("picture");
                  String sub     = oauthUser.getName(); // Google subject id

                  // upsert user in DB
                  User user = userRepository.findByEmail(email)
                      .orElseGet(() -> {
                          User u = new User();
                          u.setEmail(email);
                          return u;
                      });

                  user.setName(name);
                  user.setProfilePicture(picture);
                  user.setOauthProvider("google");
                  user.setOauthId(sub);

                  user = userRepository.save(user);

                  // build deep link with user info
                  String redirectUrl =
                      "exp://10.11.116.151:8081" +
                      "?userId=" + user.getUserId() +
                      "&name=" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8) +
                      "&email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8) +
                      "&picture=" + java.net.URLEncoder.encode(picture != null ? picture : "", java.nio.charset.StandardCharsets.UTF_8);

                  response.sendRedirect(redirectUrl);
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
