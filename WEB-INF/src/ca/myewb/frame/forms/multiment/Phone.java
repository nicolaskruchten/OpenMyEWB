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

package ca.myewb.frame.forms.multiment;

import java.util.Iterator;

import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Text;


public class Phone extends Multiment
{
	public Phone(String name, String label, String[] value, boolean required)
	{
		super(name, label, value, required);

		if (value == null)
		{
			this.value = new String[]{"", "", "", ""};
		}

		try
		{
			this.value[0] = makeNotNull(this.value[0]);
			this.value[1] = makeNotNull(this.value[1]);
			this.value[2] = makeNotNull(this.value[2]);
			this.value[3] = makeNotNull(this.value[3]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			this.value = new String[]{"", "", "", ""};
		}

		elements.add(new Text(name + "1", "", this.value[0], true));
		elements.add(new Text(name + "2", "", this.value[1], true));
		elements.add(new Text(name + "3", "", this.value[2], true));
		elements.add(new Text(name + "4", "ext.", this.value[3], false));

		type = "phone";
	}

	public String getValue()
	{
		if (value[0].equals("") && value[1].equals("") && value[2].equals("") && value[3].equals(""))
		{
			return "";
		}
		else if (!value[3].equals(""))
		{
			return "(" + value[0] + ") " + value[1] + "-" + value[2]
			       + "  ext. " + value[3];
		}
		else
		{
			return "(" + value[0] + ") " + value[1] + "-" + value[2];
		}
	}

	public void setValue(String v)
	{
		try
		{
			value[0] = v.substring(1, 4);
			value[1] = v.substring(6, 9);
			value[2] = v.substring(10, 14);
			value[3] = v.substring(21);
		}
		catch (Exception e)
		{
			// Catch is really for the value[3], as not all nums have an extension
		}
	}

	public boolean validate()
	{
		boolean isClean = true;

		Iterator i = elements.iterator();

		while (i.hasNext())
		{
			Element e = (Element)i.next();

			if (!e.ensureNumeric())
			{
				isClean = false;
				highlight();
				setError(e.getError());
			}
		}

		if (((value[0].length() != 3) && (value[0].length() != 0))
		    || ((value[1].length() != 3) && (value[1].length() != 0))
		    || ((value[2].length() != 4) && (value[2].length() != 0)))
		{
			isClean = false;
			highlight();
			setError("Not enough digits in phone number");
		}

		return (super.validate() && isClean);
	}
}
