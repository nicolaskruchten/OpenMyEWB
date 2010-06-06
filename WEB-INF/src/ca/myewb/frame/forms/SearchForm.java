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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import ca.myewb.frame.forms.element.Dropdown;



public class SearchForm extends Form
{
	public SearchForm(String target, HashMap<String, String> terms) throws Exception
	{
		super(target, "search");
		addText("Body", "Search for", terms.get("Body"), false).setSize(300);		
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		Dropdown d = addDropdown("Since", "In items in the past", terms.get("Since"), false);
		d.addOption("", "All dates");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -6);
		d.addOption(df.format(cal.getTime()), "6 months");
		cal.add(Calendar.MONTH, -6);
		d.addOption(df.format(cal.getTime()), "year");
		cal.add(Calendar.MONTH, -6);
		d.addOption(df.format(cal.getTime()), "18 months");
		cal.add(Calendar.MONTH, -6);
		d.addOption(df.format(cal.getTime()), "2 years");
		cal.add(Calendar.MONTH, -12);
		d.addOption(df.format(cal.getTime()), "3 years");
		
		addCheckbox("Posts", "Posts", terms.get("Posts"), 
				"(check to include posts, emails and replies in results)");
				
		addCheckbox("Events", "Events", terms.get("Events"), 
				"(check to include events in results)");
				
		addCheckbox("Whiteboards", "Whiteboards", terms.get("Whiteboards"), 
				"(check to include post-, group- and event-whiteboards in results)");

		
		
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return isClean;
	}
}
