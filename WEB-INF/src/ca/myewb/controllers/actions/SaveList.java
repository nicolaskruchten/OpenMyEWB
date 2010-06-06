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

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Message;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ListEditForm;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;


public class SaveList extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM listid or new, rethrow to EditList

		// Create & validate form object
		log.debug("Building form");

		ListEditForm form = new ListEditForm(path + "/actions/SaveList/"
		                                     + urlParams.getParam(),
		                                     requestParams);
		log.debug("Validating form");

		Message m = form.validate();

		//now make sure the shortname isn't already taken
		GroupChapterModel parent = currentUser.getChapter();

		String shortName = form.getParameter("Shortname");
		if (m == null)
		{
			int numWithShortname = 0;

			if (parent != null)
			{
				numWithShortname = (hibernateSession.createQuery("FROM GroupModel WHERE shortname=? AND parent=?")
				                    .setString(0, shortName)
				                    .setEntity(1, parent).list()).size();
			}
			else
			{
				numWithShortname = (hibernateSession.createQuery("FROM GroupModel WHERE shortname=? AND parent IS NULL")
				                    .setString(0, shortName)
				                    .list()).size();
			}

			if ((urlParams.getParam().equals("new") && (numWithShortname > 0))
			    || (!urlParams.getParam().equals("new")
			       && (numWithShortname > 1)))
			{
				m = new ErrorMessage("That shortname is already taken, please choose another.");
			}
		}

		// No messages: changes are valid
		if (m != null)
		{
			// Display error and prompt user to fix
			if (urlParams.getParam().equals("new"))
			{
				throw getValidationException(form, m, path + "/mailing/NewList");
			}
			else
			{
				throw getValidationException(form, m,
				                             path + "/mailing/EditList/"
				                             + urlParams.getParam());
			}
		}

		GroupModel list;
		int listId;

		//security not an issue yet, only Exec/Admin can see this page...
		if (urlParams.getParam().equals("new"))
		{
			log.debug("Adding a mailing list");

			list = GroupModel.newGroup(parent);
			listId = list.getId();
		}
		else
		{
			log.debug("Updating a list");
			list = (GroupModel)getAndCheckFromUrl(GroupModel.class);
			listId = list.getId();
		}

		String securityURL = path + "/mailing/ListInfo/" + listId;

		if (!Permissions.canUpdateGroupInfo(currentUser, list))
		{
			throw getSecurityException("You can't edit this list!",
			                           securityURL);
		}

		log.debug("Updating a list");

		// Save form info into chapter object
		String name = form.getParameter("Name");
		String desc = form.getParameter("Description");
		boolean isPublic = form.getParameter("Public").equals("on");
		
		list.save(name, shortName, desc, isPublic);

		// Leave a message in the session
		setSessionMessage(("Mailing List Updated"));

		throw new RedirectionException(securityURL);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
}
