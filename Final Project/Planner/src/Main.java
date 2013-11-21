import com.rip.javasteroid.entity.BaseEntity;
import com.rip.javasteroid.remote.QueryInterface;
import com.rip.javasteroid.remote.RmiServer;

import java.rmi.Naming;
import java.util.ArrayList;

public class Main
{
	private static QueryInterface m_Server;
	/**
	 * Main entry point for the application
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Hello World!");

		try
		{
			m_Server = (QueryInterface) Naming.lookup("rmi://localhost/Query");
			ArrayList<BaseEntity> data = m_Server.getGameData();
		}
		catch(Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
	}
}