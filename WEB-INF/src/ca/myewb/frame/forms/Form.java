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
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.forms.element.Checkbox;
import ca.myewb.frame.forms.element.DatePicker;
import ca.myewb.frame.forms.element.DateTimePicker;
import ca.myewb.frame.forms.element.Dropdown;
import ca.myewb.frame.forms.element.Element;
import ca.myewb.frame.forms.element.FileChooser;
import ca.myewb.frame.forms.element.Header;
import ca.myewb.frame.forms.element.Hidden;
import ca.myewb.frame.forms.element.Password;
import ca.myewb.frame.forms.element.Radio;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.element.TextArea;
import ca.myewb.frame.forms.multiment.Address;
import ca.myewb.frame.forms.multiment.IntlAddress;
import ca.myewb.frame.forms.multiment.Phone;


public class Form
{
	private List<Element> elements;
	private Hashtable<String, Element> namesToElements;
	protected String target;
	protected String submitText;

	public Form(String target)
	{
		this(target, "Submit");
	}

	public Form(String target, String submitText)
	{
		this.target = target;
		this.submitText = submitText;
		elements = new LinkedList<Element>();
		namesToElements = new Hashtable<String, Element>();
	}

	public List getElements()
	{
		return elements;
	}

	public String getTarget()
	{
		return target;
	}

	public String getSubmitText()
	{
		return submitText;
	}

	private String makeNotNull(String value)
	{
		if (value == null)
		{
			return "";
		}
		else
		{
			return value;
		}
	}

	public Text addText(String name, String label, String value,
	                    boolean required)
	{
		Text e = new Text(name, label, makeNotNull(value), required);
		addToElements(name, e);

		return e;
	}
	
	public DatePicker addDatePicker(String name, String label, String value,
            boolean required)
	{
		DatePicker e = new DatePicker(name, label, makeNotNull(value), required);
		addToElements(name, e);
		
		return e;
	}
	
	public DateTimePicker addDateTimePicker(String name, String label, String value,
            boolean required)
	{
		DateTimePicker e = new DateTimePicker(name, label, makeNotNull(value), required);
		addToElements(name, e);
		
		return e;
	}
	
	public Header addHeader(String name, String label)
	{
		Header e = new Header(name, label);
		addToElements(name, e);
		
		return e;
	}

	public TextArea addTextArea(String name, String label, String value,
	                            boolean required)
	{
		TextArea t = new TextArea(name, label, makeNotNull(value), required);
		addToElements(name, t);

		return t;
	}

	public FileChooser addFileChooser(String name, String label, String value,
	                                  boolean required)
	{
		return addFileChooser(name, label, makeNotNull(value),
		                                required, true);
	}

	public FileChooser addFileChooser(String name, String label, String value,
	                                  boolean required, boolean multi)
	{
		FileChooser t = new FileChooser(name, label, makeNotNull(value),
		                                required, multi);
		addToElements(name, t);

		return t;
	}

	public Password addPassword(String name, String label, String value,
	                            boolean required)
	{
		Password p = new Password(name, label, makeNotNull(value), required);
		addToElements(name, p);

		return p;
	}

	public Dropdown addDropdown(String name, String label, String value,
	                            boolean required)
	{
		Dropdown d = new Dropdown(name, label, makeNotNull(value), required);
		addToElements(name, d);

		return d;
	}

	public Radio addRadio(String name, String label, String value,
	                      boolean required)
	{
		Radio r = new Radio(name, label, makeNotNull(value), required);
		addToElements(name, r);

		return r;
	}

	public Hidden addHidden(String name, String value, boolean required)
	{
		Hidden h = new Hidden(name, makeNotNull(value), required);
		addToElements(name, h);

		return h;
	}

	public Checkbox addCheckbox(String name, String label, String value,
	                            String boxLabel)
	{
		Checkbox c = new Checkbox(name, label, makeNotNull(value), boxLabel);
		addToElements(name, c);

		return c;
	}

