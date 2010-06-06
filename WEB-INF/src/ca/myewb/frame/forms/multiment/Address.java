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

import org.apache.log4j.Logger;

import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.Text;


public class Address extends Multiment
{
	public static int LINE_1 = 0;
	public static int SUITE = 1;
	public static int LINE_2 = 2;
	public static int CITY = 3;
	public static int PROVINCE = 4;
	public static int POSTAL = 5;
	public static int COUNTRY = 6;
	
	
	public Address(String name, String label, String[] value, boolean required)
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

		Dropdown prov = new Dropdown(name + "5", "Province/State", this.value[4], true);
		
		populateProvsAndState(prov);
		elements.add(prov);

		elements.add(new Text(name + "6", "Postal/Zip Code", this.value[5], true));

		Dropdown country = new Dropdown(name + "7", "Country", this.value[6], true);
		country.addOption("CA", "  Canada");
		country.addOption("US", "  United States");
		elements.add(country);

		type = "address";
	}

	public List getProvinces()
	{
		return ((Dropdown)elements.get(4)).getOptions();
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
		
		Dropdown prov = (Dropdown)elements.get(4);
		Dropdown country = (Dropdown)elements.get(6);

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
			else if (j == 5)
			{
				if ((value[5] != null) && !value[5].equals(""))
				{
					if (country.getValue().equals("CA") && !e.ensurePostalCode())
					{
						isClean = false;
						highlight();
						setError(e.getError());
					}
					else if(country.getValue().equals("US") && !e.ensureZipCode())
					{
						isClean = false;
						highlight();
						setError(e.getError());
					}
					else
					{
						value[5] = e.getValue(); // since ensurePostal does formatting
					}
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
		
		if( (country.getValue().equals("CA") && !isCanadianProvince(prov.getValue()))
				|| (country.getValue().equals("US") && !isAmericanState(prov.getValue())))
		{
			Logger.getLogger(this.getClass()).info(country.getValue() + " does not match province " + prov.getValue());
			isClean = false;
			highlight();
			prov.highlight();
			country.highlight();
			setError("Province/State does not match Country");
		}

		return (super.validate() && isClean);
	}
	
	public static boolean isCanadianProvince(String province)
	{
		String provinces = "AB, BC, MB, NB, NL, NT, NS, NU, ON, PE, QC, SK, YT";
		
		return provinces.matches(".*" + province + ".*");
	}
	
	public static boolean isAmericanState(String state)
	{
		String states = "AK, AL, AR, AZ, CA, CO, CT, DC, DE, FL, GA, HI, IA, ID, IL, IN, KS, KY, " +
				"LA, MA, MD, ME, MI, MN, MO, MS, MT, NC, ND, NE, NH, NJ, NM, NV, NY, OH, OK, OR, " +
				"PA, RI, SC, SD, TN, TX, UT, VA, VI, VT, WA, WI, WV, WY";
		
		return states.matches(".*" + state + ".*");
	}
	
	public static void populateProvsAndState(Dropdown d)
	{
		d.addOptGroup("Provinces");
		d.addOption("AB", "Alberta");
		d.addOption("BC", "British Colombia");
		d.addOption("MB", "Manitoba");
		d.addOption("NB", "New Brunswick");
		d.addOption("NL", "Newfoundland");
		d.addOption("NT", "Northwest Territories");
		d.addOption("NS", "Nova Scotia");
		d.addOption("NU", "Nunavut");
		d.addOption("ON", "Ontario");
		d.addOption("PE", "Prince Edward Island");
		d.addOption("QC", "Quebec");
		d.addOption("SK", "Saskatchewan");
		d.addOption("YT", "Yukon Territory");
		d.addOptGroup("States");
		d.addOption("AK", "Alaska");
		d.addOption("AL", "Alabama");
		d.addOption("AR", "Arkansas");
		d.addOption("AZ", "Arizona");
		d.addOption("CA", "California");
		d.addOption("CO", "Colorado");
		d.addOption("CT", "Connecticut");
		d.addOption("DC", "District of Columbia");
		d.addOption("DE", "Delaware");
		d.addOption("FL", "Florida");
		d.addOption("GA", "Georgia");
		d.addOption("HI", "Hawaii");
		d.addOption("IA", "Iowa");
		d.addOption("ID", "Idaho");
		d.addOption("IL", "Illinois");
		d.addOption("IN", "Indiana");
		d.addOption("KS", "Kansas");
		d.addOption("KY", "Kentucky");
		d.addOption("LA", "Louisiana");
		d.addOption("MA", "Massachusetts");
		d.addOption("MD", "Maryland");
		d.addOption("ME", "Maine");
		d.addOption("MI", "Michigan");
		d.addOption("MN", "Minnesota");
		d.addOption("MO", "Missouri");
		d.addOption("MS", "Mississippi");
		d.addOption("MT", "Montana");
		d.addOption("NC", "North Carolina");
		d.addOption("ND", "North Dakota");
		d.addOption("NE", "Nebraska");
		d.addOption("NH", "New Hampshire");
		d.addOption("NJ", "New Jersey");
		d.addOption("NM", "New Mexico");
		d.addOption("NV", "Nevada");
		d.addOption("NY", "New York");
		d.addOption("OH", "Ohio");
		d.addOption("OK", "Oklahoma");
		d.addOption("OR", "Oregon");
		d.addOption("PA", "Pennsylvania");
		d.addOption("RI", "Rhode Island");
		d.addOption("SC", "South Carolina");
		d.addOption("SD", "South Dakota");
		d.addOption("TN", "Tennessee");
		d.addOption("TX", "Texas");
		d.addOption("UT", "Utah");
		d.addOption("VA", "Virginia");
		d.addOption("VI", "Virgin Islands");
		d.addOption("VT", "Vermont");
		d.addOption("WA", "Washington");
		d.addOption("WI", "Wisconsin");
		d.addOption("WV", "West Virginia");
		d.addOption("WY", "Wyoming");
	}

	
}
