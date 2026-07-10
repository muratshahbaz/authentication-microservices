package az.jet.logregis.config;
import az.jet.logregis.dao.repository.UserRepository;
import az.jet.logregis.filter.JwtAuthenticationFilter;
import az.jet.logregis.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    private final UserRepository userRepository;


    @Bean
    public UserDetailsService userDetailsService() {

        return username ->
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException("User not found")
                        );
    }


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService
    ) {

        return new JwtAuthenticationFilter(
                jwtService,
                userDetailsService
        );
    }



    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationProvider authenticationProvider
    ) throws Exception {


        http

                .csrf(csrf -> csrf.disable())


                .cors(cors ->
                        cors.configurationSource(request -> {

                            CorsConfiguration config =
                                    new CorsConfiguration();

                            config.addAllowedOriginPattern("*");
                            config.addAllowedHeader("*");
                            config.addAllowedMethod("*");

                            return config;
                        })
                )


                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/v1/us/register",
                                "/api/v1/us/login",
                                "/api/v1/us/refresh",
                                "/api/v1/us/activate",
                                "/api/v1/otp/**",
                                "/api/v1/us/token"
                        )
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                )


                .authenticationProvider(authenticationProvider)


                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}