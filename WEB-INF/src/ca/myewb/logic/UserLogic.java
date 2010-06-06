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

package ca.myewb.logic;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

import ca.myewb.beans.Post;
import ca.myewb.beans.User;
import ca.myewb.frame.BCrypt;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.RoleModel;
import ca.myewb.model.UserModel;

public abstract class UserLogic extends User
{

	protected ThreadLocal<Boolean> adminCache = new ThreadLocal<Boolean>();
	
	public UserLogic() throws Exception
	{
		super();
	}

	public static String[] companySizes = { "", "1-5 employees",
			"6-20 employees", "20-100 employees", "100+ employees" };

	public static String[] incomeLevels = { "", "< $30k/year",
			"$30k/year - $45k/year", "$45k/year - $75k/year", "> $75k/year" };

	public static String[] studentLevels = { "", "High school",
			"College/Undergraduate", "Graduate", "Other" };

	public static String[] gradMonths = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	public String getProcompsizeString()
	{
		try
		{
			return companySizes[procompsize];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			log.warn("procompsize out of range for user=" + username
					+ " with procompsize=" + procompsize, e);

			return "";
		}
	}

	public String getProincomelevelString()
	{
		try
		{
			return incomeLevels[proincomelevel];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			log.warn("proincomelevel out of range for user=" + username
					+ " with proincomelevel=" + proincomelevel, e);

			return "";
		}
	}

	public String getStudentlevelString()
	{
		try
		{
			return studentLevels[studentlevel];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			log.warn("studentlevel out of range for user=" + username
					+ " with studentlevel=" + studentlevel, e);

			return "";
		}
	}

	public String getStudentgradmonthString()
	{
		try
		{
			return gradMonths[studentgradmonth - 1];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			log.warn("studentgradmonth out of range for user=" + username
					+ " with studentgradmonth=" + studentgradmonth, e);

			return "";
		}
	}
	
	public String getAddress() 
	{
		if(address1 == null)
			return null;
		
		return address1 + "\n" + suite + "\n" + address2 + "\n" 
		+ city + "\n" + province + "\n" + postalcode + "\n" + country;
	}

	public void setAddress(String address) 
	{		
		if (address == null)
		{
			setAddress1(null);
			setSuite(null);
			setAddress2(null);
			setCity(null);
			setProvince(null);
			setPostalcode(null);
			setCountry(null);
		}
		else
		{
			String[] splitAddress = address.split("\n");
			setAddress1(	(splitAddress.length > 0 && splitAddress[0] != null) ? splitAddress[0] : "");
			setSuite(		(splitAddress.length > 1 && splitAddress[1] != null) ? splitAddress[1] : "");
			setAddress2(	(splitAddress.length > 2 && splitAddress[2] != null) ? splitAddress[2] : "");
			setCity(		(splitAddress.length > 3 && splitAddress[3] != null) ? splitAddress[3] : "");
			setProvince(	(splitAddress.length > 4 && splitAddress[4] != null) ? splitAddress[4] : "");
			setPostalcode(	(splitAddress.length > 5 && splitAddress[5] != null) ? splitAddress[5] : "");
			setCountry(		(splitAddress.length > 6 && splitAddress[6] != null) ? splitAddress[6] : "");
		}
		setAddressUpdated(new Date());
	}
	
	public String getGenderString()
	{
		if (gender == 0)
		{
			return "";
		}
		else if (gender == 'f')
		{
			return "Female";
		}
		else if (gender == 'm')
		{
			return "Male";
		}

		log.warn("non-0, non-m, non-f gender value!");

		return new String(gender + "");
	}

	public String getStudentString()
	{
		if (student == 0)
		{
			return "";
		}
		else if (student == 'y')
		{
			return "Student";
		}
		else if (student == 'n')
		{
			return "Non-student";
		}

		log.warn("non-0, non-n, non-y student value!");

		return new String(student + "");
	}

	public List<GroupModel> getGroups() throws HibernateException
	{
		return (new SafeHibList<GroupModel>(session.createQuery(
					"select r.group from RoleModel as r where r.user=? AND r.end IS NULL")
						.setEntity(0, this))).list();
	}

	public List<GroupModel> getGroups(char lvl) throws HibernateException
	{
		return (new SafeHibList<GroupModel>(session.createQuery(
				"select r.group from RoleModel as r where r.user=? AND r.end IS NULL AND r.level=? order by r.group.id asc")
				.setEntity(0, this).setCharacter(1, lvl))).list();
	}

	public boolean checkPassword(String password) throws Exception
	{		
		return BCrypt.checkpw(password, passhash);
	}

	public void setPassword(String password) throws Exception
	{
		passhash = BCrypt.hashpw(password, BCrypt.gensalt());
	}

