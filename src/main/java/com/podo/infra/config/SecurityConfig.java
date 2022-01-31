package com.podo.infra.config;


import com.podo.modules.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // POST도 허용
                .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
                        "/email-login", "check-email-login", "/login-link", "search/crew").permitAll()
                // 프로필 요청은 GET만 허용
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                // 나머지 요청은 로그인 해야 가능
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login").permitAll();
        // 로그아웃 한 후 이동 페이지
        http.logout()
                .logoutSuccessUrl("/");

        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());
    }

    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    // resource들은 spring security필터 적용 X
    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                // 적합한 회원 로그인
                .antMatchers("/favicon.ico", "/resources/**", "/error");
    }
}
