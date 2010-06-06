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

import org.hibernate.Session;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.logic.GroupChapterLogic;

public class GroupChapterModel extends GroupChapterLogic
{
	public static GroupChapterModel newChapter()
			throws Exception
	{
		Session hibernateSession = HibernateUtil.currentSession();
		GroupModel exec = new GroupModel();
		exec.setPublic(false);
		exec.setShortname("exec");
		exec.setExecList(true);
		hibernateSession.save(exec);
		GroupChapterModel chapter = new GroupChapterModel(exec);
		exec.setParent(chapter);
		chapter.setChapter(true);
		
		chapter.setWhiteboard(WhiteboardModel.newWhiteboard(null, null, chapter));
		exec.setWhiteboard(WhiteboardModel.newWhiteboard(null, null, exec));
		
		hibernateSession.save(chapter);
		return chapter;
	}

	public void delete() throws Exception
	{
		Iterator subgroups = session.createQuery(
				"SELECT g FROM GroupModel g " + "WHERE g.parent=?").setEntity(
				0, this).list().iterator();

		while (subgroups.hasNext())
		{
			((GroupModel)subgroups.next()).delete();
		}

		Iterator users = session.createQuery(
				"SELECT r.user FROM RoleModel r "
						+ "WHERE r.group=? AND r.end IS NULL").setEntity(0,
				this).list().iterator();


		while (users.hasNext())
		{
			UserModel theUser = ((UserModel)users.next());

			if (theUser.isMember(this))
			{
				theUser.leaveChapter(this);
			}
			else
			{
				theUser.remGroup(this);
			}

		}

		// Set list as invisible & un-joinable
		setPublic(false);
		setVisible(false);
	}

	// Hiberate only please
	GroupChapterModel() throws Exception
	{
		super();
	}

	GroupChapterModel(GroupModel exec) throws Exception
	{
		super(exec);
	}

	public void save(String name, String shortName, String address,
			String phone, String fax, String email, String url, boolean franco, boolean pro)
	{
		setName(name);
		setPostName("Anyone in the " + name);
		setDescription("The " + name + " announcement list.");

		GroupModel exec = getExec();

		exec.setName(name + " Exec");
		exec.setPostName("Any exec in the " + name);
		exec.setDescription("The " + name + " Exec announcement list.");

		setShortname(shortName);

		setAddress(address);
		setEmail(email);
		setPhone(phone);
		setUrl(url);
		setFax(fax);
		setFrancophone(franco);
		setProfessional(pro);

		session.flush();
	}

	public static List<GroupChapterModel> getChapters() 
	{
		return new SafeHibList<GroupChapterModel>(HibernateUtil.currentSession().createQuery("FROM GroupChapterModel WHERE visible=true ORDER BY name")).list();
	}

	
}
