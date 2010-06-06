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
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.TextArea;


public class UserProfileForm extends Form
{
	public UserProfileForm(String target, PostParamWrapper requestParams,
	                       boolean isRegular)
	{
		super(target, "save basic info");
		addText("Firstname", "First name", requestParams.get("Firstname"), true);
		addText("Lastname", "Last name", requestParams.get("Lastname"), true);
		addText("Email", "Primary email address", requestParams.get("Email"), true).setInstructions("All emails will be sent to this account");
		
		Element e = addTextArea("Emails", "Secondary email addresses",
                requestParams.get("Emails"), false);
		e.setInstructions("Please list all your other email addresses to help us ensure that we do not make duplicate user accounts for you.  One email address per line, no commas or other delimiters");
		((TextArea) e).setSize(30, 5);
		
		Element p = addPassword("Password", "Choose a new password",
		                        requestParams.get("Password"), false);
		p.setInstructions("optional: if left blank, current password will be unchanged<br /> passwords must be at least 6 characters");

		Element p2 = addPassword("Verify", "Confirm new password",
		                         requestParams.get("Verify"), false);
		p2.setInstructions("must be at least 6 characters");


		Radio l = addRadio("Language", "Preferred language",
		                   requestParams.get("Language"), false);
		l.addOption("en", "English");
		l.addOption("fr", "French");
		l.setNumAcross(2);
		
		Radio d = addRadio("Gender", "Gender", requestParams.get("Gender"),
                false);
		d.addOption("m", "Male");
		d.addOption("f", "Female");
		d.setNumAcross(2);

		addText("BirthYear", "Year of birth", requestParams.get("BirthYear"),false).setSize(50);
		
		Radio c = addRadio("Canadianinfo", "Place of residence",
                requestParams.get("Canadianinfo"), isRegular);
		c.addOption("y", "In North America");
		c.addOption("n", "Outside of North America");
		c.setNumAcross(2);
		c.setInstructions("To provide us with your address in step 2, please answer this question.");

		Radio s = addRadio("Student", "Student status",
		                   requestParams.get("Student"), isRegular);
		s.addOption("y", "Student");
		s.addOption("n", "Non-student");
		s.setNumAcross(2);
		s.setInstructions("To provide us with information about your field of study or employment in step 3, please answer this question.");


	}

	public boolean cleanAndValidate(boolean isClean)
	{
		// Clean form
		isClean = (getElement("Firstname")).ensureName() && isClean;
		isClean = (getElement("Lastname")).ensureName() && isClean;
		isClean = (getElement("Email")).ensureEmail() && isClean;
		isClean = (getElement("Password")).ensurePasswordStrength() && isClean;
		isClean = (getElement("Verify")).ensurePasswordStrength() && isClean;
		isClean = (getElement("BirthYear")).ensureNumeric(1890, 2005)
		          && isClean;
		isClean = getElement("Emails").ensureEmailList() && isClean;

		// Check to see if passwords match
		if ((getElement("Password")).getValue()
		    .equals((getElement("Verify")).getValue()))
		{
			if ((isClean == false)
			    && !((getElement("Password")).getHighlight()
			    || (getElement("Verify")).getHighlight())
			    && !((getElement("Password")).getValue().equals("")
			    || (getElement("Verify")).getValue().equals("")))
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
