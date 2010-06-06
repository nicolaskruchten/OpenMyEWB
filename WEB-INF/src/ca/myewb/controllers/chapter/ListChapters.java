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
import ca.myewb.frame.toolbars.ChapterList;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupChapterModel;


public class ListChapters extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		List<GroupChapterModel> chapters = GroupChapterModel.getChapters();
		
		ctx.put("chapters", chapters);

		if (currentUser.getChapter() == null)
		{
			ctx.put("nochapter", new Boolean(true));
		}

		if (currentUser.getUsername().equals("guest"))
		{
			ctx.put("isguest", new Boolean(true));
		}
		
		
		if (currentUser.isAdmin())
		{
			Vector<Toolbar> toolbars = new Vector<Toolbar>();
			ChapterList theToolbar = new ChapterList();
			toolbars.add(theToolbar);
			ctx.put("toolbars", toolbars);
		}
		
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Chapter List";
	}

	public int weight()
	{
		return 5;
	}
}
