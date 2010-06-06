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
import ca.myewb.frame.forms.PostEditForm;
import ca.myewb.logic.TagLogic;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;


public class SavePost extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// Create & validate form object
		PostEditForm form = new PostEditForm(path + "/actions/SavePost",
		                                     requestParams, null, Permissions.postGroups(currentUser), currentUser);

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m, path + "/home/NewPost");
		}
		
		String confirmText;
		if(!isOnConfirmLeg())
		{
			GroupModel pretargetGroup = (GroupModel)getAndCheck(GroupModel.class,
	                new Integer(form.getParameter("WhoCanSeeThisPost")));
			
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("subject", form.getParameter("Subject"));
			confirmCtx.put("visibility", pretargetGroup.getPostName());
			confirmCtx.put("tags", TagLogic.extractTagNames(form.getParameter("Keywords")));
			confirmCtx.put("hasFile", (!requestParams.getFilesReceived().isEmpty()? "yes":"no"));
			confirmCtx.put("intro", form.getParameter("Intro"));
			confirmCtx.put("bodyWithIntro", form.getParameter("Intro") + "\n\n" + form.getParameter("Body"));
			confirmCtx.put("helpers", new Helpers());
			confirmCtx.put("responseMode", form.getParameter("ResponseType"));
			
			Template template = Velocity.getTemplate("confirmations/savepost.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		
		requireConfirmation("Confirm: post this as-is?",
							confirmText, path + "/home/NewPost",
		                    path + "/actions/SavePost", "home", form, true);

		form = new PostEditForm(path + "/actions/SavePost", requestParams,
				null, Permissions.postGroups(currentUser), currentUser);
		form.validate();

		// Find visible-to group
		GroupModel targetGroup = (GroupModel)getAndCheck(GroupModel.class,
		                                       new Integer(form.getParameter("WhoCanSeeThisPost")));

		// Check permissions here to... why not...
		if (!Permissions.canPostToGroup(currentUser, targetGroup))
		{
			throw getSecurityException("You cannot post to this group.",
			                           path + "/home/Posts");
		}


		String subject = form.getParameter("Subject");
		String intro = form.getParameter("Intro");
		String body = form.getParameter("Body");
		String tags = form.getParameter("Keywords");
		boolean useWhiteboard = form.getParameter("ResponseType").equals("Whiteboard");
		PostModel p = PostModel.newPost(currentUser, targetGroup, subject, intro, body, tags, useWhiteboard);
		
		p.setHasfile(!requestParams.getFilesReceived().isEmpty());	
		
		log.info("This post has files: " + p.getHasfile());
		
		if(p.getHasfile())
		{
			log.info("Attempting to upload file");
			requestParams.saveFile("File", "posts/" + p.getId());
			log.info("Upload Successful");
		}

		// Leave a message in the session
		setSessionMessage(("Your post was successfully saved."));

		// Redirect to somewhere
		throw new RedirectionException(path + "/home/ShowPost/" + p.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
