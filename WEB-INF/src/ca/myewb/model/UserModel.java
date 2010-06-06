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

package ca.myewb.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.logic.GroupLogic;
import ca.myewb.logic.UserLogic;

public class UserModel extends UserLogic
{

	
	UserModel() throws Exception
	{
		super();
	}

	public void signIn()
	{
		lastLogin = currentLogin;
		currentLogin = new Date();

		if (lastLogin == null)
		{
			lastLogin = currentLogin;
		}

		logins++;
		
		if (!isAdmin())
		{
			Helpers.currentDailyStats().logSignin();
		}
	}

	public boolean subscribe(GroupLogic list) throws Exception
	{
		//member/recipient invariant
		if (!isMember(list, false))
		{
			return addGroup(list, 'r');
		}
		
		return false;
	}

	public void unsubscribe(GroupLogic list) throws Exception
	{
		//can't use this to leave chapters or admin groups
		remGroup(list, 'r');
		remGroup(list, 's');
		remGroup(list, 'l');
	}

	public boolean joinChapter(GroupChapterModel chapter) throws Exception
	{
		if(isMember("Chapter", false))
		{
			throw new IllegalStateException("can't join 2 chapters!");
		}
		
		boolean newMember = !remGroup(chapter, 'r');
		addGroup(chapter, 'm');
		
		// Also add to the "in a chapter" group and remove from "not in a
		// chapter"
		GroupLogic g = Helpers.getGroup("Chapter");

		if (!isMember(g, false))
		{
			addGroup(g, 'm');
		}

		g = Helpers.getGroup("NoChapter");

		if (isMember(g, false))
		{
			remGroup(g, 'm');
		}

		return newMember;
	}

	public void leaveChapter(GroupChapterModel chapter) throws Exception
	{
		if(chapter == null)
		{
			return;
		}
		
		// just in case they're an exec
		downgradeFromExec();
		downgradeFromNatlRep();
		
		remGroup(chapter);
		List<GroupModel> chapterLists = chapter.getChildGroups(true, true);
		for (GroupLogic g : chapterLists)
		{
			remGroup(g);
		}

		// And remove from the "in a chapter" group, placing them in "not in a
		// chapter"
		GroupLogic g = Helpers.getGroup("Chapter");

		if (isMember(g, false))
		{
			remGroup(g, 'm');
		}

		g = Helpers.getGroup("NoChapter");

		if (!isMember(g, false))
		{
			addGroup(g, 'm');
		}
	}

	public void upgradeToExec() throws Exception
	{
		GroupChapterModel chapter = getChapter();
		// Add to chapter exec
		GroupLogic chapExec = chapter.getExec();

		// remove any sort-of exec status
		remGroup(chapExec);
		remGroup(chapter, 's');

		addGroup(chapExec, 'm');
		addGroup(chapExec, 'l'); // put on exec list
		addGroup(chapter, 'l'); // grant chapter admin powers

		// Add to global exec
		addGroup(Helpers.getGroup("Exec"), 'm');
		addGroup(Helpers.getGroup("Exec"), 's');
		
		// Remove from global exec
		if(chapter.isProfessional())
		{
			addGroup(Helpers.getGroup("ProChaptersExec"), 'm');
			addGroup(Helpers.getGroup("ProChaptersExec"), 's');
		}
		else
		{
			addGroup(Helpers.getGroup("UniChaptersExec"), 'm');
			addGroup(Helpers.getGroup("UniChaptersExec"), 's');
		}
	}

	public void upgradeToNatlRep() throws Exception
	{
		addGroup(Helpers.getGroup("NatlRep"), 'm');
	}

	public void upgradeToNMT() throws HibernateException, Exception
	{
		addGroup(Helpers.getGroup("NMT"), 'm');
		adminCache = new ThreadLocal<Boolean>();
	}

	public void upgradeToAdmin() throws HibernateException, Exception
	{
		addGroup(Helpers.getGroup("Admin"), 'm');
		adminCache = new ThreadLocal<Boolean>();
	}
	
	public void downgradeFromNatlRep() throws Exception
	{
		// Remove from global exec
		remGroup(Helpers.getGroup("NatlRep"));
		
		//Remove from any rep lists
		List<GroupModel> execLists = Helpers.getNationalRepLists(true, true);
		for(GroupModel execList: execLists)
		{
			remGroup(execList);
		}
	}
	
	public void downgradeFromExec() throws Exception
	{
		GroupChapterModel chapter = getChapter();
		// Remove from chapter exec
		GroupLogic chapExec = chapter.getExec();
		remGroup(chapExec);
		remGroup(chapter, 'l');

		// Remove from global exec
		remGroup(Helpers.getGroup("ProChaptersExec"));
		remGroup(Helpers.getGroup("UniChaptersExec"));
		remGroup(Helpers.getGroup("Exec"));
		
		//Remove from any rep lists
		List<GroupModel> execLists = Helpers.getNationalRepLists(true, true);
		for(GroupModel execList: execLists)
		{
			remGroup(execList);
		}
	}

