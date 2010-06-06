/*

    This file is part of OpenMyEWB.

    OpenMyEWB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenMyEWB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenMyEWB.  If not, see <http://www.gnu.org/licenses/>.

    OpenMyEWB is Copyright 2005-2009 Nicolas Kruchten (nicolas@kruchten.com), Francis Kung, Engineers Without Borders Canada, Michael Trauttmansdorff, Jon Fishbein, David Kadish

*/

package ca.myewb.tests;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.UserModel;


public class PostTest extends TestCase
{
	Session session;
	Transaction tx;

	public void setUp()
	{
		try
		{
			// Register DB
			Class.forName("com.mysql.jdbc.Driver");

			// Start a Hibernate session
			HibernateUtil.createFactory("test");
			session = HibernateUtil.currentSession();
			tx = session.beginTransaction();
		}
		catch (Exception e)
		{
			System.err.println("Exception " + e);
			e.printStackTrace();
		}
	}

	public void tearDown()
	{
		try
		{
			tx.commit();
			HibernateUtil.closeSession();
		}
		catch (Exception e)
		{
			System.err.println("Excpetion: " + e);
			e.printStackTrace();
		}
	}

	// Test general getters/setters
	public void testGeneral()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			PostModel post = PostModel.newPost(u, g, "All About Me", "Wouldn't you want to know?", "Actually, I'm just a post", "");
			Integer i = post.getId();


			// New object to make sure info was written to database
			PostModel post2 = (PostModel)session.load(PostModel.class, i);
			assertTrue(post2.getSubject().equals("All About Me"));
			assertTrue(post2.getIntro().equals("Wouldn't you want to know?"));
			assertTrue(post2.getBody().equals("Actually, I'm just a post"));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void testNewPost()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			g.save("Test Group", "tg", "A test group", false);
			TagModel t = TagModel.getOrCreateTag("TestTag");
			PostModel post = PostModel.newPost(u, g, "All About Me", "Wouldn't you want to know?", "Actually, I'm just a post", "TestTag");
			assertNotNull("The post should not be null", post);
			assertTrue("The user should have the post added", u.getPosts().contains(post));
			assertTrue("The group should have the post added", g.getPosts().contains(post));
			assertTrue("The tag should have the post added", t.getPosts().contains(post));
			Integer i = post.getId();

			// New object to make sure info was written to database
			PostModel post2 = (PostModel)session.load(PostModel.class, i);
			assertTrue("The post subject should be what was set", post2.getSubject().equals("All About Me"));
			assertTrue("The post Intro should be what was set", post2.getIntro().equals("Wouldn't you want to know?"));
			assertTrue("The post body should be what was set", post2.getBody().equals("Actually, I'm just a post"));
			assertEquals("The poster should be the user who posted", u, post2.getPoster());
			assertEquals("The group should be the group posted to", g, post2.getGroup());
			assertTrue("The tag should be the tag associated", post2.getTags().contains(t));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	

	public void testGroups()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel grp = GroupModel.newGroup();
			PostModel post = PostModel.newPost(u, grp, "", "", "", ""); 
			Integer i = post.getId();

			// Try adding a group
			grp.addPost(post); // (implicit call to post.setGroup()
			session.flush();