	private boolean checkRole(GroupLogic g, char c, boolean override)
			throws HibernateException
	{
		if(override && isAdmin())
		{
			return true;
		}
		
		int countRoles = ((Long)session.createQuery(
				"SELECT count(*) from RoleModel as r WHERE r.user=? AND r.group.id=? AND r.level=? AND r.end IS NULL")
				.setEntity(0, this).setInteger(1, g.getId()).setCharacter(2, c).uniqueResult()).intValue();

		return countRoles != 0;
	}
	
	private boolean checkRoleInAdminGroup(String shortname, char c, boolean override)
	throws HibernateException
	{
		if(override && isAdmin())
		{
			return true;
		}
		
		int countRoles = ((Long)session.createQuery(
				"SELECT count(*) from RoleModel as r WHERE r.user=? AND r.group.shortname=? AND r.group.admin=true AND r.level=? AND r.end IS NULL")
				.setEntity(0, this).setString(1, shortname).setCharacter(2, c).uniqueResult()).intValue();

		return countRoles != 0;
	}

	//role-checkers, string, override
	public boolean isLeader(String s, boolean override)
			throws HibernateException
	{
		return checkRoleInAdminGroup(s, 'l', override);
	}

	public boolean isSender(String s, boolean override)
			throws HibernateException
	{
		return checkRoleInAdminGroup(s, 's', override);
	}

	public boolean isMember(String s, boolean override)
			throws HibernateException
	{
		return checkRoleInAdminGroup(s, 'm', override);
	}

	public boolean isRecipient(String s, boolean override)
			throws HibernateException
	{
		return checkRoleInAdminGroup(s, 'r', override);
	}
	
	//role-checkers, string, no override
	public boolean isLeader(String s) throws HibernateException
	{
		return isLeader(s, true);
	}

	public boolean isSender(String s) throws HibernateException
	{
		return isSender(s, true);
	}

	public boolean isMember(String s) throws HibernateException
	{
		return isMember(s, true);
	}

	public boolean isRecipient(String s) throws HibernateException
	{
		return isRecipient(s, true);
	}

	//role-checkers, group, override
	public boolean isLeader(GroupLogic g, boolean override)
			throws HibernateException
	{
		return checkRole(g, 'l', override);
	}

	public boolean isSender(GroupLogic g, boolean override)
			throws HibernateException
	{
		return checkRole(g, 's', override);
	}

	public boolean isMember(GroupLogic g, boolean override)
			throws HibernateException
	{
		return checkRole(g, 'm', override);
	}

	public boolean isRecipient(GroupLogic g, boolean override)
			throws HibernateException
	{
		return checkRole(g, 'r', override);
	}
	
	//role-checkers, group, no override
	public boolean isLeader(GroupLogic g) throws HibernateException
	{
		return isLeader(g, true);
	}

	public boolean isSender(GroupLogic g) throws HibernateException
	{
		return isSender(g, true);
	}

	public boolean isMember(GroupLogic g) throws HibernateException
	{
		return isMember(g, true);
	}

	public boolean isRecipient(GroupLogic g) throws HibernateException
	{
		return isRecipient(g, true);
	}

	public boolean addGroup(GroupLogic g, char level) throws Exception
	{
		return addGroup(g, level, new Date());
	}

	public boolean addGroup(GroupLogic g, char lvl, Date start) throws Exception
	{
		if (!checkRole(g, lvl, false))
		{
			RoleModel role = new RoleModel(lvl, start);
			roles.add(role);
			role.setUser(this);
			g.addRole(role);
			session.save(role);

			log.debug("Granted " + g.getName() + " (" + lvl + ") access to "
					+ getUsername());
			
			return true;
		}
		else
		{
			log.debug(getUsername() + " is already a " + lvl + " member of "
					+ g.getName());
			
			return false;
		}
	}

	public boolean remGroup(GroupLogic g) throws Exception
	{
		return remGroup(g, null);
	}

	public boolean remGroup(GroupLogic g, Character level)
			throws HibernateException, Exception
	{
		return remGroup(g, level, new Date());
	}

	public boolean remGroup(GroupLogic g, Character lvl, Date end)
			throws HibernateException, Exception
	{
		List r;

		if (lvl != null)
		{
			r = session.createQuery(
					"FROM RoleModel r WHERE r.user=? AND r.group.id=? AND r.level=? AND r.end IS NULL")
					.setEntity(0, this).setInteger(1, g.getId()).setCharacter(2, lvl.charValue()).list();
		}
		else
		{
			r = session.createQuery(
					"FROM RoleModel r WHERE r.user=? AND r.group.id=? AND r.end IS NULL")
					.setEntity(0, this).setInteger(1, g.getId()).list();
		}

		if (!r.isEmpty())
		{
			Iterator it = r.iterator();
			while (it.hasNext())
			{
				RoleModel role = (RoleModel) it.next();
				role.end(end);
				log.debug("Removed " + g.getName() + " (" + lvl + ") access from " + getUsername());
			}
			
			return true;
		}
		else
		{
			log.debug(getUsername() + " is not a " + lvl + " member of " + g.getName());
			
			return false;
		}
	}

