package testpay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;
  private static final String MERCHANTS_SECRET = "123";
  public static final String SHA2_MERCHANTS_SECRET;
  private static final Logger logger = LogManager.getLogger(AuthServerConfig.class);

  static {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      logger.fatal("No SHA-256 algorithm found. {}", e.getMessage());
      throw new RuntimeException("No SHA-256 algorithm found", e);
    }
    SHA2_MERCHANTS_SECRET = new String(digest.digest(MERCHANTS_SECRET.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
  }

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints.pathMapping("/oauth/token", "/oauth2/token")
      .authenticationManager(this.authenticationManager);
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    oauthServer.tokenKeyAccess("permitAll()")
      .checkTokenAccess("isAuthenticated()");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.inMemory()
      .withClient("john")
      .secret(passwordEncoder.encode(MERCHANTS_SECRET))
      .authorizedGrantTypes("client_credentials")
      .scopes("https://api.testpay.com/payments/.*")
      .resourceIds("Payment_Resources")
      .autoApprove(true);
  }

}