package testpay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component("restAuthenticationEntryPoint")
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private static Map<String, String> errorResponse = new HashMap<>(2);
  static {
    errorResponse.put("error", "AUTHENTIFICATION_FAILURE");
    errorResponse.put("error_description", "Authentication failed due to invalid authentication credentials");
  }
  private static ObjectMapper mapper = new ObjectMapper();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authenticationException) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON.toString());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getOutputStream().println(mapper.writeValueAsString(errorResponse));
  }
}
