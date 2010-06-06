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

package ca.myewb.controllers.common;

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;
import org.hibernate.Session;

import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.WhiteboardEditForm;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.UserModel;
import ca.myewb.model.WhiteboardModel;


public class WhiteboardEdit extends Controller
{
	public WhiteboardEdit(HttpSession httpSession, Session hibernate,
	                PostParamWrapper requestParams, GetParamWrapper urlParams,
	                UserModel currentUser)
	{
		super();
		this.httpSession = httpSession;
		this.hibernateSession = hibernate;
		this.requestParams = requestParams;
		this.currentUser = currentUser;
		this.urlParams = urlParams;
	}
	
	public void generatePage(Context ctx, WhiteboardModel w) throws Exception{
		EventModel e = w.getParentEvent();
		PostModel p = w.getParentPost();
		GroupModel g = w.getParentGroup();
		
		if(!Permissions.canUpdateWhiteboard(currentUser, w))
		{
			throw getSecurityException("Can't see that" +
					(e != null ? " event" : "" ) +
					(p != null ? " post" : "" ) +
					(g != null ? " group" : "" ) +
					"."
					,
					path +
					(e != null ? "/events/Events" : "" ) +
					(p != null ? "/home/Posts" : "" ) +
					(g != null ? "/mailing/MyLists" : "" )
				);
		}
		
		if(!w.isEnabled() && e != null )
		{
			throw getSecurityException("This whiteboard is disabled.", path + "/events/EventInfo/" + e.getId());
		}
		
		WhiteboardEditForm whiteboard = (WhiteboardEditForm)checkForValidationFailure(ctx);

		if (whiteboard == null)
		{
			// First try: create a fresh form			
			whiteboard = new WhiteboardEditForm(path + "/actions/SaveWhiteboard/" + w.getId(), requestParams, g== null,
					e != null && Permissions.canSendEmailToGroup(currentUser, e.getGroup()));
			
			whiteboard.getElement("body").setValue(w.getBody());
			whiteboard.getElement("currentCount").setValue(new Integer(w.getNumEdits()).toString());
		}
		else if(w.getNumEdits() != Integer.parseInt(whiteboard.getParameter("currentCount")))
		{
			ctx.put("newerWhiteboard", w);		
			ctx.put("expiredWhiteboard", whiteboard.getParameter("body"));		
			whiteboard.getElement("currentCount").setValue(new Integer(w.getNumEdits()).toString());
			whiteboard.getElement("body").setValue(w.getBody());
		}

		ctx.put("form", whiteboard);
		ctx.put("parent", (e != null ? e : (p != null ? p : g )));
	}

	@Override
	public void handle(Context ctx) throws Exception {
		
		
	}
}
