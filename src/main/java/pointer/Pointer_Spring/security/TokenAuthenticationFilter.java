package pointer.Pointer_Spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pointer.Pointer_Spring.config.AppProperties;
import pointer.Pointer_Spring.validation.ExceptionCode;

import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final String[] WHITE_LIST = { // auth 관련 형식
            "/api/v1/auth/**"
    };

    @Autowired
    private AppProperties appProperties;

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            String jwt = getJwtFromRequest(request);
            if (isFilterCheck(request.getRequestURI()) && StringUtils.hasText(jwt)) {
                if (tokenProvider.isTokenExpired(jwt)) {
                    createResponse(ExceptionCode.EXPIRED_JWT_TOKEN, response);
                } else {
                    Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(jwt);

                    Long userId = tokenProvider.getUserIdFromToken(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                            userDetails.getPassword(), userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }

        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
            createResponse(ExceptionCode.INVALID_JWT_SIGNATURE, response);
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
            createResponse(ExceptionCode.INVALID_JWT_TOKEN, response);
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
            createResponse(ExceptionCode.EXPIRED_JWT_TOKEN, response);
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
            createResponse(ExceptionCode.UNSUPPORTED_JWT_TOKEN, response);
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
            createResponse(ExceptionCode.JWT_CLAIMS_STRING_IS_EMPTY, response);
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 인증 관련 응답 형식 생성
     * @param exceptionCode 예외 코드
     * @param response 서블릿 객체
     * @throws IOException 예외
     */
    private void createResponse(ExceptionCode exceptionCode, ServletResponse response) throws IOException {
        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("state", HttpServletResponse.SC_UNAUTHORIZED); // exceptionCode.getStatus().getValue()
        json.put("code", exceptionCode.getCode());

        //String message = exceptionCode.getMessage();
        //String escapedDescription = UriUtils.encode(message, "UTF-8");
        json.put("message", exceptionCode.getMessage());

        String newResponse = new ObjectMapper().writeValueAsString(json);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(newResponse.getBytes(StandardCharsets.UTF_8).length);
        response.getWriter().write(newResponse);
    }

    /**
     * 화이트 리스트에 포함되어 인증이 필요한지
     * @param requestURI 요청 받은 uri
     * @return 인증 필요 여부
     */
    private boolean isFilterCheck(String requestURI) {
        return !PatternMatchUtils.simpleMatch(WHITE_LIST, requestURI);
    }
}
