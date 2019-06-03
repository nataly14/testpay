package testpay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  @Autowired
  private AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.resourceId("Payment_Resources");
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
      .requestMatchers()
      .antMatchers("/payments/**")
      .and()
      .authorizeRequests().antMatchers("/payments/**").authenticated()
      .and()
      .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
  }
}