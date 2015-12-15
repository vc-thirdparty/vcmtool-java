package com.github.redsolo.vcm;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.ModelResourceParser;

public class ModelResourceParserTest {

	@Test
	public void assertValuesCanBeRead() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("VcId \"421fb14f-42c4-4b66-9a19-8d1ed0fce9d7\"\n" +
			"Revision 48\n" + 
			"Red 0.30588236451149\n" + 
			"Matrix 1 -2.37718303408e-28 0 0 2.37718303408e-28 1 0 0 0 0 1 0 -6.91251986207e-11 -0.5 -0.174560546875 1");
		assertThat((String) resource.getValue("VcId"), is("421fb14f-42c4-4b66-9a19-8d1ed0fce9d7"));
		assertThat((Long) resource.getValue("Revision"), is((long) 48));
		assertThat((Double) resource.getValue("Red"), is(0.30588236451149));
		assertThat(resource.getValue("Matrix").toString(), is("1 -2.37718303408e-28 0 0 2.37718303408e-28 1 0 0 0 0 1 0 -6.91251986207e-11 -0.5 -0.174560546875 1"));
	}
	
	@Test
	public void assertNestedResourcesCanBeRead() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("KeywordMap\n{\n  Keyword\n  {\n    Key \"Manufacturer\"\n    Value \"ACME\"\n  }\n}\n");
		assertThat(resource.getResource("KeywordMap"), is(not(nullValue())));
		assertThat(resource.getResource("KeywordMap").getResource("Keyword"), is(not(nullValue())));

		assertThat((String) resource.getResource("KeywordMap").getResource("Keyword").getValue("Key"), is("Manufacturer"));
		assertThat((String) resource.getResource("KeywordMap").getResource("Keyword").getValue("Value"), is("ACME"));
	}	
	@Test
	public void assertEmptyResourceValuesCanBeRead() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("Items\n{\n  Item\n  {\n    Name \"Manufacturer\"\n    Logo \"\"\n  }\n}\n");
		assertThat((String) resource.getResource("Items").getResource("Item").getValue("Logo"), is(""));
	}
	@Test
	public void assertEmptyResourceCanBeReader() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("Items\n{\n}\n");
		assertThat(resource.getResource("Items").getValues().size(), is(0));
	}
	@Test
	public void assertResourceItemsSpanningMultipleRows() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("Items\n{\n  Item\n  {\n    Name \"Manufacturer\"\n    Logo \"Logo\\\nType\"\n  }\n}\n");
		assertThat((String) resource.getResource("Items").getResource("Item").getValue("Logo"), is("LogoType"));
	}
	@Test
	public void assertResourceItemsSpanningMultipleRowsTwo() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("Expression \"direction_input_field==\"left\"?Tx(117.16).Ty(303.57).Rz(-45):\\\nTx(117.16).Ty(-303.57).Rz(45)\"\n");
		assertThat((String) resource.getValue("Expression"), is("direction_input_field==\"left\"?Tx(117.16).Ty(303.57).Rz(-45):Tx(117.16).Ty(-303.57).Rz(45)"));
	}

	

	
	@Test
	public void assertSettingsCanBeRead() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("    Settings\n" +
		    "{\n" +
		    "  VISIBLE\n" +
		    "  EDITABLE_DISCONNECTED\n" +
		    "  EDITABLE_CONNECTED\n" +
		    "  ON_EDIT_REBUILD \n" +
		    "}");
		ModelResource settings = resource.getResource("Settings");
		assertThat(settings.getValue("VISIBLE"), is(nullValue()));
		assertThat(settings.hasValue("ON_EDIT_REBUILD"), is(true));
		assertThat(settings.getValue("ON_EDIT_REBUILD"), is(nullValue()));
	}

	@Test
	public void assertCustomStatesCanBeRead() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("CustomStates\n" +
			"{\n" +
			"1\n" + 
			"\"Warmup\"\n" +
			"2\n" +
			"\"Break\"\n" +
			"1\n" +
			"}\n");
		ModelResource settings = resource.getResource("CustomStates");
		assertThat(settings.getValue("1"), is(nullValue()));
		assertThat(settings.getValue("\"Warmup\""), is(nullValue()));
	}

	@Test
	public void assertPythonScriptCanBeRead() throws Throwable {
		ModelResource resource = new ModelResourceParser().parse("Functionality \"rPythonScript\"\n" + 
			"{\n" +
			"Id 3\n" +
			"Name \"PythonScript\"\n" +
			"Script \"from vcScript import *\r\\ncomp      = getComponent()\r\\nbeamLen   = \"ACME::Length\"\r\\npass\"\n" +
			"}");
		ModelResource settings = resource.getResource("Functionality", "rPythonScript");
		assertThat(settings.getValue("Id").toString(), is("3"));
		assertEquals(settings.getValue("Script").toString(), 
				"from vcScript import *\r\\ncomp      = getComponent()\r\\nbeamLen   = \"ACME::Length\"\r\\npass");
	}

    @Test
    public void assertEmptyResourceName() throws Throwable {
        ModelResource resource = new ModelResourceParser().parse("VariableSpace \"\"\n{\n}");
        ModelResource variableSpace = resource.getResources("VariableSpace").get(0);
        assertThat(variableSpace.getType(), is("VariableSpace"));
        assertThat(variableSpace.getName(), is(""));
    }

    @Test
    public void assertNonStringResourceName() throws Throwable {
        ModelResource resource = new ModelResourceParser().parse("InternalProxy 2\n{\n}");
        ModelResource variableSpace = resource.getResources("InternalProxy").get(0);
        assertThat(variableSpace.getType(), is("InternalProxy"));
        assertThat(variableSpace.getName(), is("2"));
    }
}
