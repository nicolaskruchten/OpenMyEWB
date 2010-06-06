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

import java.util.List;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.model.GroupChapterModel;


public class SignUpForm extends Form
{
	public SignUpForm(String target, PostParamWrapper requestParams)
	{
		super(target, "sign up");
		addText("Firstname", "First name", requestParams.get("Firstname"), true);
		
		addText("Lastname", "Last name", requestParams.get("Lastname"), true);
		
		addText("Email", "Primary email address", requestParams.get("Email"), true).setInstructions("all emails will be sent to this account");
		
		Dropdown chapter = addDropdown("Chapter", "Join a Chapter", requestParams.get("Chapter"), false);
		chapter.setInstructions("this is optional, but recommended if you want to attend meetings or get more involved with members in your area");
		List<GroupChapterModel> chapters = GroupChapterModel.getChapters();
		for(GroupChapterModel c : chapters)
		{
			chapter.addOption(Integer.toString(c.getId()), c.getName());
		}

		
		
		Element p = addPassword("Password", "Choose a password",
		                        requestParams.get("Password"), true);
		p.setInstructions("must be at least 6 characters");
		
		Element p2 = addPassword("Verify", "Confirm password",
		                         requestParams.get("Verify"), true);
		p2.setInstructions("must match password above");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		// Clean form
		isClean = (getElement("Firstname")).ensureName() && isClean;
		isClean = (getElement("Lastname")).ensureName() && isClean;
		isClean = (getElement("Email")).ensureEmail() && isClean;
		isClean = (getElement("Password")).ensurePasswordStrength() && isClean;
		isClean = (getElement("Verify")).ensurePasswordStrength() && isClean;

		// Check to see if passwords match
		if ((getElement("Password")).getValue()
		    .equals((getElement("Verify")).getValue()))
		{
			if ((isClean == false)
			    && !((getElement("Password")).getHighlight()
			    || (getElement("Verify")).getHighlight()))
			{
				(getElement("Password")).highlight();
				(getElement("Password")).setError("Field was cleared, please fill in");
				(getElement("Verify")).highlight();
				(getElement("Verify")).setError("Field was cleared, please fill in");
			}

			return isClean;
		}
		else
		{
			// Passwords didn't match; highlight them
			(getElement("Password")).highlight();
			(getElement("Password")).setError("Passwords do not match");
			(getElement("Verify")).highlight();
			(getElement("Verify")).setError("Passwords do not match");

			return false;
		}
	}
}