	public GroupChapterModel getChapter() throws HibernateException
	{
		return (GroupChapterModel) session.createQuery(
				"SELECT g FROM GroupChapterModel g, RoleModel r "
					+ "WHERE r.group.id=g.id AND r.user=? AND r.level='m' AND r.end IS NULL")
				.setEntity(0, this).uniqueResult();

	}

	
	public boolean isAdmin() throws HibernateException
	{
		if(adminCache.get() == null)
		{
			//LOOK hardcoded admin and nmt groupid's here for performance
			adminCache.set((!session.createQuery(
				"FROM RoleModel r WHERE r.user=? AND (r.group.shortname='Admin' OR r.group.shortname='NMT') AND r.end IS NULL")
				.setEntity(0, this).list().isEmpty()));
		}
		
		return adminCache.get();
	}
	
	public boolean isPresident()
	{
		return isMember("UniPresidents") || isMember("ProPresidents");
	}
	
	public void addPost(PostLogic p)
	{
		posts.add((PostModel) p);
		p.setPoster(this);
	}

	public void remPost(PostLogic p)
	{
		posts.remove(p);
	}


	public boolean isLastAdmin()
	{
		if (!isAdmin())
		{
			return false;
		}

		int size = session
				.createQuery(
						"select r from RoleModel as r where r.group=? AND r.end IS NULL")
				.setEntity(0, Helpers.getGroup("Admin")).list().size();

		log.debug("remaining admins: " + size);

		return (size == 1);
	}

	public boolean isLastExec()
	{
		if (getChapter() == null)
		{
			return false;
		}

		if (!isMember("Exec", false))
		{
			return false;
		}

		int size = session
				.createQuery(
						"select r from RoleModel as r where r.group=? and r.level='l' AND r.end IS NULL")
				.setEntity(0, this.getChapter()).list().size();
		log.debug("remaining execs: " + size);

		return (size == 1);
	}
	
