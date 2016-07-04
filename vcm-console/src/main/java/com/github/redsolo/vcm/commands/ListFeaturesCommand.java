package com.github.redsolo.vcm.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandDescription = "Lists features from resource.dat")
public class ListFeaturesCommand extends AbstractModelCollectionCommand implements Command  {
    private static Logger log = Logger.getLogger(ListFeaturesCommand.class);

    @Override
    public String getName() {
        return "list-features";
    }

    @Parameter(description="maximum node depth (-1 = no max)", names={"--max-depth"})
    private int maxDepth = -1;

    @Parameter(description="type of feature to list (defaults to all)", names={"-t", "--type"})
    protected String type;

    @Parameter(description="no indent (defaults to false)", names={"--no-indent"})
    private boolean noIndent = false;

    @Parameter(description="prefix line with file name", names={"--prefix-with-filename"})
    private boolean prefixWithFilename;

    @Parameter(description="list properties of feature (only features with property will be shown, comma separated list)", names={"-p", "--properties"})
    private String propertyNames;
    private String[] propertyNamesList;

    @Override
    protected void validateParameters(MainConfiguration mainConfiguration) {
        super.validateParameters(mainConfiguration);
        if (StringUtils.isNotEmpty(propertyNames)) {
            propertyNamesList = propertyNames.split(",");
        }
    }

    @Override
    protected void executeModel(Model model) throws IOException, ZipException {
        ModelResource resource = model.getResourceData();
        findFeaturesInWorld(model.getFile(), resource.getResource("Node").getResource("NodeClass"), 0);
    }

    private void findFeaturesInWorld(File file, ModelResource parent, int depth) {
        for (ModelResource node : parent.getResources()) {
            if ("Feature".equals(node.getType())) {
                if (type == null || StringUtils.equalsIgnoreCase(node.getName(), type)) {
                    printFeature(file, node, depth);
                }
                if (maxDepth == -1 || maxDepth > depth) {
                    findFeaturesInWorld(file, node, depth + (noIndent ? 0 : 1));
                }
            }
        }
    }

    private void printFeature(File file, ModelResource node, int depth) {
        StringBuilder builder = new StringBuilder(StringUtils.repeat("  ", depth));
        if (prefixWithFilename) {
            builder.append(getRelativePath(file));
            builder.append(", ");
        }
        builder.append(node.getName());
        builder.append(", '");
        builder.append(node.getValue("Name"));
        builder.append("'");
        if (propertyNamesList != null) {
            builder.append(", [");
            List<String> values = getPropertyValues(node);
            if (values.size() > 0) {
                builder.append(StringUtils.join(values, ','));
                builder.append("]");
                log.info(builder.toString());
            }
        } else {
            log.info(builder.toString());
        }
    }

    private List<String> getPropertyValues(ModelResource node) {
        ArrayList<String> strings = new ArrayList<>();
        for (String propertyName : propertyNamesList) {
            Object value = node.getValue(propertyName);
            String valueString = null;
            if (value != null) {
                valueString = value.toString();
            } else {
                ModelResource childNode = node.getResource(propertyName);
                if (childNode != null) {
                    valueString = childNode.getName();
                }
            }
            if (valueString != null) {
                strings.add(String.format("%s=%s", propertyName, StringUtils.remove(valueString, '"').trim()));
            }
        }
        return strings;
    }
}