	public void downgradeFromNMT() throws HibernateException, Exception
	{
		remGroup(Helpers.getGroup("NMT"), 'm');
		adminCache = new ThreadLocal<Boolean>();
	}

	public void downgradeFromAdmin() throws HibernateException, Exception
	{
		remGroup(Helpers.getGroup("Admin"), 'm');
		adminCache = new ThreadLocal<Boolean>();
	}

	public void changeNatlRepTitle(String title, Hashtable<GroupModel, Boolean> repLists) throws HibernateException, Exception
	{
		setNatlRepTitle(title);

		Set<GroupModel> keys = repLists.keySet();
		
		for(GroupModel grp: keys)
		{
			if(repLists.get(grp))
			{
				addGroup(grp, 'm');
				addGroup(grp, 's');
			}
			else
			{
				remGroup(grp, 'm');
				remGroup(grp, 's');
			} 
		}
	}
	
	public void changeExecTitle(String title, Hashtable<GroupModel, Boolean> repLists) throws HibernateException, Exception
	{
		setExecTitle(title);

		Set<GroupModel> keys = repLists.keySet();
		
		for(GroupModel grp: keys)
		{
			if(repLists.get(grp))
			{
				addGroup(grp, 'm');
				addGroup(grp, 's');
			}
			else
			{
				remGroup(grp, 'm');
				remGroup(grp, 's');
			} 
		}
	}

	public void assignPlacement(PlacementModel placement) throws Exception
	{
		placements.add(placement);
		placement.setOv(this);

		placement.setActive(true);
		addGroup(Helpers.getGroup("OVs"), 'm');
	}

	public static UserModel newAssociateSignUp(String email,
			String firstname, String lastname, String password)
			throws Exception
	{
		return UserModel.newAssociateSignUp(null, email, firstname,
				lastname, password);
	}

	public static UserModel newAssociateSignUp(UserModel u, String email, String firstname, String lastname, String password)
			throws Exception
	{

		Session hibernateSession = HibernateUtil.currentSession();

		if (u == null) //create a new user
		{
			u = new UserModel();
			u.changePrimaryEmail(email);
			Helpers.currentDailyStats().logSignup();
		} 
		else // This is an upgrade
		{
			Helpers.currentDailyStats().logMailinglistupgrade();
		}

		// Set user info
		u.setUsername("." + System.currentTimeMillis());
		u.setFirstname(firstname);
		u.setLastname(lastname);
		u.setPassword(password);
		hibernateSession.save(u);

		// Set initial permissions & group memberships:
		// Global "everyone" group
		u.addGroup(Helpers.getGroup("Org"), 'm');

		// Registered Users groups
		u.addGroup(Helpers.getGroup("Users"), 'm');
		u.addGroup(Helpers.getGroup("Associate"), 'm');

		if (!u.isMember(Helpers.getGroup("Chapter")))
		{
			// "Not in a chapter" group
			u.addGroup(Helpers.getGroup("NoChapter"), 'm');
		}

		return u;
	}

	public static UserModel newMailingListSignUp(String formEmail) throws Exception
	{

		Session hibernateSession = HibernateUtil.currentSession();

		Helpers.currentDailyStats().logMailinglistsignup();

		UserModel targetUser = new UserModel();
		targetUser.changePrimaryEmail(formEmail);
		hibernateSession.save(targetUser);

		targetUser.addGroup(Helpers.getGroup("Org"), 'm');
		targetUser.addGroup(Helpers.getGroup("NoChapter"), 'm');


		return targetUser;
	}

	public void delete() throws HibernateException, Exception
	{
		// Alright, that's it. Good bye.
		// Remove all current memberships
		Iterator results = session.createQuery(
				"FROM RoleModel r WHERE r.user=?").setEntity(0, this).list()
				.iterator();

		while (results.hasNext())
		{
			RoleModel r = (RoleModel) results.next();
			r.end(new Date());
		}

		// And make member of Deleted users
		this.addGroup(Helpers.getGroup("Deleted"), 'm');

		email = null;
		language = null;
		setAddress(null);
		phone = null;
		cellno = null;
		alternateno = null;
		businessno = null;
		gender = 0;
		birth = 0;
		student = 0;
		lastLogin = currentLogin;
		currentLogin = null;

		studentnumber = "";
		studentinstitution = "";
		studentfield = "";
		studentlevel = 0;
		studentgradmonth = 0;
		studentgradyear = 0;

		proemployer = "";
		prosector = "";
		proposition = "";
		procompsize = 0;
		proincomelevel = 0;

		getEmails().clear();
		setEmails(null);
		
		Helpers.currentDailyStats().logDeletion();
	}

