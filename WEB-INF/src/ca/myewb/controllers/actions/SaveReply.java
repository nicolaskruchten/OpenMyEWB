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
import java.util.List;
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
import ca.myewb.frame.forms.ReplyEditForm;
import ca.myewb.logic.TagLogic;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;


public class SaveReply extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		PostModel parent = (PostModel)getAndCheckFromUrl(PostModel.class);
		
		GroupModel group = parent.getGroup();
		if (!Permissions.canReplyToPost(currentUser, parent))
		{
			throw getSecurityException("You cannot reply to this post.",
			                           path + "/home/Posts");
		}

		boolean canSend = (group.getId() != 1) && !(group.isChapter())
		&& (Permissions.canSendEmailToGroup(currentUser, group));
		
		// Create & validate form object
		ReplyEditForm form = new ReplyEditForm(path + "/actions/SaveReply/"
		                                       + urlParams.getParam(),
		                                       requestParams, canSend, group.getTotalShortname());
		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/home/ShowPost/"
			                             + urlParams.getParam());
		}

		String confirmText;
		if(!isOnConfirmLeg())
		{
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("tags", TagLogic.extractTagNames(form.getParameter("Keywords")));
			confirmCtx.put("hasFile", (!requestParams.getFilesReceived().isEmpty()? "yes":"no"));
			if(canSend)
			{
				confirmCtx.put("asEmail", (form.getParameter("SendAsEmail").equals("on")? "yes":"no"));
			}
			else
			{
				confirmCtx.put("asEmail", "no");
			}
			confirmCtx.put("shortname", group.getTotalShortname());
			confirmCtx.put("body", form.getParameter("Body"));
			confirmCtx.put("helpers", new Helpers());
			
			Template template = Velocity.getTemplate("confirmations/savereply.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		requireConfirmation("Confirm: post this reply as-is?", confirmText,
		                    path + "/home/ShowPost/" + urlParams.getParam(),
		                    path + "/actions/SaveReply/" + urlParams.getParam(),
		                    "home", form, true);

		form = new ReplyEditForm(path + "/actions/SaveReply/"
		                         + urlParams.getParam(), requestParams, canSend, group.getTotalShortname());
		form.validate();

		String tags = form.getParameter("Keywords");
		PostModel reply = parent.reply(currentUser, form.getParameter("Body"), tags);
		
		reply.setHasfile(!requestParams.getFilesReceived().isEmpty());	
		
		for( String fileName : requestParams.getFilesReceived().keySet() )
		{
			requestParams.saveFile(fileName, "posts/" + reply.getId());
			log.info(fileName + " attached to post with id " + reply.getId());
		}

		String sender = "\"" +currentUser.getFirstname() + " "
         + currentUser.getLastname() + "\" <"
         + currentUser.getEmail() + ">";
		
		List<String> emailsForReplies = parent.getEmailsForReplies();
		if(!emailsForReplies.isEmpty())
		{
			reply.sendAsWatchListEmail(sender, emailsForReplies);
		}
		
		if(canSend && form.getParameter("SendAsEmail").equals("on"))
		{	
			
			try
			{
				reply.sendAsEmail(sender, emailsForReplies);
			} 
			catch (IllegalStateException e)
			{
				throw getSecurityException("Email couldn't be sent, too many recipients! " +
						"<br />(this system currently has trouble with more than 3000ish recipients)",
						path + "/home/Home");
			} 
		}
		
		// Leave a message in the session
		setSessionMessage(("Your reply was successfully saved."));

		// Redirect to somewhere
		throw new RedirectionException(path + "/home/ShowPost/"
		                               + urlParams.getParam());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
