package models;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class ActivityTest extends UnitTest {

	@Before
	public void setUp() throws Exception {
		Fixtures.deleteAll();
		Fixtures.load("users-and-study-sessions.yml");
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testCreateAndRetrieve()  throws Exception {
		createCourse();
		List<Activity> preActivities = Activity.findAll();
		
		List<SocialUser> users = SocialUser.findAll();
		Assert.assertNotSame(0, users.size());
		List<Course> courses = Course.findAll();
		Course course = courses.get(0);
		List<CourseSection> sections = course.fetchSectionsByPlacement();
		CourseSection theSection = sections.get(0);
		
		String title = "test activity";
		String content = "test activity content";
		Activity activity = new Activity(title, content);
		theSection.activities.add(activity);
		theSection.save();
		
		List<Activity> allActivities = Activity.findAll();
		assertEquals(preActivities.size() + 1, allActivities.size());
		
		//TODO: I am not sure if this is a good idea... will we always get the activity at this location ?
		Activity retrievedActivity = allActivities.get(preActivities.size());
		assertEquals(title, retrievedActivity.title);
		assertEquals(content, retrievedActivity.content);
	}
	
	@Test
	public void testHasResponded() throws Exception {
		createCourseWithActivities();
		List<SocialUser> socialUsers = SocialUser.findAll();
		Course course = Course.findBySanitizedTitle("play-framework");
		CourseSection section = CourseSection.findBySanitizedTitleByCouse(course, "introduction");
		Activity activity = section.activities.iterator().next();
		ActivityResponse activityResponse = new ActivityResponse(socialUsers.get(0), activity, "title", "http://diycomputerscience.com");
		activityResponse.save();
		
		assertTrue(activity.hasResponded(String.valueOf(socialUsers.get(0).id)));
		assertFalse(activity.hasResponded(String.valueOf(socialUsers.get(1).id)));
	}
	
	public static void createCourse() {
		CourseCategory cat = new CourseCategory("courses");
		cat.save();
		
		Course course = new Course("Play Framework", "Play framework course");
		course.category = cat;
		course.save();
		CourseSection section = new CourseSection(course, "introduction", "Introductory section");
		section.save();
	}
	
	public static void createCourseWithActivities() {
		CourseCategory cat = new CourseCategory("courses");
		cat.save();
		
		Course course = new Course("Play Framework", "Play framework course");
		course.category = cat;
		course.save();
		CourseSection section = new CourseSection(course, "introduction", "Introductory section");
		Activity activity = new Activity("Blog", "Please write a blog post");
		section.activities.add(activity);
		section.save();
	}

}
