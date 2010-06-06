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
import java.util.LinkedList;
import java.util.List;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.forms.element.Element;


public abstract class Multiment extends Element
{
	protected String[] value;
	protected List<Element> elements;

	public Multiment(String name, String label, String[] value, boolean required)
	{
		highlight = false;
		error = "";

		this.internalName = name;
		this.label = label;
		this.value = value;
		this.required = required;
		this.elements = new LinkedList<Element>();
	}

	protected String makeNotNull(String str)
	{
		if (str == null)
		{
			return "";
		}
		else
		{
			return str;
		}
	}

	public abstract void setValue(String value);

	public boolean validate()
	{
		boolean result = true;

		if (required || !getValue().equals(""))
		{
			Iterator i = elements.iterator();

			// Iterate over all elements, calling their individual validate fcns
			while (i.hasNext())
			{
				Element e = (Element)i.next();

				if (e.isRequired()
				    && ((e.getValue() == null) || e.getValue().equals("")))
				{
					highlight();
					e.highlight();
					setError("A required field is missing");

					result = false;
				}
			}
		}

		return result;
	}

	public String showValue(int index)
	{
		if (index >= value.length)
		{
			return "";
		}
		else
		{
			return (value[index]);
		}
	}

	public boolean isRequired(int index)
	{
		return elements.get(index).isRequired();
	}

	public boolean getHighlight(int index)
	{
		return elements.get(index).getHighlight();
	}

    public String getSafeValue()
    {
        if (value == null)
            return null;
        else
        {
            return Helpers.htmlSafe(getValue().replaceAll("\n", " "));
        }
    }
	

	public void setValue(String[] value)
	{
		this.value = value;
	}
}
