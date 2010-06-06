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

import ca.myewb.frame.Controller;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Message;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.EmailEditForm;
import ca.myewb.logic.TagLogic;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;


public class DoSendEmail extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		
		GroupModel list = (GroupModel)getAndCheckFromUrl(GroupModel.class);

		if (!Permissions.canSendEmailToGroup(currentUser, list))
		{
			throw getSecurityException("Can't send mail to this list!",
			                           path + "/mailing/ListInfo/"
			                           + urlParams.getParam());
		}

		// Create & validate form object
		EmailEditForm form = new EmailEditForm(path + "/actions/DoSendEmail/"
		                                       + list.getId(), requestParams,
		                                       list, currentUser);

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/mailing/SendEmail/"
			                             + list.getId());
		}

		String confirmText;
		if(!isOnConfirmLeg())
		{			
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("list", list);
			confirmCtx.put("subject", form.getParameter("Subject"));
			confirmCtx.put("tags", TagLogic.extractTagNames(form.getParameter("Keywords")));
			confirmCtx.put("hasFile", (!requestParams.getFilesReceived().isEmpty()? "yes":"no"));
			confirmCtx.put("body", form.getParameter("Body"));
			confirmCtx.put("helpers", new Helpers());
			confirmCtx.put("responseMode", form.getParameter("Responses"));
			
			Template template = Velocity.getTemplate("confirmations/dosendemail.vm");
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
		                    path + "/mailing/SendEmail/" + list.getId(),
		                    path + "/actions/DoSendEmail/" + list.getId(),
		                    "mailing", form, true);
		
		form = new EmailEditForm(path + "/actions/DoSendEmail/" + list.getId(),
		                         requestParams, list, currentUser);
		

		String emailBody = form.getParameter("Body");
		String body = "";
		String intro = "";
		if (emailBody.length() < 600)
		{
			intro = emailBody;
			body = " ";
		}
		else if (emailBody.indexOf(" ", 300) != -1)
		{
			intro = emailBody.substring(0, emailBody.indexOf(" ", 300)) + " ...";
			body = "..." + emailBody.substring(emailBody.indexOf(" ", 300));
		}
		else
		{
			//shouldn't be reachable if ensureWordLength works, but just in case...
			intro = " ";
			body = emailBody;
		}
		
		
		PostModel p = PostModel.newPost(currentUser, list, form.getParameter("Subject"), 
					intro, body, form.getParameter("Keywords"),
					form.getParameter("Responses").equals("whiteboard") );
		
		p.setHasfile(!requestParams.getFilesReceived().isEmpty());	
		
		for( String fileName : requestParams.getFilesReceived().keySet() )
		{
			requestParams.saveFile(fileName, "posts/" + p.getId());
			log.info(fileName + " attached to email with id " + p.getId());
		}		

		String sender = "\"" + currentUser.getFirstname() + " "
         + currentUser.getLastname() + "\" <"
         + currentUser.getEmail() + ">";

		GroupChapterModel chapter = currentUser.getChapter();
		if( currentUser.isMember(Helpers.getGroup("Exec"), false)
				&& (chapter!= null)
				&& (list.equals(chapter) || ((list.getParent() != null) && list.getParent().equals(chapter)))
				&& form.getParameter("Sender").equals("chapter"))
		{	
			sender = "\"" + chapter.getName() + "\" <" + chapter.getEmail() + ">";
		}
		
		try
		{
			p.sendAsEmail(sender);
		} 
		catch (IllegalStateException e)
		{
			throw getSecurityException("Email couldn't be sent, too many recipients! " +
					"<br />(System currently has trouble with more than 3000ish recipients)",
					path + "/mailing/Mailing");
		} 


		setSessionMessage(("Your email was sent, and was archived as this post."));
		
		throw new RedirectionException(path + "/home/ShowPost/" + p.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
