package de.sormuras.stash;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StashTests {

	@Test void stashIsInterface() {
		assertTrue(Stash.class.isInterface());
	}

}
