package com.nike.externalsreporter.ui.tree;

import com.nike.buildmaster.projects.BuildableProjects;

import java.util.ArrayList;
import java.util.List;


/** Representation of a project - may contain other projects, or just branches, but not both. */
public class ProjectNode extends CheckableNode
{
    private BuildableProjects buildableProject;
    private List<BranchNode> branches = new ArrayList<BranchNode>();
    private List<ProjectNode> subProjects = new ArrayList<ProjectNode>();

    public ProjectNode(BuildableProjects buildableProject)
    {
        super(buildableProject.getProjectName());
        this.buildableProject = buildableProject;
    }

    public ProjectNode(String buildableProject)
    {
        super(buildableProject);
    }

    @SuppressWarnings({"ChainOfInstanceofChecks"})
    @Override
    public int indexOf(Object child)
    {
        int index = 0;

        if (child instanceof BranchNode)
        {
            for (BranchNode branch : branches)
            {
                if (branch.equals(child))
                {
                    return index;
                }

                index++;
            }
        }
        else if (child instanceof ProjectNode)
        {
            for (ProjectNode subProject : subProjects)
            {
                if (subProject.equals(child))
                {
                    return index;
                }

                index++;
            }
        }

        return -1;
    }

    @Override
    public List<? extends CheckableNode> getChildren()
    {
        if (branches.isEmpty())
        {
            return subProjects;
        }
        else
        {
            return branches;
        }
    }

    public void add(CheckableNode node)
    {
        if (node instanceof BranchNode)
        {
            if (subProjects.isEmpty())
            {
                branches.add(((BranchNode) node));
            }
            else
            {
                throw new IllegalArgumentException("Cant add a branch when a project already has sub projects!");
            }
        }
        else if (node instanceof ProjectNode)
        {
            if (branches.isEmpty())
            {
                subProjects.add(((ProjectNode) node));
            }
            else
            {
                throw new IllegalArgumentException("Cant add a sub project when a project already has branches!");
            }
        }
    }

    public List<BranchNode> getBranches()
    {
        return branches;
    }

    public BuildableProjects getBuildableProject()
    {
        return buildableProject;
    }

    public List<ProjectNode> getSubProjects()
    {
        return subProjects;
    }

    /** Is this a "master" projct, one that only has other projects under it? */
    public boolean isMasterProject()
    {
        return !subProjects.isEmpty();
    }
}