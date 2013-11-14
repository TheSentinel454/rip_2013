package com.rip.javasteroid.remote;

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
	ArrayList<BaseEntity> getGameData() throws RemoteException;
}
