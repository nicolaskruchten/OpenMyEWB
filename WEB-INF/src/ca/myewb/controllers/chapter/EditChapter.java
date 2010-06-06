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

package ca.myewb.controllers.chapter;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.ChapterEditForm;
import ca.myewb.model.GroupChapterModel;



public class EditChapter extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM chapterid, not required
		GroupChapterModel chapter;

		if (urlParams.getParam() != null)
		{
			// If you are requesting a certain group via param, then we show it
			chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);
		}
		else
		{
			// Otherwise we load up info about your chapter
			chapter = currentUser.getChapter();
		}

		// Not in a chapter...
		if (chapter == null || !Permissions.canUpdateGroupInfo(currentUser, chapter))
		{
			throw getSecurityException("There was an error with your request",
			                           path + "/chapter/ListChapters");
		}

		// Leaders get to edit chapter info
		ChapterEditForm f = (ChapterEditForm)checkForValidationFailure(ctx);

		if (f == null)
		{
			// First try: create a fresh form
			f = new ChapterEditForm(path + "/actions/SaveChapter/"
			                        + chapter.getId(), requestParams,
			                        currentUser.isAdmin());

			if (currentUser.isAdmin())
			{
				f.setValue("Name", chapter.getName());
				f.setValue("Shortname", chapter.getShortname());
				if(chapter.isFrancophone())
				{
					f.setValue("Francophone", "on");
				}
				if(chapter.isProfessional())
				{
					f.setValue("Professional", "on");
				}
			}

			f.setValue("Address", chapter.getAddress());
			f.setValue("Phone", chapter.getPhone());
			f.setValue("Fax", chapter.getFax());
			f.setValue("Email", chapter.getEmail());
			f.setValue("Url", chapter.getUrl());
		}

		ctx.put("form", f);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");
		s.add("NMT");

		return s;
	}

	public String displayName()
	{
		return "Update Chapter";
	}

	public int weight()
	{
		return 10;
	}
}
