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


public class UserEmailForm extends Form
{
	public UserEmailForm(String target, PostParamWrapper requestParams)
	{
		super(target, "save emails");
		addText("Email", "Primary email address", requestParams.get("Email"), true);
		
		Element e = addTextArea("Emails", "Secondary email addresses",
                requestParams.get("Emails"), false);
		((TextArea) e).setSize(30, 5);
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		// Clean form
		isClean = (getElement("Email")).ensureEmail() && isClean;
		isClean = getElement("Emails").ensureEmailList() && isClean;
		return isClean;
	}
}
