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

package ca.myewb.frame;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import ca.myewb.logic.GroupLogic;
import ca.myewb.logic.PageLogic;
import ca.myewb.logic.UserLogic;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PageModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.UserModel;
import ca.myewb.model.WhiteboardModel;

public class Permissions 
{
	
	// Constructor so that Permissions can be added to Helpers Static Class
	public Permissions()
	{
		
	}
	
	
	public static boolean canManageSubdomainEmails(UserModel user, UserModel targetUser)
	{
		if(!targetUser.isMember("Chapter"))
		{
			return false; //subdomain is user's chapter
		}
		
		if(user.isAdmin())
		{
			return true; //of course
		}
		
		if(user.isMember("Exec"))
		{
			//must be in the same chapter
			return targetUser.getChapter().equals(user.getChapter());
		}
		
		return false;
	}
	
	/***	GROUPS		***/
	// View list details such as size, description, etc. and 
	public static boolean canReadGroupInfo( UserModel u, GroupModel g )
	{
		return postsCanBeSeenBy(u,g);
	}
	
	// Update group names, descriptions, etc. and contact info for chapters
	public static boolean canUpdateGroupInfo( UserModel u, GroupModel g )
	{
		return canControlGroup(u, g) && g.getVisible() && !g.isExecList() && !g.getAdmin();
	}

	// View list membership spreadsheets and update member status (member/leader/sender and regular/associate)
	public static boolean canAdministerGroupMembership( UserModel u, GroupModel g )
	{
		return canControlGroup(u, g) && g.getVisible() && !g.getAdmin();
	}
	
	// Delete chapters or groups
	public static boolean canDeleteGroup( UserModel u, GroupModel g )
	{
		return canControlGroup(u, g) && !g.isChapter() && !g.isExecList() && !g.getAdmin() && g.getVisible();
	}
	
	/***	USERS		***/
	// View contact info, work/student info, etc.
	public static boolean canReadPersonalDetails( UserModel u, UserModel target )
	{
		return canControlUser(u, target);
	}
	
	// Upgrade/downgrade user, Edit exec/NMT titles (as long as 'u' has permission to change exec/NMT titles) 
	public static boolean canUpdateUserStatus( UserModel u, UserModel target )
	{
		return canControlUser(u, target);
	}
		
	/***	POSTS IN A GROUP		***/
	// Create new posts for a group
	public static boolean canPostToGroup( UserModel u, GroupModel g )
	{
		return (u.isAdmin() && g.getVisible()) || postGroups(u).contains(g);
	}
	
	// Send emails to a group
	public static boolean canSendEmailToGroup( UserModel u, GroupModel g )
	{
		return (canControlGroup(u, g) || u.isSender(g)) && g.getVisible() && g.getId() != 1;
	}
	
	// Read the posts in a group
	public static boolean canReadPostsInGroup( UserModel u, GroupModel g )
	{
		return postsCanBeSeenBy(u, g);
	}
	
	// Guests can read posts in a group
	public static boolean guestsCanReadPostsInGroup(GroupModel g)
	{
		return canReadPostsInGroup((UserModel)HibernateUtil.currentSession().load(UserModel.class, 1), g);
	}
	
	/***	POSTS		***/
	// Reply to a post
	public static boolean canReplyToPost( UserModel u, PostModel p )
	{
		return postsCanBeSeenBy(u, p.getGroup()) && p.getGroup().getVisible();
	}
	
	// Read a post
	public static boolean canReadPost( UserModel u, PostModel p )
	{
		return postsCanBeSeenBy(u, p.getGroup());
	}
	
	// Delete a post
	public static boolean canDeletePost( UserModel u, PostModel p )
	{
		return canControlGroup(u,p.getGroup());
	}
	
	/***	EVENTS		***/
	// Update an Event
	public static boolean canUpdateEvent( UserModel u, EventModel e )
	{
		return canSendToGroup(u, e.getGroup());
	}
	
