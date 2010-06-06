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

import java.util.List;

import junit.framework.TestCase;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PageModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.UserModel;


public class GroupTest extends TestCase
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
			GroupModel grp = GroupModel.newGroup();
			Integer i = grp.getId();

			grp.setName("Francis");
			grp.setDescription("Testing time");

			// New object to make sure info was written to database
			
			GroupModel grp2 = (GroupModel)session.load(GroupModel.class, i);
			assertEquals("The group name should be the one we set", "Francis", grp2.getName());
			assertEquals("The group description should be the one we set", "Testing time", grp2.getDescription());
		}
		catch (HibernateException e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
			fail("Unexpected Exception");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
	}

	public void testPosts()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			
			GroupModel grp = GroupModel.newGroup();
			Integer i = grp.getId();

			// Try adding a post
			PostModel p1 = PostModel.newPost(u, grp, "", "", "", "");

			GroupModel grp2 = (GroupModel)session.load(GroupModel.class, i);
			assertEquals(1, grp2.getPosts().size());
			assertEquals(p1, grp2.getPosts().iterator().next());

			// And a second one
			PostModel p2 = PostModel.newPost(u, grp, "", "", "", "");

			GroupModel grp3 = (GroupModel)session.load(GroupModel.class, i);
			assertEquals(2, grp3.getPosts().size());
			assertTrue(grp3.getPosts().contains(p1));
			assertTrue(grp3.getPosts().contains(p2));

			// No need to remove posts
		}
		catch (HibernateException e)
		{
			System.err.println("Exception caught: " + e.getMessage()
			                   + e.getStackTrace());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void testPages()
	{
		try
		{
			GroupModel grp = GroupModel.newGroup();
			Integer i = grp.getId();

			// Add a page
			PageModel pg = PageModel.newPage();
			grp.addPage(pg);

			GroupModel grp2 = (GroupModel)session.load(GroupModel.class, i);
			assertTrue(grp2.getPages().contains(pg));
			assertEquals(1, grp2.getPages().size());
			assertTrue(pg.getGroups().contains(grp2));

			// Try a second one
			PageModel pg2 = PageModel.newPage();
			grp.addPage(pg2);
			session.flush();

			GroupModel grp3 = (GroupModel)session.load(GroupModel.class, i);
			assertTrue(grp3.getPages().contains(pg2));
			assertEquals(2, grp3.getPages().size());

			// And a third
			PageModel pg3 = PageModel.newPage();
			grp.addPage(pg3);
			session.flush();

			GroupModel grp4 = (GroupModel)session.load(GroupModel.class, i);
			assertTrue(grp4.getPages().contains(pg3));
			assertEquals(3, grp4.getPages().size());
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

	public void testChapterStuff()
	{
		try
		{
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			GroupModel exec = chapter.getExec();
			int chapterid = (Integer)session.save(chapter);

			assertTrue(chapter.isChapter());
			assertFalse(chapter.isExecList());

			assertFalse(exec.isChapter());
			assertTrue(exec.isExecList());

			GroupModel childGroup = GroupModel.newGroup(chapter);

			GroupModel nonChildGroup = GroupModel.newGroup();

			List<GroupModel> children = chapter.getChildGroups(true, true);
			assertTrue(children.contains(childGroup));
			assertTrue(children.contains(exec));
			assertFalse(children.contains(nonChildGroup));

			GroupModel test = (GroupModel)session.load(GroupModel.class, chapterid);
			assertTrue(test.isChapter());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testSaveGroup() 
	{
		try
		{
			GroupModel grp = GroupModel.newGroup();
			int id = grp.getId();
			grp.save("Test Group", "tg", "A test group", false);
			
			GroupModel test = (GroupModel)session.load(GroupModel.class, id);
			assertEquals("The group name should be what we set", "Test Group", test.getName());
			assertEquals("The group short name should be what we set", "tg", test.getShortname());
			assertEquals("The group description should be what we set", "A test group", test.getDescription());
			assertFalse("The group name should be private, as set", test.getPublic());
			assertFalse("The group should not be an admin group", test.getAdmin());
			assertEquals("The group post name should be set up properly", "Anyone on the [" + Helpers.getEnShortName() + "-tg] list", test.getPostName());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception Thrown");
		}	
	}
	
	public void testDelete()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			GroupModel grp = GroupModel.newGroup();
			u.addGroup(grp, 'r');
			assertTrue("User should be added to the group", u.getGroups().contains(grp));
			
			grp.delete();
			
			assertFalse("Deleted groups should be invisible", grp.getVisible());
			assertFalse("User should have been removed from group when the group was deleted", u.getGroups().contains(grp));
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception thrown");
		}
	}
	
	public void testNewGroup()
	{
		
		try
		{
//			Non parent version
			GroupModel g = GroupModel.newGroup();
			assertNotNull("Group should exist", g);
			assertNull("Group should have no parent", g.getParent());
			
			//parent version
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			g = GroupModel.newGroup(chapter);
			assertNotNull("Group should exist", g);
			assertEquals("Group's parent should be set to the chapter", chapter, g.getParent());
			assertTrue("Group should be a child of the chapter", chapter.getChildGroups(true, true).contains(g));
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected exception thrown");
		}
	}

	public void testWeirdCGLibError()
	{
		try
		{
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			GroupModel exec = chapter.getExec();

			List result = session.createQuery("SELECT g FROM GroupModel g WHERE g.exec=?")
			              .setEntity(0, exec).list();

			assertFalse(result.isEmpty());

			UserModel u = UserModel.newAssociateSignUp(null, "a@b.com", "Test", "User", "123456");
			u.joinChapter(chapter);

			GroupModel chapgroup = (GroupModel)session.get(GroupModel.class, chapter.getId());

			result = session.createQuery("SELECT r FROM RoleModel r WHERE r.group=? AND r.user=? AND r.end IS NULL AND r.level=?")
			         .setEntity(0, chapgroup).setEntity(1, u)
			         .setCharacter(2, 'm').list();

			assertFalse(result.isEmpty());

			result = session.createQuery("SELECT r FROM RoleModel r WHERE r.group=? AND r.user=? AND r.end IS NULL AND r.level=?")
			         .setEntity(0, chapgroup).setEntity(1, u)
			         .setCharacter(2, 'l').list();

			assertTrue(result.isEmpty());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
