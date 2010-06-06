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

import java.util.LinkedList;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class EmailEditForm extends Form
{
	public EmailEditForm(String target, PostParamWrapper requestParams,
	                     GroupModel list, UserModel currentUser) throws Exception
	{
		super(target, "preview email");
		
		Radio r;

		Element elem = addText("Subject", "Subject",
		                       requestParams.get("Subject"), true);
		elem.setInstructions("this will be prefixed with \"[" + list.getTotalShortname()+ "]\"");
		((Text)elem).setSize(300);
		
		GroupChapterModel chapter = currentUser.getChapter();
		
		r = new Radio("Sender", "Send from", requestParams.get("Sender"), true);
		if (requestParams.get("Sender") == null)
			r.setValue("");

		if(currentUser.isMember(Helpers.getGroup("Exec"), true))
		{
			r.addOption("self", "my email address");
						
			if (chapter!= null
				&& (list.equals(chapter) || ((list.getParent() != null) && list.getParent().equals(chapter))))
			{
				r.addOption("chapter", chapter.getEmail());
			}
		}
		
		if (r.getNumOptions() > 1)
			addToElements(r);
		else
			addHidden("Sender", "self", true);

		elem = addTextArea("Body", "Body", requestParams.get("Body"), true);

		elem.setInstructionTemplate("formatting");
		((TextArea)elem).makeTwoCols();

		

		r = addRadio("Responses", "Response Type", (requestParams.get("Responses") != null ? requestParams.get("Responses") : "replies"), true);
		r.addOption("replies", "Replies");
		r.addOption("whiteboard", "Whiteboard");
		r.setNumAcross(2);
		
		elem = addFileChooser("File", "Attach file(s)",
		                      requestParams.get("File"), false);
		elem.setInstructions("no file will sent with the email, but a link will be automatically added to the email footer.");

		
		Text tagField = addText("Keywords", "Tags",
		                        requestParams.get("Keywords"), true);
		tagField.setSize(350);
		tagField.setInstructionTemplate("tags");



		

	}

	public boolean cleanAndValidate(boolean isClean)
	{
		isClean = getElement("Subject").ensureTotalLength(true, 100) && isClean;

		LinkedList<Character> allowed = new LinkedList<Character>();
		allowed.add(new Character(' '));
		allowed.add(new Character('-'));
		allowed.add(new Character(','));
		isClean = (getElement("Keywords")).ensureAlphanumeric(allowed, true)
		          && isClean;

		return isClean;
	}
}
