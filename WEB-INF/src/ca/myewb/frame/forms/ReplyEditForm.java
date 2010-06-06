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

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;


public class ReplyEditForm extends Form
{
	public ReplyEditForm(String target, PostParamWrapper requestParams, boolean canSend, String shortname)
	              throws Exception
	{
		super(target, "preview reply");

		Element elem = addTextArea("Body", "Body", requestParams.get("Body"),
		                           true);
		elem.setInstructionTemplate("formatting");
		((TextArea)elem).makeTwoCols();
		
		if(canSend)
		{
			addCheckbox("SendAsEmail", "Send as email?", requestParams.get("SendAsEmail"),
					"check this box to email this reply to the [" + shortname + "] list");
		}

		elem = addFileChooser("File", "Attach file(s)",
		                      requestParams.get("File"), false);

		Text tagField = addText("Keywords", "Tags to add to this post",
		                        requestParams.get("Keywords"), false);
		tagField.setSize(350);
		tagField.setInstructionTemplate("tags");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		LinkedList<Character> allowed = new LinkedList<Character>();
		allowed.add(new Character(' '));
		allowed.add(new Character('-'));
		allowed.add(new Character(','));

		isClean = (getElement("Keywords")).ensureAlphanumeric(allowed, true)
		          && isClean;

		return isClean;
	}
}