	public void adminToggle()
	{
		adminToggle = !adminToggle;
	}

	public void downgradeFromListSender(GroupLogic grp)
			throws HibernateException, Exception
	{
		remGroup(grp, 's');
		remGroup(grp, 'l'); //this is the intuitive thing to do
	}

	public void downgradeFromListLeader(GroupLogic grp)
			throws HibernateException, Exception
	{
		//this really shouldn't happen, but just in case, silently return
		if(grp.isChapter() || grp.isExecList() || grp.getAdmin())
		{
			return;
		}
		
		remGroup(grp, 'l');
		remGroup(grp, 's'); //this is the intuitive thing to do
	}

	public boolean upgradeToListSender(GroupLogic grp) throws Exception
	{
		//even admins get added as r
		boolean newMember = subscribe(grp);
		
		//admins already have full powers
		if (isAdmin())
		{
			return newMember;
		}
		
		//shouldn't downgrade execs from leader to sender for chapter/exec groups!
		GroupChapterModel chapter = getChapter();
		if ((chapter != null) && isLeader(grp) && 
				(grp.equals(chapter) || (grp.isExecList() && grp.chapterIfExec().equals(chapter))))
		{
			return newMember;
		}

		//sender/leader invariant
		remGroup(grp, 'l');
		addGroup(grp, 's');
		
		return newMember;
	}

	public boolean upgradeToListLeader(GroupLogic grp) throws HibernateException,
			Exception
	{
		//even admins get added as r
		boolean newMember = subscribe(grp);

		//admins already have full powers
		if (isAdmin())
		{
			return newMember;
		}

		//this really shouldn't happen, but just in case, silently return
		if(grp.isChapter() || grp.isExecList() || grp.getAdmin())
		{
			return newMember;
		}

		//sender/leader invariant
		remGroup(grp, 's');
		addGroup(grp, 'l');
		
		return newMember;
	}

	public OVInfoModel retreiveOVInfo()
	{
		OVInfoModel theInfo = getOVInfo();

		if (theInfo == null)
		{
			theInfo = OVInfoModel.newOVInfo();
			setOVInfo(theInfo);
		}

		return theInfo;
	}

	public void saveUser(String firstname, String lastname, String email,
			Set<String> emails, String password, String language,
			String gender, String student, String birthYear, String canadianInfo)
			throws Exception
	{
		
		setFirstname(firstname);
		setLastname(lastname);
		emails.add(getEmail()); //just in case, and so that changePrimary will work
		setEmails(emails); //updated all secondaries
		changePrimaryEmail(email); // go through normal channels to update primary

		if ((password != null) && !password.equals(""))
		{
			setPassword(password);
		}

		setLanguage(language);

		if (!gender.equals(""))
		{
			setGender(gender.charAt(0));
		}

		if (!student.equals(""))
		{
			setStudent(student.charAt(0));
		}

		if ((birthYear == null) || birthYear.equals(""))
		{
			setBirth(0); // so why doesn't new Integer(null) give you a zero?
			// =)
		} else
		{
			setBirth(new Integer(birthYear).intValue());
		}

		if (!canadianInfo.equals(""))
		{
			setCanadianinfo(canadianInfo.charAt(0));
		}

	}
	public void saveEmails(String email, Set<String> emails)
			throws Exception
	{
		
		emails.add(getEmail()); //just in case, and so that changePrimary will work
		setEmails(emails); //updated all secondaries
		changePrimaryEmail(email); // go through normal channels to update primary
	}

	
	public void saveAddress(String address, String phone, String business, String cell, String alt)
	{
		setAddress(address);
		setPhone(phone);
		setBusinessno(business);
		setCellno(cell);
		setAlternateno(alt);
	}

	public void saveProfessionalData(String employer, String position,
			String sector, String compsize, String income)
	{
		setProemployer(employer);
		setProposition(position);
		setProsector(sector);

		if (compsize.equals(""))
		{
			compsize = "0";
		}

		setProcompsize(new Integer(compsize).intValue());

		if (income.equals(""))
		{
			income = "0";
		}

		setProincomelevel(new Integer(income).intValue());
	}

	public void saveStudentData(String field, String institution,
			String studentNo, String studentlevel, String gradmonth,
			String gradyear)
	{
		setStudentfield(field);
		setStudentinstitution(institution);
		setStudentnumber(studentNo);

		if (studentlevel.equals(""))
		{
			studentlevel = "0";
		}

		setStudentlevel(new Integer(studentlevel).intValue());

		if (gradmonth.equals(""))
		{
			gradmonth = "0";
		}

		setStudentgradmonth(new Integer(gradmonth).intValue());

		if (gradyear.equals(""))
		{
			gradyear = "0";
		}

		setStudentgradyear(new Integer(gradyear).intValue());
	}

