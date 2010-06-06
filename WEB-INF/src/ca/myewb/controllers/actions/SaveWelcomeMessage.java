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

import ca.myewb.beans.GroupChapter;
import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.WelcomeMessageEditForm;
import ca.myewb.model.GroupModel;


public class SaveWelcomeMessage extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// PARAM listid or new, rethrow to EditList

		// Create & validate form object

		GroupModel list;
		int listId;

		list = (GroupModel)getAndCheckFromUrl(GroupModel.class);
		listId = list.getId();
		
		String securityURL = path + "/mailing/ListInfo/" + listId;

		if (!Permissions.canUpdateGroupInfo(currentUser, list))
		{
			throw getSecurityException("You can't edit this list!",
			                           securityURL);
		}
		
		log.debug("Building form");

		WelcomeMessageEditForm form = new WelcomeMessageEditForm(path + "/actions/SaveWelcomeMessage/"
		                                     + urlParams.getParam(),
		                                     requestParams);
		log.debug("Validating form");

		Message m = form.validate();

		// No messages: changes are valid
		if (!((m == null) || (isOnConfirmLeg())))
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
					path + "/mailing/EditWelcomeMessage/"
					+ urlParams.getParam());
		}
		
		String confirmText;
		if(!isOnConfirmLeg())
		{
			VelocityContext confirmCtx = new VelocityContext();
			confirmCtx.put("from", list.isChapter() ? ((GroupChapter)list).getEmail() : Helpers.getSystemEmail());
			confirmCtx.put("subject", "Welcome to the " + list.getName() + " mailing list!");
			confirmCtx.put("body", form.getParameter("Body"));
			confirmCtx.put("helpers", new Helpers());
			
			Template template = Velocity.getTemplate("confirmations/savewelcomemessage.vm");
			StringWriter writer = new StringWriter();
			template.merge(confirmCtx, writer);
			confirmText = writer.toString();
		}
		else
		{
			confirmText = "";
		}
		
		requireConfirmation("Confirm: post this as-is?",
							confirmText, path + "/mailing/EditWelcomeMessage/" + list.getId(),
		                    path + "/actions/SaveWelcomeMessage/"
                            + urlParams.getParam(), "mailing", form, true);

		// Save form info into chapter object
		if( form.getParameter("Body").trim().equals(""))
		{
			list.setWelcomeMessage(null);
		}
		else
		{
			list.setWelcomeMessage(form.getParameter("Body"));
		}

		// Leave a message in the session
		setSessionMessage(("Welcome E-mail Message Updated"));

		throw new RedirectionException(securityURL);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
}
