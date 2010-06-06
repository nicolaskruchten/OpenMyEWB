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
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.TextArea;


public class ApplicationContactInfoForm extends Form
{
	public ApplicationContactInfoForm(String target, PostParamWrapper requestParams)
	{
		super(target, "Save and Proceed with Application &raquo;");
		addText("Firstname", "First name", requestParams.get("Firstname"), true);
		addText("Lastname", "Last name", requestParams.get("Lastname"), true);
		addText("Email", "Email address", requestParams.get("Email"), true);
		addPhone("Phone", "Phone number", requestParams.getArray("Phone"), true);

		addNumOptions(addRadio("En1", "English ability (writing)", requestParams.get("En1"), false));
		addNumOptions(addRadio("En2", "English ability (reading)", requestParams.get("En2"), false));
		addNumOptions(addRadio("En3", "English ability (speaking)", requestParams.get("En3"), false));
		
		addNumOptions(addRadio("Fr1", "French ability (writing)", requestParams.get("Fr1"), false));
		addNumOptions(addRadio("Fr2", "French ability (reading)", requestParams.get("Fr2"), false));
		addNumOptions(addRadio("Fr3", "French ability (speaking)", requestParams.get("Fr3"), false));

		TextArea e = addTextArea("Schooling", "Program(s) of study, year(s) of graduation, and school(s)", requestParams.get("Schooling"), false);
		e.makeTwoCols(true);
		
		addText("GPA", "Final undergraduate average", requestParams.get("GPA"), false)
		.setInstructions("Please provide an estimate (expressed as a percentage) of your " +
				"average grade during your undergraduate studies. " +
				"This information will not be used as a metric for selection.");
		
		e = addTextArea("Resume", "Resum&eacute;", requestParams.get("Resume"), false);
		e.makeTwoCols(true);
		

		e = addTextArea("References", 
				"Please provide the name, contact information and a brief description of your relationship for two professional references. These can be university or college professors, past employers, or supervisors from paid or voluntary positions. The references should not be personal in nature or unable to speak directly to your skills as a potential overseas volunteer. We do not require a letter from these references but my contact them to consult on your suitability for this position.", 
				requestParams.get("References"), false);
		e.makeTwoCols(true);

	}

	private void addNumOptions(Radio s)
	{
		int num = 5;
		for(int i=0; i<=num; i++)
		{
			s.addOption(new Integer(i).toString(), new Integer(i).toString());
		}
		s.setNumAcross(num+1);
		s.setInstructions("0=none, 1=minimal, " + num + "=total fluency");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		// Clean form
		isClean = (getElement("Firstname")).ensureName() && isClean;
		isClean = (getElement("Lastname")).ensureName() && isClean;
		isClean = (getElement("Email")).ensureEmail() && isClean;
		isClean = (getElement("GPA")).ensureFloat() && isClean;
		return isClean;
	}
}