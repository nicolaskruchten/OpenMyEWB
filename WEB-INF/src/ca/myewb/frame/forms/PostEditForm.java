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
import java.util.List;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class PostEditForm extends Form
{
	public PostEditForm(String target, PostParamWrapper requestParams,
	                    GroupModel group, List<GroupModel> groups, UserModel user) throws Exception
	{
		super(target, "preview post");
		

		
		addText("Subject", "Subject", requestParams.get("Subject"), true).setSize(300);

		Element elem = addTextArea("Intro", "Introduction",
		                           requestParams.get("Intro"), true);
		elem.setInstructionTemplate("formatting");
		elem.setInstructions("this is a short intro to the post, which appears on the front page and before the body");
		((TextArea)elem).setSize(50, 4);

		elem = addTextArea("Body", "Body", requestParams.get("Body"), false);
		elem.setInstructionTemplate("formatting");
		elem.setInstructions("this is the main body text of the post");
		((TextArea)elem).makeTwoCols();
		
		Radio r = addRadio("ResponseType", "Response type", requestParams.get("ResponseType"), true);
		r.setNumAcross(2);
		r.addOption("Replies", "Replies");
		r.addOption("Whiteboard", "Whiteboard");

		elem = addFileChooser("File", "Attach file(s)",
		                      requestParams.get("File"), false);
		elem.setInstructions("to upload multiple files, click the 'add more files' button");

		Text tagField = addText("Keywords", "Tags",
		                        requestParams.get("Keywords"), true);
		tagField.setSize(350);

		tagField.setInstructionTemplate("tags");

		Dropdown d2 = addDropdown("WhoCanSeeThisPost",
		                          "Who should be able to read this post?",
		                          requestParams.get("WhoCanSeeThisPost"), true);

		//Collections.sort(groups, new GroupOrder());
		for (GroupModel g : groups)
		{
			if(!g.getName().equals("DeletedPosts"))
			{
				d2.addOption((new Integer(g.getId())).toString(),  g.getPostName());
			}
		}
		
		if(group != null && groups.contains(group))
		{
			d2.setValue(Integer.toString(group.getId()));
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		isClean = getElement("Subject").ensureTotalLength(true, 100) && isClean;

		isClean = getElement("Intro").ensureTotalLength(true, 550) && isClean;

		LinkedList<Character> allowed = new LinkedList<Character>();
		allowed.add(new Character(' '));
		allowed.add(new Character('-'));
		allowed.add(new Character(','));

		isClean = (getElement("Keywords")).ensureAlphanumeric(allowed, true)
		          && isClean;

		isClean = (getElement("WhoCanSeeThisPost")).ensureNumeric() && isClean;

		return isClean;
	}
}
