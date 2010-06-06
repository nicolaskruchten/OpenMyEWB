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
import ca.myewb.frame.forms.EventEmailEditForm;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;


public class DoSendEventEmail extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		EventModel event = (EventModel)getAndCheckFromUrl(EventModel.class);
		GroupModel list = event.getGroup();

		if (!Permissions.canSendEmailToGroup(currentUser, list))
		{
			throw getSecurityException("Can't send mail to this list!",
			                           path + "/events/EventInfo/"
			                           + urlParams.getParam());
		}

		// Create & validate form object
		EventEmailEditForm form = new EventEmailEditForm(path + "/actions/DoSendEventEmail/"
		                                       + event.getId(), requestParams,
		                                       list, currentUser);

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/events/SendEventEmail/"
			                             + event.getId());
		}

		String confirmText;
		if(!isOnConfirmLeg())
		{
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("list", list);
			confirmCtx.put("subject","[" + list.getTotalShortname()+ "] " + form.getParameter("Subject"));
			confirmCtx.put("body", form.getParameter("Body"));
			confirmCtx.put("helpers", new Helpers());
			
			Template template = Velocity.getTemplate("confirmations/dosendeventemail.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		
		requireConfirmation("Confirm: email this to the ["
		                    + list.getTotalShortname() + "] list as-is?",
		                    confirmText,
		                    path + "/events/SendEventEmail/" + event.getId(),
		                    path + "/actions/DoSendEventEmail/" + event.getId(),
		                    "events", form, true);
		
		form = new EventEmailEditForm(path + "/actions/DoSendEventEmail/" + event.getId(),
		                         requestParams, list, currentUser);
				
		String sender = "\"" + currentUser.getFirstname() + " "
         + currentUser.getLastname() + "\" <"
         + currentUser.getEmail() + ">";

		GroupChapterModel chapter = currentUser.getChapter();
		if( currentUser.isMember(Helpers.getGroup("Exec"), false)
				&& (chapter!= null)
				&& (list.equals(chapter) || ((list.getParent() != null) && list.getParent().equals(chapter)))
				&& form.getParameter("Sender").equals("chapter"))
		{	
			sender = chapter.getEmail();
		}

		
		event.sendAsEmail(sender, form.getParameter("Subject"), form.getParameter("Body"));
		
		
		setSessionMessage(("Your email was sent."));
		
		throw new RedirectionException(path + "/events/EventInfo/" + event.getId());
	}
	
	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
