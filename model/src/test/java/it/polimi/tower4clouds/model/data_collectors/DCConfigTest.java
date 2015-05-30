package it.polimi.tower4clouds.model.data_collectors;

import static org.junit.Assert.*;
import it.polimi.tower4clouds.model.data_collectors.DCConfiguration;
import it.polimi.tower4clouds.model.ontology.Method;
import it.polimi.tower4clouds.model.ontology.Resource;
import it.polimi.tower4clouds.model.ontology.VM;

import org.junit.Test;

public class DCConfigTest {

	@Test
	public void configurationShouldBeAboutResourceWhenIsSameClass() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("VM", null, null);
		Resource resource = new VM();
		assertTrue(conf.isAboutResource(resource));
	}

	@Test
	public void configurationShouldBeAboutResourceWhenIsASuperclassOfTarget1() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("VM", null, null);
		Resource resource = new Resource();
		assertTrue(conf.isAboutResource(resource));
	}

	@Test
	public void configurationShouldBeAboutResourceWhenIsASuperclassOfTarget2() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("VM", "Frontend", null);
		Resource resource = new Resource();
		assertTrue(conf.isAboutResource(resource));
	}

	@Test
	public void configurationShouldBeAboutResourceWhenIsASuperclassOfTarget3() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("VM", "Frontend", "Frontend1");
		Resource resource = new Resource();
		assertTrue(conf.isAboutResource(resource));
	}

	@Test
	public void test() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("VM", "Frontend", null);
		VM vm = new VM("Frontend", "Frontend1");
		assertTrue(conf.isAboutResource(vm));
	}
	
	@Test
	public void test2() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("VM", "Frontend", null);
		Method method = new Method("Frontend", "Frontend1");
		assertFalse(conf.isAboutResource(method));
	}

	@Test
	public void test3() {
		DCConfiguration conf = new DCConfiguration();
		conf.addTargetResource("Method", null, null);
		Method method = new Method("Login", "Login1");
		assertTrue(conf.isAboutResource(method));
	}
}
