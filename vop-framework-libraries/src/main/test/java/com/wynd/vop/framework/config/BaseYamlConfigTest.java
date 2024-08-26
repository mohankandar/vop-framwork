/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wynd.vop.framework.config;

import com.wynd.vop.framework.config.BaseYamlConfig.BaseYamlEnvironment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class BaseYamlConfigTest {
	
	@Autowired
	ResourceLoader resourceLoader;
	
	/**
     * Test of properties method, of class BaseYamlConfigTest.
     */
    @Test
    public void testYamlProperties() {
        
        //Resource responseBody = new ClassPathResource("classpath:/src/test/resources/application.yml");
        
        PropertySourcesPlaceholderConfigurer result = BaseYamlConfig.properties(resourceLoader.getResource("application.yml"));
        assertNotNull(result);
        
        BaseYamlEnvironment basePropertiesEnvironment = new BaseYamlEnvironment();
        basePropertiesEnvironment.postConstruct();
    }
    @Test
    public void testConstructor() {
    	BaseYamlConfig test = new BaseYamlConfig();
    	assertNotNull(test);
    }
}
