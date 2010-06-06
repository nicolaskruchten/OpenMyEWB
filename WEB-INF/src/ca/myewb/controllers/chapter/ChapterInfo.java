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

package ca.myewb.controllers.chapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.toolbars.ChapterControl;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupChapterModel;


public class ChapterInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM chapterid, not required
		GroupChapterModel chapter;

		if (urlParams.getParam() != null)
		{
			// If you are requesting a certain group via param, then we show it
			chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);
		}
		else
		{
			// Otherwise we load up info about your chapter
			chapter = currentUser.getChapter();
		}

		// Not in a chapter...
		if ((chapter == null) || (!chapter.getVisible()))
		{
			throw new RedirectionException(path + "/chapter/ListChapters");
		}

		// Leaders get an edit chapter link
		if (currentUser.isLeader(chapter))
		{
			ctx.put("isLeader", path + "/chapter/EditChapter/"
			        + chapter.getId());
		}

		ctx.put("chapter", chapter);

		// And the exec list
		List execs = hibernateSession.createQuery("SELECT u FROM UserModel u, RoleModel r "
		                                          + "WHERE r.user=u AND r.group=? AND r.level='l' AND r.end IS NULL")
		             .setEntity(0, chapter).list();

		ctx.put("execs", execs);
		ctx.put("lists", Permissions.visibleGroupsInChapter(currentUser, chapter));


		Vector<Toolbar> toolbars = new Vector<Toolbar>();

		if (currentUser.getUsername().equals("guest"))
		{
			ctx.put("isguest", new Boolean(true));
		}
		else if (currentUser.getChapter() == null)
		{
			ctx.put("nochapter", new Boolean(true));

			ChapterControl theToolbar = new ChapterControl();
			toolbars.add(theToolbar);
		}
		else if (currentUser.getChapter().equals(chapter))
		{
			ctx.put("thischapter", new Boolean(true));

			ChapterControl theToolbar = new ChapterControl();
			toolbars.add(theToolbar);
		}
		else if (currentUser.isAdmin())
		{
			ChapterControl theToolbar = new ChapterControl();
			toolbars.add(theToolbar);
		}

		if (currentUser.isAdmin())
		{
			ctx.put("isAdmin", "yes");
		}

		ctx.put("toolbars", toolbars);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Chapter");

		return s;
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Chapter Info";
	}

	public int weight()
	{
		return 10;
	}
}
