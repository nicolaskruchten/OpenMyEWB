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

package ca.myewb.controllers.events;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.EventList;
import ca.myewb.frame.Controller;
import ca.myewb.frame.toolbars.DisplaySettingsE;
import ca.myewb.frame.toolbars.Keywords;
import ca.myewb.frame.toolbars.Toolbar;

public class ListUpcoming extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		new EventList(httpSession, hibernateSession, requestParams, urlParams,
				currentUser).list(ctx, "upcoming", 15);
		
		urlParams.processParams(new String[]{"filter", "pagenum"},
                new String[]{"Any", "0"});
		
		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		toolbars.add(new DisplaySettingsE(ctx));
		toolbars.add(new Keywords(currentUser, false, ctx));
		ctx.put("toolbars", toolbars);
		ctx.put("filter", urlParams.get("filter"));
		ctx.put("currentPage", "listUpcoming");
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Upcoming Events";
	}

	public int weight()
	{
		return -20;
	}
}
