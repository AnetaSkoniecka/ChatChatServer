package Controler;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Commands.Executable;
import Model.Model;
import View.ServerView;

public class ServerControler extends Thread {
	
	private final ServerView view;
	private final Model model;
	private final ServerConsole console;
	private final BlockingQueue<Executable> commandQueue;
	public ServerControler(ServerView view, Model model) {
		this.view = view;
		this.model = model;
		Object[] connection = new Object[] { new Integer(0) };
		view.SetConnection(connection);
		commandQueue = new LinkedBlockingQueue<Executable>();
		view.setCommandQueue(commandQueue, this);
		console = new ServerConsole((Integer)connection[0], model);
		console.start();
	}
	public void run() {
		Executable executable;
		try {
			executable = commandQueue.take();
			executable.execute();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void closeServer(int state) {
		if(state < 0)
			System.exit(-1);
		else
			System.exit(0);
	}


}
