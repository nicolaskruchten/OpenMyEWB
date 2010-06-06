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
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.model.GroupChapterModel;


public class Dashboard extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupChapterModel chapter = currentUser.getChapter();
		if(chapter == null && urlParams.getParam() != null)
		{
			//only admins can get here, and only after choosing a chapter from the pulldown
			chapter = (GroupChapterModel) getAndCheckFromUrl(GroupChapterModel.class);
		}
		
		if(chapter != null)
		{
			ctx.put("chapter", chapter);
		}
		
		if(currentUser.isAdmin())
		{
			//only admins can get here, when they first load the page
			ctx.put("chapterList", GroupChapterModel.getChapters());
		}
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}

	public String displayName()
	{
		return "Graphs";
	}

	public int weight()
	{
		return -102;
	}
}
