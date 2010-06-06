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
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;
import ca.myewb.model.GroupModel;

public class EventEditForm extends Form
{
	public EventEditForm(String target, PostParamWrapper requestParams,
			List<GroupModel> controllableGroups, boolean isExec)
	{
		super(target, "save event");

		
		addText("Name", "Name", requestParams.get("Name"), true).setSize(300);
		
		addDatePicker("StartDate", "Start Date", requestParams.get("StartDate"), true)
		.setInstructions("Please use YYYY-MM-DD");

		addText("StartTime", "Start Time", requestParams.get("StartTime"), false)
		.setSize(40)
		.setInstructions("Please use HH:MM (24-hour time)");
		
		
		addText("Duration", "Duration", requestParams.get("Duration"), false)
		.setSize(40)
		.setInstructions("In hours (e.g. 0.5 for 30 minutes, 48 for 2 days etc)");
		
		
		addText("Location", "Location", requestParams.get("Location"), false);

		TextArea elem = addTextArea("Notes", "Event Details", requestParams.get("Notes"), false);
		elem.makeTwoCols();
		elem.setInstructionTemplate("formatting");
		
		addCheckbox("Whiteboard","Whiteboard",requestParams.get("Whiteboard"), "Enable the whiteboard for this event");
		
		addCheckbox("Email", "Email this event", requestParams.get("Email"),
				"Check this box to email event info to the list selected below<br />(you'll be able to edit the email before it goes out)");

		
		Text tagField = addText("Keywords", "Tags",
                requestParams.get("Keywords"), true);
		tagField.setSize(350);

		tagField.setInstructionTemplate("tags");
		
		Dropdown d_who = addDropdown("Group", "Who should be able to see this event?", requestParams
				.get("Group"), true);
		//Collections.sort(controllableGroups, new GroupOrder());
		for (GroupModel g : controllableGroups)
		{

			d_who.addOption(Integer.toString(g.getId()), g.getPostName());
		}
		
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		try
		{
			if(this.getParameter("StartTime").equals(""))
				this.setValue("StartTime", "0:00");
			if(this.getParameter("Duration").equals(""))
				this.setValue("Duration", "0");
		}
		catch (Exception e)
		{
			;
		}
			
		isClean = (getElement("StartDate")).ensureDate() && isClean;
		isClean = (getElement("StartTime")).ensureTime() && isClean;
		isClean = (getElement("Duration")).ensureFloat() && isClean;
		return isClean;
	}
}
