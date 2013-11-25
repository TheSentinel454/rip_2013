package com.rip.javasteroid.remote;

import com.rip.javasteroid.GameData;
import com.rip.javasteroid.engine.InputHandler;
import com.rip.javasteroid.entity.BaseEntity;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/13/13
 * Time: 9:27 PM
 */
public class RmiServer extends UnicastRemoteObject implements QueryInterface
{
	private GameData m_GameData;
	private InputHandler m_Handler;
	// This method is called from the remote client by the RMI.
	// This is the implementation of the "Query Interface"

	/**
	 * Get the current Game Data and return it to the calling process
	 * @return Game Data
	 * @throws RemoteException
	 */
	public GameData getGameData() throws RemoteException
	{
		return m_GameData;
	}

	@Override
	public void startForward() throws RemoteException
	{
		m_Handler.startForward();
	}

	@Override
	public void stopForward() throws RemoteException
	{
		m_Handler.stopForward();
	}

	@Override
	public void startRight() throws RemoteException
	{
		m_Handler.startRight();
	}

	@Override
	public void stopRight() throws RemoteException
	{
		m_Handler.stopRight();
	}

	@Override
	public void startLeft() throws RemoteException
	{
		m_Handler.startLeft();
	}

	@Override
	public void stopLeft() throws RemoteException
	{
		m_Handler.stopLeft();
	}

	@Override
	public void fire() throws RemoteException
	{
		m_Handler.fire();
	}

	/**
	 * Initialize the RMI Server to allow for querying of
	 * Game data from a remote process
	 * @throws RemoteException
	 */
	public RmiServer(GameData gameData, InputHandler handler) throws RemoteException
	{
		m_GameData = gameData;
		m_Handler = handler;
		try
		{
			LocateRegistry.createRegistry(1099);
			Naming.rebind("rmi:///Query", this);
		}
		catch(RemoteException re)
		{
			System.out.println("RemoteException: " + re);
		}
		catch(MalformedURLException mfe)
		{
			System.out.println("MalformedURLException: " + mfe);
		}
	}
}