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

package ca.myewb.frame.forms.element;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;


public class Dropdown extends Element
{
	private List<List> options;

	public Dropdown(String name, String label, String value, boolean required)
	{
		super(name, label, value, required);
		this.options = new LinkedList<List>();

		type = "dropdown";
	}

	public void addOption(String value, String name)
	{
		List<String> l = new LinkedList<String>();
		l.add(value);
		l.add(name);
		options.add(l);
	}
	
	public void addOptGroup(String name)
	{
		List<String> l = new LinkedList<String>();
		l.add(name);
		options.add(l);
	}

	public boolean validate()
	{
		// Make sure chosen option was either one of the allowed ones or nothing
		if (value.equals("") || (value == null))
		{
			return super.validate();
		}

		Iterator i = options.iterator();
		boolean optionFound = false;

		while (i.hasNext() && !optionFound)
		{
			LinkedList option = (LinkedList)i.next();

			if (option.get(0).equals(value))
			{
				optionFound = true;
			}
		}

		if (!optionFound)
		{
			error = "Invalid option selected";
			Logger.getLogger(this.getClass())
			.warn("invalid dropdown option detected: " + label + " = " + value, new Throwable());
		}

		return (super.validate() && optionFound);
	}

	public List getOptions()
	{
		return options;
	}
}
