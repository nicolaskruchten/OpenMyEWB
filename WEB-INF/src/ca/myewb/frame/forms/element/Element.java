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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.myewb.frame.Helpers;


public abstract class Element
{
	protected String type;
	protected String label;
	protected String internalName;
	protected String value;
	protected String error;
	protected boolean highlight;
	protected boolean required;
	private String instructions = "";
	private String instructionTemplate = "";

	public String getInstructionTemplate() {
		return instructionTemplate;
	}

	public void setInstructionTemplate(String instructionTemplate) {
		this.instructionTemplate = instructionTemplate;
	}

	// This is only so Multiment can extend this
	protected Element()
	{
	}

	protected Element(String name, String label, String value, boolean required)
	{
		highlight = false;
		error = "";

		this.internalName = name;
		this.label = label;
		this.value = value;
		this.required = required;
	}

	public boolean validate()
	{
		// Only validation done here is to check required fields.
		// Any more detailed validation & input cleaning should be done
		// in overriding methods in specific elements.
		if (required && ((value == null) || value.trim().equals("")))
		{
			highlight();
			setError("This field is required");

			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean ensureAlphabetic(List<Character> allowed,
	                                boolean allowAccents)
	{
		return ensureAlphabetic(allowed, value, allowAccents);
	}

	private boolean ensureAlphabetic(List<Character> allowed, String value,
	                                 boolean allowAccents)
	{
		if ((value == null) || value.equals(""))
		{
			return true;
		}

		String strippedValue = value;

		for (char ok : allowed)
		{
			while (strippedValue.indexOf(ok) != -1)
			{
				String v2 = strippedValue.substring(0, strippedValue.indexOf(ok));
				strippedValue = v2
				                + strippedValue.substring(strippedValue.indexOf(ok)
				                                          + 1);
			}
		}

		if ((strippedValue == null) || strippedValue.equals(""))
		{
			return true;
		}

		char[] chars = strippedValue.toCharArray();

		for (int i = 0; i < chars.length; i++)
		{
			if (((chars[i] < 'a') || (chars[i] > 'z'))
			    && ((chars[i] < 'A') || (chars[i] > 'Z')))
			{
				if (allowAccents)
				{
					Pattern alphabetic = Pattern.compile("^\\p{L}*$",
					                                     Pattern.CANON_EQ
					                                     | Pattern.UNICODE_CASE);
					Matcher myMatcher = alphabetic.matcher(strippedValue);

					if (!myMatcher.find())
					{
						highlight();
						setError("This field can only contain letters");

						return false;
					}
				}
				else
				{
					highlight();
					setError("This field can only contain letters with no accents or diacritics");

					return false;
				}
			}
		}

		return true;
	}

	public boolean ensureNumeric(List<Character> allowed)
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		char[] chars = value.toCharArray();

		for (int i = 0; i < chars.length; i++)
		{
			if (((chars[i] < '0') || (chars[i] > '9'))
			    && !(allowed.contains(new Character(chars[i]))))
			{
				highlight();
				setError("This field can only contain numbers");

				return false;
			}
		}

		return true;
	}

	// Bounded numeric
	public boolean ensureNumeric(List<Character> allowed, int lower, int upper)
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		if (ensureNumeric(allowed) == true)
		{
			if (((new Integer(value).intValue()) < lower)
			    || ((new Integer(value).intValue()) > upper))
			{
				highlight();
				setError("This field can only contain numbers between " + lower
				         + " and " + upper);

				return false;
			}

			return true;
		}

		return false;
	}

	public boolean ensureNumeric()
	{
		return ensureNumeric(new LinkedList<Character>());
	}

	public boolean ensureNumeric(int lower, int upper)
	{
		return ensureNumeric(new LinkedList<Character>(), lower, upper);
	}

	public boolean ensureNumeric(int lower)
	{
		return ensureNumeric(new LinkedList<Character>(), lower,
		                     Integer.MAX_VALUE);
	}

	public boolean ensureAlphanumeric(List<Character> allowed, String value,
	                                  boolean allowAccents)
	{
		List<Character> localAllowed = new LinkedList<Character>();
		localAllowed.addAll(allowed);

		for (int i = 0; i < 10; i++)
		{
			localAllowed.add(new String(i + "").charAt(0));
		}

		if (!ensureAlphabetic(localAllowed, value, allowAccents))
		{
			highlight();
			setError("This field can only contain letters and numbers");

			return false;
		}

		return true;
	}

	public boolean ensureAlphanumeric(List<Character> l, boolean allowAccents)
	{
		return ensureAlphanumeric(l, value, allowAccents);
	}

	public boolean ensureAlphanumeric(boolean allowAccents)
	{
		return ensureAlphanumeric(new LinkedList<Character>(), allowAccents);
	}


