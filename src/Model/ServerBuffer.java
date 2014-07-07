package Model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerBuffer {
	private final ConcurrentHashMap<Integer, Account> accountMap;
	private final Map<Account, Queue<Message>> messageMap;
	/** 
	 * Tworzony jest bufor na konta i bufor na wiadomoœci.
	 */
	public ServerBuffer() {
		messageMap = Collections.synchronizedMap(new HashMap<Account, Queue<Message>>());
		accountMap = new ConcurrentHashMap<Integer, Account>();
	}
	/**
	 * Dodaje konto do buforów
	 * @param account
	 */
	public synchronized void addAccount(Account account) {
		DebugPrint.print("dodawanie");
		accountMap.put(account.getProfile().getId(), account);
		messageMap.put(account, new LinkedList<Message>());
	}
	public Account checkIfAccountExist(Integer id) {
		return accountMap.get(id);
	}
	public synchronized void setAccountStatement(Integer id, Statement statement) {
		accountMap.get(id).setStatement(statement);
	}
	public synchronized Statement checkAccountStatement(Integer id) {
		return accountMap.get(id).getStatement();
	}
	private Queue<Message> getMessageBuffer(Account acc) {
		return messageMap.get(acc);
	}
	private Message getMessage(Integer id) {
		Account acc = checkIfAccountExist(id);
		if(acc != null) {
			Queue<Message> queue = getMessageBuffer(acc);
			return queue.poll();
		}
		else return null;
	}
	public void putMessage(Account acc, Message message) {
			Queue<Message> queue = getMessageBuffer(acc);
			queue.add(message);
	}
	public Queue<Message> getMessages(Account acc) {
		Queue<Message> returnQueue = new LinkedList<>();
		if(acc != null) {
			Message message;
			Queue<Message> queue = getMessageBuffer(acc);
			while( (message = queue.poll()) != null ) {
				returnQueue.add(message);
			}
		}
		return returnQueue;
	}
	public Status setStatus(Integer id, String password, Status status) {
		Account acc = checkIfAccountExist(id);
		if(acc != null && acc.accessAccount(id, password))
			return acc.setStatus(status);
		else
			return new Status();
	}
	public Status getStatus(Integer id) {
		Account acc = checkIfAccountExist(id);
		if(acc != null) 
			return acc.getStatus();
		else
			return new Status();
	}
}
