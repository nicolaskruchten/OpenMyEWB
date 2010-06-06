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


public class LeaveChapter extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupChapterModel chapter = currentUser.getChapter();

		if (chapter != null)
		{
			String bigMessage = "Are you sure you want to leave the "
			                    + chapter.getName() + "?";
			String littleMessage = "";

			if (currentUser.isLeader(chapter, false))
			{
				littleMessage = bigMessage;
				bigMessage = "You are an exec of this chapter: if you leave the chapter, you will lose all exec rights!";
			}

			requireConfirmation(bigMessage, littleMessage,
			                    path + "/chapter/Chapter",
			                    path + "/actions/LeaveChapter", "chapter", null);

			currentUser.leaveChapter(chapter);

			log.debug("Removed from chapter: " + chapter.getName());

			setSessionMessage(("You have left the "
			                                     + chapter.getName()));
		}
		else
		{
			log.warn(currentUser.getUsername()
			         + " tried leaving non-existent chapter");

			setSessionMessage(("You were not in a chapter!"));
		}

		throw new RedirectionException(path + "/chapter/ListChapters");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