	// See an event in the calendar and look at its details
	public static boolean canReadEvent( UserModel u, EventModel e )
	{
		return postsCanBeSeenBy(u, e.getGroup());
	}
	
	/***	EVENTS IN A GROUP		***/
	// Create new posts for a group
	public static boolean canUpdateEventInGroup( UserModel u, GroupModel g )
	{
		return canSendToGroup(u, g);
	}
	
	/***	WHITEBOARDS		***/
	// Update a whiteboard
	public static boolean canUpdateWhiteboard( UserModel u, WhiteboardModel w )
	{
		if( w.getParentEvent() != null )
		{
			return w.getParentEvent().getGroup().getVisible()
				&& postsCanBeSeenBy(u, w.getParentEvent().getGroup());
		}
		else if( w.getParentPost() != null )
		{
			return w.getParentPost().getGroup().getVisible()
				&& postsCanBeSeenBy(u, w.getParentPost().getGroup());
		}
		else
		{
			return postsCanBeSeenBy(u, w.getParentGroup()) && w.getParentGroup().getVisible();
		}
	}
	
	/***	GROUPFILES		***/
	// Add/Delete files/folders in group file shares
	public static boolean canManageFilesInGroup( UserModel u, GroupModel g )
	{
		return canControlGroup(u, g) || u.isSender(g);
	}
	
	// View files/folders in group file shares
	public static boolean canReadFilesInGroup( UserModel u, GroupModel g )
	{
		return postsCanBeSeenBy(u,g);
	}



	
	
	////FUNCTIONS FROM OTHER PLACES
	//GROUPLOGIC
	// See posts from group
	public static boolean postsCanBeSeenBy(UserLogic u, GroupLogic g) 
	{
		//must at least return true if uLogic.visibleGroups(false).contains(this)
		//unless that group is an invisible admin group like us
		//admins also have special viewing powers here that are not reflected in visibleGroups
		
		if (!g.getVisible() &&  g.getAdmin())
		{
			return false; // no one can see invisible admin groups
		}
		
		if ((g.getId() == 1) || g.getPublic() || u.isAdmin())
		{
			return true;
		}

		if(u.getId() != 1) //shortcut for guest
		{
			if((u.isMember(g) || u.isRecipient(g)))
			{
				return true; //you're actually in the group
			}
			
			if ((g.getParent() != null) && u.isLeader(g.getParent()))
			{
				return true; //execs can see all private groups in their chapter
			}
			
			if(g.getAdmin() && u.isMember("Exec"))
			{
				return true; //execs can see all exec groups
			}
		}
	
		return false;
	}

	// Leader of group or Parent
	public static boolean canControlGroup(UserLogic u, GroupLogic g) {
		//isLeader will cover explicit leadership and implicit admin-ship
		//if getParent==null, we fall back on admin-ship, covered by previous call
		//if getParent is NOT null, then leaders of the parent chapter can get in
		return (u.isLeader(g) || ((g.getParent() != null) && u.isLeader(g.getParent())));
	}
	
	
	
	
	//USERLOGIC
	// Leader of the chapter that targetUser is in, targetUser is not admin (or admin if adminOverride is true)
	private static boolean canControlUser(UserLogic u, UserLogic targetUser)
	{
		
		// if admin && override return true
		if (u.isAdmin())
		{
			return true;
		}
		else
		{
			// else check that we're in the same chapter and that we're exec and
			// they're not admin
			GroupChapterModel chapter = u.getChapter();
			GroupChapterModel chapter2 = targetUser.getChapter();

			if ((!targetUser.isAdmin()) && (chapter2 != null)
					&& (chapter != null) && chapter2.equals(chapter)
					&& (u.isLeader(chapter)))
			{
				return true;
			}
			else
			{
				Logger.getLogger(Permissions.class.getName()).info(u.getUsername() + " tried to view info of "
						+ targetUser.getUsername() + " with bad privileges!");

				return false;
			}
		}
	}
	
