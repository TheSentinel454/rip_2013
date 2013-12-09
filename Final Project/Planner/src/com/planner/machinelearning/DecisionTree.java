package com.planner.machinelearning;

import com.rip.javasteroid.GameData;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.*;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 12/7/13
 * Time: 8:38 PM
 */
public class DecisionTree implements Serializable
{
	/* Constants */
	private static final String DECISION_TREE_FILENAME = "DecisionTree-%s.dt";
	private static final String DECISION_TREE_FILENAME_CSV = "DecisionTree-%s.csv";

	/* Private Attributes */
	private DefaultMutableTreeNode m_Root;

	/**
	 * Constructor of the decision tree
	 */
	public DecisionTree()
	{
		this(DECISION_TREE_FILENAME);
		// See if we don't have a root
		if (m_Root == null)
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
				final int DISTANCE_INC = 100;
				for(int b = 0; b < 1000; b += DISTANCE_INC)
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
						DefaultMutableTreeNode ttiNode = new DefaultMutableTreeNode(new RangeValue("Time Till Impact", d, (d == 1000 - TTI_INC ? Float.MAX_VALUE : d + TTI_INC)));
						// Add Time Till Impact clone to each node
						child2.add((DefaultMutableTreeNode)ttiNode.clone());
					}
					// Only bother if this is an "Off Path" Node
					if ("OffPath".equals(((BooleanValue) child.getUserObject()).getName()))
					{
						// Go through all the Time Till Impact Nodes
						for(int e = 0; e < child2.getChildCount(); e++)
						{
							DefaultMutableTreeNode child3 = (DefaultMutableTreeNode) child2.getChildAt(e);
							final int ANGLE_INC = 30;
							for(int f = 0; f < 360; f += ANGLE_INC)
							{
								DefaultMutableTreeNode angleNode = new DefaultMutableTreeNode(new RangeValue("Angle", f, f + ANGLE_INC));
								// Add Angle clone to each node
								child3.add((DefaultMutableTreeNode)angleNode.clone());
							}
						}
					}
				}
			}
			// Add Tree Data defaults to each of the tree leaves
			Enumeration<DefaultMutableTreeNode> en = m_Root.postorderEnumeration();
			while (en.hasMoreElements())
			{
				DefaultMutableTreeNode node = en.nextElement();
				if (node.isLeaf())
					node.add(new DefaultMutableTreeNode(new LeafData(0.00f, 0, 0)));
			}
			// Finally save the tree as the default
			saveTree(0);
		}
	}

	/**
	 * Constructor of the decision tree
	 * @param filename - Filename to load the decision tree from
	 */
	private DecisionTree(String filename)
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
	 */
	public void saveTree(int gameNumber)
	{
		OutputStream buffer = null;
		ObjectOutput output = null;
		try
		{
			// Serialize the List
			buffer = new BufferedOutputStream(new FileOutputStream(String.format(DECISION_TREE_FILENAME, gameNumber)));
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
	 * Save the tree to a file
	 */
	public void saveCsvTree(GameData gameData, int gameNumber)
	{
		BufferedWriter output = null;
		try
		{
			// Serialize the List
			output = new BufferedWriter(new FileWriter(String.format(DECISION_TREE_FILENAME_CSV, gameNumber)));
			output.write("GameTime,Score\n");
			output.write((gameData.getGameTime() / 100.0f) + "," + gameData.getScore() + "\n");
			output.write("On Path,Distance,Time Till Impact,Angle,Prediction,Ratio,Prediction%\n");
			final String newRow = "%s,%s,%s,%s,%s,%s,%s\n";
			// Just changing enumeration kind here
			Enumeration<DefaultMutableTreeNode> en = m_Root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				DefaultMutableTreeNode node = en.nextElement();
				if (node.isLeaf())
				{
					LeafData leafData = (LeafData)node.getUserObject();
					Object[] pathData = node.getUserObjectPath();
					BooleanValue bvRelativeLocation = (BooleanValue)pathData[1];
					RangeValue rvDistance = (RangeValue)pathData[2];
					RangeValue rvTimeTillImpact = (RangeValue)pathData[3];
					RangeValue rvAngle = null;
					if (!bvRelativeLocation.getValue())
						rvAngle = (RangeValue)pathData[4];
					output.write(String.format(newRow,
							bvRelativeLocation.getValue(),
							rvDistance.toValueString(),
							rvTimeTillImpact.toValueString(),
							rvAngle != null ? rvAngle.toValueString() : "N/A",
							leafData.getPrediction(),
							Float.toString(leafData.getRatio() * 100.0f) + "%",
							Float.toString(leafData.getProbability() * 100.0f) + "%"));
				}
			}
			output.flush();
		}
		catch(Exception e)
		{
			System.out.println("DecisionTree.saveCsvTree(): ");
			e.printStackTrace();
		}
		finally
		{
			if (output != null)
				try
				{
					output.close();
				}
				catch (IOException e){}
		}
	}

	/**
	 * Train the decision tree
	 * @param criteria Search criteria to find node
	 * @param success True if successful, False otherwise
	 */
	public void train(SearchCriteria criteria, boolean success)
	{
		// Make sure we have a root
		if (m_Root != null)
		{
			// Increment the total trains
			IntegerValue totalTrains = (IntegerValue)m_Root.getUserObject();
			totalTrains.setValue(totalTrains.getValue() + 1);
			// Find the specific Leaf node
			((LeafData)find(criteria).getUserObject()).train(success, totalTrains.getValue());
		}
	}

	/**
	 * Find a particular node based on the specified parameters
	 * @param criteria Search criteria to find node
	 * @return Node if found, Otherwise the last leaf in the postorderEnumeration
	 */
	public DefaultMutableTreeNode find(SearchCriteria criteria)
	{
		// Make sure we have a root
		if (m_Root != null)
		{
			DefaultMutableTreeNode node = null;
			// Just changing enumeration kind here
			Enumeration<DefaultMutableTreeNode> en = m_Root.postorderEnumeration();
			while (en.hasMoreElements())
			{
				node = en.nextElement();
				if (node.isLeaf())
				{
					Object[] objects = node.getUserObjectPath();
					// Let's get all of the pieces in the path
					BooleanValue bvRelativeLocation = (BooleanValue)objects[1];
					RangeValue rvDistance = (RangeValue)objects[2];
					RangeValue rvTimeTillImpact = (RangeValue)objects[3];
					RangeValue rvAngle = null;
					if (!bvRelativeLocation.getValue())
						rvAngle = (RangeValue)objects[4];

					// See if we are good to go
					if (criteria.isRelativeLocation() == bvRelativeLocation.getValue() &&
						rvDistance.inRange(criteria.getDistance()) &&
						rvTimeTillImpact.inRange(criteria.getTimeTillImpact()) &&
						(rvAngle == null || rvAngle.inRange(criteria.getAngle())))
					{
						// Found the node
						return node;
					}
				}
			}
			return node;
		}
		System.out.println("Invalid state, no root node");
		return null;
	}

	/**
	 * Print the tree to the output
	 */
	public void printTree()
	{
		// Just changing enumeration kind here
		Enumeration<DefaultMutableTreeNode> en = m_Root.preorderEnumeration();
		while (en.hasMoreElements())
		{
			DefaultMutableTreeNode node = en.nextElement();
			for(int i = 0; i < node.getLevel(); i++)
				System.out.print('\t');
			TreeNode[] path = node.getPath();
			System.out.println((node.isLeaf() ? "- " : "+ ") + path[path.length - 1]);
		}
	}
}
