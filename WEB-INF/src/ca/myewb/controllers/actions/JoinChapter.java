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
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupChapterModel;


public class JoinChapter extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupChapterModel grp = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);
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
			                    path + "/actions/JoinChapter/"
			                    + urlParams.getParam(), "chapter", null);
		}

		if (chapter != null)
		{
			currentUser.leaveChapter(chapter);
			log.debug("Removed from old chapter: " + chapter.getName());
		}


		if( currentUser.joinChapter(grp) && grp.getWelcomeMessage() != null)
		{
			EmailModel.sendEmail(grp.getEmail(), currentUser.getEmail(), grp.getFullWelcomeEmail());
		}
		

		log.debug("Joined new chapter: " + grp.getName());
		setSessionMessage(("Welcome to the " + grp.getName()
		                                     + "!"));
		
		throw new RedirectionException(path + "/chapter/ChapterInfo");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
