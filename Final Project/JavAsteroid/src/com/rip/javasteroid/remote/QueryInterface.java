package com.rip.javasteroid.remote;

import com.rip.javasteroid.GameData;
import com.rip.javasteroid.entity.BaseEntity;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Luke
 * Date: 11/13/13
 * Time: 9:25 PM
 */
public interface QueryInterface extends Remote
{
	GameData getGameData() throws RemoteException;
	void startForward() throws RemoteException;
	void stopForward() throws RemoteException;
	void startRight() throws RemoteException;
	void stopRight() throws RemoteException;
	void startLeft() throws RemoteException;
	void stopLeft() throws RemoteException;
	void fire() throws RemoteException;
	void reset() throws RemoteException;
	void quit() throws RemoteException;
}
