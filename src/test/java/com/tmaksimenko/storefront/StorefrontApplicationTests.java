package com.tmaksimenko.storefront;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class StorefrontApplicationTests {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	void contextLoads() {
	}

	@Test
	void test() {
		Long test = Long.valueOf("1001");
		logger.info("{}", test);
	}

}