			PostModel post2 = (PostModel)session.load(PostModel.class, i);
			assertEquals(post2.getGroup(), grp);
		}
		catch (HibernateException e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testDelete()
	{
		try
		{
			//Delete a post with a reply -- acheives full statment coverage in the method :-)
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			g.save("Test Group", "tg", "A test group", false);
			PostModel post = PostModel.newPost(u, g, "All About Me", "Wouldn't you want to know?", "Actually, I'm just a post", "TestTag");
			PostModel reply = post.reply(u, "A reply", "ReplyTestTag");
			assertNotNull("The reply should exist", reply);
			int pid = post.getId();
			int rid = reply.getId();

			// New object to make sure info was written to database
			PostModel post2 = (PostModel)session.load(PostModel.class, pid);
			post2.delete();
			assertEquals("The subject should reflect the post's deletion", "*deleted* All About Me", post2.getSubject());
			assertEquals("The post should be in the deleted group", Helpers.getGroup("DeletedPosts"), post2.getGroup());
			assertTrue("The post intro should start with the deleted bit", post2.getIntro().startsWith("deleted "));
			assertTrue("The post intro should end with the deleted bit", post2.getIntro().endsWith(", original group was: Test Group\n- - -\nWouldn't you want to know?"));
			
			PostModel reply2 = (PostModel)session.load(PostModel.class, rid);
			assertEquals("The subject should reflect the replies implicit deletion", "*deleted* reply", reply2.getSubject());
			assertEquals("The reply should be in the deleted group", Helpers.getGroup("DeletedPosts"), reply2.getGroup());
			assertTrue("The reply intro should start with the deleted bit", reply2.getIntro().startsWith("deleted "));
			assertTrue("The post intro should end with the deleted bit", reply2.getIntro().endsWith(", original group was: Test Group\n- - -\nparent's subject was: All About Me"));
				
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testReply()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			g.save("Test Group", "tg", "A test group", false);
			TagModel t2 = TagModel.getOrCreateTag("ReplyTestTag");
			PostModel post = PostModel.newPost(u, g, "All About Me", "Wouldn't you want to know?", "Actually, I'm just a post", "TestTag");
			PostModel reply = post.reply(u, "A reply", "ReplyTestTag");
			assertNotNull("The reply should exist", reply);
			int pid = post.getId();
			int rid = reply.getId();
			
			PostModel post2 = (PostModel)session.load(PostModel.class, pid);
			PostModel reply2 = (PostModel)session.load(PostModel.class, rid);
			assertTrue("The post should contain the reply", post2.getReplies().contains(reply2));
			assertEquals("The reply should have the post as a parent", post2, reply2.getParent());
			assertTrue("The reply tag should be added to the parent", post2.getTags().contains(t2));
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpwcted Exception thrown");
		}
	}
	
	public void testClone()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			g.save("Test Group", "tg", "A test group", false);
			PostModel post = PostModel.newPost(u, g, "All About Me", "Wouldn't you want to know?", "Actually, I'm just a post", "TestTag");
			PostModel clone = post.clone();
			
			assertEquals("Cloned poster should be the same as post", post.getPoster(), clone.getPoster());
			assertEquals("Cloned group should be the same as post", post.getGroup(), clone.getGroup());
			assertEquals("Cloned subject should be the same as post", post.getSubject(), clone.getSubject());
			assertEquals("Cloned intro should be the same as post", post.getIntro(), clone.getIntro());
			assertEquals("Cloned body should be the same as post", post.getBody(), clone.getBody());
			assertEquals("Cloned date should be the same as post", post.getDate(), clone.getDate());
			assertEquals("Cloned tags should be the same as post", post.getTags(), clone.getTags());
			assertEquals("Cloned parent should be the same as post", post.getParent(), clone.getParent());
			assertEquals("Cloned replies should be the same as post", post.getReplies(), clone.getReplies());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}

	public void testUsers()
	{
		try
		{
			// Try setting the user
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			
			PostModel post = PostModel.newPost(usr, g, "", "", "", "");
			Integer i = post.getId();

			PostModel post2 = (PostModel)session.load(PostModel.class, i);
			assertEquals(post2.getPoster(), usr);
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testTags()
	{
		try
		{
			TagModel t = TagModel.getOrCreateTag("Tester");
			session.flush();
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel g = GroupModel.newGroup();
			PostModel post = PostModel.newPost(u, g, "", "", "", "Tester");
			Integer i = post.getId();

			PostModel post2 = (PostModel)session.load(PostModel.class, i);
			assertEquals(1, post2.getTags().size());
			assertTrue(post2.getTags().contains(t));

			// Eh, let's test the tags here too, since it's so small
			assertEquals(((TagModel)post2.getTags().iterator().next()).getName(),
			             "Tester");
			assertTrue(t.getPosts().contains(post));

			// Add a second tag
			TagModel t2 = TagModel.getOrCreateTag("OtherTestTag");
			session.save(t2);
			post.addTag(t2);
			session.flush();

			PostModel post3 = (PostModel)session.load(PostModel.class, i);
			assertEquals(2, post3.getTags().size());
			assertTrue(post3.getTags().contains(t));
			assertTrue(post3.getTags().contains(t2));

			// And remove a tag
			post.remTag(t2);
			session.flush();

			PostModel post4 = (PostModel)session.load(PostModel.class, i);
			assertEquals(1, post4.getTags().size());
			assertTrue(post4.getTags().contains(t));
			assertFalse(post4.getTags().contains(t2));

			// Two posts with the same tag
			PostModel post5 = PostModel.newPost(u, g, "", "", "", "Tester");
			Integer i2 = post5.getId();
			session.flush();

			PostModel post6 = (PostModel)session.load(PostModel.class, i2);
			assertTrue(post6.getTags().contains(t));
			assertTrue(post2.getTags().contains(t));
			assertTrue(t.getPosts().contains(post2));
			assertTrue(t.getPosts().contains(post6));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}


	public void testDateFormatter()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyy");

		GregorianCalendar testDate = new GregorianCalendar();

		testDate.setTime(new Date());
		testDate.set(Calendar.HOUR, 10);
		testDate.set(Calendar.MINUTE, 10);
		testDate.set(Calendar.SECOND, 10);
		testDate.add(Calendar.DAY_OF_YEAR, -10);

		assertEquals(Helpers.formatDate(testDate.getTime()),
		             "on " + formatter.format(testDate.getTime()));

		testDate.setTime(new Date());
		testDate.set(Calendar.HOUR, 10);
		testDate.set(Calendar.MINUTE, 10);
		testDate.set(Calendar.SECOND, 10);
		testDate.add(Calendar.DAY_OF_YEAR, -1);

		assertEquals(Helpers.formatDate(testDate.getTime()), "yesterday");

		testDate.setTime(new Date());
		testDate.set(Calendar.HOUR, 10);
		testDate.set(Calendar.MINUTE, 10);
		testDate.set(Calendar.SECOND, 10);

		assertEquals(Helpers.formatDate(testDate.getTime()), "today");

		testDate.setTime(new Date());
		testDate.set(Calendar.HOUR, 10);
		testDate.set(Calendar.MINUTE, 10);
		testDate.set(Calendar.SECOND, 10);
		testDate.add(Calendar.DAY_OF_YEAR, 1);

		assertEquals(Helpers.formatDate(testDate.getTime()), "tomorrow");

		testDate.setTime(new Date());
		testDate.set(Calendar.HOUR, 10);
		testDate.set(Calendar.MINUTE, 10);
		testDate.set(Calendar.SECOND, 10);
		testDate.add(Calendar.DAY_OF_YEAR, 2);

		assertEquals(Helpers.formatDate(testDate.getTime()),
		             "on " + formatter.format(testDate.getTime()));
	}
}