	public static Set<GroupModel> visibleGroups(UserLogic u, boolean excludeChapters)
	throws HibernateException
	{
		return visibleGroups(u, excludeChapters, true);
	}
	// Groups that you can see their posts
	public static Set<GroupModel> visibleGroups(UserLogic u, boolean excludeChapters, boolean honourExecToggle)
			throws HibernateException
	{		
		//GroupLogic.postsCanBeSeenBy(user) must return true for all
		//groups in the result!
		
		Set<GroupModel> theGroups = new HashSet<GroupModel>();

		if (u.getUsername().equals("guest"))
		{
			theGroups.add(Helpers.getGroup("Org"));
		}
		else
		{
			theGroups.addAll(u.getGroups());
		}

		if (!excludeChapters)
		{
			theGroups.addAll(GroupChapterModel.getChapters());
		}

		if (!u.getUsername().equals("guest"))
		{
			boolean isExec = u.isMember("Exec");
			
			GroupChapterModel myChapter = u.getChapter();
			if (myChapter != null)
			{
				theGroups.addAll(myChapter.getChildGroups(true, isExec));
			}
			
			if(isExec)
			{
				theGroups.add(Helpers.getGroup("Exec"));
				theGroups.add(Helpers.getGroup("ProChaptersExec"));
				theGroups.add(Helpers.getGroup("UniChaptersExec"));
				if(!honourExecToggle || u.isAdmin() || u.getAdminToggle())
				{
					theGroups.addAll(Helpers.getNationalRepLists(true, true));
				}
			}
		}

		return theGroups;
		
	}

	// Groups that you can see their posts in your chapter
	public static List<GroupModel> visibleGroupsInChapter(UserLogic u, GroupChapterModel chapter)
	{
		List<GroupModel> chapterGroups = chapter.getChildGroups(true, false);

		List<GroupModel> chapterPrivateGroups = chapter.getChildGroups(false, true);

		if (!u.isLeader(chapter))
		{
			// if not exec, only return private groups you're part of
			chapterPrivateGroups.retainAll(u.getGroups());
		}

		chapterGroups.addAll(chapterPrivateGroups);
		chapterGroups.add(chapter);
		return chapterGroups;
	}

	// Groups that you can post to
	public static List<GroupModel> postGroups(UserLogic u)
	{
		Session session = HibernateUtil.currentSession();
		
		if (u.isAdmin())
		{
		        return (new SafeHibList<GroupModel>(session.createQuery(
		                        "FROM GroupModel g WHERE g.visible=true order by admin desc, " +
	                        "chapter desc, execList desc, parent.id asc, public desc"))).list();
		}
		else
		{
			Query results = session
			.createQuery(
					"SELECT r.group FROM RoleModel r "
					+ "WHERE r.user=? AND r.level!='r' "
					+ "AND r.group.visible=true "
					+ "AND r.end IS NULL"
					).setEntity(0, u);
	
			List<GroupModel> theGroups = (new SafeHibList<GroupModel>(results)).list();
			
			boolean isExec = u.isMember("Exec");
			
			GroupChapterModel chapter = u.getChapter();
			if ((chapter != null) && isExec)
			{
				List<GroupModel> chapterGroups = chapter.getChildGroups(true, true);
				chapterGroups.removeAll(theGroups);
				theGroups.addAll(chapterGroups);
			}
			
			if(isExec)
			{
				List<GroupModel> repGroups = Helpers.getNationalRepLists(true, true);
				repGroups.removeAll(theGroups);
				theGroups.addAll(repGroups);
			}
		
			return theGroups;
		}
	}
	
	public static List<GroupModel> visibleGroupsWithRoles(UserLogic u)
	{
		Session session = HibernateUtil.currentSession();
		String query = "select distinct r.group from RoleModel as r where r.user=:user " +
				"AND r.end IS NULL AND r.group.visible=true order by r.group.admin desc, " +
	                        "r.group.chapter desc, r.group.execList desc, r.group.parent.id asc, r.group.public desc";
		return (new SafeHibList<GroupModel>(session.createQuery(query)
		.setEntity("user", u)).list());
	}

