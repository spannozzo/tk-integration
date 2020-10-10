package org.acme.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.acme.util.AESUtil;


@ApplicationScoped
public class AppConfig {

	
	@Produces
	AESUtil getAESUtils() {
		return AESUtil.getInstance();
	}
}
