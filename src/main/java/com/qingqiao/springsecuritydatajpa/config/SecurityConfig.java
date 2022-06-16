package com.qingqiao.springsecuritydatajpa.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.qingqiao.springsecuritydatajpa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.InvalidSessionStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Properties;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;
    @Autowired
    DataSource dataSource;
    @Autowired
    private CustomSessionInformationExpiredStrategy sessionInformationExpiredStrategy;
    @Autowired
    MyWebAuthenticationDetailsSource myWebAuthenticationDetailsSource;

    @Bean
    Producer verifyCode() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");
        String lowerString = "abcdefghijkmnpqrstuvwxyz";
        String upperString = lowerString.toUpperCase();
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789"+lowerString+upperString);
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    @Bean
    MyAuthenticationProvider myAuthenticationProvider() {
        MyAuthenticationProvider myAuthenticationProvider = new MyAuthenticationProvider();
        myAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        myAuthenticationProvider.setUserDetailsService(userService);
        return myAuthenticationProvider;
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        ProviderManager manager = new ProviderManager(Arrays.asList(myAuthenticationProvider()));
        return manager;
    }

    @Bean
    JdbcTokenRepositoryImpl jdbcTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
//        jdbcTokenRepository.setCreateTableOnStartup(true);
        return jdbcTokenRepository;
    }

    /*
        密码加密方法
     */
    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    RoleHierarchy roleHierarchy(){
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_admin > ROLE_user");
        return roleHierarchy;
    }

    /**
     * 注册 SessionRegistry，该 Bean 用于管理 Session 会话并发控制
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 配置 Session 的监听器（如果使用并发 Sessoion 控制，一般都需要配置）
     * 解决 Session 失效后, SessionRegistry 中 SessionInformation 没有同步失效问题
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /*
        认证相关
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/admin/**").hasRole("admin")
                .antMatchers("/user/**").hasRole("user")
                .antMatchers("/rememberme").rememberMe() // 这个接口只能在自动登录之后才能访问
                .antMatchers("/pay").fullyAuthenticated() // 这个接口只能在二次校验成功之后才能访问
                .antMatchers("/vc.jpg").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login.html")
                .permitAll()
                .authenticationDetailsSource(myWebAuthenticationDetailsSource)
                .loginProcessingUrl("/doLogin")
                .defaultSuccessUrl("/hello")
                .failureUrl("/error.html")
                .and()
                .rememberMe()// 记住密码
                .rememberMeParameter("rem")
                .tokenRepository(jdbcTokenRepository())// 持久化令牌
                .userDetailsService(userService)
                .key("key")
                .and()
                .logout()
                .logoutSuccessUrl("/login.html")
                .deleteCookies()
                .and()
                .csrf()
                .disable()
                .sessionManagement()
                // 设置 Session 会话失效时重定向路径，默认为 loginPage()
                // .invalidSessionUrl("/login/page")
                // 配置使用自定义的 Session 会话失效处理策略
//                .invalidSessionStrategy(sessionInformationExpiredStrategy)
                // 设置单用户的 Session 最大并发会话数量，-1 表示不受限制
                .maximumSessions(1)
                // 设置为 true，表示某用户达到最大会话并发数后，新会话请求会被拒绝登录；
                // 设置为 false，表示某用户达到最大会话并发数后，新会话请求访问时，其最老会话会在下一次请求时失效
                .maxSessionsPreventsLogin(true)
                // 设置所要使用的 sessionRegistry，默认为 SessionRegistryImpl 实现类
                .sessionRegistry(sessionRegistry())
                // 最老会话在下一次请求时失效，并重定向到 /login/page
                //.expiredUrl("/login/page");
                // 最老会话在下一次请求时失效，并按照自定义策略处理
                .expiredSessionStrategy(sessionInformationExpiredStrategy);
    }


    /*
        权限相关
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    /*
        静态资源放行
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**","/css/**","/images/**");
    }
}
