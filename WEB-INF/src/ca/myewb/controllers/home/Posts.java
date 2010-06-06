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

package ca.myewb.controllers.home;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.PostList;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.toolbars.AtAGlance;
import ca.myewb.frame.toolbars.DisplaySettingsP;
import ca.myewb.frame.toolbars.Keywords;
import ca.myewb.frame.toolbars.Online;
import ca.myewb.frame.toolbars.SignInToolbar;
import ca.myewb.frame.toolbars.Syndication;
import ca.myewb.frame.toolbars.Toolbar;


public class Posts extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if(urlParams.toString().contains("Any plus Replies"))
		{
			throw new RedirectionException(path+"/actions/SetDisplayMode/yes/yes/home/Posts/Any");
		}
		
		if(urlParams.toString().contains("Any minus Emails"))
		{
			throw new RedirectionException(path+"/actions/SetDisplayMode/no/no/home/Posts/Any");
		}

		ctx.put("postStart", System.currentTimeMillis());
		PostList postList = (new PostList(httpSession, hibernateSession, requestParams, urlParams,
		              currentUser));
		
		//next three lines normally taken care of by PostList.list()
		urlParams.processParams(new String[]{"filter", "pagenum"},
		                        new String[]{"Any", "1"});
		
		
		ctx.put("filterParam", urlParams.get("filter"));
		ctx.put("pagenum", urlParams.get("pagenum"));
		
		ctx.put("afterList", System.currentTimeMillis());

		//Generate Featured Posts list for Sidebar
		ctx.put("featuredPosts", postList.visiblePosts(null, 0, 10, false, false, false, true, true, false));

		
		ctx.put("afterPostBars", System.currentTimeMillis());
		

		// Also add all toolbars used by this page
		Vector<Toolbar> toolbars = new Vector<Toolbar>();

		
		//Calendar Sidebar and At-a-Glance
		if(!currentUser.getUsername().equals("guest") && (currentUser.getLastLogin() != null))
		{
			toolbars.add(new AtAGlance());
		}
		else if(currentUser.getUsername().equals("guest"))
		{
			toolbars.add(new SignInToolbar());
		}

		ctx.put("afterGlance", System.currentTimeMillis());
		if (!currentUser.getUsername().equals("guest"))
		{
			toolbars.add(new DisplaySettingsP(httpSession, ctx, currentUser, false, false, true));
		}
		
		ctx.put("afterCats", System.currentTimeMillis());
		toolbars.add(new Keywords(currentUser, true, ctx));
		toolbars.add(new Syndication());
		ctx.put("afterKey", System.currentTimeMillis());
		Online whosOnline = new Online(httpSession);
		whosOnline.setUpCtx(ctx);
		toolbars.add(whosOnline);

		ctx.put("afterOtherBars", System.currentTimeMillis());
		ctx.put("toolbars", toolbars);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Discussion Board";
	}

	public int weight()
	{
		return 100;
	}
}
