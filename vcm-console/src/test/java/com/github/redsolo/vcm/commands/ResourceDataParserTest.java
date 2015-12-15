package com.github.redsolo.vcm.commands;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import com.github.redsolo.vcm.commands.ResourceDataParser;
import com.github.redsolo.vcm.util.TestUtil;

public class ResourceDataParserTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void assertPropertiesCanBeRetrieved() throws Throwable {
        ModelResource needleModelResource = new ModelResource("VariableSpace");
        ModelResource root = new ModelResource("Root").addResource(new ModelResource("Node", "rSimResource").addResource(needleModelResource));

        ModelResource modelResource = ResourceDataParser.getVariables(root);
        
        assertThat(modelResource, is(sameInstance(needleModelResource)));
    }
    
    @Test
    public void assertPythonScriptsCanBeRetrieved() throws Throwable {
        File file = TestUtil.getResourceFile(folder.getRoot());
        List<ModelResource> scripts = ResourceDataParser.getPythonScripts(new Model(file).getResourceData());
        
        assertThat(scripts.size(), is(equalTo(1)));
        assertThat((String)scripts.get(0).getValue("Name"), is(equalTo("PythonScript")));
    }
}