	public void renew(UserModel userDoingUpgrade, boolean isCCUpgrade)
			throws HibernateException, Exception
	{
		if (getExpiry() == null)
		{
			setExpiry(new Date());
			remGroup(Helpers.getGroup("Associate"), 'm');
			Helpers.currentDailyStats().logRegupgrade();
		} else
		{
			remGroup(Helpers.getGroup("Regular"), 'm'); // To trap renewal info in role table
			Helpers.currentDailyStats().logRenewal();
		}

		addGroup(Helpers.getGroup("Regular"), 'm');

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getExpiry());
		calendar.add(Calendar.YEAR, 1);
		setExpiry(calendar.getTime());

		// Log whether it's a NMT or Chapter payment
		if (isCCUpgrade)
		{
			// This is a credit card system self upgrads
			setUpgradeLevel('n');
		}
		else if (userDoingUpgrade != null && userDoingUpgrade.isAdmin())
		{
			setUpgradeLevel('n');
		} 
		else if (userDoingUpgrade != null && getChapter() != null
				&& userDoingUpgrade.isLeader(getChapter()))
		{
			setUpgradeLevel('c');
		} 
		else
		{
			setUpgradeLevel('a');
		}
	}

	public void expire() throws HibernateException, Exception
	{
		remGroup(Helpers.getGroup("Regular"));
		addGroup(Helpers.getGroup("Associate"), 'm');
		setExpiry(null);
		Helpers.currentDailyStats().logRegdowngrade();
	}
	
	public static UserModel createGuestUser() throws Exception
	{
		Session session = HibernateUtil.currentSession();
		UserModel guest = new UserModel();
		guest.setUsername("guest");
		session.save(guest);
		guest.addGroup(Helpers.getGroup("Org"), 'm');
		guest.addGroup(Helpers.getGroup("Guest"), 'm');
		guest.addGroup(Helpers.getGroup("NoChapter"), 'm');
		session.flush();
		return guest;
	}

	public ApplicationModel applyToSession(ApplicationSessionModel session)
	{
		ApplicationModel app = ApplicationModel.newApplication(session, this);
		getApplications().add(app);
		return app;
	}

	public ApplicationModel getAppForSession(ApplicationSessionModel s)
	{
		return (ApplicationModel) session
				.createQuery(
						"FROM ApplicationModel WHERE userid = :userid AND sessionid = :sessionid")
				.setInteger("userid", getId()).setInteger("sessionid",
						s.getId()).uniqueResult();
	}

	public void saveApplicationData(String firstname, String lastname,
			String email, String phone)
	{
		setFirstname(firstname);
		setLastname(lastname);
		changePrimaryEmail(email);
		setPhone(phone);
	}

	public void saveUpgradeData(String firstname, String lastname,
			String email, String address, String phone, String student)
	{
		setFirstname(firstname);
		setLastname(lastname);
		changePrimaryEmail(email);
		saveAddress(address, phone, getBusinessno(), getCellno(), getAlternateno());
		setPhone(phone);

		if (!student.equals(""))
		{
			setStudent(student.charAt(0));
		}
	}

	public void changePrimaryEmail(String email)
	{
		setEmail(email);
		getEmails().add(email); //old primary still there, new primary is too now
	}
	
	
	public void mergeRolesWithMailAccount( UserModel mailAcct ) throws Exception
	{
		for( GroupModel g: mailAcct.getGroups())
		{			
			if( g.isChapter() && !isMember("Chapter") && mailAcct.isMember(g) )
			{
				//won't step in if mailAcct is only recip of chapter group, but that's not possible
				joinChapter((GroupChapterModel) g);
				log.info(getUsername() + " added to chapter " + g.getShortname());				
			}
			
			if( !g.getAdmin() )
			{
				if(!g.isChapter() && !g.isExecList() && (isLeader(g) || mailAcct.isLeader(g)))
				{
					upgradeToListLeader(g);
					log.info(getUsername() + " upgraded to leader of " + g.getShortname());	
				}
				else if( isSender(g) || mailAcct.isSender(g) )
				{
					upgradeToListSender(g);
					log.info(getUsername() + " upgraded to sender of " + g.getShortname());
				}
				else
				{
					subscribe(g);
					log.info(getUsername() + " added as recipient of " + g.getShortname());
				}
			}
		}

		mailAcct.delete();
	}
	
	public static String generateRandomPassword()
	{
		Random rand = new Random();
		String newpassword = "";

		for (int i = 0; i < 8; i++)
		{
			int next = rand.nextInt(61);

			if (next < 10)
			{
				newpassword += (char)('0' + next);
			}
			else if (next < 36)
			{
				newpassword += (char)(('A' + next) - 10);
			}
			else
			{
				newpassword += (char)(('a' + next) - 36);
			}
		}
		return newpassword;
	}
	
}
