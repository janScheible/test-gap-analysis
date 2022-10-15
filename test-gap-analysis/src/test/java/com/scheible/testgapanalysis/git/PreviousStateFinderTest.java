package com.scheible.testgapanalysis.git;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.assertj.core.util.Sets;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.git.PreviousStateFinder.TimestampedState;

/**
 *
 * @author sj
 */
public class PreviousStateFinderTest {

	private static final LocalDate TODAY = LocalDate.of(2022, 9, 9);

	@Test
	public void testFindPreviousNotHead() {
		assertThat(PreviousStateFinder.findPrevious(Sets.newLinkedHashSet( //
				new TimestampedState("test", time(10, 0, 0), mock(ObjectId.class), false) //
		), "test").getName()).isEqualTo("test");
	}

	@Test
	public void testFindPreviousHeadMultipleStates() {
		assertThat(PreviousStateFinder.findPrevious(Sets.newLinkedHashSet( //
				new TimestampedState("test2", time(12, 0, 0), mock(ObjectId.class), false), //
				new TimestampedState("test1", time(10, 0, 0), mock(ObjectId.class), true)), "test\\d+").getName())
						.isEqualTo("test2");
	}

	@Test
	public void testFindPreviousHeadSingleState() {
		assertThat(PreviousStateFinder.findPrevious(Sets.newLinkedHashSet( //
				new TimestampedState("test", time(10, 0, 0), mock(ObjectId.class), true) //
		), "test").getName()).isEqualTo("test");
	}

	@Test
	public void testNameMatching() {
		assertThat(PreviousStateFinder.matches("v1", "v\\d+")).isTrue();
	}

	@Test
	public void testNameMatchingWithSlash() {
		assertThat(PreviousStateFinder.matches("refs/remote/v1", "v\\d+")).isTrue();
	}

	private static OffsetDateTime time(int hour, int minute, int second) {
		return OffsetDateTime.of(TODAY, LocalTime.of(hour, minute, second), ZoneOffset.UTC);
	}
}
