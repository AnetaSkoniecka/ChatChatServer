package Model;

import java.util.concurrent.atomic.AtomicInteger;

public class IdCreator {
	private static AtomicInteger idCreator = new AtomicInteger(0);
	public static Integer getIdentity() {
		return idCreator.incrementAndGet();
	}
}
