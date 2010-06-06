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

package ca.myewb.controllers.actions;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.WhiteboardEditForm;
import ca.myewb.frame.servlet.AjaxServlet;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.WhiteboardModel;


public class SaveWhiteboard extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		WhiteboardModel w = (WhiteboardModel)getAndCheckFromUrl(WhiteboardModel.class);
		PostModel p = w.getParentPost();
		EventModel e = w.getParentEvent();
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
		
		// Create & validate form object
		WhiteboardEditForm form;
		form = new WhiteboardEditForm(path + "/actions/SaveWhiteboard/" + w.getId(), requestParams, g == null,
				e != null && Permissions.canSendEmailToGroup(currentUser, e.getGroup()));

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
					path +
					(e != null ? "/events/EditWhiteboard/" : "" ) +
					(p != null ? "/home/EditPostWhiteboard/" : "" ) +
					(g != null ? "/mailing/EditGroupWhiteboard/" : "" ) + w.getId());
		}
		
		String confirmText;
		if(!isOnConfirmLeg())
		{
			
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("hasFile", (!requestParams.getFilesReceived().isEmpty()? "yes":"no"));
			confirmCtx.put("body", form.getParameter("body"));
			confirmCtx.put("helpers", new Helpers());
			
			Template template = Velocity.getTemplate("confirmations/savewhiteboard.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		
		requireConfirmation("Confirm: save this whiteboard as-is?",
							confirmText,
							path + 
							(e != null ? "/events/EditWhiteboard/" : "" ) +
							(p != null ? "/home/EditPostWhiteboard/" : "" ) +
							(g != null ? "/mailing/EditGroupWhiteboard/" : "" ) + w.getId(),
		                    path + "/actions/SaveWhiteboard/" + w.getId(),
		                    (e != null ? "events" : (p != null ? "home" : "mailing" ) ),
		                    form, true);

		form = new WhiteboardEditForm(path + "/actions/SaveWhiteboard/" + w.getId(), requestParams, g == null,
				e != null && Permissions.canSendEmailToGroup(currentUser, e.getGroup()));
		
		form.validate();

		if(w.getNumEdits() != Integer.parseInt(form.getParameter("currentCount")))
		{
			httpSession.setAttribute("form", form);
			setSessionErrorMessage("Someone else saved their changes to this whiteboard before you did... " +
					"<br />You can take a look at their version below, re-do your changes and re-save.");
			throw new RedirectionException(
					path +
					(e != null ? "/events/EditWhiteboard/" : "" ) +
					(p != null ? "/home/EditPostWhiteboard/" : "" ) +
					(g != null ? "/mailing/EditGroupWhiteboard/" : "" ) + w.getId());

		}
		
		
		String body = form.getParameter("body");
		
		w.setHasfile(!requestParams.getFilesReceived().isEmpty() || w.getHasfile());	
		
		if(w.getHasfile())
		{
			requestParams.saveFile("File", "whiteboards/" + w.getId());
		}
		
		w.save(body, currentUser);
		

		if( p != null )
		{
			p.setLastReply(new Date());
			AjaxServlet.invalidateFrontPageCache(p.getGroup().getId());
		}
		

		if( e != null && Permissions.canSendEmailToGroup(currentUser, e.getGroup())
				&& form.getParameter("Email").equals("on") )
		{
			throw new RedirectionException(path + "/events/SendEventEmail/" + e.getId());
		}
		

		// Leave a message in the session
		setSessionMessage(("Your whiteboard was successfully saved."));

		// Redirect to somewhere
		throw new RedirectionException( path +  
				(e != null ? "/events/EventInfo/" + e.getId() : "" ) +
				(p != null ? "/home/ShowPost/" + p.getId() : "" ) +
				(g != null ? "/mailing/ListInfo/" + g.getId() : "" ));
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");
		
		return s;
	}
}
