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

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.myewb.beans.Group;
import ca.myewb.beans.GroupChapter;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PageModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.RoleModel;
import ca.myewb.model.WhiteboardModel;

public abstract class GroupLogic extends Group {

	public GroupLogic() throws Exception {
		super();
	}
	
	public boolean isNationalRepGroup()
	{
		return nationalRepType=='b' || nationalRepType=='s' || nationalRepType=='p';
	}

	public void addRole(RoleLogic r) {
		Logger.getLogger(this.getClass()).debug("adding role to group...");
		roles.add((RoleModel)r);
		Logger.getLogger(this.getClass()).debug("role added to group!");
		r.setGroup(this);
	}

	public void remRole(RoleLogic r) {
		roles.remove(r);
	}

	public void addPage(PageLogic p) {
		pages.add((PageModel)p);
		p.addGroup(this);
	}

	public void addInvisiblePage(PageLogic p) {
		invisiblePages.add((PageModel)p);
		p.addInvisibleGroup(this);
	}

	public void addPost(PostLogic p) {
		posts.add((PostModel)p);
		p.setGroup(this);
	}

	private void remPost(PostLogic p) {
		posts.remove(p);
	}

	public GroupChapterModel chapterIfExec() {
		return (GroupChapterModel)session.createQuery("select c from GroupChapterModel as c where c.exec=?")
		                     .setEntity(0, this).list().get(0);
	}

	public List<GroupModel> getChildGroups(boolean showPublic, boolean showPrivate) 
	{
		return getChildGroups(showPublic, showPrivate, true);	
	}
	
	public List<GroupModel> getChildGroups(boolean showPublic, boolean showPrivate, boolean visible) {
		
		if(!showPublic && !showPrivate)
		{
			throw new IllegalStateException("no groups would be returned!");
		}
		
		String query = "select g from GroupModel as g where g.parent=? and g.admin=false";
		
		if(visible)
		{
			query += " and g.visible=true";
		}
		else
		{
			query += " and g.visible=false";
		}
		
		if(!showPublic)
		{
			query += " and g.public!=true";
		}
		
		if(!showPrivate)
		{
			query += " and g.public!=false";
		}
		
		return (new SafeHibList<GroupModel>(session.createQuery(query).setEntity(0, this))).list();
	}

	public boolean equals(GroupLogic g) {
		if((parent == null) && (g.getParent() == null))
		{
			return (name.equals(g.getName()))
		       && (description.equals(g.getDescription()))
		       && shortname.equals(g.getShortname());
		}
		else if((parent == null) || (g.getParent() == null))
		{
			return false;
		}
		else if(parent.equals(g.getParent()))
		{
			return (name.equals(g.getName()))
		       && (description.equals(g.getDescription()))
		       && shortname.equals(g.getShortname());
		}
		else
		{
			return false;
		}
	}

	public String getTotalShortname() {
	
		if (id == 1)
		{
			return Helpers.getEnShortName();
		}

		String prefix = Helpers.getEnShortName();
		if (parent != null)
		{
			if(parent.isFrancophone())
			{
				prefix = Helpers.getFrShortName();
			}
			
			return prefix + "-" + parent.getShortname().toLowerCase() + "-" + shortname.toLowerCase();
		}
		else
		{
			if(this.isChapter() && ((GroupChapter)this).isFrancophone())
			{
				prefix = Helpers.getFrShortName();
			}
			return prefix + "-" + shortname.toLowerCase();
		}
	
	}

	public boolean isMailingList() {
		return ((!admin) && (!isExecList()) && (!isChapter()));
	}

	public int getNumLeaders() {
		return getNumRolesOfLevel('l');
	}

	public int getNumMembers() {
		return getNumRolesOfLevel('m');
	}

	public int getNumNonMembers() {
		return getNumRolesOfLevel('r') + getNumRolesOfLevel('s')
		       + getNumRolesOfLevel('l');
	}

	public int getNumRecipients() {
		return getNumRolesOfLevel('r');
	}

	public int getNumSenders() {
		return getNumRolesOfLevel('s');
	}
	
	private int getNumRolesOfLevel(char level) {
		String query = "SELECT count(*) FROM RoleModel r WHERE r.group=:group and r.end is null and r.level=:level";
	
		return ((Long)session.createQuery(query).setEntity("group", this)
		                .setCharacter("level", level).uniqueResult()).intValue();
	}

	public void addEvent(EventModel event)
	{
		events.add(event);
	}
	
	public String getFullWelcomeEmail()
	{
		return "[ewb] Welcome to the " + getName() + " mailing list!\n\n" + getWelcomeMessage();
	}
	
	public Date getLastPostOrResponseDate()
	{
		return (Date)(HibernateUtil.currentSession().createQuery(
					"SELECT MAX(p.lastReply) " +
					"FROM PostModel AS p " +
					"WHERE p.group = :group "
				).setEntity("group", this).uniqueResult());
	}

	public WhiteboardModel getWhiteboard() 
	{
		if(selfWhiteboards.size() == 0)
			return null;
		
		return selfWhiteboards.iterator().next();
	}

	public void setWhiteboard(WhiteboardModel whiteboard) 
	{
		if(selfWhiteboards.size() != 0)
			selfWhiteboards.remove(selfWhiteboards.iterator().next());
		
		selfWhiteboards.add(whiteboard);
	}
	
}