	public boolean canRenew()
	{
		if (expiry == null)
		{
			return false;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(expiry);
		cal.add(Calendar.DAY_OF_YEAR, -35);

		return Calendar.getInstance().after(cal);
	}

	public String getUniqueToken() throws NoSuchAlgorithmException
	{
		// this is a re-usable unique token for a given user
		// we can use it as an emailable one-shot password, eg to unsub without
		// logging in
		// bonus: it's parsable in PHP so it's easy to pass data in/out through
		// URLs

		// reasoning for this hash:
		// username is immutable, but is null for many users
		// id is immutable, but might be visible at some point
		// passhash is assumed to be hidden, but is null for many users
		// email is assumed to be fairly hidden from most users
		// taken together, I think it's hard to guess the correct combo for any
		// given user
		// even if it were, it would be a mailing list user (passhash)
		String key = username + "salt" + id + "pepper" + passhash + "sugar"
				+ email;
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(key.getBytes());

		byte[] v = md.digest();

		// Thank you
		// http://forum.java.sun.com/thread.jspa?threadID=429739&messageID=1921162
		String HEX_DIGITS = "0123456789abcdef";
		StringBuffer sb = new StringBuffer(v.length * 2);

		for (int i = 0; i < v.length; i++)
		{
			int b = v[i] & 0xFF;
			sb.append(HEX_DIGITS.charAt(b >>> 4)).append(
					HEX_DIGITS.charAt(b & 0xF));
		}

		key = sb.toString();

		return key;
	}

	public Date getAccountCreationDate()
	{
		Criteria criteria = session.createCriteria(RoleModel.class);
		criteria.add(Restrictions.eq("user", this));
		criteria.add(Restrictions.eq("group", Helpers.getGroup("Org")));
		criteria.add(Restrictions.isNull("end"));
		Iterator it = criteria.list().iterator();

		if (it.hasNext())
		{
			return ((RoleModel) it.next()).getStart();
		}
		else
		{
			return null;
		}
	}

	public String getNatlRepTitle() throws HibernateException
	{
		// returns "" if this user isn't currently an exec
		// otherwise, it returns the title field of the currently unended exec
		// role
		GroupModel natlRep = Helpers.getGroup("NatlRep");

		List execRole = session
				.createQuery(
						"select r from RoleModel as r where r.user=? AND r.end IS NULL AND r.level=? and r.group=?")
				.setEntity(0, this).setCharacter(1, 'm').setEntity(2,
						natlRep).list();

		if (execRole.isEmpty())
		{
			return "";
		}

		return ((RoleModel) execRole.get(0)).getTitle();
	}

	public String getExecTitle() throws HibernateException
	{
		// returns "" if this user isn't currently an exec
		// otherwise, it returns the title field of the currently unended exec
		// role
		GroupChapterModel theChapter = getChapter();

		if (theChapter == null)
		{
			return "";
		}

		List execRole = session
				.createQuery(
						"select r from RoleModel as r where r.user=? AND r.end IS NULL AND r.level=? and r.group=?")
				.setEntity(0, this).setCharacter(1, 'l').setEntity(2,
						theChapter).list();

		if (execRole.isEmpty())
		{
			return "";
		}

		return ((RoleModel) execRole.get(0)).getTitle();
	}

	public String getNMTTitle() throws HibernateException
	{
		if (isMember("NMT", false))
		{
			List nmtRole = session
					.createQuery(
							"select r from RoleModel as r where r.user=? AND r.end IS NULL and r.group=?")
					.setEntity(0, this).setEntity(1, Helpers.getGroup("NMT"))
					.list();

			return ((RoleModel) nmtRole.get(0)).getTitle();
		}
		else if (isMember("Admin", false))
		{
			return "Admin User";
		}
		else
		{
			return "";
		}
	}

	public void setNatlRepTitle(String title) throws HibernateException
	{
		// does nothing if the user isn't currently an exec
		// otherwise, it sets the title field of the currently unended exec role
		List execRole = session
				.createQuery(
						"select r from RoleModel as r where r.user=? AND r.end IS NULL AND r.level=? and r.group=?")
				.setEntity(0, this).setCharacter(1, 'm').setEntity(2,
						Helpers.getGroup("NatlRep")).list();

		if (execRole.isEmpty())
		{
			return;
		}

		((RoleModel) execRole.get(0)).setTitle(title);
	}

	public void setExecTitle(String title) throws HibernateException
	{
		// does nothing if the user isn't currently an exec
		// otherwise, it sets the title field of the currently unended exec role
		List execRole = session
				.createQuery(
						"select r from RoleModel as r where r.user=? AND r.end IS NULL AND r.level=? and r.group=?")
				.setEntity(0, this).setCharacter(1, 'l').setEntity(2,
						getChapter()).list();

		if (execRole.isEmpty())
		{
			return;
		}

		((RoleModel) execRole.get(0)).setTitle(title);
	}

	public void setNMTTitle(String title) throws HibernateException
	{
		// does nothing if the user isn't currently an nmt
		// otherwise, it sets the title field of the currently unended nmt role
		List nmtRole = session
				.createQuery(
						"select r from RoleModel as r where r.user=? AND r.end IS NULL and r.group=?")
				.setEntity(0, this).setEntity(1, Helpers.getGroup("NMT")).list();

		if (nmtRole.isEmpty())
		{
			return;
		}

		((RoleModel) nmtRole.get(0)).setTitle(title);
	}

	public boolean hasPicture()
	{
		return (new File(Helpers.getUserFilesDir() + "/userpics/thumbs/" + id
				+ ".jpg")).exists();
	}
	
	public String getFormattedEmailList()
	{
		StringBuffer emails = new StringBuffer();

		for (Iterator<String> it = getEmails().iterator(); it.hasNext();)
		{
			String email = it.next();
			if (!email.equals(getEmail()))
			{
				emails.append(email);
				if (it.hasNext())
				{
					emails.append("\n");
				}
			}
		}

		return emails.toString();
	}

	public static UserModel getUserForEmail(String email)
	{
		List result = HibernateUtil.currentSession().createQuery(
				"FROM UserModel u WHERE :email in elements(u.emails)")
				.setString("email", email).list();
		if (result.isEmpty())
		{
			return null;
		}
		
		if(result.size() > 1)
		{
			Logger.getLogger(UserModel.class).error("2 users with email address " + email);
		}
		
		return (UserModel) result.get(0);
	}
	
	public void flagPost(PostModel p)
	{
		getFlaggedPosts().add(p);
		
		HibernateUtil.currentSession().flush();
		
		if(p.getFlaggedByUsers().size() == Post.FlagsToFeature)
		{
			p.feature();
		}
	}
	
	public void unflagPost(PostModel p)
	{
		getFlaggedPosts().remove(p);
	}
	
	public boolean hasFlagged(PostModel p)
	{
		return getFlaggedPosts().contains(p);
	}
}