	// Groups that you can send e-mails to
	public static List<GroupModel> sendGroups(UserLogic u)
	{
		Session session = HibernateUtil.currentSession();
		
		if (u.isAdmin())
		{

	        return (new SafeHibList<GroupModel>(session.createQuery(
	                        "FROM GroupModel g WHERE g.visible=true and g.id!=1 order by admin desc, " +
	                        "chapter desc, execList desc, parent.id asc, public desc"))).list();
		}
		else
		{
			String query = "select distinct g from GroupModel as g, RoleModel as r " +
					"where r.user=:user " +
					"and ((r.group=g and (r.level='l' or r.level='s')) " +
					"or (r.group=g.parent and r.level='l')) AND r.end IS NULL AND g.visible=true";
			return (new SafeHibList<GroupModel>(session.createQuery(query)
					.setEntity("user", u)).list());
		}
	}
	
	public static List<GroupModel> visibleDeletedGroups(UserLogic u)
	{
		Session session = HibernateUtil.currentSession();
		if (u.isAdmin())
		{

	        return (new SafeHibList<GroupModel>(session.createQuery(
	                        "FROM GroupModel g WHERE g.visible=false and g.admin=false order by " +
	                        "parent.id asc, public desc"))).list();
		}
		else
		{
			//old general public groups
			List<GroupModel> visibleGroups = (new SafeHibList<GroupModel>(session.createQuery(
					"from GroupModel as g where g.admin=false and g.visible=false and g.public=true " +
					"and g.parent IS NULL")))
					.list();
	
			//old chapter groups, public (and private if exec)
			GroupChapterModel myChapter = u.getChapter();
			if (myChapter != null)
			{
				visibleGroups.addAll(myChapter.getChildGroups(true, u.isMember("Exec"), false));
			}
				
			return visibleGroups;
		}
	}

	public static boolean canSendToGroup(UserLogic u, GroupLogic g)
	{
		//must at least return true if uLogic.visibleGroups(false).contains(this)
		//unless that group is an invisible admin group like us
		//admins also have special viewing powers here that are not reflected in visibleGroups
		
		if (!g.getVisible() || g.getId() == 1 || u.getId() == 1)
		{
			return false; //admins can see almost all groups
		}
		
		if ( u.isAdmin() || u.isSender(g) || u.isLeader(g) )
		{
			return true;
		}
		
		if ((g.getParent() != null) && u.isLeader(g.getParent()))
		{
			return true; //execs can see all private groups in their chapter
		}
		
		if(g.getAdmin() && u.isMember("Exec"))
		{
			return true; //execs can see all exec groups
		}
	
		return false;
	}
	
	// Load a page onto the screen
	public static boolean canViewPage(UserLogic u, PageLogic p) throws HibernateException
	{		
		return !(HibernateUtil.currentSession()
				.createQuery(
						"from RoleModel as role where role.user=? and (? in elements(role.group.pages) OR ? in elements(role.group.invisiblePages)) AND role.end IS NULL")
				.setEntity(0, u).setEntity(1, p).setEntity(2, p).list()
				.isEmpty());
	}
	
	// Pages that a user can see
	public static List<PageModel> visiblePages(UserLogic u, String area) throws HibernateException
	{
		LinkedHashSet<PageModel> hs = new LinkedHashSet<PageModel>();
		hs
				.addAll((new SafeHibList<PageModel>(
						HibernateUtil.currentSession()
								.createQuery(
										"select p from PageModel as p, RoleModel as r where r.user=? and p in elements(r.group.pages) and p.area=? AND r.end IS NULL ORDER BY p.weight DESC, p.name")
								.setEntity(0, u).setString(1, area))).list());

		List<PageModel> v = new Vector<PageModel>();

		for (PageModel p : hs)
		{
			v.add(p);
		}

		return v;
	}

}
