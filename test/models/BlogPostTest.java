package models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BlogPostTest extends UnitTest {
	
	@Before
	public void setUp() {
		Fixtures.deleteAll();
		Fixtures.load("data.yml");
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void testCreateAndRetreiveBlogPost() throws Exception {
		SocialUser user = SocialUser.findById((long)1);
		BlogPost blogPost = new BlogPost(user,
										 "Test blog post",
										 "This is a test blog post");
		blogPost.save();
		BlogPost retrievedBlogPost = BlogPost.find("select b from BlogPost b").first();
		assertNotNull(retrievedBlogPost);
		assertEquals("Test blog post", retrievedBlogPost.title);
		assertEquals("This is a test blog post", retrievedBlogPost.content);
	}
	
	@Test
	public void testRetrieveLatest() throws Exception {
		SocialUser user = SocialUser.findById((long)1);
		
		BlogPost blogPost = new BlogPost(user,
				 "Blog post 1",
				 "This is a test blog post 1");
		blogPost.save();
		
		Thread.sleep(100);
		
		BlogPost blogPost2 = new BlogPost(user,
				 "Blog post 2",
				 "This is a test blog post 2");
		blogPost2.save();
		
		List<BlogPost> blogPosts = BlogPost.tail(50);
		assertEquals(2, blogPosts.size());
		assertEquals("Blog post 2", blogPosts.get(0).title);
	}
	
	@Test
	public void testFindBySanitizedTitleAndDate() throws Exception {
		SocialUser user = SocialUser.findById((long)1);
		BlogPost blogPost1 = new BlogPost(user, "blog post 1", "content");
		blogPost1.save();
		
		Date postedAt = blogPost1.postedAt;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedPostedAt = dateFormat.format(postedAt);
		String tokens[] = formattedPostedAt.split("-");
		BlogPost retrievedBlogPost = BlogPost.findBySanitizedTitleAndDate(tokens[0], 
											 							  tokens[1], 
											 							  tokens[2], 
											 							  blogPost1.sanitizedTitle);
		assertEquals(blogPost1.id, retrievedBlogPost.id);
	}
	
}
