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

import ca.myewb.frame.PostParamWrapper;


public class ApplicationSessionReopenForm extends Form
{
	public ApplicationSessionReopenForm(String target, PostParamWrapper requestParams)
	{
		super(target, "reopen application session");
		
		addDateTimePicker("Duedate", "Due Date", requestParams.get("Duedate"), true).setInstructions("The date applicants are informed applications are due.\nPlease use YYYY-MM-DD HH:MM (24-hour time)");
		addDateTimePicker("Closedate", "Close Date", requestParams.get("Closedate"), true).setInstructions("The date no more applications will be accepted.\nPlease use YYYY-MM-DD HH:MM (24-hour time)");
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		isClean = (getElement("Duedate")).ensureDateTime()
        && isClean;
		isClean = (getElement("Closedate")).ensureDateTime()
        && isClean;
		
		try
		{
			Date dueDate = getParameterAsDateTime("Duedate");
			Date closeDate = getParameterAsDateTime("Closedate");
			
			if(new Date().getTime() > dueDate.getTime())
			{
				getElement("Duedate").highlight();
				getElement("Duedate").setError("The session cannot be due before now");
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
			getElement("Duedate").highlight();
			getElement("Closedate").highlight();
			getElement("Duedate").setError("There was a problem with the form dates");
			getElement("Closedate").setError("There was a problem with the form dates");
			isClean = false;
		}

		return isClean;
	}
}