	public boolean ensureEmail()
	{
		return ensureEmail(value, true);
	}

	public boolean ensureEmail(String value, boolean fixCase)
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		List<Character> allowed = new LinkedList<Character>();
		allowed.add(new Character('.'));
		allowed.add(new Character('@'));
		allowed.add(new Character('_'));
		allowed.add(new Character('-'));

		if ((ensureAlphanumeric(allowed, value, false) == false) || 
				(value.indexOf('@') != value.lastIndexOf('@')))
		{
			highlight();
			setError(value + " is not a valid email address");
			return false;
		}

		if (value.indexOf('@') != -1)
		{
			String user = value.substring(0, value.indexOf('@'));
			String domain = value.substring(value.indexOf('@') + 1);

			boolean isValid = !user.equals("") && (domain.indexOf('.') != -1);

			while (isValid && (domain.indexOf('.') != -1))
			{
				isValid = isValid
				          && (!domain.substring(0, domain.indexOf('.'))
				               .equals(""));
				domain = domain.substring(domain.indexOf('.') + 1);
			}

			isValid = isValid && (domain.length() > 1);

			if (isValid)
			{
				if (fixCase)
				{
					this.value = value.toLowerCase();
				}
				return true;
			}
		}

		highlight();
		setError(value + " is not a valid email address");
		return false;
	}

	public boolean ensureEmailList()
	{
		String[] emails = value.split("\n");
		String cleanList = "";
		boolean isClean = true;

		for (int i = 0; i < emails.length; i++)
		{
			String email = emails[i].trim();
			isClean = ensureEmail(email, false) && isClean;
			cleanList = cleanList + email.toLowerCase() + "\n";
		}

		this.value = cleanList.substring(0, cleanList.length() - 1);

		return isClean;
	}

	public boolean ensureWordLength(int maxLength, boolean split)
	{
		String[] words = value.split("\\s"); //split on any whitespace

		for (String word : words)
		{
			if (word.length() > maxLength)
			{
				if (split)
				{
					String fixedword = word.substring(0, maxLength);
					String secondPart = word.substring(maxLength);

					while (secondPart.length() > maxLength)
					{
						fixedword += (" " + secondPart.substring(0, maxLength));
						secondPart = secondPart.substring(maxLength);
					}

					fixedword += (" " + secondPart);

					value = value.replace(word, fixedword);
				}
				else //simple trim
				{
					value = value.replace(word, word.substring(0, maxLength));
				}
			}
		}

		return true;
	}

	public boolean ensureTotalLength(boolean invalidIfTooLong, int maxLength)
	{
		if (value.length() > maxLength)
		{
			if (invalidIfTooLong)
			{
				highlight();
				setError("This field can only contain up to " + maxLength
				         + " characters.");

				return false;
			}
			else
			{
				value = value.substring(0, maxLength);
			}
		}

		return true;
	}

	public boolean ensureZipCode()
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		if( !ensureNumeric(0, 99999) )
		{
			setError("Not a valid zip code.");
			return false;
		}

		return true;
	}

	public boolean ensurePostalCode()
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		// Remove spaces, if any
		while (value.indexOf(' ') != -1)
		{
			String v2 = value.substring(0, value.indexOf(' '));
			value = v2 + value.substring(value.indexOf(' ') + 1);
		}

		// Convert to upper case
		value = value.toUpperCase();

		// Check for validity: bad characters & length
		if (value.length() != 6)
		{
			highlight();
			setError("This field contained an invalid postal code");

			return false;
		}

		char[] letters = new char[]
		                 {
		                     value.charAt(0), value.charAt(2), value.charAt(4)
		                 };

		for (int i = 0; i < letters.length; i++)
		{
			if ((letters[i] < 'A') || (letters[i] > 'Z'))
			{
				highlight();
				setError("This field contained an invalid postal code");

				return false;
			}
		}

		char[] numbers = new char[]
		                 {
		                     value.charAt(1), value.charAt(3), value.charAt(5)
		                 };

		for (int i = 0; i < numbers.length; i++)
		{
			if ((numbers[i] < '0') || (numbers[i] > '9'))
			{
				highlight();
				setError("This field contained an invalid postal code");

				return false;
			}
		}

		// Now re-add the space
		String v2 = value.substring(0, 3);
		value = v2 + " " + value.substring(3);

		return true;
	}

	public boolean ensureDate()
	{

		if (value.equals("") || (value == null))
		{
			return true;
		}
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try
		{
			formatter.parse(value);
			return true;
		}
		catch (ParseException e)
		{
			highlight();
			setError("Please use YYYY-MM-DD format");
			return false;
		}
	}
	
	public boolean ensureDateTime()
	{

		if (value.equals("") || (value == null))
		{
			return true;
		}
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        try
		{
			formatter.parse(value);
			return true;
		}
		catch (ParseException e)
		{
			highlight();
			setError("Please use YYYY-MM-DD hh:mm (24-hour time) format");
			return false;
		}
	}
	
	public boolean ensureTime()
	{

		if (value.equals("") || (value == null))
		{
			return true;
		}
		DateFormat formatter = new SimpleDateFormat("kk:mm");
        try
		{
			formatter.parse(value);
			return true;
		}
		catch (ParseException e)
		{
			highlight();
			setError("Please use hh:mm (24-hour time) format");
			return false;
		}
	}
	
	public boolean ensureName()
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		List<Character> allowed = new LinkedList<Character>();
		allowed.add(new Character('-'));
		allowed.add(new Character(' '));
		allowed.add(new Character('.'));
		allowed.add(new Character('\''));

		if (ensureAlphanumeric(allowed, true) == false)
		{
			highlight();
			setError("This contained invalid characters");

			return false;
		}

		// Reformat it for the lazy people
		if (value.toUpperCase().equals(value)
		    || value.toLowerCase().equals(value))
		{
			value = value.toLowerCase();

			// And capitalize every letter after a space, period, or hyphen
			int i = 0;

			while ((i > -1) && (i < value.length()))
			{
				// Capitalize the letter at i
				String front = value.substring(0, i);
				String end = value.substring(i + 1);
				String toCap = String.valueOf(value.charAt(i));
				toCap = toCap.toUpperCase();
				value = front + toCap + end;

				// Find the next letter needing capitalizing
				int j = value.indexOf(' ', i);

				int k = value.indexOf('-', i);

				if ((j == -1) || ((k < j) && (k != -1)))
				{
					j = k;
				}

				k = value.indexOf('.', i);

				if ((j == -1) || ((k < j) && (k != -1)))
				{
					j = k;
				}

				// Some funky stuff to prevent infinite loops =)
				if (j == -1)
				{
					i = -1;
				}
				else
				{
					i = j + 1;
				}
			}
		}

		return true;
	}

	public boolean getHighlight()
	{
		return highlight;
	}

	public void highlight()
	{
		highlight = true;
	}

	public String getName()
	{
		return label;
	}

	public String getValue()
	{
		if (value == null)
		{
			return null;
		}

		return value
			.replace('\u2018', '\'').replace('\u2019', '\'') //fancy apostrophes
			.replace('\u201D', '"').replace('\u201C', '"')   //fancy quotes
			.replace('\u00AB', '"').replace('\u00BB', '"')   //french quotes
			.replace('\u2013', '-').replace('\u2014', '-')   //en and em dashes
			.replace("\u2026", "...") 					     //ellipsis
			.replace("\u2022", "-").replace("\u25E6", "-")	 //bullets
			.replace("\u25AA", "-").replace("\u25AB", "-")	 //bullets
			;
	}
    
    public String getSafeValue()
    {
        if (value == null)
            return null;
        else
        {
            return Helpers.htmlSafe(getValue());
        }
    }
	
	public void setValue(String value)
	{
		this.value = value;
	}

	public String getType()
	{
		return type;
	}

	public String getInternalName()
	{
		return internalName;
	}

	public void require()
	{
		required = true;
	}

	public boolean isRequired()
	{
		return required;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	public String getTemplate()
	{
		return ("frame/elements/" + getType() + ".vm").toLowerCase();
	}

	public boolean ensurePasswordStrength()
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}

		if (value.length() < 6)
		{
			highlight();
			setError("Please use at least 6 characters in your password");

			return false;
		}

		return true;
	}

	public String getInstructions()
	{
		return instructions;
	}

	public void setInstructions(String instructions)
	{
		this.instructions = instructions;
	}

	public boolean ensureFloat() 
	{	
		if (value.equals("") || (value == null))
		{
			return true;
		}
		
		try
		{
			if(Float.parseFloat(value) < 0)
			{
				highlight();
				setError("This field must only have positive numbers");
				return false;
			}
		}
		catch(NumberFormatException e)
		{
			highlight();
			setError("This field must only have a decimal number");
			return false;
		}
		
		return true;
	}

	public boolean ensureUrl()
	{
		if (value.equals("") || (value == null))
		{
			return true;
		}
		
		if(!value.startsWith("http://") && !value.startsWith("https://"))
		{
			highlight();
			setError("This field must start with http:// or https://");
			return false;
		}
		
		if(!value.contains("."))
		{
			highlight();
			setError("That doesn't look like a valid URL.");
			return false;
		}
		
		
		return true;
		
	}
		
}
