package View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import Commands.Executable;
import Controler.ServerControler;

public class ServerView {	
	private final JFrame window;
	private final JPanel panel;
	private BlockingQueue<Executable> commandQueue;
	private ServerControler controler;
	private final JButton closeButton;
	
	public ServerView() {
		window = new JFrame();
		panel = new JPanel();
		closeButton = new JButton("Zamknij serwer");
		panel.add(closeButton);
		window.add(panel);
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (ClassNotFoundException | InstantiationException
//				| IllegalAccessException | UnsupportedLookAndFeelException e) {}
		window.pack();
		window.setVisible(true);		
	}
	public void SetConnection(Object[] connection){
		JTextField portField = new JTextField("6666");
		final JComponent[] inputs = new JComponent[] {
				new JLabel("Numer portu"),
				portField,
		};
		JOptionPane.showMessageDialog(null, inputs, "Numer portu", JOptionPane.PLAIN_MESSAGE);
		ServerSocket serverSocket = null;
		while(true){		
		    try{
		        serverSocket = new ServerSocket( Integer.parseInt(portField.getText()) );
		        break;
		    }
		    catch(IOException exc1) {
		    	JOptionPane.showMessageDialog(null, inputs, "Serwer nie mo¿e dzia³aæ na tym porcie", JOptionPane.PLAIN_MESSAGE);
		    }
		    catch(Exception exc2){
		    	JOptionPane.showMessageDialog(null, inputs, "Niepoprawny numer portu", JOptionPane.PLAIN_MESSAGE);
		    }
		}
	    if(serverSocket == null) {
	    	final JComponent[] errormess = new JComponent[] { new JLabel("Nie uda³o siê nawi¹zaæ po³¹czenia") };
	    	JOptionPane.showMessageDialog(null, errormess, "B³¹d serwera", JOptionPane.ERROR_MESSAGE);
	    	System.exit(0);
	    }
	    else {
	    	connection[0] = Integer.parseInt(portField.getText());
	    	try {
				serverSocket.close();
			} catch (IOException e) {}
	    }
	}
	public void setCommandQueue(BlockingQueue<Executable> commandQueue, ServerControler controler) {
		this.commandQueue = commandQueue;
		this.controler = controler;
		setListing();
	}
	private void setListing() {
		closeButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				commandQueue.add(new Executable() {
					@Override
					public void execute() {
						controler.closeServer(0);
					}
				});		
			}
		});
	}

}
