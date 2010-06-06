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

package ca.myewb.tests;

import junit.framework.TestCase;

import java.util.LinkedList;
import java.util.List;

import ca.myewb.frame.forms.element.Text;
import ca.myewb.frame.forms.multiment.Address;
import ca.myewb.frame.forms.multiment.Phone;


public class FormTest extends TestCase
{
	public void testEnsureAlphabetic()
	{
		List<Character> allowed = new LinkedList<Character>();
		allowed.add('.');

		Text text = new Text("test", "test", "abcdef", true);
		assertTrue(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertTrue(text.ensureAlphabetic(allowed, false));
		assertTrue(text.ensureAlphabetic(new LinkedList<Character>(), true));
		assertTrue(text.ensureAlphabetic(allowed, true));

		text = new Text("test", "test", "kbkedHkmELK", true);
		assertTrue(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertTrue(text.ensureAlphabetic(new LinkedList<Character>(), true));

		text = new Text("test", "test", "abslrhgsjh ajhf", true);
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertFalse(text.ensureAlphabetic(allowed, false));
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), true));
		assertFalse(text.ensureAlphabetic(allowed, true));

		text = new Text("test", "test", "ab23gw", true);
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), true));

		text = new Text("test", "test", "adfad.df", true);
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertTrue(text.ensureAlphabetic(allowed, false));
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), true));
		assertTrue(text.ensureAlphabetic(allowed, true));

		text = new Text("test", "test", "asdf\u00E8", true);
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertTrue(text.ensureAlphabetic(new LinkedList<Character>(), true));

		text = new Text("test", "test", "adfad.df\u00E8", true);
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), false));
		assertFalse(text.ensureAlphabetic(allowed, false));
		assertFalse(text.ensureAlphabetic(new LinkedList<Character>(), true));
		assertTrue(text.ensureAlphabetic(allowed, true));
	}

	public void testEnsureNumeric()
	{
		List<Character> allowed = new LinkedList<Character>();
		allowed.add(' ');

		Text text = new Text("test", "test", "123896745624", true);
		assertTrue(text.ensureNumeric());
		assertTrue(text.ensureNumeric(allowed));

		text = new Text("test", "test", "23", true);
		assertTrue(text.ensureNumeric(10, 30));
		assertFalse(text.ensureNumeric(3, 7));

		text = new Text("test", "test", "123kfv99", true);
		assertFalse(text.ensureNumeric());

		text = new Text("test", "test", "123 345", true);
		assertFalse(text.ensureNumeric());
		assertTrue(text.ensureNumeric(allowed));
	}

	public void testEnsureAlphanumeric()
	{
		List<Character> allowed = new LinkedList<Character>();
		allowed.add('.');

		Text text = new Text("test", "test", "123abc123", true);
		assertTrue(text.ensureAlphanumeric(false));

		text = new Text("test", "test", "123456", true);
		assertTrue(text.ensureAlphanumeric(false));

		text = new Text("test", "test", "abcdef", true);
		assertTrue(text.ensureAlphanumeric(false));

		text = new Text("test", "test", "123abc..123", true);
		assertFalse(text.ensureAlphanumeric(false));
		assertTrue(text.ensureAlphanumeric(allowed, false));

		text = new Text("test", "test", "123abc..123\u00E8", true);
		assertFalse(text.ensureAlphanumeric(false));
		assertFalse(text.ensureAlphanumeric(allowed, false));
		assertTrue(text.ensureAlphanumeric(allowed, true));
	}

	public void testEnsureEmail()
	{
		Text text = new Text("test", "test", "francis@sytem.com", true);
		assertTrue(text.ensureEmail());

		text = new Text("test", "test", "francis.kung@utoronto.ca", true);
		assertTrue(text.ensureEmail());

		text = new Text("test", "test", "president@utoronto.sytem.com", true);
		assertTrue(text.ensureEmail());

		text = new Text("test", "test", "francis.kung123@utoronto.sytem.com", true);
		assertTrue(text.ensureEmail());

		text = new Text("test", "test", "someone@hotmail456.com", true);
		assertTrue(text.ensureEmail());

		text = new Text("test", "test", "someon_-asdf@_d.d-.tmail456.com", true);
		assertTrue(text.ensureEmail());

		text = new Text("test", "test", "fr\u00E8ncis@sytem.com", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis kung@utoronto.ca", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis@ewbca", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "me", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "a::b", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis@.ewbca", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis@.ewbca.", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis@ewb.", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis@ewb..ca", true);
		assertFalse(text.ensureEmail());

		text = new Text("test", "test", "francis@...", true);
		assertFalse(text.ensureEmail());
		
		text = new Text("test", "test", "francis@@sytem.com", true);
		assertFalse(text.ensureEmail());
		
		text = new Text("test", "test", "francis@a@sytem.com", true);
		assertFalse(text.ensureEmail());
		
		text = new Text("test", "test", "francis@system,com", true);
		assertFalse(text.ensureEmail());
	}

	public void testEnsurePostalCode()
	{
		Text text = new Text("test", "test", "A1A1A1", true);
		assertTrue(text.ensurePostalCode());
		assertEquals("A1A 1A1", text.getValue());

		text = new Text("test", "test", "A1A 1A1", true);
		assertTrue(text.ensurePostalCode());

		text = new Text("test", "test", "j3A 5v2", true);
		assertTrue(text.ensurePostalCode());
		assertEquals("J3A 5V2", text.getValue());

		text = new Text("test", "test", "ABCDEF", true);
		assertFalse(text.ensurePostalCode());

		text = new Text("test", "test", "123 456", true);
		assertFalse(text.ensurePostalCode());

		text = new Text("test", "test", "a1a 1a", true);
		assertFalse(text.ensurePostalCode());

		text = new Text("test", "test", "a1a1a", true);
		assertFalse(text.ensurePostalCode());

		text = new Text("test", "test", "a1a 1a1a", true);
		assertFalse(text.ensurePostalCode());

		text = new Text("test", "test", "a1a1a1a", true);
		assertFalse(text.ensurePostalCode());
	}

	public void testEnsureName()
	{
		Text text = new Text("test", "test", "Francis", true);
		assertTrue(text.ensureName());

		text = new Text("test", "test", "Francis Kung", true);
		assertTrue(text.ensureName());

		text = new Text("test", "test", "Francis Kung-Hyphenated", true);
		assertTrue(text.ensureName());

		text = new Text("test", "test", "Francis N.W. Kung", true);
		assertTrue(text.ensureName());

		text = new Text("test", "test", "francis", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Francis");

		text = new Text("test", "test", "francis kung", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Francis Kung");

		text = new Text("test", "test", "FRANCIS", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Francis");

		text = new Text("test", "test", "FRANCIS KUNG", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Francis Kung");

		text = new Text("test", "test", "francis n.w. kung", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Francis N.W. Kung");

		text = new Text("test", "test", "francis kung-other", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Francis Kung-Other");

		text = new Text("test", "test", "fr\u00E8ncis kung-other's", true);
		assertTrue(text.ensureName());
		assertEquals(text.getValue(), "Fr\u00E8ncis Kung-Other's");
	}

	public void testPhone()
	{
		String[] number = new String[]{"416", "833", "5570", "12"};
		Phone phone = new Phone("test", "test", number, true);
		assertEquals(phone.getValue(), "(416) 833-5570  ext. 12");
		assertTrue(phone.validate());

		number = new String[]{"416", "833", "5570", ""};
		phone = new Phone("test", "test", number, true);
		assertEquals(phone.getValue(), "(416) 833-5570");
		assertTrue(phone.validate());

		number = new String[]{"416", "833", "5570", null};
		phone = new Phone("test", "test", number, true);
		assertEquals(phone.getValue(), "(416) 833-5570");
		assertTrue(phone.validate());

		phone = new Phone("test", "test", null, true);
		phone.setValue("(416) 833-5570");
		assertEquals(phone.getValue(), "(416) 833-5570");

		phone = new Phone("test", "test", null, true);
		phone.setValue("(416) 833-5570  ext. 27");
		assertEquals(phone.getValue(), "(416) 833-5570  ext. 27");

		number = new String[]{"46", "833", "5570", ""};
		phone = new Phone("test", "test", number, true);
		assertFalse(phone.validate());

		number = new String[]{"416", "8b3", "5c70", "d"};
		phone = new Phone("test", "test", number, true);
		assertFalse(phone.validate());
	}

	public void testAddress()
	{
		String[] address = new String[]
		                   {
		                       "Engineers Without Borders", "201",
		                       "188 Davenport Road", "Toronto", "ON",
		                       "M5R 1J2", "CA"
		                   };
		Address add = new Address("test", "test", address, true);
		assertEquals(add.getValue(),
		             "Engineers Without Borders\n" + "Suite 201\n"
		             + "188 Davenport Road\n" + "Toronto\n" + "ON\n"
		             + "M5R 1J2\n" + "CA");
		assertTrue(add.validate());

		address = new String[]
		          {
		              "201-188 Davenport Rd.", null, null, "Toronto", "ON",
		              "M5R 1J2", "CA"
		          };
		add = new Address("test", "test", address, true);
		assertEquals(add.getValue(),
		             "201-188 Davenport Rd.\n\n\n" + "Toronto\n" + "ON\n"
		             + "M5R 1J2\n" + "CA");
		assertTrue(add.validate());

		add = new Address("test", "test", null, true);
		add.setValue("188 Davenport Road\n" + "Suite 201\n" + "\n" + "Toronto\n"
		             + "ON\n" + "M5R 1J2");
		assertEquals(add.getValue(),
		             "188 Davenport Road\n" + "Suite 201\n\n" + "Toronto\n"
		             + "ON\n" + "M5R 1J2\n");

		address = new String[]
		          {
		              "201;188 Davenport Rd.", null, null, "Toronto", "ON",
		              "M5R 1J2"
		          };
		add = new Address("test", "test", address, true);
		assertFalse(add.validate());

		address = new String[]
		          {
		              "201-188 Davenport Rd.", null, null, "Toronto", null,
		              "M5R 1J2"
		          };
		add = new Address("test", "test", address, true);
		assertFalse(add.validate());

		address = new String[]
		          {
		              "Engineer\u00E8's Without Borders 1", "201",
		              "188 D\u00E8venport's Road", "Toronta's 100", "ON",
		              "M5R 1J2", "CA"
		          };
		add = new Address("test", "test", address, true);
		assertEquals(add.getValue(),
		             "Engineer\u00E8's Without Borders 1\n" + "Suite 201\n"
		             + "188 D\u00E8venport's Road\n" + "Toronta's 100\n"
		             + "ON\n" + "M5R 1J2\n" + "CA");
		assertTrue(add.validate());
	}

	public void testLengths()
	{
		Text text = new Text("test", "test", "1234567890", true);
		assertTrue(text.ensureWordLength(5, true));
		assertEquals(text.getValue(), "12345 67890");
		assertTrue(text.ensureWordLength(3, true));
		assertEquals(text.getValue(), "123 45 678 90");
		assertTrue(text.ensureWordLength(2, false));
		assertEquals(text.getValue(), "12 45 67 90");
		assertTrue(text.ensureTotalLength(false, 10));
		assertEquals(text.getValue(), "12 45 67 9");
		assertFalse(text.ensureTotalLength(true, 5));
		assertEquals(text.getValue(), "12 45 67 9");

		text = new Text("test", "test", "1234567890", true);
		assertTrue(text.ensureWordLength(2, true));
		assertEquals(text.getValue(), "12 34 56 78 90");

	}
	
	public void testDate()
	{
		assertFalse(new Text("test", "test", "asldfjks", true).ensureDate());
		assertTrue(new Text("test", "test", "2006-06-06", true).ensureDate());
	}
}
