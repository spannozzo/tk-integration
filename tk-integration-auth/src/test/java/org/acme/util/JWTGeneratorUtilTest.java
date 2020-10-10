package org.acme.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;



@QuarkusTest
class JWTGeneratorUtilTest {

	@Inject
	JWTGeneratorUtil util;
	
	@ConfigProperty(name = "mp.jwt.verify.issuer")
	String issuer;
	
	@ConfigProperty(name = "mp.jwt.verify.upn")
	String upn;
	
	@ConfigProperty(name = "mp.jwt.verify.roles")
	Set<String> roles;
	
	@Test
	void should_create_a_JWT_token() throws InvalidJwtException, IOException {
		String token = util.getToken();
		        
		assertThat(token, is(not(emptyOrNullString())));
	
		String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
		JWTCallerPrincipal principal= new DefaultJWTCallerPrincipal(JwtClaims.parse(json));
		
		assertThat(principal, is(notNullValue()));
		assertThat(principal.getIssuer(), is(issuer));
		assertThat(principal.getName(), is(upn));
		assertThat(principal.getGroups(), is(roles));
		assertThat(util.isTokenExpired(token), is(false));
	}

}