	public Phone addPhone(String name, String label, String[] value,
	                      boolean required)
	{
		Phone p = new Phone(name, label, value, required);
		addToElements(name, p);

		return p;
	}

	public Address addAddress(String name, String label, String[] value,
	                          boolean required)
	{
		Address a = new Address(name, label, value, required);
		addToElements(name, a);

		return a;
	}

	public IntlAddress addIntlAddress(String name, String label, String[] value,
	                          boolean required)
	{
		IntlAddress a = new IntlAddress(name, label, value, required);
		addToElements(name, a);

		return a;
	}

	public void addToElements(Element e)
	{
		addToElements(e.getInternalName(), e);
	}
	
	public void addToElements(String name, Element e)
	{
		elements.add(e);
		namesToElements.put(name, e);
	}

	public final Message validate()
	{
		boolean isClean = true;

		// Iterate over all elements, calling their individual validate fcns
		Iterator i = elements.iterator();

		while (i.hasNext())
		{
			Element e = (Element)i.next();
			isClean = (e.validate() && isClean);
		}

		isClean = this.cleanAndValidate(isClean) && isClean;

		if (!isClean)
		{
			return new ErrorMessage("There was a problem with the form.");
		}
		else
		{
			return null;
		}
	}

	public String getParameter(String param) throws Exception
	{
		return getParameter(param, true);
	}


	public String getNulledStringParam(String param) throws Exception
	{
		String p = getParameter(param, true);
		return (p == null || p.equals("")) ? null : p;
	}

	public Character getNulledCharParam(String param) throws Exception
	{
		String p = getParameter(param, true);
		return (p == null || p.equals("")) ? null : p.charAt(0);
	}
	
	public Integer getNulledIntParam(String param) throws Exception
	{
		String p = getParameter(param, true);
		return (p == null || p.equals("")) ? null : Integer.parseInt(p);
	}
	
	public Float getNulledFloatParam(String param) throws Exception
	{
		String p = getParameter(param, true);
		return (p == null || p.equals("")) ? null : Float.parseFloat(p);
	}

	
	public Date getParameterAsDate(String param) throws Exception
	{
		String paramValue = getParameter(param, true);
		if((paramValue == null) || paramValue.equals(""))
		{
			return null;
		}
		else
		{
			return new SimpleDateFormat("yyyy-MM-dd").parse(paramValue);
		}
	}
	
	public Date getParameterAsDateTime(String param) throws Exception
	{
		String paramValue = getParameter(param, true);
		if((paramValue == null) || paramValue.equals(""))
		{
			return null;
		}
		else
		{
			return new SimpleDateFormat("yyyy-MM-dd kk:mm").parse(paramValue);
		}
	}

	public String getParameter(String param, boolean checkExistance)
	                    throws Exception
	{
		Element e = getElement(param);

		if (checkExistance && (e == null))
		{
			Logger.getLogger(this.getClass())
			.warn("tried to get non-existent parameter! (" + param + ")", new Throwable());

			return null;
		}
		else if (e == null)
		{
			return null;
		}
		else
		{
			return e.getValue();
		}
	}

	public void setValue(String element, String value)
	              throws Exception
	{
		Element e = getElement(element);

		if (e != null)
		{
			if (value == null)
			{
				e.setValue("");
			}
			else
			{
				e.setValue(value);
			}
		}
		else
		{
			if (Helpers.isDevMode())
			{
				throw new Exception("tried to set non-existent param!");
			}
			else
			{
				Logger.getLogger(this.getClass())
				.error("tried to set non-existent parameter! (" + element + ")", new Throwable());
			}
		}
	}

	public void setError(String element, String message)
	{
		Element e = getElement(element);

		if (e != null)
		{
			e.setError(message);
			e.highlight();
		}
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		return isClean;
	}

	public Element getElement(String name)
	{
		return namesToElements.get(name);
	}

	protected Date getParameterAsTime(String string)
	{
		return null;
	}
}
