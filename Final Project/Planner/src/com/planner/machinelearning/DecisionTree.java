package com.planner.machinelearning;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 8:38 PM
 */
public class DecisionTree implements Serializable
{
	/* Private Attributes */
	private DefaultMutableTreeNode m_Root;

	/**
	 * Constructor of the decision tree
	 */
	public DecisionTree()
	{
		m_Root = new DefaultMutableTreeNode(new IntegerValue("DecisionTree", 0));
		// Construct the decision tree for our problem

		DefaultMutableTreeNode offPathNode = new DefaultMutableTreeNode(new BooleanValue("OffPath", false));
		m_Root.add(offPathNode);
		DefaultMutableTreeNode onPathNode = new DefaultMutableTreeNode(new BooleanValue("OnPath", true));
		m_Root.add(onPathNode);

		// Go through all the Relative Location Nodes
		for(int a = 0; a < m_Root.getChildCount(); a++)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) m_Root.getChildAt(a);
			// Add the Distance Nodes
			final int DISTANCE_INC = 25;
			for(int b = 0; b < 100; b += DISTANCE_INC)
			{
				DefaultMutableTreeNode distanceNode = new DefaultMutableTreeNode(new RangeValue("Distance", b, b + DISTANCE_INC));
				// Add Distance clone to each node
				child.add((DefaultMutableTreeNode)distanceNode.clone());
			}
			// Go through all the Distance Nodes
			for(int c = 0; c < child.getChildCount(); c++)
			{
				DefaultMutableTreeNode child2 = (DefaultMutableTreeNode) child.getChildAt(c);
				// Add the Time Till Impact Nodes
				final int TTI_INC = 100;
				for(int d = 0; d < 1000; d += TTI_INC)
				{
					DefaultMutableTreeNode ttiNode = new DefaultMutableTreeNode(new RangeValue("Time Till Impact", d, d + TTI_INC));
					// Add Time Till Impact clone to each node
					child2.add((DefaultMutableTreeNode)ttiNode.clone());
				}
				// Only bother if this is an "Off Path" Node
				if ("OffPath".equals(child.getUserObject()))
				{
					// Go through all the Time Till Impact Nodes
					for(int e = 0; e < child2.getChildCount(); e++)
					{
						DefaultMutableTreeNode child3 = (DefaultMutableTreeNode) child2.getChildAt(e);
						final int ANGLE_INC = 15;
						for(int f = 0; f < 180; f += ANGLE_INC)
						{
							DefaultMutableTreeNode angleNode = new DefaultMutableTreeNode(new RangeValue("Angle", f, f + ANGLE_INC));
							// Add Angle clone to each node
							child3.add((DefaultMutableTreeNode)angleNode.clone());
						}
					}
				}
			}
		}
	}

	/**
	 * Constructor of the decision tree
	 * @param filename - Filename to load the decision tree from
	 */
	public DecisionTree(String filename)
	{
		InputStream buffer = null;
		ObjectInput input = null;
		try
		{
			buffer = new BufferedInputStream(new FileInputStream(filename));
			input = new ObjectInputStream (buffer);
			// De-serialize the Tree
			DecisionTree decisionTree = (DecisionTree)input.readObject();
			m_Root = decisionTree.m_Root;
		}
		catch(Exception e)
		{
			System.out.println("DecisionTree.loadTree(): ");
			e.printStackTrace();
		}
		finally
		{
			if (buffer != null)
				try
				{
					buffer.close();
				}
				catch (IOException e){}
			if (input != null)
				try
				{
					input.close();
				}
				catch (IOException e){}
		}
	}

	/**
	 * Save the tree to a file
	 * @param filename - Filename to save the decision tree to
	 */
	public void saveTree(String filename)
	{
		OutputStream buffer = null;
		ObjectOutput output = null;
		try
		{
			// Serialize the List
			buffer = new BufferedOutputStream(new FileOutputStream(filename));
			output = new ObjectOutputStream(buffer);
			output.writeObject(this);
			output.flush();
		}
		catch(Exception e)
		{
			System.out.println("DecisionTree.saveTree(): ");
			e.printStackTrace();
		}
		finally
		{
			if (buffer != null)
				try
				{
					buffer.close();
				}
				catch (IOException e){}
			if (output != null)
				try
				{
					output.close();
				}
				catch (IOException e){}
		}
	}

	/**
	 * Load the decision tree from a file
	 * @param filename - File to load the tree from
	 * @return Decision Tree
	 */
	public static DecisionTree loadTree(String filename)
	{
		return new DecisionTree(filename);
	}
}
