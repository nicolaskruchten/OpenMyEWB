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

import java.util.Date;
import java.util.Vector;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.TextArea;


public class ApplicationSessionEditForm extends Form
{
	public ApplicationSessionEditForm(String target, PostParamWrapper requestParams)
	{
		super(target, "save application session");
		addText("Name", "Name", requestParams.get("Name"), true);
		addDateTimePicker("Opendate", "Open Date", requestParams.get("Opendate"), true).setInstructions("The date the application session is open to receive applications.\nPlease use YYYY-MM-DD HH:MM (24-hour time)");
		addDateTimePicker("Duedate", "Due Date", requestParams.get("Duedate"), true).setInstructions("The date applicants are informed applications are due.\nPlease use YYYY-MM-DD HH:MM (24-hour time)");
		addDateTimePicker("Closedate", "Close Date", requestParams.get("Closedate"), true).setInstructions("The date no more applications will be accepted.\nPlease use YYYY-MM-DD HH:MM (24-hour time)");
		
		TextArea text = addTextArea("EngInstructions", "English Instructions", requestParams.get("EngInstructions"), true);
		text.setInstructionTemplate("formatting");
		text.setInstructions("Applicants will see these instructions before they begin their application.");
		text.makeTwoCols(true);
		
		text = addTextArea("FrInstructions", "French Instructions", requestParams.get("FrInstructions"), true);
		text.setInstructionTemplate("formatting");
		text.setInstructions("Applicants will see these instructions before they begin their application.");
		text.makeTwoCols(true);
		
		text = addTextArea("Thankyoumessage", "Thank You Message", requestParams.get("Thankyoumessage"), true);
		text.setInstructionTemplate("formatting");
		text.setInstructions("Applicants will see this message once they complete their application.");
		text.makeTwoCols(true);
		
		text = addTextArea("Sessioncloseemail", "Session Closing Email", requestParams.get("Sessioncloseemail"), true);
		text.setInstructionTemplate("formatting");
		text.setInstructions("Applicants will be e-mailed this message when the session closes.");
		text.makeTwoCols(true);
		
		text = addTextArea("Rejectionemailtext", "Rejected Application Email", requestParams.get("Rejectionemailtext"), true);
		text.setInstructionTemplate("formatting");
		text.setInstructions("Applicants will be e-mailed this message when their application is rejected.");
		text.makeTwoCols(true);
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		Vector<Character> allowed = new Vector<Character>();
		isClean = (getElement("Opendate")).ensureDateTime()
        && isClean;
		isClean = (getElement("Duedate")).ensureDateTime()
        && isClean;
		isClean = (getElement("Closedate")).ensureDateTime()
        && isClean;
		allowed.add(new Character(' '));
		allowed.add(new Character('\''));
		allowed.add(new Character('-'));
		allowed.add(new Character(','));
		allowed.add(new Character('['));
		allowed.add(new Character(']'));
		allowed.add(new Character(')'));
		allowed.add(new Character('('));
		allowed.add(new Character(':'));
		isClean = (getElement("Name")).ensureAlphanumeric(allowed, true)
        && isClean;
		
		try
		{
			Date openDate = getParameterAsDateTime("Opendate");
			Date dueDate = getParameterAsDateTime("Duedate");
			Date closeDate = getParameterAsDateTime("Closedate");
			
			if(openDate.getTime() > dueDate.getTime())
			{
				getElement("Opendate").highlight();
				getElement("Duedate").highlight();
				getElement("Opendate").setError("The session cannot open after it is due");
				getElement("Duedate").setError("The session cannot open after it is due");
				isClean = false;
			}
			
			if(dueDate.getTime() > closeDate.getTime())
			{
				getElement("Duedate").highlight();
				getElement("Closedate").highlight();
				getElement("Duedate").setError("The session cannot be due after it closes");
				getElement("Closedate").setError("The session cannot be due after it closes");
				isClean = false;
			}
			
		} catch (Exception e)
		{
			getElement("Opendate").highlight();
			getElement("Duedate").highlight();
			getElement("Closedate").highlight();
			getElement("Opendate").setError("There was a problem with the form dates");
			getElement("Duedate").setError("There was a problem with the form dates");
			getElement("Closedate").setError("There was a problem with the form dates");
			isClean = false;
		}

		return isClean;
	}
}
