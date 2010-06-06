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
import java.util.List;
import java.util.Vector;

import ca.myewb.frame.Countries;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Text;


public class IntlAddress extends Multiment
{
	public IntlAddress(String name, String label, String[] value, boolean required)
	{
		super(name, label, value, required);

		if (this.value == null)
		{
			this.value = new String[]{"", "", "", "", "", "", ""};
		}
		
		String[] newValue = new String[7];
		for(int i=0; i< 7; i++)
		{
			newValue[i] = "";
		}
		for(int i=0; i< this.value.length; i++)
		{
			newValue[i] = makeNotNull(this.value[i]);
		}
		this.value = newValue;

		elements.add(new Text(name + "1", "Line 1", this.value[0], true));
		elements.add(new Text(name + "2", "Suite", this.value[1], false));
		elements.add(new Text(name + "3", "Line 2", this.value[2], false));
		elements.add(new Text(name + "4", "City", this.value[3], true));

		Text prov = new Text(name + "5", "Province/State", this.value[4], false);
		elements.add(prov);

		elements.add(new Text(name + "6", "Postal/Zip Code", this.value[5], false));

		Dropdown country = new Dropdown(name + "7", "Country", this.value[6], true);
		Countries c = new Countries();
		for( String s : c.getCountryAbbreviations())
		{
			country.addOption(s, c.getCountryName(s));
		}
		elements.add(country);

		type = "intladdress";
	}

	public List getCountries()
	{
		return ((Dropdown)elements.get(6)).getOptions();
	}

	public String getValue()
	{
		if (value[0].equals("") &&
				value[1].equals("") &&
				value[2].equals("") &&
				value[3].equals("") &&
				value[4].equals("") &&
				value[5].equals("") &&
				value[6].equals(""))
		{
			return "";
		}
		else
		{
			String s;

			s = value[0] + "\n";

			if (!value[1].equals(""))
			{
				s += ("Suite " + value[1] + "\n");
			}
			else
			{
				s += "\n";
			}

			s += (value[2] + "\n");

			s += (value[3] + "\n");
			s += (value[4] + "\n");
			s += (value[5] + "\n");
			s += value[6];

			return s;
		}
	}

	public void setValue(String v)
	{
		try
		{
			if ((v == null) || v.trim().equals(""))
			{
				value = new String[]{"", "", "", "", "", "", ""};
			}
			else
			{
				value = v.trim().split("\n");
				
				if (value.length < 7)
				{
					String[] newValue = new String[7];
					for(int i=0; i< 7; i++)
					{
						newValue[i] = "";
					}
					for(int i=0; i< value.length; i++)
					{
						newValue[i] = value[i];
					}
					value = newValue;
				}
			}
	
			if (value[1].startsWith("Suite"))
			{
				value[1] = value[1].substring(6);
			}
		}
		catch(ArrayIndexOutOfBoundsException aioobe)
		{
			value = new String[]{"", "", "", "", "", "", ""};
		}
	}

	public boolean validate()
	{
		boolean isClean = true;

		Iterator i = elements.iterator();
		int j = 0;

		while (i.hasNext())
		{
			Element e = (Element)i.next();

			Vector<Character> allowed = new Vector<Character>();
			allowed.add(new Character(' '));
			allowed.add(new Character(','));
			allowed.add(new Character('.'));
			allowed.add(new Character('\''));
			allowed.add(new Character('-')); // ie, for apt / room / suite number
			allowed.add(new Character('/')); // ie, for "c/o someone"

			if (j == 1)
			{
				if (!e.ensureAlphanumeric(true))
				{
					isClean = false;
					highlight();
					setError(e.getError());
				}
			}
			else
			{
				if (!e.ensureAlphanumeric(allowed, true))
				{
					isClean = false;
					highlight();
					setError(e.getError());
				}
			}

			j++;
		}

		return (super.validate() && isClean);
	}
	
}
