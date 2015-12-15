package com.github.redsolo.vcm.commands;

import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.github.redsolo.vcm.Model;
import com.github.redsolo.vcm.ModelResource;

public class ListNodesCommand extends AbstractModelCollectionCommand implements Command {
    private static Logger log = Logger.getLogger(ListNodesCommand.class);
    public ListNodesCommand() {
        includeLayouts = true;
        excludeComponents = true;
    }

    @Parameter(description="maximum node depth (-1 = no max)", names={"--max-depth"})
    protected int maxDepth = -1;
    
    @Override
    public String getName() {
        return "list-nodes";
    }

    @Override
    protected void executeModel(Model model) throws IOException, ZipException {
        ModelResource resource = model.getResourceData();
        ModelResource worldContainer = resource.getResource("Node", "rSimGroup").getResource("Node", "rSimGroup");
        for (ModelResource world : worldContainer.getResources("Node")) {
            findNodesInWorld(world, 0);
        }
    }
    
    private void findNodesInWorld(ModelResource world, int depth) {
        for (ModelResource node : world.getResources("Node")) {
            if ("rSimResource".equals(node.getName())) {
                log.info(String.format("%s%s (%s)", StringUtils.repeat("  ", depth), node.getValue("Name"), node.getValue("VCID")));
                if (maxDepth == -1 || maxDepth > depth) {
                    findNodesInWorld(node, depth + 1);
                }
            }
        }
    }
}
