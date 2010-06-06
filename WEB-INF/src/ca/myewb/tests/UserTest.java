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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.controllers.common.PostList;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Permissions;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.OVInfoModel;
import ca.myewb.model.PageModel;
import ca.myewb.model.PlacementModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.UserModel;


public class UserTest extends TestCase
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
	
	public void testCreateGuestUser()
	{
		try
		{
			UserModel u = UserModel.createGuestUser();
			assertNotNull("The guest user should exist", u);
			int id = u.getId();
			
			UserModel u2 = (UserModel)session.load(UserModel.class, id);
			assertEquals("The username should be guest", "guest", u2.getUsername());
			assertTrue("The user should be in the Org Group", u2.getGroups().contains(Helpers.getGroup("Org")));
			assertTrue("The user should be in the Guest Group", u2.getGroups().contains(Helpers.getGroup("Guest")));
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testNewAssociateSignup()
	{
		try
		{
			//Test new signup
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			assertNotNull("The user should exist", u);
			int id = u.getId();
			
			UserModel u2 = (UserModel)session.load(UserModel.class, id);
			assertEquals("The email should be the one set", "t@u.com", u2.getEmail());
			assertEquals("The firstnam should be the one set", "Test", u2.getFirstname());
			assertEquals("The lastname should be the one set", "User", u2.getLastname());
			assertTrue("The password should be the one set", u2.checkPassword("testuser"));
			assertFalse("The user should not be admin", u2.isAdmin());
			assertTrue("The user should be in the Org Group", u2.getGroups().contains(Helpers.getGroup("Org")));
			assertTrue("The user should be in the Users Group", u2.getGroups().contains(Helpers.getGroup("Users")));
			assertTrue("The user should be in the Associate Group", u2.getGroups().contains(Helpers.getGroup("Associate")));
			assertTrue("The user should be in the NoChapter Group", u2.getGroups().contains(Helpers.getGroup("NoChapter")));
			
			//Test mailing list upgrade
			long emailPrefix = System.currentTimeMillis();
			u = UserModel.newMailingListSignUp(emailPrefix + "@u.com");
			assertNotNull("The mailing list user should not be null", u);
			id = u.getId();
			
			UserModel.newAssociateSignUp(u, "t@u.com", "Test", "User", "testuser");
			
			u2 = (UserModel)session.load(UserModel.class, id);
			assertEquals("The email should be the one set", emailPrefix + "@u.com", u2.getEmail());
			assertEquals("The firstnam should be the one set", "Test", u2.getFirstname());
			assertEquals("The lastname should be the one set", "User", u2.getLastname());
			assertTrue("The password should be the one set", u2.checkPassword("testuser"));
			assertTrue("The user should be in the Org Group", u2.getGroups().contains(Helpers.getGroup("Org")));
			assertTrue("The user should be in the Users Group", u2.getGroups().contains(Helpers.getGroup("Users")));
			assertTrue("The user should be in the Associate Group", u2.getGroups().contains(Helpers.getGroup("Associate")));
			assertTrue("The user should be in the NoChapter Group", u2.getGroups().contains(Helpers.getGroup("NoChapter")));
			
		} catch (Exception e)
		{
			fail("Unexpected Exception Thrown");
		}
	}
	
	public void testNewMailinglistSignup()
	{
		try
		{
			long emailPrefix = System.currentTimeMillis();
			UserModel u = UserModel.newMailingListSignUp(emailPrefix + "@u.com");
			assertNotNull("The mailing list user should not be null", u);
			int id = u.getId();
			
			UserModel.newAssociateSignUp(u, "t@u.com", "Test", "User", "testuser");
			
			UserModel u2 = (UserModel)session.load(UserModel.class, id);
			assertEquals("The email should be the one set", emailPrefix + "@u.com", u2.getEmail());
			assertTrue("The user should be in the Org Group", u2.getGroups().contains(Helpers.getGroup("Org")));
			assertTrue("The user should be in the NoChapter Group", u2.getGroups().contains(Helpers.getGroup("NoChapter")));
			
		} catch (Exception e)
		{
			fail("Unexpected Exception Thrown");
		}
	}
	
	public void testAssignPlacement()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			PlacementModel pl = PlacementModel.newPlacementModel();
			assertNotNull("The user should exist", u);
			assertFalse("The user should have no placements", u.getPlacements().contains(pl));
			assertNull("The placement should have no OV assigned", pl.getOv());
			assertFalse("The placement should be unassigned", pl.isAssigned());
			assertFalse("The placement should be inactive", pl.isActive());
			int id = u.getId();
			
			u.assignPlacement(pl);
			
			UserModel u2 = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user should have the placement assigned", u2.getPlacements().contains(pl));
			assertEquals("The placement should have the user assigned as an OV", u2, pl.getOv());
			assertTrue("The placement should be assigned", pl.isAssigned());
			assertTrue("The placement should be active", pl.isActive());
			
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testChangeExecTitle()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			assertNotNull("The user should exist", u);
			assertEquals("The User's exec title should be blank since not a chapter member", "", u.getExecTitle());
			u.joinChapter(chapter);
			u.upgradeToExec();
			assertEquals("The User's exec title should be blank", "", u.getExecTitle());
			GroupModel prezGrp = Helpers.getGroup("UniPresidents");
			GroupModel financeGrp = Helpers.getGroup("Finance");
			assertFalse("The user should not be a member of the presidents group", u.getGroups().contains(prezGrp));
			assertFalse("The user should not be a member of the finance group", u.getGroups().contains(financeGrp));
			int id = u.getId();
			

			Hashtable<GroupModel, Boolean> repLists = new Hashtable<GroupModel, Boolean>();
			repLists.put(prezGrp, false);
			repLists.put(financeGrp, false);
			
			u.changeExecTitle("Test title", repLists);
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The exec title should be the one set", "Test title", u.getExecTitle());
			assertFalse("The user should not be a member of the presidents group", u.getGroups().contains(prezGrp));
			assertFalse("The user should not be a member of the finance group", u.getGroups().contains(financeGrp));

			repLists.put(prezGrp, false);
			repLists.put(financeGrp, true);
			u.changeExecTitle("Finance guy", repLists);
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The exec title should be the one set", "Finance guy", u.getExecTitle());
			assertFalse("The user should not be a member of the presidents group", u.getGroups().contains(prezGrp));
			assertTrue("The user should be a member of the finance group", u.isMember(financeGrp, false));
			assertTrue("The user should be a sender of the finance group", u.isSender(financeGrp, false));

			repLists.put(prezGrp, true);
			repLists.put(financeGrp, true);
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.changeExecTitle("President", repLists);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The exec title should be the one set", "President", u.getExecTitle());
			assertTrue("The user should be a member of the presidents group", u.isMember(prezGrp, false));
			assertTrue("The user should be a sender of the presidents group", u.isSender(prezGrp, false));
			assertTrue("The user should be a president", u.isPresident());
			
			
		} catch (Exception e)
		{
			fail("Unexpected exception thrown");
		}
	}
	
	public void testDelete()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			GroupModel grp = GroupModel.newGroup();
			u.joinChapter(chapter);
			u.subscribe(grp);
			assertTrue("The user has joined the chapter", u.getGroups().contains(chapter));
			assertTrue("The user has joined the group", u.getGroups().contains(grp));
			int id = u.getId();
			Date d = u.getCurrentLogin();
			
			u.delete();
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The user should only be one group", 1, u.getGroups().size());
			assertTrue("The user should be in the deleted group", u.getGroups().contains(Helpers.getGroup("Deleted")));
			
			assertNull("The e-mail should be set to null", u.getEmail());
			assertNull("The language should be set to null", u.getLanguage());
			assertNull("The address should be set to null", u.getAddress());
			assertNull("The phone should be set to null", u.getPhone());
			assertNull("The cell should be set to null", u.getCellno());
			assertNull("The alt should be set to null", u.getAlternateno());
			assertNull("The businessno should be set to null", u.getBusinessno());
			assertEquals("The gender should be set to 0", 0, u.getGender());
			assertEquals("The birth should be set to 0", 0, u.getBirth());
			assertEquals("The student should be set to 0", 0, u.getStudent());
			assertEquals("The lastlogin should not be set to the old current login", d, u.getLastLogin());
			assertNull("The currentlogin should be set to null", u.getCurrentLogin());

			assertEquals("The studentnumber should be set to the null string", "", u.getStudentnumber());
			assertEquals("The student insitution should be set to the null string", "", u.getStudentinstitution());
			assertEquals("The student field should be set to the null string", "", u.getStudentfield());
			assertEquals("The student level should be set to 0", 0, u.getStudentlevel());
			assertEquals("The student grad month should be set to 0", 0, u.getStudentgradmonth());
			assertEquals("The student grad year should be set to 0", 0, u.getStudentgradyear());

			assertEquals("The employer should be set to the null string", "", u.getProemployer());
			assertEquals("The sector should be set to the null string", "", u.getProsector());
			assertEquals("The position should be set to the null string", "", u.getProposition());
			assertEquals("The company size should be set to 0", 0, u.getProcompsize());
			assertEquals("The income level should be set to 0", 0, u.getProincomelevel());
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testDowngradeFromAdmin()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			int id = u.getId();
			u.upgradeToAdmin();
			
			u.downgradeFromAdmin();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not be an admin", u.isAdmin());
			assertFalse("The user should not be in the admin group", u.getGroups().contains(Helpers.getGroup("Admin")));
	
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
		
	}
	
	public void testDowngradeFromExec()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.downgradeFromExec();
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not be a leader of the chapter", u.isLeader(chapter));
			assertFalse("The user should not be a leader of the chapter exec list", u.isLeader(chapter.getExec()));
			assertFalse("The user should not be a member of the chapter exec list", u.isMember(chapter.getExec()));
			assertFalse("The user should not be in the global exec list", u.getGroups().contains(Helpers.getGroup("Exec")));
			assertTrue("The user should still be a member of the chapter", u.isMember(chapter));
		} catch (Exception e)
		{
			fail("Unexpected Exception caught");
		}
	}
	
	public void testDowngradeFromListLeader()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupModel group = GroupModel.newGroup();
			u.upgradeToListLeader(group);
			u.downgradeFromListLeader(group);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("User should not be a leader of group", u.isLeader(group));
			assertTrue("User should still be a recipient of group", u.isRecipient(group));
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testDowngradeFromListSender()
	{
		try
		{
			//General list
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupModel group = GroupModel.newGroup();
			u.upgradeToListSender(group);
			u.downgradeFromListSender(group);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("User should not be a sender of group", u.isSender(group));
			assertTrue("User should still be a recipient of group", u.isRecipient(group));
			
			//Chapter list as member
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.upgradeToListSender(chapter);
			u.downgradeFromListSender(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("User should not be a sender of chapter list", u.isSender(chapter));
			assertTrue("User should still be a member of chapter", u.isMember(chapter));
			
			//Chapter list as non-member
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.subscribe(chapter);
			u.upgradeToListSender(chapter);
			u.downgradeFromListSender(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("User should not be a sender of chapter list", u.isSender(chapter));
			assertTrue("User should still be a recipient of chapter list", u.isRecipient(chapter));
			
			//Chapter Exec list
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.subscribe(chapter.getExec());
			u.upgradeToListSender(chapter.getExec());
			u.downgradeFromListSender(chapter.getExec());
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("User should not be a sender of chapter exec list", u.isSender(chapter.getExec()));
			assertTrue("User should still be a recipient of chapter exec list", u.isRecipient(chapter.getExec()));
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testDowngradeFromNMT()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.upgradeToNMT();
			u.downgradeFromNMT();
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("User's group should not contain NMT", u.getGroups().contains(Helpers.getGroup("NMT")));
			assertFalse("user should have no admin powers", u.isAdmin());
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testJoinChapter()
	{
		try
		{
			//Cold join
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("User is member of the chapter", u.isMember(chapter));
			assertFalse("User's groups do not contain NoChapter", u.getGroups().contains(Helpers.getGroup("NoChapter")));
			assertTrue("The user should be a member of the Chapter group", u.isMember(Helpers.getGroup("Chapter")));
			
			//Already on Mail list join
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.subscribe(chapter);
			u.joinChapter(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("User is member of the chapter", u.isMember(chapter));
			assertFalse("User's groups do not contain NoChapter", u.getGroups().contains(Helpers.getGroup("NoChapter")));
			assertTrue("The user should be a member of the Chapter group", u.isMember(Helpers.getGroup("Chapter")));
			assertFalse("User should no longer be a recipient of the chapter", u.isRecipient(chapter));
			
			//Cold join - Admin
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.upgradeToAdmin();
			u.joinChapter(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("User is member of the chapter", u.isMember(chapter));
			assertFalse("User's groups do not contain NoChapter", u.getGroups().contains(Helpers.getGroup("NoChapter")));
			assertTrue("The user should be a member of the Chapter group", u.isMember(Helpers.getGroup("Chapter"), false));
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testLeaveChapter()
	{
		try
		{
			//Remove from sublists
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			GroupModel sublist = GroupModel.newGroup(chapter);
			u.joinChapter(chapter);
			u.subscribe(sublist);
			u.leaveChapter(chapter);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not have the chapter in its groups", u.getGroups().contains(chapter));
			assertFalse("User's groups do not contain Chapter", u.getGroups().contains(Helpers.getGroup("Chapter")));
			assertTrue("The user should be a member of the NoChapter group", u.isMember(Helpers.getGroup("NoChapter")));
			assertFalse("The sublist should not be in the user's groups", u.getGroups().contains(sublist));
			
			//Say goodbye to an exec
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.leaveChapter(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not have the chapter in its groups", u.getGroups().contains(chapter));
			assertFalse("The user should not have the exec list in its groups", u.getGroups().contains(chapter.getExec()));
			assertFalse("User's groups do not contain Chapter", u.getGroups().contains(Helpers.getGroup("Chapter")));
			assertTrue("The user should be a member of the NoChapter group", u.isMember(Helpers.getGroup("NoChapter")));
			assertFalse("The sublist should not be in the user's groups", u.getGroups().contains(sublist));
			
			//Say goodbye to an admin
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.upgradeToAdmin();
			u.joinChapter(chapter);
			u.leaveChapter(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not have the chapter in its groups", u.getGroups().contains(chapter));
			assertFalse("User's groups do not contain Chapter", u.getGroups().contains(Helpers.getGroup("Chapter")));
			assertTrue("The user should be a member of the NoChapter group", u.isMember(Helpers.getGroup("NoChapter"), false));
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testRenew()
	{
		try
		{
			UserModel renewer = UserModel.newAssociateSignUp("re@new.com", "Renewer", "User", "renewuser");
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.renew(renewer, false);
			Date expiry = u.getExpiry();
			u.renew(renewer, false);
			int id = u.getId();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(expiry);
			cal.add(Calendar.YEAR, 1);
			Date newExp = cal.getTime();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user should be a member of Regular", u.isMember(Helpers.getGroup("Regular")));
			assertEquals("The new expiry date should be one year from the old one", newExp, u.getExpiry());
		} catch (Exception e)
		{
			fail("Unexpected exception thrown");
		}
	}
	
	public void testRetreiveOVInfo()
	{
		try
		{
			//No previous OVInfo
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			OVInfoModel info = u.retreiveOVInfo();
			assertNotNull("The OVInfo should exist", info);
			
			//With a previous OVInfo
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			info = OVInfoModel.newOVInfo();
			Date testDate = new Date();
			info.setDob(testDate);
			info.setE1address("Test Address E1");
			info.setE1business("Test Busness E1");
			info.setE1email("Test Email E1");
			info.setE1fax("Test Fax E1");
			info.setE1home("Test Home E1");
			info.setE1language("Test Language E1");
			info.setE1name("Test Name E1");
			info.setE1relation("Test Relation E1");
			info.setE1updates(true);
			info.setE2address("Test Address E2");
			info.setE2business("Test Business E2");
			info.setE2email("Test Email E2");
			info.setE2fax("Test Fax E2");
			info.setE2home("Test Home E2");
			info.setE2language("Test Language E2");
			info.setE2name("Test Name E2");
			info.setE2relation("Test Relation E2");
			info.setE2updates(true);
			info.setHealthnumber("Test Healthnumber");
			info.setPassportend(testDate);
			info.setPassportname("Test Passportname");
			info.setPassportnumber("Test Passportnumber");
			info.setPassportplace("Test Passportplace");
			info.setPassportstart(testDate);
			info.setSin("Test Sin");
			u.setOVInfo(info);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			info = u.retreiveOVInfo();
			assertEquals("The DOB should be what was set", testDate, info.getDob());
			assertEquals("The E1 Address should be what was set", "Test Address E1", info.getE1address());
			assertEquals("The E1 email should be what was set", "Test Email E1", info.getE1email());
			assertEquals("The E1 Fax should be what was set", "Test Fax E1", info.getE1fax());
			assertEquals("The E1 Home should be what was set", "Test Home E1", info.getE1home());
			assertEquals("The E1 Language should be what was set", "Test Language E1", info.getE1language());
			assertEquals("The E1 Name should be what was set", "Test Name E1", info.getE1name());
			assertEquals("The E1 relation should be what was set", "Test Relation E1", info.getE1relation());
			assertTrue("The E1 updates should be what was set", info.isE1updates());
			assertEquals("The E2 Address should be what was set", "Test Address E2", info.getE2address());
			assertEquals("The E2 email should be what was set", "Test Email E2", info.getE2email());
			assertEquals("The E2 Fax should be what was set", "Test Fax E2", info.getE2fax());
			assertEquals("The E2 Home should be what was set", "Test Home E2", info.getE2home());
			assertEquals("The E2 Language should be what was set", "Test Language E2", info.getE2language());
			assertEquals("The E2 Name should be what was set", "Test Name E2", info.getE2name());
			assertEquals("The E2 relation should be what was set", "Test Relation E2", info.getE2relation());
			assertTrue("The E2 updates should be what was set", info.isE2updates());			
			assertEquals("The healthnumber should be what was set", "Test Healthnumber", info.getHealthnumber());
			assertEquals("The passportend should be what was set", testDate, info.getPassportend());
			assertEquals("The passportname should be what was set", "Test Passportname", info.getPassportname());
			assertEquals("The passportnumber should be what was set", "Test Passportnumber", info.getPassportnumber());
			assertEquals("The passportplace should be what was set", "Test Passportplace", info.getPassportplace());
			assertEquals("The passportstart should be what was set", testDate, info.getPassportstart());
			assertEquals("The sin should be what was set", "Test Sin", info.getSin());

		} catch (Exception e)
		{
			fail("Unexpected Excpetion thrown");
		}
	}
	
	public void testSaveAddress()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.saveAddress("Test Address\n\n\nToronto\nON\nM6J 3J6\nCA", "Test Phone", "Test Business", "Test Cell", "Test Alt");
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The address should be the one set", "Test Address\n\n\nToronto\nON\nM6J 3J6\nCA", u.getAddress());
			assertEquals("The phone should be the one set", "Test Phone", u.getPhone());
			assertEquals("The cell should be the one set", "Test Cell", u.getCellno());
			assertEquals("The alt should be the one set", "Test Alt", u.getAlternateno());
			assertEquals("The business should be the one set", "Test Business", u.getBusinessno());
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testSaveProfessionalData()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.saveProfessionalData("Test Employer", "Test Position", "Test Sector", "2", "3");
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The employer should be the one set", "Test Employer", u.getProemployer());
			assertEquals("The position should be the one set", "Test Position", u.getProposition());
			assertEquals("The sector should be the one set", "Test Sector", u.getProsector());
			assertEquals("The compsize should be the one set", 2, u.getProcompsize());
			assertEquals("The income should be the one set", 3, u.getProincomelevel());
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testSaveStudentData()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.saveStudentData("Test Field", "Test Institution", "Test Studentno", "2", "6", "2006");
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The field should be the one set", "Test Field", u.getStudentfield());
			assertEquals("The instituiton should be the one set", "Test Institution", u.getStudentinstitution());
			assertEquals("The Studentno should be the one set", "Test Studentno", u.getStudentnumber());
			assertEquals("The studentlevel should be the one set", 2, u.getStudentlevel());
			assertEquals("The grad year should be the one set", 2006, u.getStudentgradyear());
			assertEquals("The grad month should be the one set", 6, u.getStudentgradmonth());
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testSaveUser()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			HashSet<String> emails = new HashSet<String>();
			emails.add("Test Email");
			u.saveUser("Test Firstname", "Test Lastname", "Test Email", emails, "Test Password", "Test Language", "m", "Test Student", "2006", "y");
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The firstname should be the one set", "Test Firstname", u.getFirstname());
			assertEquals("The lastname should be the one set", "Test Lastname", u.getLastname());
			assertEquals("The email should be the one set", "Test Email", u.getEmail());
			assertTrue("The password should be the one set", u.checkPassword("Test Password"));
			assertEquals("The languager should be the one set", "Test Language", u.getLanguage());
			assertEquals("The gender should be the one set", 'm', u.getGender());
			assertEquals("The birthyear should be the one set", 2006, u.getBirth());
			assertEquals("The canadianinfo should be the one set", 'y', u.getCanadianinfo());
			assertEquals("The emails should be teh one set", emails, u.getEmails());
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testSignIn()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			int numLogins = u.getLogins();
			Date newCurrentLogin = new Date();
			u.signIn();
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertEquals("The number of logins should have increased by one", numLogins + 1, u.getLogins());
			assertEquals("The new current login should be now", newCurrentLogin, u.getCurrentLogin());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testSubscribe()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupModel grp = GroupModel.newGroup();
			u.subscribe(grp);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a recipeint of the group", u.isRecipient(grp));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testUnsubscribe()
	{
		try
		{
			//unsubscribe recipient
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupModel grp = GroupModel.newGroup();
			u.subscribe(grp);
			u.unsubscribe(grp);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not be a recipient of the group", u.isRecipient(grp));
			assertFalse("The user should not have the group in its groups", u.getGroups().contains(grp));
			
			//unsubscribe sender
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			grp = GroupModel.newGroup();
			u.subscribe(grp);
			u.upgradeToListSender(grp);
			u.unsubscribe(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not be a sender of the group", u.isSender(grp));
			assertFalse("The user should not be a recipient of the group", u.isRecipient(grp));
			assertFalse("The user should not have the group in its groups", u.getGroups().contains(grp));
			
			//unsubscribe leader
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			grp = GroupModel.newGroup();
			u.subscribe(grp);
			u.upgradeToListLeader(grp);
			u.unsubscribe(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The user should not be a leader of the group", u.isLeader(grp));
			assertFalse("The user should not be a recipient of the group", u.isRecipient(grp));
			assertFalse("The user should not have the group in its groups", u.getGroups().contains(grp));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testUpgradeToAdmin()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.upgradeToAdmin();
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is has admin powers", u.isAdmin());
			assertTrue("The user is a member of the admin group", u.isMember(Helpers.getGroup("Admin")));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testUpgradeToExec()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.upgradeToExec();
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a leader of the chapter", u.isLeader(chapter));
			assertTrue("The user is a member of the chapter exec list", u.isMember(chapter.getExec()));
			assertTrue("The user is a leader of the chapter exec list", u.isLeader(chapter.getExec()));
			assertFalse("The user is not a recipient of the exec list", u.isRecipient(chapter.getExec()));
			assertFalse("The user is not a sender of the exec list", u.isSender(chapter.getExec()));
			assertTrue("The user is a member of the global exec list", u.isMember(Helpers.getGroup("Exec")));
			
			//Already a pseudo-exec
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.addGroup(chapter.getExec(), 'r');
			u.addGroup(chapter.getExec(), 's');
			u.upgradeToExec();
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a leader of the chapter", u.isLeader(chapter));
			assertTrue("The user is a member of the chapter exec list", u.isMember(chapter.getExec()));
			assertTrue("The user is a leader of the chapter exec list", u.isLeader(chapter.getExec()));
			assertFalse("The user is not a recipient of the exec list", u.isRecipient(chapter.getExec()));
			assertFalse("The user is not a sender of the exec list", u.isSender(chapter.getExec()));
			assertTrue("The user is a member of the global exec list", u.isMember(Helpers.getGroup("Exec")));
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testUpgradeToListLeader()
	{
		try
		{
			//Already subscribed
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupModel grp = GroupModel.newGroup();
			u.subscribe(grp);
			u.upgradeToListLeader(grp);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a leader of the group", u.isLeader(grp));
			
			//Not subscribed
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			grp = GroupModel.newGroup();
			u.upgradeToListLeader(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a leader of the group", u.isLeader(grp));
			assertTrue("The user is a recipient of the group", u.isRecipient(grp));
			

			
			//Admin does nothing
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			grp = GroupModel.newGroup();
			u.upgradeToAdmin();
			u.upgradeToListSender(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The admin user is not actualy a sender of the group", u.isLeader(grp, false));
			assertTrue("The admin user is actualy a recipient of the group", u.isRecipient(grp, false));
			assertTrue("The admin user has sender status through admin powers", u.isLeader(grp));
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testUpgradeToListSender()
	{
		try
		{
			//Already subscribed
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupModel grp = GroupModel.newGroup();
			u.subscribe(grp);
			u.upgradeToListSender(grp);
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a sender of the group", u.isSender(grp));
			
			//Not subscribed
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			grp = GroupModel.newGroup();
			u.upgradeToListSender(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is a sender of the group", u.isSender(grp));
			assertTrue("The user is a recipient of the group", u.isRecipient(grp));
			
			//Admin does nothing
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			grp = GroupModel.newGroup();
			u.upgradeToAdmin();
			u.upgradeToListSender(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The admin user is not actualy a sender of the group", u.isSender(grp, false));
			assertTrue("The admin user is actualy a recipient of the group", u.isRecipient(grp, false));
			assertTrue("The admin user has sender status through admin powers", u.isSender(grp));
			
			//User is exec and group is their chapter
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.upgradeToListSender(chapter);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The exec user is not actualy a sender of the chapter list", u.isSender(chapter, false));
			assertFalse("The exec user is not actualy a recipient of the chapter list", u.isRecipient(chapter, false));
			
			//User is exec and group is not their chapter
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			grp = GroupChapterModel.newChapter();
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.upgradeToListSender(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The exec user is a sender of the other chapter list", u.isSender(grp, false));
			assertTrue("The exec user is not actualy a recipient of the chapter list", u.isRecipient(grp, false));
			
			//User is exec and group is their chapter exec list
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			grp = chapter.getExec();
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.upgradeToListSender(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertFalse("The exec user is not actualy a sender of the chapter exec list", u.isSender(grp, false));
			assertFalse("The exec user is not actualy a recipient of the chapter exec list", u.isRecipient(grp, false));
			
			//User is exec and group is not their chapter exec list
			u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			chapter = GroupChapterModel.newChapter();
			grp = GroupChapterModel.newChapter().getExec();
			u.joinChapter(chapter);
			u.upgradeToExec();
			u.upgradeToListSender(grp);
			id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The exec user is a sender of the other chapter exec list", u.isSender(grp, false));
			assertTrue("The exec user is not actualy a recipient of the chapter exec list", u.isRecipient(grp, false));
			
		} catch (Exception e)
		{
			fail("Unexpected Exception thrown");
			e.printStackTrace();
		}
	}
	
	public void testUpgradeToNMT()
	{
		try
		{
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			u.upgradeToNMT();
			int id = u.getId();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user is has admin powers", u.isAdmin());
			assertTrue("The user is a member of the nmt group", u.isMember(Helpers.getGroup("NMT")));
		} catch (Exception e)
		{
			fail("unexpected exception thrown");
			e.printStackTrace();
		}
	}
	
	public void testUpgradeToRegular()
	{
		try
		{
			UserModel renewer = UserModel.newAssociateSignUp("re@new.com", "Renewer", "User", "renewuser");
			UserModel u = UserModel.newAssociateSignUp("t@u.com", "Test", "User", "testuser");
			Date now = new Date();
			u.renew(renewer, false);
			int id = u.getId();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			cal.add(Calendar.YEAR, 1);
			Date newExp = cal.getTime();
			
			u = (UserModel)session.load(UserModel.class, id);
			assertTrue("The user should be a member of Regular", u.isMember(Helpers.getGroup("Regular")));
			assertFalse("The user should not have associate as part of its groups", u.getGroups().contains(Helpers.getGroup("Associate")));
			assertEquals("The expiry date should be one year from now", newExp, u.getExpiry());
		} catch (Exception e)
		{
			fail("Unexpected exception thrown");
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

	public void testGeneral()
	{
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			Integer i = usr.getId();

			// General getters and setters
			usr.setUsername("fkung");
			usr.setFirstname("Francis");
			usr.setLastname("Kung");
			usr.setPassword("password");

			// New object to make sure info was written to database
			UserModel usr2 = (UserModel)session.load(UserModel.class, i);

			assertTrue(usr2.getUsername().equals("fkung"));
			assertTrue(usr2.getFirstname().equals("Francis"));
			assertTrue(usr2.getLastname().equals("Kung"));
			assertTrue(usr2.checkPassword("password"));
			assertFalse(usr2.checkPassword("Password"));
			assertFalse(usr2.checkPassword(""));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testMemberships()
	{
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			Integer i = usr.getId();

			// Add a group
			GroupModel grp = GroupModel.newGroup();
			usr.addGroup(grp, 'r');
			session.flush();

			UserModel usr2 = (UserModel)session.load(UserModel.class, i);
			assertTrue(usr2.getGroups().contains(grp));
			assertEquals(5, usr2.getGroups().size());
			assertFalse(usr2.isMember(grp));
			assertFalse(usr2.isLeader(grp));
			assertFalse(usr2.isSender(grp));
			assertTrue(usr2.isRecipient(grp));

			// Member of two groups
			GroupModel g2 = GroupModel.newGroup();
			usr.addGroup(g2, 'r');
			usr.addGroup(g2, 'l');
			session.flush();

			UserModel usr3 = (UserModel)session.load(UserModel.class, i);
			assertTrue(usr3.getGroups().contains(g2));
			assertEquals(7, usr3.getGroups().size());
			assertFalse(usr3.isMember(g2));
			assertTrue(usr3.isLeader(g2));
			assertFalse(usr3.isSender(g2));
			assertTrue(usr3.isRecipient(g2));
			assertFalse(usr3.isMember(grp));
			assertFalse(usr3.isLeader(grp));

			// And removing a non-existent role (wrong level)
			usr.remGroup(grp, 'l');
			session.flush();

			UserModel usr4 = (UserModel)session.load(UserModel.class, i);
			assertTrue(usr4.getGroups().contains(grp));
			assertFalse(usr4.isMember(grp));
			assertFalse(usr4.isLeader(grp));

			// Removing a group for real now
			usr.remGroup(grp, 'r');
			session.flush();

			UserModel usr5 = (UserModel)session.load(UserModel.class, i);
			assertFalse(usr5.getGroups().contains(grp));
			assertFalse(usr5.isMember(grp));
			assertEquals(6, usr5.getGroups().size());
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testPosts()
	{
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			Integer i = usr.getId();
			GroupModel g = GroupModel.newGroup();

			// User makes a post
			PostModel p1 = PostModel.newPost(usr, g, "", "", "", "");
			session.flush();

			UserModel usr2 = (UserModel)session.load(UserModel.class, i);
			assertEquals(1, usr2.getPosts().size());
			assertEquals(p1, usr2.getPosts().iterator().next());
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testVisibility()
	{
		try
		{
			// Set up user & group 
			GroupModel grp = GroupModel.newGroup();

			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			usr.addGroup(grp, 'r');

			// make a page
			PageModel p = PageModel.newPage();
			p.setArea("area51");
			session.save(p);

			// link page to group
			grp.addPage(p);
			session.flush();

			// make sure user can see page
			assertTrue(Permissions.canViewPage(usr, p));
			assertTrue(Permissions.visiblePages(usr, "area51").contains(p));

			// Add a second page
			PageModel p2 = PageModel.newPage();
			p2.setArea("area51");
			session.save(p2);
			grp.addPage(p2);

			// Make sure we can see both pages
			assertTrue(Permissions.canViewPage(usr, p2));
			assertTrue(Permissions.visiblePages(usr,"area51").contains(p2));
			assertTrue(Permissions.visiblePages(usr,"area51").contains(p));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testGroups()
	{
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");

			// Add a few groups with various userlevels
			GroupModel g1 = GroupModel.newGroup();
			usr.addGroup(g1, 'r');

			GroupModel g3 = GroupModel.newGroup();
			usr.addGroup(g3, 'r');
			usr.addGroup(g3, 's');

			GroupModel g5 = GroupModel.newGroup();
			usr.addGroup(g5, 'r');
			usr.addGroup(g5, 'l');

			session.flush();

			assertTrue(usr.isRecipient(g1));
			assertFalse(usr.isMember(g1));
			assertFalse(usr.isSender(g1));
			assertFalse(usr.isLeader(g1));

			assertTrue(usr.isSender(g3));

			assertTrue(usr.isRecipient(g5));
			assertFalse(usr.isMember(g5));
			assertFalse(usr.isSender(g5));
			assertTrue(usr.isLeader(g5));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testChapters()
	{
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			Integer i = usr.getId();
			assertTrue(usr.getChapter() == null);

			// Add a chapter
			GroupChapterModel grp = GroupChapterModel.newChapter();
			usr.joinChapter(grp);
			session.flush();

			UserModel usr2 = (UserModel)session.load(UserModel.class, i);
			assertTrue(usr2.getChapter().equals(grp));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void testVisibleGroups()
	{
		
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");

			GroupModel orgGroup = Helpers.getGroup("Org");
			GroupModel finance = Helpers.getGroup("Finance");
			GroupModel reg = Helpers.getGroup("Regular");
			GroupModel associate = Helpers.getGroup("Associate");
			assertTrue(Permissions.visibleGroups(usr, false).contains(orgGroup));
			assertTrue(Permissions.canReadPostsInGroup(usr, orgGroup));

			
			GroupChapterModel chapter = GroupChapterModel.newChapter();
			GroupModel exec = chapter.getExec();
			
			GroupModel privateInChapter = GroupModel.newGroup(chapter);
			privateInChapter.setPublic(false);
			session.save(privateInChapter);
			
			GroupModel publicInChapter = GroupModel.newGroup(chapter);
			session.save(publicInChapter);

			GroupModel publicGroup = GroupModel.newGroup();
			session.save(publicGroup);
			
			GroupModel privateGroup = GroupModel.newGroup();
			privateGroup.setPublic(false);
			session.save(privateGroup);

			
			//baseline
			assertFalse(Permissions.visibleGroups(usr, true).contains(chapter));
			assertTrue(Permissions.visibleGroups(usr, false).contains(chapter));
			assertTrue( Permissions.canReadPostsInGroup(usr, chapter));
			
			assertFalse(Permissions.visibleGroups(usr, false).contains(exec));
			assertFalse( Permissions.canReadPostsInGroup(usr, exec));

			assertFalse(Permissions.visibleGroups(usr, false).contains(privateInChapter));
			assertFalse( Permissions.canReadPostsInGroup(usr,privateInChapter ));

			assertFalse(Permissions.visibleGroups(usr, false).contains(publicInChapter));
			assertTrue( Permissions.canReadPostsInGroup(usr, publicInChapter));
			
			assertFalse(Permissions.visibleGroups(usr, false).contains(publicGroup));
			assertTrue( Permissions.canReadPostsInGroup(usr, publicGroup));
			
			assertFalse(Permissions.visibleGroups(usr, false).contains(privateGroup));
			assertFalse( Permissions.canReadPostsInGroup(usr, privateGroup));

			assertFalse(Permissions.visibleGroups(usr, false).contains(finance));
			assertFalse( Permissions.canReadPostsInGroup(usr,finance ));

			assertFalse(Permissions.visibleGroups(usr, false).contains(reg));
			assertFalse( Permissions.canReadPostsInGroup(usr, reg));
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(associate));
			assertFalse( Permissions.canReadPostsInGroup(usr, associate));
			
			usr.addGroup(privateGroup, 'r');
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(privateGroup));
			assertTrue( Permissions.canReadPostsInGroup(usr, privateGroup));
			
			usr.addGroup(privateInChapter, 'r');
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(privateInChapter));
			assertTrue( Permissions.canReadPostsInGroup(usr, privateInChapter));
			
			usr.remGroup(privateInChapter);
			
 
			//in the chapter
			usr.joinChapter(chapter);
			session.flush();

			assertTrue(Permissions.visibleGroups(usr, true).contains(chapter));
			assertTrue( Permissions.canReadPostsInGroup(usr, chapter));
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(publicInChapter));
			assertTrue( Permissions.canReadPostsInGroup(usr, publicInChapter));
			
			//in the exec
			usr.upgradeToExec();
			session.flush();
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(exec));
			assertTrue( Permissions.canReadPostsInGroup(usr, exec));
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(privateInChapter));
			assertTrue( Permissions.canReadPostsInGroup(usr, privateInChapter));
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(finance));
			assertTrue( Permissions.canReadPostsInGroup(usr, finance));
			
			usr.leaveChapter(chapter);
			
			publicGroup.setVisible(false);
			
			assertFalse(Permissions.visibleGroups(usr, false).contains(publicGroup));
			assertTrue( Permissions.canReadPostsInGroup(usr, publicGroup));
			
			usr.upgradeToAdmin();

			assertFalse(Permissions.visibleGroups(usr, false).contains(reg));
			assertFalse( Permissions.canReadPostsInGroup(usr, reg));
			
			assertTrue(Permissions.visibleGroups(usr, false).contains(associate));
			assertFalse( Permissions.canReadPostsInGroup(usr, associate));
			
			assertFalse(Permissions.visibleGroups(usr, false).contains(publicGroup));
			assertTrue( Permissions.canReadPostsInGroup(usr, publicGroup));
			

		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void testVisiblePosts()
	{
		try
		{
			UserModel usr = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");
			Integer i = usr.getId();

			// Add some groups
			GroupModel g = GroupModel.newGroup();

			GroupModel g2 = GroupModel.newGroup();
			g2.setPublic(false);
			

			usr.addGroup(g, 'r');
			session.flush();

			// And some posts

			PostModel p = PostModel.newPost(usr, g,  "", "", "", "");

			PostModel p2 = PostModel.newPost(usr, g,  "", "", "", "");

			PostModel p3 = PostModel.newPost(usr, g, "", "", "", "");

			PostModel p4 = PostModel.newPost(usr, g2,  "", "", "", "");

			session.flush();

			// Can we see the right ones?
			UserModel usr2 = (UserModel)session.load(UserModel.class, i);
			
			PostList pl = new PostList(null, session, null, null, usr2);
			
			List<PostModel> visiblePosts = pl.visiblePosts(null, 0, 10, false,  false, false, false, true, false);
			
			assertTrue(visiblePosts.contains(p));
			assertTrue(visiblePosts.contains(p2));
			assertTrue(visiblePosts.contains(p3));
			assertFalse(visiblePosts.contains(p4));

			assertTrue(Permissions.canReplyToPost(usr2, p));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void testApplyToSession()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel
					.newApplicationSession("Test Session", "Test Instructions", "Test Instructions",
							"Test completed app message",
							"Test close email text", new Date(), new Date(),
							new Date(), "Test Rejection Email");
			UserModel usr = UserModel.newAssociateSignUp("User", "a@b.com",
					"User", "123456");
			ApplicationModel app = usr.applyToSession(s);
			int uID = usr.getId();
			int appID = app.getId();
			
			usr = (UserModel)session.load(UserModel.class, uID);
			app = (ApplicationModel)session.load(ApplicationModel.class, appID);
			assertSame("The app should point to the user", usr, app.getUser());
			assertTrue("The application should be in the users applications", usr.getApplications().contains(app));			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpexted Exception Caught");
		}		
		
	}
	
	public void testGetAppForSession()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel
					.newApplicationSession("Test Session", "Test Instructions", "Test Instructions",
							"Test completed app message",
							"Test close email text", new Date(), new Date(),
							new Date(), "Test Rejection Email");
			UserModel usr = UserModel.newAssociateSignUp("a@b.com",
					"Test", "User", "123456");
			ApplicationModel app = usr.applyToSession(s);
			int uID = usr.getId();
			
			usr = (UserModel)session.load(UserModel.class, uID);
			assertSame("The app should be the ne we just created", app, usr.getAppForSession(s));
			
			//New Session - No App
			s = ApplicationSessionModel
			.newApplicationSession("Test Session", "Test Instructions", "Test Instructions",
					"Test completed app message",
					"Test close email text", new Date(), new Date(),
					new Date(), "Test Rejection Email");
			assertNull("The application for an unapplied session should be null", usr.getAppForSession(s));
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpexted Exception Caught");
		}		
	}
	
	public void testSaveApplicationData()
	{
		try {
			UserModel usr = UserModel.newAssociateSignUp("a@b.com",
					"Test", "User", "123456");
			usr.saveApplicationData("App firstname", "App lastname", "App email", "App phone");
			int id = usr.getId();
			
			usr = (UserModel)session.load(UserModel.class, id);
			assertEquals("The firstname should be the one set", "App firstname", usr.getFirstname());
			assertEquals("The lastname should be the one set", "App lastname", usr.getLastname());
			assertEquals("The email should be the one set", "App email", usr.getEmail());
			assertEquals("The phone number should be the one set", "App phone", usr.getPhone());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Excpetion Thrown");
		}
	}
	
	public void testMergeListRoles() throws Exception
	{
		UserModel regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		UserModel mailing = UserModel.newMailingListSignUp("mail@mail.com");
		
		GroupModel g = GroupModel.newGroup();
		
// Regular is non-affiliated
					
		//Mailing is nothing
		regular.mergeRolesWithMailAccount(mailing);
		assertFalse("An associate non-affiliated merged with a mailing non-affiliated should be non-affiliated.",
				regular.isLeader(g) || regular.isSender(g) || regular.isMember(g) || regular.isRecipient(g) );
		
		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.subscribe(g);
		
		//Mailing is recipient
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate non-affiliated merged with a mailing recipient should be a recipient.",
				regular.isRecipient(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListSender(g);
		
		//Mailing is sender
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate non-affiliated merged with a mailing sender should be a sender.",
				regular.isSender(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListLeader(g);
		
		//Mailing is Leader
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate non-affiliated merged with a mailing leader should be a leader.",
				regular.isLeader(g));
		
// Regular is a recipient
		regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		regular.subscribe(g);
		
		//Mailing is nothing
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate recipient merged with a mailing non-affiliated should a recipient.",
				regular.isRecipient(g) );

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.subscribe(g);
		
		//Mailing is recipient
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate recipient merged with a mailing recipient should be a recipient.",
				regular.isRecipient(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListSender(g);
		
		//Mailing is sender
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate recipient merged with a mailing sender should be a sender.",
				regular.isSender(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListLeader(g);
		
		//Mailing is Leader
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate recipient merged with a mailing leader should be a leader.",
				regular.isLeader(g));
		
// Regular is a sender
		regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		regular.upgradeToListSender(g);
		
		//Mailing is nothing
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate sender merged with a mailing non-affiliated should be a sender.",
				regular.isSender(g) );

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.subscribe(g);
		
		//Mailing is recipient
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate sender merged with a mailing recipient should be a sender.",
				regular.isSender(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListSender(g);
		
		//Mailing is sender
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate sender merged with a mailing sender should be a sender.",
				regular.isSender(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListLeader(g);
		
		//Mailing is Leader
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate recipient merged with a mailing leader should be a leader.",
				regular.isLeader(g));
		
// Regular is a leader
		regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		regular.upgradeToListLeader(g);
		
		//Mailing is nothing
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate leader merged with a mailing non-affiliated should be a leader." ,
				regular.isLeader(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.subscribe(g);
		
		//Mailing is recipient
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate leader merged with a mailing recipient should be a leader.",
				regular.isLeader(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListSender(g);
		
		//Mailing is sender
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate leader merged with a mailing sender should be a leader.",
				regular.isLeader(g));

		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.upgradeToListLeader(g);
		
		//Mailing is Leader
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate leader merged with a mailing leader should be a leader.",
				regular.isLeader(g));
	}

		
	public void testMergeChapterMemberRoles() throws Exception
	{
		UserModel regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		UserModel mailing = UserModel.newMailingListSignUp("mail@mail.com");
		
		GroupChapterModel g = GroupChapterModel.newChapter();
		
		// Regular is a Member, Mailing is not a Member
		regular.joinChapter(g);
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate member merged with a mailing non-affiliate should a member.",
				regular.isMember(g));
				
		regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		mailing.joinChapter(g);

		// Regular is not a member, Mailing is a member
		regular.joinChapter(g);
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("An associate non-affiliate merged with a mailing member should a member.",
				regular.isMember(g));

		regular = UserModel.newAssociateSignUp("reg@reg.com", "Regular", "User", "regular");
		mailing = UserModel.newMailingListSignUp("mail@mail.com");
		regular.joinChapter(g);
		mailing.joinChapter(GroupChapterModel.newChapter());
		GroupChapterModel c = mailing.getChapter();
				
		// Both are members of different chapters
		regular.mergeRolesWithMailAccount(mailing);
		assertTrue("The associate member should still be a member of its initial group",
				regular.isMember(g));
		assertTrue("The associate member should be a recipient of the mailing member's group",
				regular.isRecipient(c));	
	}
	
	//next test is here because we can eventually use md5 passwords
	public void testMD5()
	{
		String fromPHP = "3adbbad1791fbae3ec908894c4963870";
		assertEquals(fromPHP, Helpers.md5("hello, world!"));
	}
}
