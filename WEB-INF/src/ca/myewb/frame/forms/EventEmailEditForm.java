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

package ca.myewb.frame.forms;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class EventEmailEditForm extends Form
{
	public EventEmailEditForm(String target, PostParamWrapper requestParams,
	                     GroupModel list, UserModel currentUser) throws Exception
	{
		super(target, "preview email");
		Radio r;

		Element elem = addText("Subject", "Subject",
		                       requestParams.get("Subject"), true);
		elem.setInstructions("this will be prefixed with \"[" + list.getTotalShortname()+ "]\"");
		((Text)elem).setSize(300);
		
		GroupChapterModel chapter = currentUser.getChapter();
		if(currentUser.isMember(Helpers.getGroup("Exec"), false)
				&& (chapter!= null)
				&& (list.equals(chapter) || ((list.getParent() != null) && list.getParent().equals(chapter))))
		{
			r = addRadio("Sender", "Send from", requestParams.get("Sender"), true);
			r.addOption("self", "my email address");
			r.addOption("chapter", chapter.getEmail());
		}

		elem = addTextArea("Body", "Body", requestParams.get("Body"), true);
		((TextArea)elem).makeTwoCols();
		elem.setInstructionTemplate("formatting");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return getElement("Subject").ensureTotalLength(true, 100) && isClean;
	}
}
