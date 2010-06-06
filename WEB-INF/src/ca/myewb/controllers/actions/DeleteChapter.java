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

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.GroupChapterModel;


public class DeleteChapter extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupChapterModel theChapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);

		//no extra security required, this is only accessible to admins
		requireConfirmation("Do you really want to delete this chapter?",
		                    "It would be difficult to undo this action. All of this chapter's mailng lists will get deleted also!",
		                    path + "/chapter/ChapterInfo/"
		                    + urlParams.getParam(),
		                    path + "/actions/DeleteChapter/"
		                    + urlParams.getParam(), "chapter", null);

		//deal with all chapter lists, which includes the exec list
		
		theChapter.delete();

		setSessionMessage(("chapter deleted"));

		throw new RedirectionException(path + "/chapter/Chapter");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");
		s.add("NMT");

		return s;
	}
}
