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
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.PostList;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.toolbars.DisplaySettingsP;
import ca.myewb.frame.toolbars.Keywords;
import ca.myewb.frame.toolbars.Online;
import ca.myewb.frame.toolbars.Toolbar;


public class ChapterPosts extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if ((currentUser.getChapter() == null))
		{
			throw new RedirectionException(path + "/chapter/ListChapters");
		}
		
		(new PostList(httpSession, hibernateSession, requestParams, urlParams,
		              currentUser)).list(ctx, "chapterposts", 15);
		
		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		toolbars.add(new DisplaySettingsP(httpSession, ctx, currentUser, true, true, false));
		toolbars.add(new ca.myewb.frame.toolbars.Search());
		toolbars.add(new Keywords(currentUser, true, ctx));
		Online whosOnline = new Online(httpSession);
		whosOnline.setUpCtx(ctx);
		toolbars.add(whosOnline);
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
		return "Chapter Board";
	}

	public int weight()
	{
		return 5;
	}
}
