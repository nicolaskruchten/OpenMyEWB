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

import java.util.Iterator;
import java.util.List;


import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.logic.GroupLogic;

public class GroupModel extends GroupLogic
{
	//this method is prtected due to the weird inheritence heiracrhy we have going between Group and Chapter
	//The design should be fixed so this can be package access
	protected GroupModel() throws Exception
	{
		super();
	}

	public static GroupModel newGroup(GroupChapterModel parent) throws Exception
	{
		GroupModel list = new GroupModel();
		HibernateUtil.currentSession().save(list);
		list.setWhiteboard(WhiteboardModel.newWhiteboard(null, null, list));

		if (parent != null)
		{
			parent.addChild(list);
		}
		return list;
	}
	
	public static GroupModel newGroup() throws Exception
	{
		return newGroup(null);
	}

	public void save(String name, String shortName, String desc, boolean isPublic)
	{
		setName(name);
		setShortname(shortName);
		setPostName("Anyone on the [" + getTotalShortname()
		                 + "] list");
		
		setDescription(desc);
		
		setPublic(isPublic);
		setAdmin(false);

		session.flush();
	}

	public void delete() throws Exception
	{
		Iterator users = session.createQuery(
				"SELECT r.user FROM RoleModel r "
						+ "WHERE r.group=? AND r.end IS NULL").setEntity(0,
				this).list().iterator();

		while (users.hasNext())
		{
			((UserModel)users.next()).remGroup(this);
		}

		// Set list as invisible & un-joinable
		setVisible(false);
	}

	public List<String> getMemberEmails()
	{
		return new SafeHibList<String>(HibernateUtil.currentSession().createSQLQuery("SELECT DISTINCT u.email FROM users u, roles r, groups g WHERE u.id = r.userid AND r.end IS NULL AND r.groupid = :gid").setInteger("gid", getId())).list();
	}

}
