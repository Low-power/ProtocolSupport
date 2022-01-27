package protocolsupport.utils.recyclable;

import java.util.Collection;

public interface RecyclableCollection<E> extends Collection<E>, AutoCloseable {

	public void recycleObjectOnly();

}
