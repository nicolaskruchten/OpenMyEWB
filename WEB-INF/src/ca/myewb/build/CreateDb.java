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

package ca.myewb.build;

import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import ca.myewb.frame.Controller;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.DailyStatsModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PageModel;
import ca.myewb.model.UserModel;


public class CreateDb
{
	public static void main(String[] args)
	{
		String postfix = args[0];
		String action = args[1];
		System.out.println("Using database: " + postfix);

		Logger.getLogger("ca.myewb").setLevel(Level.WARN);

		try
		{
			// Set up the database connection
			Class.forName("com.mysql.jdbc.Driver");

			// Drop & re-create the database if needed
			if (action.equals("new"))
			{
				createDb(args[0]);
			}

			// Prepare for data manipulation
			HibernateUtil.createFactory(postfix);

			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();

			// Do what's needed
			if (action.equals("new"))
			{
				insertData();
			}

			controllerData();

			// Finish up
			session.flush();
			tx.commit();
			HibernateUtil.closeSession();
		}
		catch (Exception e)
		{
			System.err.print("Exception: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		Logger.getLogger("ca.myewb").setLevel(Level.DEBUG);
	}

	private static void createDb(String postfix)
	{
		try
		{
			System.out.println("Creating fresh database");

			Configuration config = HibernateUtil.getConfiguration(postfix);

			// Set up the schema exporter utility
			SchemaExport sch = new SchemaExport(config);
			sch = sch.setDelimiter(";");

			// Drop and re-create the database
			sch.drop(false, true);
			sch.create(false, true);
		}
		catch (Exception e)
		{
			System.err.print("Exception: " + e);
			e.printStackTrace();
		}
	}

	private static void insertData()
	                        throws Exception, InstantiationException, 
	                               IllegalAccessException, 
	                               ClassNotFoundException
	{
		System.out.println("Inserting data");

		Session session = HibernateUtil.currentSession();

		// BASIC GROUPS 

		{
			//must be first group!
			GroupModel g1 = GroupModel.newGroup();
			g1.setName("Org");
			g1.setShortname("Org");
			g1.setDescription("The main announcement mailing list.");
			g1.setPostName("Anyone (on public front page)");
			g1.setAdmin(true);
			g1.setVisible(true);
			g1.setPublic(false);
			session.save(g1);
		}

		{
			GroupModel g10 = GroupModel.newGroup();
			g10.setName("Guest");
			g10.setShortname("Guest");
			g10.setDescription("The guest user");
			g10.setAdmin(true);
			g10.setVisible(false);
			g10.setPublic(false);
			session.save(g10);
		}

		{
			GroupModel g2 = GroupModel.newGroup();
			g2.setName("Users");
			g2.setShortname("Users");
			g2.setDescription("All registered users");
			g2.setAdmin(true);
			g2.setVisible(false);
			g2.setPublic(false);
			session.save(g2);
		}

		//ADMIN LAYER
		
		{
			GroupModel g4 = GroupModel.newGroup();
			g4.setName("Admin");
			g4.setShortname("Admin");
			g4.setDescription("System administrators");
			g4.setPostName("All system administrators");
			g4.setAdmin(true);
			g4.setVisible(false);
			g4.setPublic(false);
			session.save(g4);
		}

		{
			GroupModel g3 = GroupModel.newGroup();
			g3.setName("NMT");
			g3.setShortname("NMT");
			g3.setDescription("NMT peoples");
			g3.setPostName("All National Management Team members");
			g3.setAdmin(true);
			g3.setVisible(false);
			g3.setPublic(false);
			session.save(g3);
		}

		//REGULAR MEMBERSHIP
		
		{
			GroupModel g6 = GroupModel.newGroup();
			g6.setName("Regular");
			g6.setShortname("Regular");
			g6.setDescription("Global group for regular members");
			g6.setPostName("All Regular Members");
			g6.setAdmin(true);
			g6.setVisible(false);
			g6.setPublic(false);
			session.save(g6);
		}

		{
			GroupModel g7 = GroupModel.newGroup();
			g7.setName("Associate");
			g7.setShortname("Associate");
			g7.setDescription("Global group for associate members");
			g7.setPostName("All Associate Members");
			g7.setAdmin(true);
			g7.setVisible(false);
			g7.setPublic(false);
			session.save(g7);
		}

		//CHAPTER MEMBERSHIP
		
		{
			GroupModel g8 = GroupModel.newGroup();
			g8.setName("Chapter");
			g8.setShortname("Chapter");
			g8.setDescription("Everyone in a chapter");
			g8.setPostName("Anyone in a chapter");
			g8.setAdmin(true);
			g8.setVisible(false);
			g8.setPublic(false);
			session.save(g8);
		}

		{
			GroupModel g9 = GroupModel.newGroup();
			g9.setName("NoChapter");
			g9.setShortname("NoChapter");
			g9.setDescription("Everyone not in a chapter");
			g9.setPostName("Anyone not in a chapter");
			g9.setAdmin(true);
			g9.setVisible(false);
			g9.setPublic(false);
			session.save(g9);
		}

		//DELETED STATES
		
		{
			GroupModel g11 = GroupModel.newGroup();
			g11.setName("Deleted");
			g11.setShortname("Deleted");
			g11.setDescription("All deleted users");
			g11.setAdmin(true);
			g11.setVisible(false);
			g11.setPublic(false);
			session.save(g11);
		}

		{
			GroupModel g14 = GroupModel.newGroup();
			g14.setName("DeletedPosts");
			g14.setShortname("DeletedPosts");
			g14.setDescription("Group for deleted posts");
			g14.setPostName("No one");
			g14.setAdmin(true);
			g14.setVisible(false);
			g14.setPublic(false);
			session.save(g14);
		}
		
		//EXEC/NATL REP SYSTEM

		{
			GroupModel g5 = GroupModel.newGroup();
			g5.setName("Chapter Executive Members (all chapters)");
			g5.setShortname("Exec");
			g5.setDescription("General executive mailing list (all chapters).");
			g5.setPostName("All chapter executive members (any chapter)");
			g5.setAdmin(true);
			g5.setVisible(true);
			g5.setPublic(false);
			session.save(g5);
		}

		{
			GroupModel g14 = GroupModel.newGroup();
			g14.setName("Chapter National Reps (all chapters)");
			g14.setShortname("NatlRep");
			g14.setDescription("National Reps mailing list (all chapters).");
			g14.setPostName("All chapter national reps (any chapter)");
			g14.setAdmin(true);
			g14.setVisible(false);
			g14.setPublic(false);
			session.save(g14);
		}
		
		{
			GroupModel g15 = GroupModel.newGroup();
			g15.setName("Student Chapter Exec");
			g15.setShortname("UniChaptersExec");
			g15.setDescription("General executive mailing list (student chapters).");
			g15.setPostName("All student chapter executive members (any student chapter)");
			g15.setAdmin(true);
			g15.setVisible(true);
			g15.setPublic(false);
			session.save(g15);
		}

		{
			GroupModel g16 = GroupModel.newGroup();
			g16.setName("Professional Chapter Exec");
			g16.setShortname("ProChaptersExec");
			g16.setDescription("General executive mailing list (professional chapters).");
			g16.setPostName("All professional chapter executive members (any professional chapter)");
			g16.setAdmin(true);
			g16.setVisible(true);
			g16.setPublic(false);
			session.save(g16);
		}

		//APPLICATIONS SYSTEM
		
		{
			GroupModel g15 = GroupModel.newGroup();
			g15.setName("OVs");
			g15.setShortname("OVs");
			g15.setDescription("All overseas volunteers");
			g15.setAdmin(true);
			g15.setVisible(false);
			g15.setPublic(false);
			session.save(g15);
		}
		
		//NATIONAL REP LISTS
		
		{
			GroupModel g12 = GroupModel.newGroup();
			g12.setName("Student Chapter Presidents");
			g12.setShortname("UniPresidents");
			g12.setDescription("Student Chapter Presidents' mailing list.");
			g12.setPostName("All student chapter presidents");
			g12.setAdmin(true);
			g12.setVisible(true);
			g12.setPublic(false);
			g12.setNationalRepType('s');
			session.save(g12);
		}

		{
			GroupModel g16 = GroupModel.newGroup();
			g16.setName("Professional Chapter Presidents");
			g16.setShortname("ProPresidents");
			g16.setDescription("Professional Chapter Presidents' mailing list");
			g16.setPostName("All professional chapter presidents");
			g16.setAdmin(true);
			g16.setVisible(true);
			g16.setPublic(false);
			g16.setNationalRepType('p');
			session.save(g16);
		}
		{
			GroupModel g13 = GroupModel.newGroup();
			g13.setName("Finance National Reps");
			g13.setShortname("Finance");
			g13.setDescription("Finance National Reps' mailing list.");
			g13.setPostName("Any Finance National Rep");
			g13.setAdmin(true);
			g13.setVisible(true);
			g13.setPublic(false);
			g13.setNationalRepType('b');
			session.save(g13);
		}

				
		session.flush();
		
		Calendar cal = Calendar.getInstance();

		for (int i = 0; i < 30; i++)
		{
			DailyStatsModel.newDailyStats(cal.getTime());
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}

		/*
		 *
		 * USERS and ROLES
		 *
		 */
		
		//guest user must be first!
		UserModel.createGuestUser();

		// first admin user
		UserModel admin = UserModel.newAssociateSignUp(null, "sample@email.com", "Anonymous", "Administrator", "admin");
		admin.upgradeToAdmin();

		session.flush();
	}

	private static void controllerData()
	                            throws InstantiationException, 
	                                   IllegalAccessException, 
	                                   ClassNotFoundException
	{
		/* Dynamically find & add all pages (controllers)
		 * This is done by directory listing... a bit of a hack,
		 *  but good enough.
		 *
		 * This assumes the only files in controllers that aren't
		 * pages are the CVS directories, and that directories are only
		 * one level deep.
		 */
		System.out.println("Inserting controller data");

		Session session = HibernateUtil.currentSession();

		// Clear the perms table first.
		Iterator pages = session.createQuery("FROM PageModel").list().iterator();

		while (pages.hasNext())
		{
			((PageModel)pages.next()).clearGroups();
		}

		session.flush();
		session.createQuery("DELETE PageModel").executeUpdate();
		session.flush();
		session.clear();

		// And re-populate it
		File f = new File("WEB-INF/src/ca/myewb/controllers");
		String[] dirs = f.list();

		System.out.println("Using controllers directory " + f.getAbsolutePath());

		for (String pkgName: dirs)
		{
			if (!pkgName.equals(".svn") && !pkgName.equals("common"))
			{
				String[] files = new File("WEB-INF/src/ca/myewb/controllers/" + pkgName).list();
				for (String fileName: files)
				{
					if (!fileName.substring(0, 1).equals("."))
					{
						File theFile = new File("WEB-INF/src/ca/myewb/controllers/" + pkgName +"/"+fileName);
						if(!theFile.isDirectory())
						{
							fileName = fileName.substring(0, fileName.length() - 5);
							putPageInDatabase(session, pkgName, fileName);
						}
						else
						{
							String[] files2 = new File("WEB-INF/src/ca/myewb/controllers/" + pkgName + "/" + fileName).list();
							for (String fileName2: files2)
							{
								if (!fileName2.substring(0, 1).equals("."))
								{
									fileName2 = fileName2.substring(0, fileName2.length() - 5);
									putPageInDatabase(session, pkgName, fileName + "." + fileName2);
								}
							}
						}
					}
				}
			}
		}
	}

	private static void putPageInDatabase(Session session, String thePackage, String file) throws InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		Controller theController = (Controller)Class.forName("ca.myewb.controllers."
		                                                               + thePackage
		                                                               + "."
		                                                               + file)
		                                .newInstance();

		PageModel p = PageModel.newPage();
		p.setName(file);
		p.setOldName(theController.oldName());
		p.setArea(thePackage);
		p.setDisplayName(theController.displayName());
		p.setWeight(theController.weight());
		session.save(p);

		Set groups = theController.defaultGroups();
		Iterator it;

		if (groups != null)
		{
			it = groups.iterator();

			boolean shouldAddAdmin = false;
			boolean shouldAddNMT = false;

			while (it.hasNext())
			{
				String s = (String)it.next();

				if (!s.equals("Guest") && !s.equals("Admin"))
				{
					shouldAddAdmin = true;
				}

				if (!s.equals("Guest") && !s.equals("NMT"))
				{
					shouldAddNMT = true;
				}

				List r = session.createQuery("FROM GroupModel WHERE shortname=?")
				         .setString(0, s).list();
				GroupModel g = (GroupModel)r.get(0);
				g.addPage(p);
				session.flush();
			}

			if (shouldAddAdmin)
			{
				List r = session.createQuery("FROM GroupModel WHERE shortname=?")
				         .setString(0, "Admin").list();
				GroupModel g = (GroupModel)r.get(0);
				g.addPage(p);
				session.flush();
			}

			if (shouldAddNMT)
			{
				List r = session.createQuery("FROM GroupModel WHERE shortname=?")
				         .setString(0, "NMT").list();
				GroupModel g = (GroupModel)r.get(0);
				g.addPage(p);
				session.flush();
			}
		}

		groups = theController.invisibleGroups();

		if (groups != null)
		{
			it = groups.iterator();

			boolean shouldAddAdmin = false;
			boolean shouldAddNMT = false;

			while (it.hasNext())
			{
				String s = (String)it.next();

				if (!s.equals("Guest") && !s.equals("Admin"))
				{
					shouldAddAdmin = true;
				}

				if (!s.equals("Guest") && !s.equals("NMT"))
				{
					shouldAddNMT = true;
				}

				List r = session.createQuery("FROM GroupModel WHERE shortname=?")
				         .setString(0, s).list();
				GroupModel g = (GroupModel)r.get(0);
				g.addInvisiblePage(p);
				session.flush();
			}

			if (shouldAddAdmin)
			{
				List r = session.createQuery("FROM GroupModel WHERE shortname=?")
				         .setString(0, "Admin").list();
				GroupModel g = (GroupModel)r.get(0);
				g.addInvisiblePage(p);
				session.flush();
			}

			if (shouldAddNMT)
			{
				List r = session.createQuery("FROM GroupModel WHERE shortname=?")
				         .setString(0, "NMT").list();
				GroupModel g = (GroupModel)r.get(0);
				g.addInvisiblePage(p);
				session.flush();
			}
		}
	}
}
