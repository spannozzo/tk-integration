package org.acme.profile;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;
/**
 * used to override the cv stored folder for simulating 
 * file save exception
 * 
 *
 */
public class WrongFolderProfile implements QuarkusTestProfile  {
	
	
	private final static String FAKE_STORE_FOLDER="./src/test/resources/META-INF/resources/unexistant-folder";
	
	@Override
    public Map<String, String> getConfigOverrides() { 
        return Collections.singletonMap("cv-store-folder",FAKE_STORE_FOLDER);
    }
}
