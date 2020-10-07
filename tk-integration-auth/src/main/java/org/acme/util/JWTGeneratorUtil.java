package org.acme.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.build.Jwt;

@ApplicationScoped
public class JWTGeneratorUtil {

	@ConfigProperty(name = "mp.jwt.verify.roles")
	Set<String> roles;
	
	@ConfigProperty(name = "mp.jwt.verify.issuer")
	String issuer;
	
	@ConfigProperty(name = "mp.jwt.verify.upn")
	String upn;

	@ConfigProperty(name = "mp.jwt.verify.duration")
	Long duration;
	
	public String getToken() {
			
		Instant now=Instant.now();
		
		return 
			Jwt.issuer(issuer)
		 	   .upn(upn)
		 	   .groups(new HashSet<>(roles))
		 	   .issuedAt(now)
		 	   .expiresAt(now.plus(duration, ChronoUnit.MINUTES))
		 	   .claim(Claims.zoneinfo.name(), ZoneId.systemDefault().toString())
		 	   .sign();
	}

	public JWTCallerPrincipal getPrincipal(String token) throws InvalidJwtException {
		String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
		return new DefaultJWTCallerPrincipal(JwtClaims.parse(json));
	}
	
	public Boolean isTokenExpired(String token) throws InvalidJwtException  {
		
		JWTCallerPrincipal principal=this.getPrincipal(token);
		
		ZoneId tokenTimeZone=ZoneId.of(principal.getClaim("zoneinfo"));
			
		return Instant.ofEpochSecond(principal.getExpirationTime()).atZone(tokenTimeZone).isBefore(Instant.now().atZone(tokenTimeZone));
		
	}

}
