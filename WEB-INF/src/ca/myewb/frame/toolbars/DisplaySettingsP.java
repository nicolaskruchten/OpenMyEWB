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

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;

import ca.myewb.model.UserModel;


public class DisplaySettingsP extends Toolbar
{
	private HttpSession httpSession;

	public DisplaySettingsP(HttpSession httpSession, Context ctx, UserModel user, 
			boolean emailOption, boolean replyOption, boolean lastReplyOption) throws Exception
	{
		super();
		this.title = "Display Settings";
		this.template = "frame/toolbars/displaysettingsp.vm";

		ctx.put("emailOption", emailOption ? "yes":"no");
		ctx.put("replyOption", replyOption ? "yes":"no");
		ctx.put("lastReplyOption", lastReplyOption ? "yes":"no");
		
		if(user.isMember("Users"))
		{
			ctx.put("modeShowEmails", user.getShowemails() ? "yes" : "no");
			ctx.put("modeShowReplies", user.getShowreplies() ? "yes" : "no");
			ctx.put("modeSortByLastReply", user.getSortByLastReply() ? "yes" : "no");
		}
		else
		{
			if((httpSession.getAttribute("showEmails") == null) ||
				httpSession.getAttribute("showEmails").equals("yes"))
			{
				ctx.put("modeShowEmails", "yes");
			}
			else
			{
				ctx.put("modeShowEmails", "no");
			}
			
			if((httpSession.getAttribute("showReplies") == null) ||
					httpSession.getAttribute("showReplies").equals("no"))
			{
				ctx.put("modeShowReplies", "no");
			}
			else
			{
				ctx.put("modeShowReplies", "yes");
			}
			
			if((httpSession.getAttribute("sortByLastReply") == null) ||
					httpSession.getAttribute("sortByLastReply").equals("yes"))
			{
				ctx.put("modeSortByLastReply", "yes");
			}
			else
			{
				ctx.put("modeSortByLastReply", "no");
			}
		}
	}
}
