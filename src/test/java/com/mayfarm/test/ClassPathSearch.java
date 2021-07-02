package com.mayfarm.test;

import org.junit.Test;

public class ClassPathSearch {
	@Test
	public void pathSearch() {
		String classpath = System.getProperty("java.class.path");
		System.out.println(classpath);
	}
}
