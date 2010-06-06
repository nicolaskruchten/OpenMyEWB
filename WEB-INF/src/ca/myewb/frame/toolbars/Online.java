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

package ca.myewb.frame.toolbars;

import java.util.Hashtable;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;


public class Online extends Toolbar
{
	HttpSession session;

	public Online(HttpSession s) throws Exception
	{
		super();
		this.title = "Recently Online";
		this.template = "frame/toolbars/online.vm";
		this.session = s;
	}

	public void setUpCtx(Context ctx) throws Exception
	{
		// Get global user and session lists
		
		// A more efficient way might be to create a container object
		// with a user and a date, and maintain that in one userList, instead
		// of having two Hashtables (userList and userTime)
		Hashtable userList = (Hashtable)this.session.getServletContext()
		                     .getAttribute("userList");
		Set sessions = (Set)this.session.getServletContext()
		               .getAttribute("sessionList");
		
		// Put it all in the context
		ctx.put("userList", userList.keySet());
		ctx.put("userTable", userList);

		// Calculate number of guests
		if ((sessions.size() - userList.size()) < 0)
		{
			ctx.put("numGuests", 0);
		}
		else
		{
			ctx.put("numGuests", sessions.size() - userList.size());
		}
	}
}
