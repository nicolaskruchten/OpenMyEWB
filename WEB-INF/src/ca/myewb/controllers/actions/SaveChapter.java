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
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ChapterEditForm;
import ca.myewb.model.GroupChapterModel;


public class SaveChapter extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM new or chapterid, rethrow to EditChapter
		// Create & validate form object
		ChapterEditForm form = new ChapterEditForm(path
		                                           + "/actions/SaveChapter/"
		                                           + urlParams.getParam(),
		                                           requestParams,
		                                           currentUser.isAdmin());

		Message m = form.validate();

		//now make sure the shortname isn't already taken
		if (currentUser.isAdmin() && (m == null))
		{
			String shortName = form.getParameter("Shortname");
			int numWithShortname = (hibernateSession.createQuery("FROM GroupModel WHERE shortname=? AND parent IS NULL")
			                        .setString(0, shortName)
			                        .list()).size();

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
			if (!urlParams.getParam().equals("new"))
			{
				throw getValidationException(form, m,
				                             path + "/chapter/EditChapter/"
				                             + urlParams.getParam());
			}
			else
			{
				throw getValidationException(form, m,
				                             path + "/chapter/EditChapter");
			}
		}

		GroupChapterModel chapter;
		int chapterId;

		if (urlParams.getParam().equals("new"))
		{
			if (currentUser.isMember(Helpers.getGroup("NMT")))
			{
				log.debug("Adding a chapter");

				chapter = GroupChapterModel.newChapter();
				chapterId = chapter.getId();
			}
			else
			{
				// Non-NMT can't add a chapter!
				throw getSecurityException("You don't have permission to add a chapter!",
				                           path + "/chapter/Chapter");
			}
		}
		else
		{
			log.debug("Updating a chapter");
			chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);
			chapterId = chapter.getId();
		}

		// Security check to prevent spoofing
		if (!Permissions.canUpdateGroupInfo(currentUser, chapter))
		{
			// Disallow the action and boot them out
			throw getSecurityException("You don't have permission to edit this chapter!",
			                           path + "/chapter/ChapterInfo/"
			                           + chapterId);
		}

		log.debug("Updating a chapter");

		// Save form info into chapter object
		String address = form.getParameter("Address");
		String phone = form.getParameter("Phone");
		String fax = form.getParameter("Fax");
		String email = form.getParameter("Email");
		String url = form.getParameter("Url");
		if (currentUser.isAdmin())
		{
			chapter.save(form.getParameter("Name"), form.getParameter("Shortname"), 
					address, phone, fax, email, url, form.getParameter("Francophone").equals("on"), 
					form.getParameter("Professional").equals("on"));
		}
		else 
		{
			chapter.save(chapter.getName(), chapter.getShortname(), address, phone, fax, email, url,
					chapter.isFrancophone(), chapter.isProfessional());
		}

		// Leave a message in the session
		setSessionMessage(("Chapter Information Updated"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/chapter/ChapterInfo/"
		                               + chapterId);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
}
