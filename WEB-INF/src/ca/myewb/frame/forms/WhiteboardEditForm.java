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

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.TextArea;


public class WhiteboardEditForm extends Form
{
	//Why is the parent needed?
	
	public WhiteboardEditForm(String target, PostParamWrapper requestParams, boolean files, boolean emailOption) throws Exception
	{
		super(target, "preview board");

		addHidden("currentCount", requestParams.get("currentCount"), true);
		Element elem = addTextArea("body", "Whiteboard Text", requestParams.get("body"), true);
		elem.setInstructionTemplate("formatting");
		((TextArea)elem).makeTwoCols();
		
		if(files){
			elem = addFileChooser("File", "Attach file(s)",
			                      requestParams.get("File"), false);
		}
		
		if(emailOption)
		{
			addCheckbox("Email", "Email Whiteboard", requestParams.get("Email"),
			"Check this box to send an e-mail to the list that owns this whiteboard<br />(you'll be able to edit the email before it goes out)");
		}
		
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return true;
	}
}
